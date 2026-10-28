/* Código comum das telas do cliente: topo (conta + contador do carrinho) e pequenos formatadores. */
const Cliente = (() => {
    // Telas que exigem login mandam quem não tem sessão para o login.
    function exigirLogin() {
        if (!CF.token() || !CF.usuario()) { location.href = CF.url('/login'); return null; }
        return CF.usuario();
    }

    function desenharTopo() {
        const u = CF.usuario();
        const logado = !!(CF.token() && u);
        const area = document.getElementById('areaConta');

        // Marca o item de menu da página atual e mostra os links que exigem login
        const caminho = location.pathname.replace(CF.url(''), '') || '/';
        document.querySelectorAll('[data-nav]').forEach((a) => a.classList.toggle('ativo', a.dataset.nav === caminho));
        document.querySelectorAll('[data-requer-login]').forEach((a) => a.classList.toggle('d-none', !logado));

        if (!logado) {
            area.innerHTML = `<a class="btn btn-sm btn-outline-primary" href="${CF.url('/login')}">Entrar</a>
        <a class="btn btn-sm btn-primary" href="${CF.url('/cadastro')}">Criar conta</a>`;
            return;
        }
        const painel = u.perfil === 'CLIENTE' ? '' :
            `<a class="btn btn-sm btn-outline-primary" href="${CF.telaDoPerfil(u.perfil)}">Meu painel</a>`;
        area.innerHTML = `<span class="small fw-semibold d-none d-md-inline">${CF.esc(u.nome || u.email || '')}</span>${painel}
      <button class="btn btn-sm btn-outline-secondary" id="btnSairTopo">Sair</button>`;
        document.getElementById('btnSairTopo').addEventListener('click', () => {
            sessionStorage.removeItem('t'); sessionStorage.removeItem('u'); location.href = CF.url('/');
        });
        atualizarContador();
    }

    // Mostra no menu quantas unidades há no carrinho. Aceita o carrinho já carregado para evitar outra chamada.
    async function atualizarContador(carrinho) {
        const el = document.getElementById('contadorCarrinho');
        if (!el || !CF.token()) return;
        try {
            const c = carrinho ?? await CF.api(CF.rotas.carrinho.ver);
            const n = (c?.lojas ?? []).flatMap((l) => l.itens).reduce((soma, i) => soma + (i.quantidade || 0), 0);
            el.textContent = n; el.classList.toggle('d-none', n === 0);
        } catch { /* o contador é só um enfeite: se falhar, some */ }
    }

    const cep = (v) => { const d = String(v ?? '').replace(/\D/g, ''); return d.length === 8 ? `${d.slice(0, 5)}-${d.slice(5)}` : (v ?? ''); };
    const enderecoLinha = (e) => e
        ? `${e.logradouro || 'Endereço'}, ${e.numero}${e.complemento ? ' - ' + e.complemento : ''} · ${e.bairro || ''} · ${e.cidade || ''}/${e.uf || ''} · CEP ${cep(e.cep)}`
        : '—';
    const dataHora = (v) => v ? new Date(v).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—';

    return { exigirLogin, desenharTopo, atualizarContador, cep, enderecoLinha, dataHora };
})();
