/* Carrinho: itens agrupados por loja, ajuste de quantidade e criação dos pedidos (1 por loja). */
(async () => {
    if (!Cliente.exigirLogin()) return;
    Cliente.desenharTopo();

    const $ = (id) => document.getElementById(id);
    const R = CF.rotas;
    const estado = { carrinho: { lojas: [], total: 0 }, enderecos: [] };

    // ---------- Dados ----------
    async function carregarCarrinho() {
        try { estado.carrinho = await CF.api(R.carrinho.ver); } catch (e) { CF.erro(e); }
        desenhar();
    }

    async function carregarEnderecos() {
        try { estado.enderecos = CF.lista(await CF.api(R.enderecos)); } catch (e) { CF.erro(e); estado.enderecos = []; }
        $('selEndereco').innerHTML = estado.enderecos.length
            ? estado.enderecos.map((e) => `<option value="${CF.esc(e.id)}">${CF.esc(Cliente.enderecoLinha(e))}</option>`).join('')
            : '<option value="">Nenhum endereço cadastrado</option>';
        desenharResumo();
    }

    // Todas as ações do carrinho devolvem o carrinho atualizado
    async function agir(chamada) {
        try { estado.carrinho = await chamada(); desenhar(); Cliente.atualizarContador(estado.carrinho); }
        catch (e) { CF.erro(e); await carregarCarrinho(); } // ex.: estoque insuficiente → recarrega os valores reais
    }

    // ---------- Tela ----------
    function itemHtml(i) {
        const id = CF.esc(i.produtoId);
        return `<div class="cli-item" data-produto="${id}">
      <div class="cli-thumb"><i class="bi bi-box-seam"></i>${i.imagem ? `<img src="${CF.esc(i.imagem)}" alt="" loading="lazy">` : ''}</div>
      <div><div class="fw-semibold">${CF.esc(i.nome)}</div>
        <div class="small text-muted">${CF.brl(i.precoUnitario)} cada</div>
        ${i.disponivel ? '' : `<div class="small text-danger mt-1">Indisponível no momento${i.estoque != null ? ` (estoque: ${CF.esc(i.estoque)})` : ''}</div>`}</div>
      <div class="cli-qtd">
        <button type="button" data-acao="menos" aria-label="Diminuir">−</button>
        <input type="number" min="1" step="1" value="${CF.esc(i.quantidade)}" data-acao="qtd" aria-label="Quantidade de ${CF.esc(i.nome)}">
        <button type="button" data-acao="mais" aria-label="Aumentar">+</button></div>
      <div class="cli-sub">${CF.brl(i.subtotal)}</div>
      <button class="btn btn-sm btn-outline-danger" data-acao="remover" aria-label="Remover ${CF.esc(i.nome)}"><i class="bi bi-trash"></i></button>
    </div>`;
    }

    function desenhar() {
        const lojas = estado.carrinho?.lojas ?? [];
        $('lojasCarrinho').innerHTML = lojas.length
            ? lojas.map((l) => `<section class="cf-card mb-3">
          <div class="cli-loja-cab"><span><i class="bi bi-shop me-2"></i>${CF.esc(l.lojaNome)}</span>
            <span class="text-muted small fw-normal">Subtotal ${CF.brl(l.subtotal)}</span></div>
          ${l.itens.map(itemHtml).join('')}</section>`).join('')
            : `<div class="cf-card"><div class="cf-vazio"><i class="bi bi-bag"></i>Seu carrinho está vazio.<br>
          <a class="btn btn-primary btn-sm mt-3" href="${CF.url('/')}">Ver produtos</a></div></div>`;
        $('btnLimpar').classList.toggle('d-none', !lojas.length);
        $('resTotal').textContent = CF.brl(estado.carrinho?.total);
        desenharResumo();
    }

    function desenharResumo() {
        const lojas = estado.carrinho?.lojas ?? [];
        const itens = lojas.flatMap((l) => l.itens);
        let aviso = '';
        if (itens.some((i) => !i.disponivel)) aviso = 'Há itens indisponíveis. Remova-os ou ajuste a quantidade para continuar.';
        else if (itens.length && !estado.enderecos.length) aviso = 'Cadastre um endereço de entrega para finalizar.';
        $('avisoResumo').textContent = aviso;
        $('avisoResumo').classList.toggle('d-none', !aviso);
        $('btnFinalizar').disabled = !itens.length || !!aviso;
    }

    // ---------- Ações ----------
    $('lojasCarrinho').addEventListener('click', (ev) => {
        const botao = ev.target.closest('[data-acao]');
        const linha = ev.target.closest('.cli-item');
        if (!botao || !linha || botao.tagName === 'INPUT') return;
        const produtoId = linha.dataset.produto;
        const atual = Number(linha.querySelector('input').value);
        if (botao.dataset.acao === 'remover' || (botao.dataset.acao === 'menos' && atual <= 1)) agir(() => CF.api(R.carrinho.item(produtoId), { metodo: 'DELETE' }));
        else agir(() => CF.api(R.carrinho.item(produtoId), { metodo: 'PUT', corpo: { quantidade: atual + (botao.dataset.acao === 'mais' ? 1 : -1) } }));
    });

    $('lojasCarrinho').addEventListener('change', (ev) => {
        if (ev.target.dataset.acao !== 'qtd') return;
        const produtoId = ev.target.closest('.cli-item').dataset.produto;
        const quantidade = Number(ev.target.value);
        if (!Number.isInteger(quantidade) || quantidade < 0) { CF.toast('Informe uma quantidade válida.', 'erro'); return carregarCarrinho(); }
        agir(() => CF.api(R.carrinho.item(produtoId), { metodo: 'PUT', corpo: { quantidade } })); // 0 remove o item
    });

    $('btnLimpar').addEventListener('click', () => {
        if (confirm('Remover todos os itens do carrinho?')) agir(() => CF.api(R.carrinho.ver, { metodo: 'DELETE' }));
    });

    $('btnFinalizar').addEventListener('click', async (ev) => {
        const enderecoId = Number($('selEndereco').value);
        if (!enderecoId) return CF.toast('Escolha o endereço de entrega.', 'erro');
        await CF.comCarregando(ev.currentTarget, async () => {
            try {
                const pedidos = await CF.api(R.pedidos, { metodo: 'POST', corpo: { enderecoId } });
                location.href = CF.url(`/pedidos?novos=${CF.lista(pedidos).length}`);
            } catch (e) { CF.erro(e); await carregarCarrinho(); }
        });
    });

    await Promise.all([carregarCarrinho(), carregarEnderecos()]);
})();
