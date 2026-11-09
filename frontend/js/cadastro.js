const form = document.getElementById('form-cadastro'), msg = document.getElementById('msg');
form.addEventListener('submit', async e => {
  e.preventDefault();
  let erro = validar(form);
  if (!erro && form.senha.value !== form.confirmar.value) { erro = 'As senhas não coincidem.'; form.confirmar.setAttribute('aria-invalid', 'true'); form.confirmar.focus(); }
  if (erro) return mensagem(msg, erro, 'erro');
  const btn = form.querySelector('button'); btn.disabled = true;
  try {
    await api('/usuarios', { metodo: 'POST', corpo: { nome: form.nome.value.trim(), email: form.email.value.trim(), senha: form.senha.value, tipo: form.tipo.value } });
    mensagem(msg, 'Conta criada com sucesso! Levando você para o login...', 'ok');
    setTimeout(() => location.href = 'login.html', 1500);
  } catch (err) { mensagem(msg, err.message, 'erro'); btn.disabled = false; }
});
