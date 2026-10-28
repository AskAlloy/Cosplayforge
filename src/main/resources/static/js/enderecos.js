/* Meus endereços: lista, cadastro, edição e exclusão (rua, bairro e cidade vêm do ViaCEP no servidor). */
(async () => {
    if (!Cliente.exigirLogin()) return;
    Cliente.desenharTopo();

    const $ = (id) => document.getElementById(id);
    const R = CF.rotas;
    let enderecos = [];

    async function carregar() {
        try { enderecos = CF.lista(await CF.api(R.enderecos)); } catch (e) { CF.erro(e); enderecos = []; }
        $('listaEnderecos').innerHTML = enderecos.length
            ? enderecos.map((e) => `<div class="cli-endereco">
          <div><i class="bi bi-geo-alt me-2 text-muted"></i>${CF.esc(Cliente.enderecoLinha(e))}</div>
          <div class="text-nowrap">
            <button class="btn btn-sm btn-outline-primary" data-editar="${CF.esc(e.id)}">Editar</button>
            <button class="btn btn-sm btn-outline-danger ms-1" data-excluir="${CF.esc(e.id)}">Excluir</button></div></div>`).join('')
            : '<div class="cf-vazio"><i class="bi bi-geo-alt"></i>Nenhum endereço cadastrado. Use o formulário ao lado.</div>';
    }

    function limpar() {
        $('formEndereco').reset(); $('endId').value = '';
        $('tituloEndereco').textContent = 'Novo endereço';
        $('endCancelar').classList.add('d-none');
    }
    $('endCancelar').addEventListener('click', limpar);

    // Máscara do CEP (00000-000)
    $('endCep').addEventListener('input', (e) => {
        const d = e.target.value.replace(/\D/g, '').slice(0, 8);
        e.target.value = d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
    });

    $('formEndereco').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const corpo = {
            cep: $('endCep').value.trim(), numero: $('endNumero').value.trim(), complemento: $('endComplemento').value.trim(),
            logradouro: $('endLogradouro').value.trim(), bairro: $('endBairro').value.trim(),
        };
        if (!/^\d{5}-?\d{3}$/.test(corpo.cep)) return CF.toast('CEP inválido: use 8 dígitos (ex.: 01310-100).', 'erro');
        if (!corpo.numero) return CF.toast('Informe o número.', 'erro');
        const id = $('endId').value;
        await CF.comCarregando(ev.submitter, async () => {
            try {
                await CF.api(id ? R.endereco(id) : R.enderecos, { metodo: id ? 'PUT' : 'POST', corpo });
                CF.toast(id ? 'Endereço atualizado.' : 'Endereço salvo.');
                limpar(); await carregar();
            } catch (e) { CF.erro(e); } // 503 quando o ViaCEP está fora do ar
        });
    });

    $('listaEnderecos').addEventListener('click', async (ev) => {
        const editar = ev.target.closest('[data-editar]');
        const excluir = ev.target.closest('[data-excluir]');
        if (editar) {
            const e = enderecos.find((x) => String(x.id) === editar.dataset.editar);
            $('endId').value = e.id; $('endCep').value = Cliente.cep(e.cep); $('endNumero').value = e.numero || '';
            $('endComplemento').value = e.complemento || ''; $('endLogradouro').value = e.logradouro || ''; $('endBairro').value = e.bairro || '';
            $('tituloEndereco').textContent = 'Editar endereço'; $('endCancelar').classList.remove('d-none');
            $('endNumero').focus();
        }
        if (excluir && confirm('Excluir este endereço?')) {
            try { await CF.api(R.endereco(excluir.dataset.excluir), { metodo: 'DELETE' }); CF.toast('Endereço excluído.'); await carregar(); }
            catch (e) { CF.erro(e); } // pode ser recusado se algum pedido usa o endereço
        }
    });

    await carregar();
})();
