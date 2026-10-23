/* Cadastro em duas etapas: 1) dados da conta, 2) confirmação do e-mail (link simulado no console). */
(() => {
    const $ = (id) => document.getElementById(id);
    const formCadastro = $('formCadastro');
    const formConfirmar = $('formConfirmar');

    const aviso = (elId, texto, tipo) => { const el = $(elId); el.className = `alert alert-${tipo}`; el.textContent = texto; };
    const soDigitos = (v) => v.replace(/\D/g, '');

    function mostrarEtapaConfirmar() {
        formCadastro.classList.add('d-none');
        formConfirmar.classList.remove('d-none');
        $('tokenConfirmar').focus();
    }
    if (new URLSearchParams(location.search).get('confirmar')) mostrarEtapaConfirmar();

    // Máscara simples do CPF enquanto digita (000.000.000-00)
    $('cpf').addEventListener('input', (e) => {
        const d = soDigitos(e.target.value).slice(0, 11);
        e.target.value = d.replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    });

    // ---------- Etapa 1 ----------
    formCadastro.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const corpo = {
            nome: $('nome').value.trim(),
            nomeUsuario: $('nomeUsuario').value.trim(),
            email: $('email').value.trim(),
            cpf: soDigitos($('cpf').value),
            telefone: soDigitos($('telefone').value),
            senha: $('senha').value,
            confirmarSenha: $('confirmarSenha').value,
            perfil: formCadastro.querySelector('input[name=perfil]:checked').value,
        };

        // Validações rápidas no navegador; a API valida tudo de novo.
        if (!corpo.nome || !corpo.nomeUsuario || !corpo.email || !corpo.cpf || !corpo.senha) return aviso('aviso', 'Preencha todos os campos obrigatórios.', 'warning');
        if (!/^\S+@\S+\.\S+$/.test(corpo.email)) return aviso('aviso', 'Informe um e-mail válido.', 'warning');
        if (corpo.cpf.length !== 11) return aviso('aviso', 'O CPF precisa ter 11 dígitos.', 'warning');
        if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(corpo.senha)) return aviso('aviso', 'A senha precisa ter 8 ou mais caracteres, com letra e número.', 'warning');
        if (corpo.senha !== corpo.confirmarSenha) return aviso('aviso', 'As senhas não são iguais.', 'warning');

        await CF.comCarregando(formCadastro.querySelector('button[type=submit]'), async () => {
            try {
                await CF.api(CF.rotas.cadastro, { metodo: 'POST', corpo, publico: true });
                CF.toast('Conta criada! Falta confirmar o e-mail.');
                mostrarEtapaConfirmar();
            } catch (e) { aviso('aviso', e.message, 'danger'); } // 409: e-mail, usuário ou CPF já usados
        });
    });

    // ---------- Etapa 2 ----------
    formConfirmar.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const bruto = $('tokenConfirmar').value.trim();
        if (!bruto) return aviso('avisoConfirmar', 'Cole o link ou o código de confirmação.', 'warning');
        const achado = bruto.match(/token=([\w-]+)/);            // aceita o link inteiro ou só o token
        const token = achado ? achado[1] : bruto;

        await CF.comCarregando(formConfirmar.querySelector('button[type=submit]'), async () => {
            try {
                await CF.api(`${CF.rotas.confirmarEmail}?token=${encodeURIComponent(token)}`, { publico: true });
                location.href = CF.url('/login?cadastrado=1');
            } catch (e) { aviso('avisoConfirmar', e.message, 'danger'); } // token usado ou inválido
        });
    });
})();
