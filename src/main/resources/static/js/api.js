/* api.js — código compartilhado por todas as telas.
   Cuida de: sessão (token), chamadas à API, redirecionamento por perfil e pequenos utilitários.
   Se algum endpoint da sua API tiver outro caminho, ajuste SÓ o objeto "rotas" abaixo. */
const CF = (() => {
    const ctx = (document.querySelector('meta[name="ctx"]')?.content || '/').replace(/\/$/, '');
    const CHAVE_TOKEN = 't';
    const CHAVE_USUARIO = 'u';

    // ---------- Rotas da API (ajuste aqui se precisar) ----------
    const rotas = {
        login: '/api/auth/login',
        cadastro: '/api/auth/cadastro',
        confirmarEmail: '/api/auth/confirmar-email',
        categorias: '/api/categorias',     // leitura pública
        produtos: '/api/produtos',         // vitrine: leitura pública, paginada
        admin: {
            lojas: '/api/admin/lojas',
            lojaStatus: (id) => `/api/admin/lojas/${id}/status`,
            usuarios: '/api/admin/usuarios',
            usuario: (id) => `/api/admin/usuarios/${id}`,                 // DELETE = inativar
            usuarioReativar: (id) => `/api/admin/usuarios/${id}/reativar`, // POST
            usuarioPerfil: (id) => `/api/admin/usuarios/${id}/perfil`,
            categorias: '/api/admin/categorias',
            categoria: (id) => `/api/admin/categorias/${id}`,
        },
        vendedor: {
            loja: '/api/vendedor/loja',
            produtos: '/api/vendedor/produtos',
            produto: (id) => `/api/vendedor/produtos/${id}`,
            estoque: (id) => `/api/vendedor/produtos/${id}/estoque`,
            pedidos: '/api/vendedor/pedidos',
            pedidoStatus: (id) => `/api/vendedor/pedidos/${id}/status`,
        },
    };

    // ---------- Sessão ----------
    const url = (caminho) => ctx + caminho;
    const token = () => sessionStorage.getItem(CHAVE_TOKEN);
    const usuario = () => { try { return JSON.parse(sessionStorage.getItem(CHAVE_USUARIO)); } catch { return null; } };
    const salvarSessao = (t, u) => { sessionStorage.setItem(CHAVE_TOKEN, t); if (u) sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(u)); };
    const salvarUsuario = (u) => sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(u));
    const limparSessao = () => { sessionStorage.removeItem(CHAVE_TOKEN); sessionStorage.removeItem(CHAVE_USUARIO); };

    function sair(expirada = false) {
        limparSessao();
        if (location.pathname.replace(ctx, '') === '/login') return; // já está no login: não recarrega
        location.href = url('/login' + (expirada ? '?sessao=expirada' : ''));
    }

    // Cada perfil tem a sua tela inicial. CLIENTE ainda não tem painel: vai para o index.html de testes.
    function telaDoPerfil(perfil) {
        if (perfil === 'ADMINISTRADOR') return url('/admin/painel');
        if (perfil === 'VENDEDOR') return url('/vendedor/painel');
        return url('/');                    // CLIENTE: vitrine (o /index.html não existe como arquivo estático)
    }

    // ---------- Chamadas à API ----------
    // A API devolve { mensagem, detalhes: [...] }; juntamos os dois num texto só.
    function mensagemDeErro(d) {
        if (!d) return null;
        if (typeof d === 'string') return d;
        const base = d.mensagem || d.message || d.erro || d.error || '';
        const det = Array.isArray(d.detalhes) ? d.detalhes.join('; ') : '';
        return base ? (det ? `${base} — ${det}` : base) : (det || null);
    }

    async function api(caminho, { metodo = 'GET', corpo, publico = false } = {}) {
        const headers = { 'Content-Type': 'application/json' };
        if (!publico && token()) headers.Authorization = 'Bearer ' + token();
        let resp;
        try {
            resp = await fetch(url(caminho), { method: metodo, headers, body: corpo !== undefined ? JSON.stringify(corpo) : undefined });
        } catch {
            throw Object.assign(new Error('Não foi possível falar com o servidor. Ele está rodando?'), { status: 0 });
        }
        const texto = await resp.text();
        let dados = null;
        if (texto) { try { dados = JSON.parse(texto); } catch { dados = texto; } }
        if (!resp.ok) {
            if (resp.status === 401 && !publico) sair(true);
            throw Object.assign(new Error(mensagemDeErro(dados) || `Erro ${resp.status}`), { status: resp.status, dados });
        }
        return dados;
    }

    // A API devolve ou uma lista simples, ou uma página do projeto: PaginaResponse { conteudo, pagina, tamanho, total, totalPaginas }.
    // (aceita também 'content', o nome padrão do Page do Spring)
    const lista = (r) => Array.isArray(r) ? r : (r?.conteudo ?? r?.content ?? []);

    // Normaliza uma resposta paginada para { itens, total, totalPaginas } (usado pela vitrine).
    const pagina = (r) => {
        const itens = lista(r);
        return {
            itens,
            total: r?.total ?? r?.totalElements ?? itens.length,
            totalPaginas: r?.totalPaginas ?? r?.totalPages ?? 1,
        };
    };

    // Garante que há login e que o perfil é o esperado; senão redireciona.
    // A API relê o perfil no banco a cada requisição, então um perfil defasado aqui só resulta em 403.
    function exigirPerfil(perfilEsperado) {
        const u = usuario();
        if (!token() || !u) { location.href = url('/login'); return null; }
        if (u.perfil !== perfilEsperado) { location.href = telaDoPerfil(u.perfil); return null; }
        return u;
    }

    // Troca de seções dentro de um painel (#lojas, #usuarios...) e chama o carregador de cada uma.
    function navegacao(carregadores) {
        const links = [...document.querySelectorAll('[data-secao]')];
        const secoes = [...document.querySelectorAll('section[data-painel]')];
        const titulo = document.getElementById('tituloSecao');
        const ir = (nome) => {
            if (!carregadores[nome]) nome = Object.keys(carregadores)[0];
            secoes.forEach((s) => s.classList.toggle('d-none', s.dataset.painel !== nome));
            links.forEach((a) => a.classList.toggle('ativo', a.dataset.secao === nome));
            titulo.textContent = links.find((a) => a.dataset.secao === nome)?.textContent.trim() || '';
            carregadores[nome]();
        };
        window.addEventListener('hashchange', () => ir(location.hash.slice(1)));
        ir(location.hash.slice(1));
        return ir;
    }

    // ---------- Utilitários de tela ----------
    // Escapa texto antes de colocar em innerHTML (evita injeção de HTML vindo dos dados).
    const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    const brl = (n) => Number(n ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const data = (v) => v ? new Date(v).toLocaleDateString('pt-BR') : '—';

    const ROTULOS = {
        EM_ANALISE: 'Em análise', ATIVA: 'Ativa', SUSPENSA: 'Suspensa', CANCELADA: 'Cancelada',
        PENDENTE: 'Pendente', PAGO: 'Pago', EM_PRODUCAO: 'Em produção', PRONTO: 'Pronto', ENVIADO: 'Enviado',
        ENTREGUE: 'Entregue', CANCELADO: 'Cancelado',
        CLIENTE: 'Cliente', VENDEDOR: 'Vendedor', ADMINISTRADOR: 'Administrador',
    };
    const CORES = {
        EM_ANALISE: 'ambar', ATIVA: 'verde', SUSPENSA: 'rosa', CANCELADA: 'cinza',
        PENDENTE: 'ambar', PAGO: 'azul', EM_PRODUCAO: 'roxo', PRONTO: 'azul', ENVIADO: 'roxo', ENTREGUE: 'verde', CANCELADO: 'cinza',
        CLIENTE: 'cinza', VENDEDOR: 'roxo', ADMINISTRADOR: 'verde',
    };
    const rotulo = (s) => ROTULOS[s] || String(s ?? '—').replace(/_/g, ' ');
    const selo = (s) => `<span class="cf-selo ${CORES[s] || 'cinza'}">${esc(rotulo(s))}</span>`;
    const vazio = (icone, texto, colunas) =>
        `<tr><td colspan="${colunas}"><div class="cf-vazio"><i class="bi ${icone}"></i>${esc(texto)}</div></td></tr>`;

    function toast(mensagem, tipo = 'sucesso') {
        const area = document.getElementById('toasts');
        if (!area) return alert(mensagem);
        const icone = tipo === 'erro' ? 'bi-x-circle-fill text-danger' : 'bi-check-circle-fill text-success';
        const el = document.createElement('div');
        el.className = 'toast align-items-center';
        el.setAttribute('role', 'alert');
        el.innerHTML = `<div class="d-flex"><div class="toast-body d-flex gap-2 align-items-start">
      <i class="bi ${icone}"></i><span>${esc(mensagem)}</span></div>
      <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Fechar"></button></div>`;
        area.appendChild(el);
        const t = new bootstrap.Toast(el, { delay: tipo === 'erro' ? 6000 : 3500 });
        el.addEventListener('hidden.bs.toast', () => el.remove());
        t.show();
    }
    const erro = (e) => toast(e.message || 'Algo deu errado.', 'erro');

    // Desabilita o botão e mostra "carregando" enquanto uma ação roda.
    async function comCarregando(botao, acao) {
        const original = botao.innerHTML;
        botao.disabled = true;
        botao.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Aguarde';
        try { return await acao(); } finally { botao.disabled = false; botao.innerHTML = original; }
    }

    return { rotas, url, token, usuario, salvarSessao, salvarUsuario, sair, telaDoPerfil, api, lista, pagina, exigirPerfil,
        navegacao, esc, brl, data, rotulo, selo, vazio, toast, erro, comCarregando };
})();