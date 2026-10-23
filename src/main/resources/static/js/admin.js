/* Painel do administrador: lojas (decidir status), usuários (promover perfil) e categorias (CRUD). */
(async () => {
    const eu = await CF.exigirPerfil('ADMINISTRADOR');
    if (!eu) return;

    const R = CF.rotas.admin;
    const $ = (id) => document.getElementById(id);
    const estado = { lojas: [], usuarios: [], categorias: [] };

    $('nomeUsuario').textContent = eu.nome || eu.email || 'Administrador';
    $('btnSair').addEventListener('click', () => CF.sair());

    // Que decisões o admin pode tomar em cada status de loja: [novoStatus, texto, estilo do botão]
    const ACOES_LOJA = {
        EM_ANALISE: [['ATIVA', 'Aprovar', 'success'], ['CANCELADA', 'Recusar', 'outline-danger']],
        ATIVA: [['SUSPENSA', 'Suspender', 'outline-warning']],
        SUSPENSA: [['ATIVA', 'Reativar', 'success'], ['CANCELADA', 'Cancelar', 'outline-danger']],
        CANCELADA: [],
    };
    const nomeVendedor = (l) => l.proprietarioNome ?? l.nomeVendedor ?? '—';

    // ---------- Lojas ----------
    async function carregarLojas() {
        try { estado.lojas = CF.lista(await CF.api(R.lojas)); } catch (e) { CF.erro(e); estado.lojas = []; }
        desenharLojas();
    }

    function botoesLoja(l) {
        const acoes = ACOES_LOJA[l.status] || [];
        if (!acoes.length) return '<span class="text-muted small">Sem ações</span>';
        return acoes.map(([novo, texto, estilo]) =>
            `<button class="btn btn-sm btn-${estilo} ms-1" data-loja="${CF.esc(l.id)}" data-novo="${novo}">${texto}</button>`).join('');
    }

    function desenharLojas() {
        $('tbLojas').innerHTML = estado.lojas.length
            ? estado.lojas.map((l) => `<tr><td class="fw-semibold">${CF.esc(l.nome)}</td><td>${CF.esc(l.cnpj || '—')}</td>
          <td>${CF.esc(nomeVendedor(l))}</td><td>${CF.selo(l.status)}</td><td class="text-end">${botoesLoja(l)}</td></tr>`).join('')
            : CF.vazio('bi-shop', 'Nenhuma loja cadastrada ainda.', 5);
    }

    async function mudarStatusLoja(botao) {
        await CF.comCarregando(botao, async () => {
            try {
                await CF.api(R.lojaStatus(botao.dataset.loja), { metodo: 'PUT', corpo: { status: botao.dataset.novo } });
                CF.toast('Status da loja atualizado.');
                await carregarLojas();
                desenharAnalise();
            } catch (e) { CF.erro(e); }
        });
    }
    // As duas tabelas de loja (lista completa e "esperando decisão") usam os mesmos botões
    document.addEventListener('click', (ev) => {
        const b = ev.target.closest('button[data-loja]');
        if (b) mudarStatusLoja(b);
    });

    // ---------- Usuários ----------
    async function carregarUsuarios() {
        try { estado.usuarios = CF.lista(await CF.api(R.usuarios)); } catch (e) { CF.erro(e); estado.usuarios = []; }
        desenharUsuarios();
    }

    function situacao(u) {
        if (u.inativo) return '<span class="cf-selo rosa">Inativo</span>';
        return u.emailConfirmado ? '<span class="cf-selo verde">Ativo</span>' : '<span class="cf-selo ambar">E-mail não confirmado</span>';
    }

    function desenharUsuarios() {
        const termo = $('buscaUsuario').value.trim().toLowerCase();
        const itens = estado.usuarios.filter((u) => !termo || `${u.nome} ${u.email}`.toLowerCase().includes(termo));
        $('tbUsuarios').innerHTML = itens.length
            ? itens.map((u) => {
                const id = CF.esc(u.id);
                const botaoSituacao = u.inativo
                    ? `<button class="btn btn-sm btn-outline-success ms-1" data-reativar="${id}">Reativar</button>`
                    : `<button class="btn btn-sm btn-outline-danger ms-1" data-inativar="${id}" data-nome="${CF.esc(u.nome)}">Inativar</button>`;
                return `<tr><td class="fw-semibold">${CF.esc(u.nome)}</td><td>${CF.esc(u.email)}</td><td>${CF.selo(u.perfil)}</td><td>${situacao(u)}</td>
            <td class="text-end text-nowrap"><div class="input-group input-group-sm d-inline-flex w-auto align-middle">
              <select class="form-select" data-perfil-de="${id}" aria-label="Novo perfil de ${CF.esc(u.nome)}">
                ${['CLIENTE', 'VENDEDOR', 'ADMINISTRADOR'].map((p) => `<option value="${p}" ${p === u.perfil ? 'selected' : ''}>${CF.rotulo(p)}</option>`).join('')}
              </select>
              <button class="btn btn-outline-primary" data-salvar-perfil="${id}">Salvar</button>
            </div>${botaoSituacao}</td></tr>`;
            }).join('')
            : CF.vazio('bi-people', 'Nenhum usuário encontrado.', 5);
    }
    $('buscaUsuario').addEventListener('input', desenharUsuarios);

    $('tbUsuarios').addEventListener('click', async (ev) => {
        const salvar = ev.target.closest('[data-salvar-perfil]');
        const inativar = ev.target.closest('[data-inativar]');
        const reativar = ev.target.closest('[data-reativar]');

        if (salvar) {
            const id = salvar.dataset.salvarPerfil;
            const perfil = document.querySelector(`select[data-perfil-de="${CSS.escape(id)}"]`).value;
            await CF.comCarregando(salvar, async () => {
                try { await CF.api(R.usuarioPerfil(id), { metodo: 'PUT', corpo: { perfil } }); CF.toast('Perfil atualizado.'); await carregarUsuarios(); }
                catch (e) { CF.erro(e); }
            });
        }
        if (inativar && confirm(`Inativar ${inativar.dataset.nome}?`)) {
            try { await CF.api(R.usuario(inativar.dataset.inativar), { metodo: 'DELETE' }); CF.toast('Usuário inativado.'); await carregarUsuarios(); }
            catch (e) { CF.erro(e); } // recusado se houver pedido em andamento ou for o último administrador
        }
        if (reativar) {
            try { await CF.api(R.usuarioReativar(reativar.dataset.reativar), { metodo: 'POST' }); CF.toast('Usuário reativado.'); await carregarUsuarios(); }
            catch (e) { CF.erro(e); }
        }
    });

    // ---------- Categorias ----------
    async function carregarCategorias() {
        try { estado.categorias = CF.lista(await CF.api(CF.rotas.categorias, { publico: true })); } catch (e) { CF.erro(e); estado.categorias = []; }
        $('tbCategorias').innerHTML = estado.categorias.length
            ? estado.categorias.map((c) => `<tr><td class="fw-semibold">${CF.esc(c.nome)}</td><td>${CF.esc(c.descricao || '—')}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-outline-primary" data-editar-cat="${CF.esc(c.id)}">Editar</button>
            <button class="btn btn-sm btn-outline-danger ms-1" data-excluir-cat="${CF.esc(c.id)}">Excluir</button>
          </td></tr>`).join('')
            : CF.vazio('bi-tags', 'Nenhuma categoria ainda. Crie a primeira ao lado.', 3);
    }

    function limparFormCategoria() {
        $('formCategoria').reset(); $('catId').value = '';
        $('tituloCategoria').textContent = 'Nova categoria';
        $('catCancelar').classList.add('d-none');
    }
    $('catCancelar').addEventListener('click', limparFormCategoria);

    $('formCategoria').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const nome = $('catNome').value.trim();
        if (!nome) return CF.toast('Informe o nome da categoria.', 'erro');
        const id = $('catId').value;
        await CF.comCarregando(ev.submitter, async () => {
            try {
                await CF.api(id ? R.categoria(id) : R.categorias, { metodo: id ? 'PUT' : 'POST', corpo: { nome, descricao: $('catDescricao').value.trim() } });
                CF.toast(id ? 'Categoria atualizada.' : 'Categoria criada.');
                limparFormCategoria();
                await carregarCategorias();
            } catch (e) { CF.erro(e); }
        });
    });

    $('tbCategorias').addEventListener('click', async (ev) => {
        const editar = ev.target.closest('[data-editar-cat]');
        const excluir = ev.target.closest('[data-excluir-cat]');
        if (editar) {
            const c = estado.categorias.find((x) => String(x.id) === editar.dataset.editarCat);
            $('catId').value = c.id; $('catNome').value = c.nome; $('catDescricao').value = c.descricao || '';
            $('tituloCategoria').textContent = 'Editar categoria';
            $('catCancelar').classList.remove('d-none');
            $('catNome').focus();
        }
        if (excluir && confirm('Excluir esta categoria?')) {
            try {
                await CF.api(R.categoria(excluir.dataset.excluirCat), { metodo: 'DELETE' });
                CF.toast('Categoria excluída.');
                await carregarCategorias();
            } catch (e) { CF.erro(e); } // 409 quando há produtos usando a categoria
        }
    });

    // ---------- Resumo ----------
    function desenharAnalise() {
        const fila = estado.lojas.filter((l) => l.status === 'EM_ANALISE');
        $('numAnalise').textContent = fila.length;
        $('numAtivas').textContent = estado.lojas.filter((l) => l.status === 'ATIVA').length;
        $('listaAnalise').innerHTML = fila.length
            ? `<table class="table cf-tabela"><tbody>${fila.map((l) => `<tr><td class="fw-semibold">${CF.esc(l.nome)}</td>
          <td>${CF.esc(nomeVendedor(l))}</td><td class="text-end">${botoesLoja(l)}</td></tr>`).join('')}</tbody></table>`
            : '<span class="text-muted">Nenhuma loja esperando decisão.</span>';
    }

    async function carregarResumo() {
        await Promise.all([carregarLojas(), carregarUsuarios(), carregarCategorias()]);
        desenharAnalise();
        $('numUsuarios').textContent = estado.usuarios.length;
        $('numCategorias').textContent = estado.categorias.length;
    }

    CF.navegacao({ resumo: carregarResumo, lojas: carregarLojas, usuarios: carregarUsuarios, categorias: carregarCategorias });
})();
