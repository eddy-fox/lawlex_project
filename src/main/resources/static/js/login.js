const form = document.querySelector('.login-form');
const idInput = document.querySelector('#username');
const pwInput = document.querySelector('#password');
const errId = document.querySelector('#err-username');
const errPw = document.querySelector('#err-password');

form.addEventListener('submit', async (e) => {
  e.preventDefault();

  const id = idInput.value.trim();
  const pw = pwInput.value.trim();

  // 에러 초기화
  errId.textContent = '';
  errPw.textContent = '';
  idInput.classList.remove('is-error');
  pwInput.classList.remove('is-error');

  // 간단 검증
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
  // 더미가 3자리까지 있어서 3으로 설정
  if (pw.length < 3) {
    errPw.textContent = '비밀번호는 3자 이상이어야 합니다.';
    pwInput.classList.add('is-error');
    pwInput.focus();
    return;
  }

  // 스프링 컨트롤러가 기대하는 이름으로 전송
  const formData = new FormData();
  formData.append('memberId', id);
  formData.append('memberPass', pw);

  try {
    const res = await fetch('/member/login', {
      method: 'POST',
      body: formData
    });

    // 컨트롤러가 redirect:/ 로 보내니까 그쪽으로 이동
    if (res.redirected) {
      window.location.href = res.url;
    } else {
      // 실패 시 다시 로그인 페이지로
      window.location.href = '/member/login?error';
    }
  } catch (err) {
    console.error(err);
    alert('로그인 중 오류가 발생했습니다.');
  }
});

// 회원가입 이동
const signupBtn = document.querySelector('#signupBtn');
if (signupBtn) {
  signupBtn.addEventListener('click', () => {
    // 너 컨트롤러 기준으로 회원가입 경로
    window.location.href = '/member/join/type';
  });
}
