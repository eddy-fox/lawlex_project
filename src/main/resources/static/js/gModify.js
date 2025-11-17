(function () {
  const msgArea = document.getElementById('msgArea');
  const showMsg = (txt, ok=false) => {
    if (!msgArea) return;
    msgArea.textContent = txt || '';
    msgArea.style.display = txt ? 'block' : 'none';
    msgArea.classList.toggle('ok', !!ok);
    msgArea.classList.toggle('err', !ok);
  };

  // ---------- 프로필 저장 ----------
  const formProfile = document.getElementById('formProfile');
  if (formProfile) {
    formProfile.addEventListener('submit', async (e) => {
      e.preventDefault();
      showMsg('');

      // 3개 관심분야 중복 금지(프론트 가드)
      const i1 = document.getElementById('interestIdx1')?.value || '';
      const i2 = document.getElementById('interestIdx2')?.value || '';
      const i3 = document.getElementById('interestIdx3')?.value || '';
      if (!i1 || !i2 || !i3) {
        showMsg('관심 분야 3개를 모두 선택해주세요.');
        return;
      }
      if (i1 === i2 || i1 === i3 || i2 === i3) {
        showMsg('관심 분야는 서로 다른 항목으로 선택해주세요.');
        return;
      }

      const fd = new FormData(formProfile);
      try {
        const res = await fetch(formProfile.action, { method: 'POST', body: fd });
        const text = await res.text();
        if (res.ok && text.trim() === 'OK') {
          // 저장 후 마이페이지 이동
          window.location.replace('/member/mypage');
        } else {
          showMsg(text || '수정 중 오류가 발생했습니다.');
        }
      } catch (err) {
        console.error(err);
        showMsg('네트워크 오류가 발생했습니다.');
      }
    });
  }

  // ---------- 비밀번호 변경 ----------
  const formPw = document.getElementById('formPw');
  if (formPw) {
    formPw.addEventListener('submit', async (e) => {
      e.preventDefault();
      showMsg('');

      const phone = document.getElementById('pw_phone')?.value.trim();
      const idnum = document.getElementById('pw_idnum')?.value.trim();
      const pw1   = document.getElementById('newPassword')?.value;
      const pw2   = document.getElementById('confirmPassword')?.value;

      if (!phone || !idnum || !pw1 || !pw2) {
        showMsg('전화번호, 생년월일, 새 비밀번호를 모두 입력하세요.');
        return;
      }

      const fd = new FormData(formPw);
      try {
        const res = await fetch(formPw.action, { method: 'POST', body: fd });
        const text = await res.text();
        if (res.ok && text.trim() === 'OK') {
          showMsg('비밀번호가 변경되었습니다.', true);
          // 입력 초기화
          formPw.reset();
        } else {
          // 서버가 "비밀번호 확인이 일치하지 않습니다." 또는 "본인 확인에 실패했습니다." 등 반환
          showMsg(text || '비밀번호 변경 실패');
        }
      } catch (err) {
        console.error(err);
        showMsg('네트워크 오류가 발생했습니다.');
      }
    });
  }

  // ---------- 회원 탈퇴 ----------
  const agreeDelete = document.getElementById('agreeDelete');
  const btnDelete = document.getElementById('btnDelete');
  if (agreeDelete && btnDelete) {
    agreeDelete.addEventListener('change', () => {
      btnDelete.disabled = !agreeDelete.checked;
    });
  }

  const formDelete = document.getElementById('formDelete');
  if (formDelete) {
    formDelete.addEventListener('submit', async (e) => {
      e.preventDefault();
      showMsg('');

      if (!agreeDelete?.checked) {
        showMsg('주의 사항에 동의해야 탈퇴할 수 있습니다.');
        return;
      }

      const phone = document.getElementById('del_phone')?.value.trim();
      const idnum = document.getElementById('del_idnum')?.value.trim();
      if (!phone || !idnum) {
        showMsg('전화번호와 생년월일(6자리)을 입력하세요.');
        return;
      }

      const fd = new FormData(formDelete);
      try {
        const res = await fetch(formDelete.action, { method: 'POST', body: fd });
        const text = await res.text();
        if (res.ok && text.trim() === 'OK') {
          // 세션 무효화 후 로그인으로 유도
          alert('회원탈퇴가 완료되었습니다.');
          window.location.replace('/member/login');
        } else {
          showMsg(text || '회원탈퇴 실패');
        }
      } catch (err) {
        console.error(err);
        showMsg('네트워크 오류가 발생했습니다.');
      }
    });
  }
})();
