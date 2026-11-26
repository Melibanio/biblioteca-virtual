const form = document.getElementById('form-login'), msg = document.getElementById('msg');
form.addEventListener('submit', async e => {
  e.preventDefault();
  const erro = validar(form); if (erro) return mensagem(msg, erro, 'erro');
  const btn = form.querySelector('button'); btn.disabled = true;
  try {
    const d = await api('/login', { metodo: 'POST', corpo: { email: form.email.value.trim(), senha: form.senha.value } });
    sessao.salvar(d.token, d.usuario);
    mensagem(msg, 'Login realizado! Redirecionando...', 'ok');
    const voltar = new URLSearchParams(location.search).get('voltar') || '';
    setTimeout(() => location.href = /^[\w-]+\.html(\?[\w=&-]*)?$/.test(voltar) ? voltar : 'index.html', 800);
  } catch (err) { mensagem(msg, err.message, 'erro'); btn.disabled = false; }
});
