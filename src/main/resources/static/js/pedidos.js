/* Meus pedidos: lista (GET /api/pedidos), detalhe expansível e cancelamento. */
(async () => {
    if (!Cliente.exigirLogin()) return;
    Cliente.desenharTopo();

    const $ = (id) => document.getElementById(id);
    const R = CF.rotas;
    let pedidos = [];

    // Pedido recém-criado no carrinho
    const novos = Number(new URLSearchParams(location.search).get('novos'));
    if (novos) {
        $('avisoNovo').textContent = novos === 1 ? 'Pedido criado com sucesso!' : `${novos} pedidos criados, um para cada loja.`;
        $('avisoNovo').classList.remove('d-none');
    }

    // O cliente só pode cancelar antes do envio; a API confirma a regra.
    const podeCancelar = (p) => !['CANCELADO', 'ENVIADO', 'ENTREGUE'].includes(p.status);

    // ItemPedidoResponse: produtoId, produtoNome, quantidade, precoUnitario, subtotal
    function itemHtml(i) {
        return `<div class="cli-linha"><span>${CF.esc(i.quantidade)} × ${CF.esc(i.produtoNome)}
      <span class="text-muted small">(${CF.brl(i.precoUnitario)} cada)</span></span><span>${CF.brl(i.subtotal)}</span></div>`;
    }

    function pedidoHtml(p) {
        const id = CF.esc(p.id);
        return `<article class="cf-card cli-pedido" data-id="${id}">
      <div class="cli-pedido-cab">
        <div><div class="fw-bold">Pedido #${id} · ${CF.esc(p.lojaNome)}</div><div class="small text-muted">${Cliente.dataHora(p.dataCriacao)}</div></div>
        <div class="d-flex align-items-center gap-3"><strong>${CF.brl(p.valorTotal)}</strong>${CF.selo(p.status)}
          <button class="btn btn-sm btn-outline-primary" data-acao="detalhe" aria-expanded="false">Ver detalhes</button></div>
      </div>
      <div class="cli-pedido-detalhe d-none">
        <h2 class="h6 mt-3">Itens</h2>
        ${(p.itens ?? []).map(itemHtml).join('')}
        <hr>
        <div class="cli-linha"><span>Produtos</span><span>${CF.brl(p.valorProdutos)}</span></div>
        <div class="cli-linha"><span>Frete (${CF.esc(p.freteServico || '—')}${p.fretePrazoDias ? ` · ${CF.esc(p.fretePrazoDias)} dia(s)` : ''})</span><span>${CF.brl(p.valorFrete)}</span></div>
        <div class="cli-linha fw-bold"><span>Total</span><span>${CF.brl(p.valorTotal)}</span></div>
        <h2 class="h6 mt-3">Entrega</h2>
        <div class="text-muted">${CF.esc(Cliente.enderecoLinha(p.enderecoEntrega))}</div>
        ${p.codigoRastreio ? `<div class="mt-2"><span class="text-muted">Código de rastreio:</span> <strong>${CF.esc(p.codigoRastreio)}</strong></div>` : ''}
        ${p.status === 'PENDENTE' ? '<p class="small text-muted mt-3 mb-0">O pagamento ainda não está disponível nesta versão: o pedido segue pendente.</p>' : ''}
        ${podeCancelar(p) ? `<button class="btn btn-sm btn-outline-danger mt-3" data-acao="cancelar">Cancelar pedido</button>` : ''}
      </div>
    </article>`;
    }

    function desenhar() {
        $('listaPedidos').innerHTML = pedidos.length
            ? pedidos.map(pedidoHtml).join('')
            : `<div class="cf-card"><div class="cf-vazio"><i class="bi bi-receipt"></i>Você ainda não fez pedidos.<br>
          <a class="btn btn-primary btn-sm mt-3" href="${CF.url('/')}">Ver produtos</a></div></div>`;
    }

    async function carregar() {
        try {
            pedidos = CF.lista(await CF.api(R.pedidos)).sort((a, b) => String(b.dataCriacao).localeCompare(String(a.dataCriacao)));
        } catch (e) { CF.erro(e); pedidos = []; }
        desenhar();
    }

    $('listaPedidos').addEventListener('click', async (ev) => {
        const botao = ev.target.closest('[data-acao]');
        if (!botao) return;
        const cartao = botao.closest('.cli-pedido');
        if (botao.dataset.acao === 'detalhe') {
            const detalhe = cartao.querySelector('.cli-pedido-detalhe');
            const abrir = detalhe.classList.toggle('d-none') === false;
            botao.textContent = abrir ? 'Ocultar detalhes' : 'Ver detalhes';
            botao.setAttribute('aria-expanded', abrir);
        }
        if (botao.dataset.acao === 'cancelar' && confirm('Cancelar este pedido? O estoque dos itens será devolvido.')) {
            await CF.comCarregando(botao, async () => {
                try { await CF.api(R.pedidoCancelar(cartao.dataset.id), { metodo: 'POST' }); CF.toast('Pedido cancelado.'); await carregar(); }
                catch (e) { CF.erro(e); }
            });
        }
    });

    await carregar();
})();
