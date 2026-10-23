/* Tela de login: envia as credenciais, guarda o token e leva cada perfil ao seu painel. */
(() => {
    const form = document.getElementById('formLogin');
    const aviso = document.getElementById('aviso');
    const campoSenha = document.getElementById('senha');

    const mostrarAviso = (texto, tipo) => { aviso.className = `alert alert-${tipo}`; aviso.textContent = texto; };

    // Já existe sessão? NÃO redireciona sozinho (isso poderia criar um laço de recarregamento):
    // mostra um aviso e deixa a pessoa escolher continuar ou entrar com outra conta.
    const sessao = CF.usuario();
    const bannerSessao = document.getElementById('sessaoAtiva'); // só existe no login.html novo
    if (bannerSessao && CF.token() && sessao?.perfil) {
        document.getElementById('sessaoNome').textContent = sessao.nome || sessao.email || 'usuário';
        document.getElementById('sessaoContinuar').href = CF.telaDoPerfil(sessao.perfil);
        bannerSessao.classList.remove('d-none');
        document.getElementById('sessaoSair').addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.removeItem('t'); sessionStorage.removeItem('u');
            bannerSessao.classList.add('d-none');
        });
    }

    const params = new URLSearchParams(location.search);
    if (params.get('cadastrado')) mostrarAviso('E-mail confirmado! Agora é só entrar.', 'success');
    if (params.get('sessao') === 'expirada') mostrarAviso('Sua sessão expirou. Entre novamente.', 'warning');

    document.getElementById('verSenha').addEventListener('click', (e) => {
        const visivel = campoSenha.type === 'text';
        campoSenha.type = visivel ? 'password' : 'text';
        e.currentTarget.innerHTML = `<i class="bi ${visivel ? 'bi-eye' : 'bi-eye-slash'}"></i>`;
    });

    form.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const login = document.getElementById('login').value.trim();
        const senha = campoSenha.value;
        if (!login || !senha) return mostrarAviso('Informe o e-mail (ou usuário) e a senha.', 'warning');

        await CF.comCarregando(form.querySelector('button[type=submit]'), async () => {
            try {
                // A API responde { token, usuario: { nome, perfil, ... } }
                const r = await CF.api(CF.rotas.login, { metodo: 'POST', corpo: { login, senha }, publico: true });
                if (!r?.token || !r?.usuario) throw new Error('Resposta de login inesperada. Confira o corpo no Postman.');
                CF.salvarSessao(r.token, r.usuario);
                location.href = CF.telaDoPerfil(r.usuario.perfil);
            } catch (e) {
                // Mostra a mensagem da própria API (senha errada, e-mail não confirmado, conta inativa...)
                mostrarAviso(e.message, 'danger');
            }
        });
    });
})();
