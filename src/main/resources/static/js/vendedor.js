/* Painel do vendedor: minha loja, produtos (CRUD + estoque) e pedidos (avançar status). */
(async () => {
    const eu = await CF.exigirPerfil('VENDEDOR');
    if (!eu) return;

    const R = CF.rotas.vendedor;
    const $ = (id) => document.getElementById(id);
    const estado = { loja: null, produtos: [], pedidos: [], categorias: [], paginaProdutos: 0, totalPaginasProdutos: 1, totalProdutos: 0 };
    const modalProduto = new bootstrap.Modal($('modalProduto'));
    const modalRastreio = new bootstrap.Modal($('modalRastreio'));
    let pedidoParaEnviar = null;

    $('nomeUsuario').textContent = eu.nome || eu.email || 'Vendedor';
    $('btnSair').addEventListener('click', () => CF.sair());

    // O vendedor só faz estas transições. PENDENTE/PAGO dependem do pagamento (ainda não implementado).
    const PROXIMO = { PAGO: 'EM_PRODUCAO', EM_PRODUCAO: 'PRONTO', PRONTO: 'ENVIADO', ENVIADO: 'ENTREGUE' };
    const numeroOuNulo = (id) => { const v = $(id).value; return v === '' ? null : Number(v); };

    // ---------- Loja ----------
    // GET /api/vendedor/loja devolve a loja do vendedor; sem loja a API recusa (404) e o formulário vira "Criar".
    async function carregarLoja() {
        try {
            const r = await CF.api(R.loja);
            estado.loja = Array.isArray(r) ? (r[0] ?? null) : (r?.id ? r : null);
        } catch (e) { estado.loja = null; if (e.status !== 404) CF.erro(e); }

        const l = estado.loja;
        const end = l?.endereco;
        $('tituloLoja').textContent = l ? 'Dados da loja' : 'Criar minha loja';
        $('seloLoja').innerHTML = l ? CF.selo(l.status) : '';
        $('btnSalvarLoja').textContent = l ? 'Salvar alterações' : 'Criar loja';
        $('lojaNome').value = l?.nome || '';
        $('lojaCnpj').value = l?.cnpj || '';
        $('lojaDescricao').value = l?.descricao || '';
        $('lojaEmail').value = l?.emailContato || '';
        $('lojaTelefone').value = l?.telefone || '';
        $('lojaCep').value = end?.cep || '';
        $('lojaNumero').value = end?.numero || '';
        $('lojaComplemento').value = end?.complemento || '';
        $('lojaEnderecoAtual').textContent = end?.logradouro
            ? `Endereço cadastrado: ${end.logradouro}, ${end.numero ?? ''} · ${end.bairro ?? ''} · ${end.cidade ?? ''}/${end.uf ?? ''}` : '';
    }

    $('formLoja').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const nome = $('lojaNome').value.trim();
        if (!nome) return CF.toast('Informe o nome da loja.', 'erro');
        const corpo = {
            nome,
            descricao: $('lojaDescricao').value.trim(),
            cnpj: $('lojaCnpj').value.replace(/\D/g, ''),
            emailContato: $('lojaEmail').value.trim(),
            telefone: $('lojaTelefone').value.trim(),
        };
        if (!corpo.cnpj) delete corpo.cnpj;
        const cep = $('lojaCep').value.trim();
        if (cep) corpo.endereco = { cep, numero: $('lojaNumero').value.trim(), complemento: $('lojaComplemento').value.trim() };

        await CF.comCarregando($('btnSalvarLoja'), async () => {
            try {
                await CF.api(R.loja, { metodo: estado.loja ? 'PUT' : 'POST', corpo });
                CF.toast(estado.loja ? 'Loja atualizada.' : 'Loja criada! Ela fica em análise até o administrador ativar.');
                await carregarLoja();
            } catch (e) { CF.erro(e); } // 503 quando o ViaCEP está fora do ar
        });
    });

    // ---------- Produtos ----------
    // Campos da API (ProdutoRequest/Response): quantidadeEstoque, pesoKg, alturaCm, larguraCm, comprimentoCm, imagem, personalizavel, status.
    const TAMANHO_PAGINA = 12;

    async function carregarCategorias() {
        if (estado.categorias.length) return;
        try { estado.categorias = CF.lista(await CF.api(CF.rotas.categorias, { publico: true })); } catch { /* o select fica vazio */ }
        $('prodCategoria').innerHTML = estado.categorias.map((c) => `<option value="${CF.esc(c.id)}">${CF.esc(c.nome)}</option>`).join('');
    }

    async function carregarProdutos() {
        await carregarCategorias();
        try {
            const pg = CF.pagina(await CF.api(`${R.produtos}?page=${estado.paginaProdutos}&size=${TAMANHO_PAGINA}`));
            estado.produtos = pg.itens; estado.totalPaginasProdutos = pg.totalPaginas; estado.totalProdutos = pg.total;
        } catch (e) {
            if (e.status !== 404) CF.erro(e);
            estado.produtos = []; estado.totalPaginasProdutos = 1; estado.totalProdutos = 0;
        }
        $('infoProdutos').textContent = `${estado.totalProdutos} produto(s)`;
        $('tbProdutos').innerHTML = estado.produtos.length
            ? estado.produtos.map((p) => {
                const id = CF.esc(p.id);
                const alvo = p.status === 'ATIVO' ? 'INATIVO' : 'ATIVO';
                return `<tr>
            <td><div class="fw-semibold">${CF.esc(p.nome)}</div><div class="small text-muted">${CF.esc(p.categoriaNome || '')}${p.personalizavel ? ' · Personalizável' : ''}</div></td>
            <td>${CF.brl(p.preco)}</td>
            <td><div class="input-group input-group-sm">
              <input type="number" step="1" min="0" class="form-control estoque-campo" value="${CF.esc(p.quantidadeEstoque ?? 0)}" data-estoque-de="${id}" aria-label="Estoque de ${CF.esc(p.nome)}">
              <button class="btn btn-outline-primary" data-salvar-estoque="${id}">Atualizar</button></div></td>
            <td>${CF.selo(p.status)}${p.status === 'ATIVO' && !p.disponivel ? '<div class="small text-muted mt-1">Sem estoque</div>' : ''}</td>
            <td class="text-end text-nowrap">
              <button class="btn btn-sm btn-outline-primary" data-editar="${id}">Editar</button>
              <button class="btn btn-sm btn-outline-secondary ms-1" data-status-de="${id}" data-novo="${alvo}">${alvo === 'ATIVO' ? 'Ativar' : 'Inativar'}</button>
              <button class="btn btn-sm btn-outline-danger ms-1" data-excluir="${id}">Excluir</button></td></tr>`;
            }).join('')
            : CF.vazio('bi-box-seam', 'Você ainda não cadastrou produtos. Use "Novo produto".', 5);

        $('pagProdutos').innerHTML = estado.totalPaginasProdutos > 1
            ? `<button class="btn btn-sm btn-outline-primary" data-pagina="-1" ${estado.paginaProdutos === 0 ? 'disabled' : ''}>Anterior</button>
         <span class="small text-muted">Página ${estado.paginaProdutos + 1} de ${estado.totalPaginasProdutos}</span>
         <button class="btn btn-sm btn-outline-primary" data-pagina="1" ${estado.paginaProdutos + 1 >= estado.totalPaginasProdutos ? 'disabled' : ''}>Próxima</button>` : '';
    }

    $('pagProdutos').addEventListener('click', (ev) => {
        const b = ev.target.closest('[data-pagina]');
        if (b) { estado.paginaProdutos += Number(b.dataset.pagina); carregarProdutos(); }
    });

    function abrirProduto(p) {
        $('formProduto').reset();
        $('prodId').value = p?.id || '';
        $('tituloProduto').textContent = p ? 'Editar produto' : 'Novo produto';
        if (p) {
            $('prodNome').value = p.nome || ''; $('prodDescricao').value = p.descricao || ''; $('prodPreco').value = p.preco ?? '';
            $('prodEstoque').value = p.quantidadeEstoque ?? 0; $('prodPeso').value = p.pesoKg ?? '';
            $('prodAltura').value = p.alturaCm ?? ''; $('prodLargura').value = p.larguraCm ?? ''; $('prodComprimento').value = p.comprimentoCm ?? '';
            $('prodImagem').value = p.imagem || ''; $('prodPersonalizavel').checked = !!p.personalizavel;
            $('prodCategoria').value = p.categoriaId ?? '';
        }
        modalProduto.show();
    }
    $('btnNovoProduto').addEventListener('click', () => abrirProduto(null));

    $('formProduto').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const id = $('prodId').value;
        const corpo = {
            nome: $('prodNome').value.trim(),
            descricao: $('prodDescricao').value.trim(),
            categoriaId: Number($('prodCategoria').value) || null,
            preco: numeroOuNulo('prodPreco'),
            quantidadeEstoque: numeroOuNulo('prodEstoque') ?? 0,
            imagem: $('prodImagem').value.trim(),
            personalizavel: $('prodPersonalizavel').checked,
            pesoKg: numeroOuNulo('prodPeso'),
            alturaCm: numeroOuNulo('prodAltura'),
            larguraCm: numeroOuNulo('prodLargura'),
            comprimentoCm: numeroOuNulo('prodComprimento'),
        };
        if (!corpo.nome || !corpo.descricao) return CF.toast('Informe o nome e a descrição do produto.', 'erro');
        if (!corpo.categoriaId) return CF.toast('Escolha uma categoria.', 'erro');
        if (!corpo.preco || corpo.preco <= 0) return CF.toast('O preço precisa ser maior que zero.', 'erro');
        if (!corpo.pesoKg || corpo.pesoKg <= 0) return CF.toast('Informe o peso em kg (maior que zero).', 'erro');
        const medidasInvalidas = ['alturaCm', 'larguraCm', 'comprimentoCm'].filter((k) => !Number.isInteger(corpo[k]) || corpo[k] < 1);
        if (medidasInvalidas.length) return CF.toast('Altura, largura e comprimento precisam ser números inteiros de 1 cm ou mais.', 'erro');

        await CF.comCarregando(ev.submitter, async () => {
            try {
                await CF.api(id ? R.produto(id) : R.produtos, { metodo: id ? 'PUT' : 'POST', corpo });
                modalProduto.hide();
                CF.toast(id ? 'Produto atualizado.' : 'Produto criado.');
                await carregarProdutos();
            } catch (e) { CF.erro(e); }
        });
    });

    $('tbProdutos').addEventListener('click', async (ev) => {
        const editar = ev.target.closest('[data-editar]');
        const excluir = ev.target.closest('[data-excluir]');
        const estoque = ev.target.closest('[data-salvar-estoque]');
        const status = ev.target.closest('[data-status-de]');
        if (editar) abrirProduto(estado.produtos.find((p) => String(p.id) === editar.dataset.editar));
        if (excluir && confirm('Excluir este produto? Produtos com pedidos não podem ser excluídos (use Inativar).')) {
            try { await CF.api(R.produto(excluir.dataset.excluir), { metodo: 'DELETE' }); CF.toast('Produto excluído.'); await carregarProdutos(); }
            catch (e) { CF.erro(e); }
        }
        if (status) {
            await CF.comCarregando(status, async () => {
                try { await CF.api(R.produtoStatus(status.dataset.statusDe), { metodo: 'PUT', corpo: { status: status.dataset.novo } }); CF.toast('Status do produto atualizado.'); await carregarProdutos(); }
                catch (e) { CF.erro(e); }
            });
        }
        if (estoque) {
            const id = estoque.dataset.salvarEstoque;
            const quantidade = Number(document.querySelector(`input[data-estoque-de="${CSS.escape(id)}"]`).value);
            if (!Number.isInteger(quantidade) || quantidade < 0) return CF.toast('Informe um estoque válido (0 ou mais).', 'erro');
            await CF.comCarregando(estoque, async () => {
                try { await CF.api(R.estoque(id), { metodo: 'PUT', corpo: { quantidade } }); CF.toast('Estoque atualizado.'); await carregarProdutos(); }
                catch (e) { CF.erro(e); }
            });
        }
    });

    // ---------- Pedidos ----------
    async function carregarPedidos() {
        try { estado.pedidos = CF.lista(await CF.api(R.pedidos)); } catch (e) { if (e.status !== 404) CF.erro(e); estado.pedidos = []; }
        $('tbPedidos').innerHTML = estado.pedidos.length
            ? estado.pedidos.map((p) => {
                const prox = PROXIMO[p.status];
                const acao = prox
                    ? `<button class="btn btn-sm btn-primary" data-pedido="${CF.esc(p.id)}" data-novo="${prox}">Marcar como ${CF.rotulo(prox).toLowerCase()}</button>`
                    : `<span class="pedido-aviso">${p.status === 'PENDENTE' ? 'Aguardando pagamento' : 'Sem ações'}</span>`;
                return `<tr><td class="fw-semibold">#${CF.esc(String(p.id).slice(0, 8))}</td><td>${CF.data(p.dataCriacao)}</td><td>${CF.esc(p.clienteNome || '—')}</td>
            <td>${CF.brl(p.valorTotal)}</td><td>${CF.selo(p.status)}</td><td class="text-end">${acao}</td></tr>`;
            }).join('')
            : CF.vazio('bi-receipt', 'Nenhum pedido recebido ainda.', 6);
    }

    async function mudarStatusPedido(id, status, codigoRastreio, botao) {
        const corpo = { status };
        if (codigoRastreio) corpo.codigoRastreio = codigoRastreio;
        await CF.comCarregando(botao, async () => {
            try {
                await CF.api(R.pedidoStatus(id), { metodo: 'PUT', corpo });
                CF.toast('Pedido atualizado.');
                modalRastreio.hide();
                await carregarPedidos();
            } catch (e) { CF.erro(e); }
        });
    }

    $('tbPedidos').addEventListener('click', (ev) => {
        const b = ev.target.closest('button[data-pedido]');
        if (!b) return;
        if (b.dataset.novo === 'ENVIADO') { pedidoParaEnviar = b.dataset.pedido; $('codigoRastreio').value = ''; modalRastreio.show(); }
        else mudarStatusPedido(b.dataset.pedido, b.dataset.novo, null, b);
    });
    $('formRastreio').addEventListener('submit', (ev) => {
        ev.preventDefault();
        const codigo = $('codigoRastreio').value.trim();
        if (!codigo) return CF.toast('Informe o código de rastreio.', 'erro');
        mudarStatusPedido(pedidoParaEnviar, 'ENVIADO', codigo, ev.submitter);
    });

    // ---------- Resumo ----------
    async function carregarResumo() {
        await Promise.all([carregarLoja(), carregarProdutos(), carregarPedidos()]);
        $('resLoja').innerHTML = estado.loja ? CF.selo(estado.loja.status) : '<span class="fs-6 text-muted">Sem loja</span>';
        $('resProdutos').textContent = estado.totalProdutos;
        $('resPedidos').textContent = estado.pedidos.filter((p) => ['PAGO', 'EM_PRODUCAO', 'PRONTO', 'ENVIADO'].includes(p.status)).length;
        const aviso = $('avisoLoja');
        const mensagens = { null: 'Você ainda não tem loja. Abra "Minha loja" para criar a sua.', EM_ANALISE: 'Sua loja está em análise. Você poderá vender depois da aprovação do administrador.', SUSPENSA: 'Sua loja está suspensa. Fale com o administrador.', CANCELADA: 'Sua loja foi cancelada.' };
        const texto = mensagens[estado.loja?.status ?? null];
        aviso.classList.toggle('d-none', !texto);
        aviso.textContent = texto || '';
    }

    CF.navegacao({ resumo: carregarResumo, loja: carregarLoja, produtos: carregarProdutos, pedidos: carregarPedidos });
})();
