/* Vitrine pública: lista produtos (GET /api/produtos, paginado), filtra e mostra o detalhe.
   Funciona sem login; se houver sessão, o topo mostra o nome e os atalhos. */
(() => {
    const $ = (id) => document.getElementById(id);
    const R = CF.rotas;
    const TAMANHO = 12;
    const estado = { pagina: 0, totalPaginas: 1, total: 0, itens: [] };
    const modal = new bootstrap.Modal($('modalProduto'));

    // ---------- Topo (conta) ----------
    function desenharConta() {
        const u = CF.usuario();
        const area = $('areaConta');
        if (!CF.token() || !u) {
            area.innerHTML = `<a class="btn btn-sm btn-outline-primary" href="${CF.url('/login')}">Entrar</a>
        <a class="btn btn-sm btn-primary" href="${CF.url('/cadastro')}">Criar conta</a>`;
            return;
        }
        const painel = u.perfil === 'CLIENTE' ? '' :
            `<a class="btn btn-sm btn-outline-primary" href="${CF.telaDoPerfil(u.perfil)}">Meu painel</a>`;
        area.innerHTML = `<span class="small fw-semibold d-none d-sm-inline">${CF.esc(u.nome || u.email || '')}</span>${painel}
      <button class="btn btn-sm btn-outline-secondary" id="btnSairVitrine">Sair</button>`;
        $('btnSairVitrine').addEventListener('click', () => {
            sessionStorage.removeItem('t'); sessionStorage.removeItem('u'); location.reload();
        });
    }

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

    // Busca com pequena espera para não chamar a API a cada tecla
    let espera;
    $('busca').addEventListener('input', () => { clearTimeout(espera); espera = setTimeout(() => { estado.pagina = 0; carregarProdutos(); }, 350); });
    $('categoria').addEventListener('change', () => { estado.pagina = 0; carregarProdutos(); });

    desenharConta();
    carregarCategorias();
    carregarProdutos();
})();
