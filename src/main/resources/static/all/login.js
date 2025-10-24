    const form = document.querySelector('.login-form');
    const idInput = document.querySelector('#username');
    const pwInput = document.querySelector('#password');
    const errId = document.querySelector('#err-username');
    const errPw = document.querySelector('#err-password');

    form.addEventListener('submit', e => {
      e.preventDefault();
      const id = idInput.value.trim();
      const pw = pwInput.value.trim();
      errId.textContent = ''; errPw.textContent = '';
      idInput.classList.remove('is-error'); pwInput.classList.remove('is-error');

      if (!id) { errId.textContent = '아이디를 입력해주세요.'; idInput.classList.add('is-error'); idInput.focus(); return; }
      if (!pw) { errPw.textContent = '비밀번호를 입력해주세요.'; pwInput.classList.add('is-error'); pwInput.focus(); return; }
      if (pw.length < 4) { errPw.textContent = '비밀번호는 4자 이상이어야 합니다.'; pwInput.classList.add('is-error'); pwInput.focus(); return; }

      alert('로그인 되었습니다.'); });

    document.querySelector('#signupBtn').addEventListener('click',()=>{
      window.location.href='signup.html'; });