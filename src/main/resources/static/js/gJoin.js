// gJoin.js - 회원가입 페이지 전용 (디자인 클래스는 그대로 사용)
(function () {
  const $ = (sel) => document.querySelector(sel);

  const form = $('#joinForm');
  const memberId = $('#memberId');
  const dupBtn = $('#dupBtn');
  const dupMsg = $('#dupMsg');

  const pass = $('#memberPass');
  const pass2 = $('#passConfirm');
  const pwMsg = $('#pwMsg');

  const phone = $('#memberPhone');
  const idnum = $('#memberIdnum');

  const i1 = $('#interest1');
  const i2 = $('#interest2');
  const i3 = $('#interest3');
  const interestMsg = $('#interestMsg');

  const privacyBtn = $('#privacyBtn');
  const memberAgree = $('#memberAgree');
  const agreeMsg = $('#agreeMsg');

  let dupOk = false;

  // 유틸
  function setMsg(el, text, ok) {
    el.textContent = text || '';
    el.classList.remove('ok', 'bad');
    if (!text) return;
    el.classList.add(ok ? 'ok' : 'bad');
  }
  function digitsOnly(input) {
    input.value = (input.value || '').replace(/\D/g, '');
  }
  function allDistinct(a, b, c) {
    return a && b && c && a !== b && a !== c && b !== c;
  }

  // 아이디 입력이 바뀌면 중복확인 상태 초기화
  memberId.addEventListener('input', () => {
    dupOk = false;
    setMsg(dupMsg, '');
  });

  // 아이디 중복확인 (member + lawyer 통합 체크, 서버가 "OK"/"DUP" 반환)
  dupBtn.addEventListener('click', async () => {
    const id = (memberId.value || '').trim();
    if (!id) {
      setMsg(dupMsg, '아이디를 입력하세요.', false);
      memberId.focus();
      return;
    }
    try {
      const res = await fetch(`/member/api/checkId?memberId=${encodeURIComponent(id)}`, {
        method: 'GET',
        credentials: 'same-origin',
      });
      const txt = (await res.text()).trim().toUpperCase();

      if (txt === 'OK') {
        dupOk = true;
        setMsg(dupMsg, '사용 가능한 아이디입니다.', true);
        dupBtn.classList.add('complete-btn');
      } else if (txt === 'DUP') {
        dupOk = false;
        setMsg(dupMsg, '이미 사용 중인 아이디입니다.', false);
        dupBtn.classList.remove('complete-btn');
      } else {
        dupOk = false;
        setMsg(dupMsg, `확인 실패: ${txt}`, false);
      }
    } catch (e) {
      dupOk = false;
      setMsg(dupMsg, '중복 확인 중 오류가 발생했습니다.', false);
    }
  });

  // 비밀번호 일치 실시간 체크
  function checkPwMatch() {
    const a = pass.value || '';
    const b = pass2.value || '';
    if (!a && !b) {
      setMsg(pwMsg, '');
      return true;
    }
    if (a.length < 3) {
      setMsg(pwMsg, '비밀번호는 3자 이상 입력하세요.', false);
      return false;
    }
    if (a === b) {
      setMsg(pwMsg, '비밀번호가 일치합니다.', true);
      return true;
    } else {
      setMsg(pwMsg, '비밀번호가 일치하지 않습니다.', false);
      return false;
    }
  }
  pass.addEventListener('input', checkPwMatch);
  pass2.addEventListener('input', checkPwMatch);

  // 숫자만
  phone.addEventListener('input', () => digitsOnly(phone));
  idnum.addEventListener('input', () => digitsOnly(idnum));

  // 관심분야 중복 방지
  function validateInterests() {
    const a = i1.value, b = i2.value, c = i3.value;
    if (!a || !b || !c) {
      setMsg(interestMsg, '관심 분야 3개를 모두 선택하세요.', false);
      return false;
    }
    if (!allDistinct(a, b, c)) {
      setMsg(interestMsg, '서로 다른 관심 분야로 선택해주세요.', false);
      return false;
    }
    setMsg(interestMsg, '');
    return true;
  }
  [i1, i2, i3].forEach(sel => sel.addEventListener('change', validateInterests));

  // 개인정보 수신 동의 토글 (두 번 누르면 취소)
  privacyBtn.addEventListener('click', () => {
    const on = memberAgree.value === 'Y';
    if (on) {
      memberAgree.value = 'N';
      privacyBtn.classList.remove('complete-btn');
      setMsg(agreeMsg, '수신 동의가 해제되었습니다.', false);
    } else {
      memberAgree.value = 'Y';
      privacyBtn.classList.add('complete-btn');
      setMsg(agreeMsg, '수신 동의 완료', true);
    }
  });

  // 제출
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // 최소 검증
    if (!dupOk) {
      setMsg(dupMsg, '아이디 중복확인을 진행하세요.', false);
      memberId.focus();
      return;
    }
    if (!checkPwMatch()) {
      pass2.focus();
      return;
    }
    if (!validateInterests()) {
      i1.focus();
      return;
    }

    try {
      const fd = new FormData(form); // CSRF 히든필드 포함
      const res = await fetch(form.action, {
        method: 'POST',
        body: fd,
        credentials: 'same-origin',
      });

      // 서버가 redirect:/member/login?joined 로 리다이렉트
      if (res.redirected) {
        window.location.href = res.url;
        return;
      }

      // 실패 시 대충 메시지
      if (!res.ok) {
        setMsg(agreeMsg, '가입 처리 중 오류가 발생했습니다.', false);
      } else {
        // 혹시 200으로 뭔가 내려오면 로그인으로 유도
        window.location.href = '/member/login?joined';
      }
    } catch (err) {
      setMsg(agreeMsg, '네트워크 오류가 발생했습니다.', false);
    }
  });
})();
