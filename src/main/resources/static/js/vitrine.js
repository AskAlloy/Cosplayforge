/* Vitrine pública: lista produtos (GET /api/produtos, paginado), filtra e mostra o detalhe.
   Funciona sem login; se houver sessão, o topo mostra o nome e os atalhos. */
(() => {
    const $ = (id) => document.getElementById(id);
    const R = CF.rotas;
    const TAMANHO = 12;
    const estado = { pagina: 0, totalPaginas: 1, total: 0, itens: [] };
    const modal = new bootstrap.Modal($('modalProduto'));
    let produtoAtual = null;

    // ---------- Dados ----------
    // GET /api/produtos aceita: termo, categoriaId, precoMin, precoMax, page, size
    async function carregarCategorias() {
        try {
            const cats = CF.lista(await CF.api(R.categorias, { publico: true }));
            $('categoria').insertAdjacentHTML('beforeend', cats.map((c) => `<option value="${CF.esc(c.id)}">${CF.esc(c.nome)}</option>`).join(''));
        } catch { /* sem categorias o filtro simplesmente fica só com "Todas" */ }
    }

    async function carregarProdutos() {
        const params = new URLSearchParams({ page: estado.pagina, size: TAMANHO });
        if ($('categoria').value) params.set('categoriaId', $('categoria').value);
        if ($('busca').value.trim()) params.set('termo', $('busca').value.trim());
        $('grade').innerHTML = '<div class="text-muted">Carregando produtos...</div>';
        try {
            const pg = CF.pagina(await CF.api(`${R.produtos}?${params}`, { publico: true }));
            estado.itens = pg.itens; estado.totalPaginas = pg.totalPaginas; estado.total = pg.total;
        } catch (e) {
            estado.itens = []; estado.totalPaginas = 1; estado.total = 0;
            $('grade').innerHTML = `<div class="cf-vazio" style="grid-column:1/-1"><i class="bi bi-wifi-off"></i>${CF.esc(e.message)}</div>`;
            $('paginacao').innerHTML = ''; $('contagem').textContent = '';
            return;
        }
        desenhar();
    }

    // ---------- Tela ----------
    function corDaCapa(nome) {
        let h = 0; for (const c of String(nome)) h = (h * 31 + c.charCodeAt(0)) % 360;
        return `hsl(${h} 70% 91%)`;
    }

    function seloEstoque(p) {
        if (!p.disponivel) return '<span class="cf-selo cinza">Indisponível</span>';
        if ((p.quantidadeEstoque ?? 99) <= 3) return '<span class="cf-selo ambar">Últimas unidades</span>';
        return '';
    }

    function desenhar() {
        $('contagem').textContent = estado.total ? `${estado.total} produto(s)` : '';
        $('grade').innerHTML = estado.itens.length
            ? estado.itens.map((p) => `<button type="button" class="vit-card" data-id="${CF.esc(p.id)}">
          <div class="vit-capa" style="--capa:${corDaCapa(p.nome)}"><i class="bi bi-box-seam"></i>${p.imagem ? `<img src="${CF.esc(p.imagem)}" alt="" loading="lazy">` : ''}</div>
          <div class="vit-info"><h3 class="vit-nome">${CF.esc(p.nome)}</h3>
            <span class="vit-loja">${CF.esc(p.lojaNome || 'Loja parceira')}</span>
            <div class="vit-rodape"><span class="vit-preco">${CF.brl(p.preco)}</span>${seloEstoque(p)}</div></div></button>`).join('')
            : `<div class="cf-vazio" style="grid-column:1/-1"><i class="bi bi-search"></i>Nenhum produto encontrado.</div>`;

        $('paginacao').innerHTML = estado.totalPaginas > 1
            ? `<button class="btn btn-sm btn-outline-primary" id="pgAnterior" ${estado.pagina === 0 ? 'disabled' : ''}>Anterior</button>
         <span class="small text-muted">Página ${estado.pagina + 1} de ${estado.totalPaginas}</span>
         <button class="btn btn-sm btn-outline-primary" id="pgProxima" ${estado.pagina + 1 >= estado.totalPaginas ? 'disabled' : ''}>Próxima</button>` : '';
    }

    // Imagem quebrada: remove a <img> e deixa o fundo colorido com o ícone
    $('grade').addEventListener('error', (ev) => { if (ev.target.tagName === 'IMG') ev.target.remove(); }, true);

    $('paginacao').addEventListener('click', (ev) => {
        if (ev.target.id === 'pgAnterior') { estado.pagina--; carregarProdutos(); window.scrollTo({ top: 0 }); }
        if (ev.target.id === 'pgProxima') { estado.pagina++; carregarProdutos(); window.scrollTo({ top: 0 }); }
    });

    $('grade').addEventListener('click', (ev) => {
        const card = ev.target.closest('.vit-card');
        if (!card) return;
        const p = estado.itens.find((x) => String(x.id) === card.dataset.id);
        if (!p) return;
        produtoAtual = p;
        $('detQtd').value = 1;
        $('btnAdicionar').disabled = !p.disponivel;
        $('btnAdicionar').innerHTML = CF.token() ? '<i class="bi bi-bag-plus me-1"></i>Adicionar ao carrinho' : 'Entrar para comprar';
        $('detNome').textContent = p.nome;
        $('detDescricao').textContent = p.descricao || 'Sem descrição.';
        $('detLoja').textContent = p.lojaNome || 'Loja parceira';
        $('detPreco').textContent = CF.brl(p.preco);
        $('detMedidas').textContent = p.pesoKg ? `Embalagem: ${p.alturaCm} × ${p.larguraCm} × ${p.comprimentoCm} cm · ${p.pesoKg} kg` : '';
        $('detSelos').innerHTML = [
            p.categoriaNome && `<span class="cf-selo roxo">${CF.esc(p.categoriaNome)}</span>`,
            p.personalizavel && '<span class="cf-selo azul">Personalizável</span>',
            seloEstoque(p),
        ].filter(Boolean).join('');
        modal.show();
    });

    // ---------- Adicionar ao carrinho ----------
    $('detMenos').addEventListener('click', () => { $('detQtd').value = Math.max(1, Number($('detQtd').value) - 1); });
    $('detMais').addEventListener('click', () => { $('detQtd').value = Number($('detQtd').value) + 1; });
    $('btnAdicionar').addEventListener('click', async (ev) => {
        if (!CF.token()) { location.href = CF.url('/login'); return; }
        const quantidade = Number($('detQtd').value);
        if (!Number.isInteger(quantidade) || quantidade < 1) return CF.toast('Informe uma quantidade de 1 ou mais.', 'erro');
        await CF.comCarregando(ev.currentTarget, async () => {
            try {
                const carrinho = await CF.api(R.carrinho.itens, { metodo: 'POST', corpo: { produtoId: produtoAtual.id, quantidade } });
                CF.toast('Produto adicionado ao carrinho.');
                Cliente.atualizarContador(carrinho);
                modal.hide();
            } catch (e) { CF.erro(e); } // ex.: estoque insuficiente, ou o vendedor tentando comprar da própria loja
        });
    });

    // Busca com pequena espera para não chamar a API a cada tecla
    let espera;
    $('busca').addEventListener('input', () => { clearTimeout(espera); espera = setTimeout(() => { estado.pagina = 0; carregarProdutos(); }, 350); });
    $('categoria').addEventListener('change', () => { estado.pagina = 0; carregarProdutos(); });

    Cliente.desenharTopo();
    carregarCategorias();
    carregarProdutos();
})();
