// Assinatura da newsletter (Jornal da Biblioteca). Rota: POST /api/newsletter
(() => {
  const form = document.getElementById('form-newsletter');
  const msg = document.getElementById('msg-newsletter');
  if (!form) return;

  if (sessao.usuario) {
    form.email.value = sessao.usuario.email || '';
    form.nome.value = sessao.usuario.nome || '';
  }

  form.addEventListener('submit', async e => {
    e.preventDefault();
    const email = form.email.value.trim();
    form.email.removeAttribute('aria-invalid');

    if (!email) { form.email.setAttribute('aria-invalid', 'true'); form.email.focus(); return mensagem(msg, 'Informe seu e-mail.', 'erro'); }
    if (!form.email.checkValidity()) { form.email.setAttribute('aria-invalid', 'true'); form.email.focus(); return mensagem(msg, 'Digite um e-mail válido.', 'erro'); }

    const btn = form.querySelector('button'); btn.disabled = true;
    try {
      const d = await api('/newsletter', { metodo: 'POST', corpo: { nome: form.nome.value.trim(), email } });
      mensagem(msg, d.mensagem || 'Assinatura realizada!', 'ok');
      form.reset();
    } catch (err) { mensagem(msg, err.message, 'erro'); }
    finally { btn.disabled = false; }
  });
})();
