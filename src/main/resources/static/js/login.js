const form = document.querySelector('.login-form');
const idInput = document.querySelector('#username');
const pwInput = document.querySelector('#password');
const errId = document.querySelector('#err-username');
const errPw = document.querySelector('#err-password');

// URL 파라미터 기준 에러/알림 출력
(function initErrorMessage() {
  const params = new URLSearchParams(window.location.search);
  const error = params.get('error');
  const joined = params.get('joined');
  const deactivated = params.get('deactivated');

  if (error === 'badpw') {
    errPw.textContent = '아이디 또는 비밀번호가 올바르지 않습니다.';
  } else if (error === 'nouser') {
    errId.textContent = '존재하지 않는 계정입니다.';
  } else if (error === 'deactivated') {
    errId.textContent = '탈퇴 처리된 계정입니다. 새로 가입해 주세요.';
  }

  if (joined === 'true') {
    alert('회원가입이 완료되었습니다. 로그인 해주세요.');
  }
  if (deactivated === 'true') {
    alert('회원탈퇴가 완료되었습니다.');
  }
})();

form.addEventListener('submit', async (e) => {
  e.preventDefault();

  const id = idInput.value.trim();
  const pw = pwInput.value.trim();

  errId.textContent = '';
  errPw.textContent = '';
  idInput.classList.remove('is-error');
  pwInput.classList.remove('is-error');

  if (!id) {
    errId.textContent = '아이디를 입력해주세요.';
    idInput.classList.add('is-error');
    idInput.focus();
    return;
  }
  if (!pw) {
    errPw.textContent = '비밀번호를 입력해주세요.';
    pwInput.classList.add('is-error');
    pwInput.focus();
    return;
  }
  if (pw.length < 3) {
    errPw.textContent = '비밀번호는 3자 이상이어야 합니다.';
    pwInput.classList.add('is-error');
    pwInput.focus();
    return;
  }

  const formData = new FormData();
  formData.append('memberId', id);
  formData.append('memberPass', pw);

  try {
    const res = await fetch('/member/login', {
      method: 'POST',
      body: formData
    });

    if (res.redirected) {
      window.location.href = res.url;
    } else {
      window.location.href = '/member/login?error=unknown';
    }
  } catch (err) {
    console.error(err);
    alert('로그인 중 오류가 발생했습니다.');
  }
});

const signupBtn = document.querySelector('#signupBtn');
if (signupBtn) {
  signupBtn.addEventListener('click', () => {
    window.location.href = '/member/join/type';
  });
}
