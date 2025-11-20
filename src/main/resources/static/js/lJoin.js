// static/js/lJoin.js
(() => {
  const form = document.getElementById('lawyerJoinForm');
  if (!form) return; // 다른 페이지에서 로드될 때 안전장치

  const $ = (id) => document.getElementById(id);
  const digits = (s) => (s || "").replace(/\D/g, "");

  const lawyerId        = $('lawyerId');
  const lawyerPass      = $('lawyerPass');
  const passConfirm     = $('passConfirm');
  const lawyerRegNo     = $('lawyerIdnum');      // 등록번호
  const lawyerName      = $('lawyerName');
  const lawyerNickname  = $('lawyerNickname');
  const lawyerEmail     = $('lawyerEmail');
  const lawyerAddress   = $('lawyerAddress');
  const lawyerPhone     = $('lawyerPhone');
  const interestIdx     = $('interestIdx');
  const lawyerIntro     = $('lawyerIntro');

  const dupBtn   = $('dupBtn');
  const dupMsg   = $('dupMsg');
  const pwMsg    = $('pwMsg');

  const addTime        = $('addTime');
  const list           = $('selectedList');
  const timeStart      = $('timeStart');
  const timeEnd        = $('timeEnd');
  const availabilityEl = $('availabilityJson');

  const privacyBtn   = $('privacyBtn');
  const lawyerAgree  = $('lawyerAgree');
  const agreeMsg     = $('agreeMsg');

  const certFile     = $('certImage');
  const certPickBtn  = $('certPickBtn');
  const certPreview  = $('certImagePreview');
  const fileMsg      = $('fileMsg');

  const lawyerImageFile  = $('lawyerImage');
  const lawyerImagePickBtn = $('lawyerImagePickBtn');
  const lawyerImagePreview = $('lawyerImagePreview');

  const setDone = (btn, base) => {
    if (!btn) return;
    btn.textContent = base + " 완료";
    btn.classList.add("complete-btn");
  };
  const setUndo = (btn, base) => {
    if (!btn) return;
    btn.textContent = base;
    btn.classList.remove("complete-btn");
  };

  const emailValid = (value) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((value || "").trim());

  // 전화번호 010-1234-5678 포맷
  const formatPhone = (raw) => {
    const d = digits(raw).slice(0, 11);
    if (d.length <= 3) return d;
    if (d.length <= 7) return `${d.slice(0,3)}-${d.slice(3)}`;
    return `${d.slice(0,3)}-${d.slice(3,7)}-${d.slice(7)}`;
  };

  // 등록번호는 숫자만
  lawyerRegNo?.addEventListener('input', (e) => {
    e.target.value = digits(e.target.value);
  });

  lawyerPhone?.addEventListener('input', (e) => {
    const before = e.target.value;
    const after  = formatPhone(before);
    e.target.value = after;
  });

  // ===== 아이디 중복체크 =====
  let lastCheckedId = "";

  const runDupCheck = async () => {
    const id = (lawyerId.value || "").trim();
    if (!id) {
      dupMsg.textContent = "아이디를 입력하세요.";
      lastCheckedId = "";
      return;
    }
    dupMsg.textContent = "확인 중...";
    try {
      // LawyerController 의 /lawyer/api/checkId 사용
      const res = await fetch(`/lawyer/api/checkId?lawyerId=${encodeURIComponent(id)}`);
      const text = (await res.text()).trim();
      if (text === "OK") {
        dupMsg.textContent = "사용 가능한 아이디입니다.";
        lastCheckedId = id;
        setDone(dupBtn, "중복확인");
      } else if (text === "DUP") {
        dupMsg.textContent = "이미 사용 중인 아이디입니다.";
        lastCheckedId = "";
        setUndo(dupBtn, "중복확인");
      } else {
        dupMsg.textContent = "확인 실패. 잠시 후 다시 시도하세요.";
        lastCheckedId = "";
        setUndo(dupBtn, "중복확인");
      }
    } catch (e) {
      console.error(e);
      dupMsg.textContent = "네트워크 오류로 확인에 실패했습니다.";
      lastCheckedId = "";
      setUndo(dupBtn, "중복확인");
    }
  };

  dupBtn?.addEventListener('click', runDupCheck);
  lawyerId?.addEventListener('input', () => {
    dupMsg.textContent = "";
    lastCheckedId = "";
    setUndo(dupBtn, "중복확인");
  });

  // ===== 비밀번호 일치 체크 =====
  const syncPwMsg = () => {
    const a = lawyerPass.value;
    const b = passConfirm.value;
    if (!a && !b) {
      pwMsg.textContent = "";
      return;
    }
    if (a === b) {
      pwMsg.textContent = "비밀번호가 일치합니다.";
    } else {
      pwMsg.textContent = "비밀번호 확인이 일치하지 않습니다.";
    }
  };
  lawyerPass?.addEventListener('input', syncPwMsg);
  passConfirm?.addEventListener('input', syncPwMsg);

  // ===== 상담 가능 요일/시간 관리 =====
  const availability = []; // { days: [...], start: '09:00', end: '18:00' }

  const refreshAvailabilityJson = () => {
    if (availabilityEl) {
      availabilityEl.value = JSON.stringify(availability);
    }
  };

  const addAvailability = () => {
    const checked = Array.from(document.querySelectorAll('.days input:checked'));
    const days = checked.map(d => d.value);
    const start = timeStart.value;
    const end   = timeEnd.value;

    if (days.length === 0 || !start || !end) {
      alert('요일과 시간을 모두 선택해주세요.');
      return;
    }

    const item = { days, start, end };
    availability.push(item);
    refreshAvailabilityJson();

    const tag = document.createElement('div');
    tag.className = 'tag';
    tag.textContent = `${days.join(', ')} ${start}~${end}`;

    const del = document.createElement('button');
    del.type = "button";
    del.textContent = '×';
    del.onclick = () => {
      const idx = availability.indexOf(item);
      if (idx >= 0) availability.splice(idx, 1);
      tag.remove();
      refreshAvailabilityJson();
    };
    tag.appendChild(del);
    list.appendChild(tag);

    checked.forEach(c => { c.checked = false; });
    timeStart.value = '';
    timeEnd.value   = '';
  };

  addTime?.addEventListener('click', addAvailability);

  // ===== 개인정보 수신 동의 토글 =====
  privacyBtn?.addEventListener('click', () => {
    if (lawyerAgree.value === "1") {
      lawyerAgree.value = "0";
      setUndo(privacyBtn, "개인 정보 수신 동의");
      if (agreeMsg) agreeMsg.textContent = "개인 정보 수신 동의가 필요합니다.";
    } else {
      lawyerAgree.value = "1";
      setDone(privacyBtn, "개인 정보 수신 동의");
      if (agreeMsg) agreeMsg.textContent = "";
    }
  });

  // ===== 등록증 파일 선택 & 미리보기 =====
  const previewFile = (input, box, baseTextBtn, btnEl) => {
    if (!input || !box) return;
    const f = input.files && input.files[0];
    box.innerHTML = "";
    if (!f) {
      if (btnEl) setUndo(btnEl, baseTextBtn);
      return;
    }
    const url = URL.createObjectURL(f);
    const img = new Image();
    img.onload = () => URL.revokeObjectURL(url);
    img.src = url;
    img.style.maxWidth = "100%";
    img.style.maxHeight = "160px";
    box.appendChild(img);
    if (btnEl) setDone(btnEl, baseTextBtn);
    if (fileMsg) fileMsg.textContent = "";
  };

  certPickBtn?.addEventListener('click', () => {
    certFile.click();
  });

  certFile?.addEventListener('change', () => {
    previewFile(certFile, certPreview, "등록증 업로드", certPickBtn);
  });

  // ===== 변호사 사진 파일 선택 & 미리보기 =====
  lawyerImagePickBtn?.addEventListener('click', () => {
    lawyerImageFile.click();
  });

  lawyerImageFile?.addEventListener('change', () => {
    const f = lawyerImageFile.files && lawyerImageFile.files[0];
    const preview = document.getElementById('lawyerImagePreview');
    const labelText = document.getElementById('photoLabelText');
    
    if (!f) return;
    
    if (preview) {
      const url = URL.createObjectURL(f);
      const img = preview.querySelector('img');
      
      if (img) {
        img.src = url;
        img.onload = () => {
          URL.revokeObjectURL(url);
        };
        preview.style.display = 'block';
        if (labelText) labelText.style.display = 'none';
      }
    }
    
    if (lawyerImagePickBtn) setDone(lawyerImagePickBtn, "변호사 사진 업로드");
  });

  const validateFile = () => {
    const ok = !!(certFile && certFile.files && certFile.files.length);
    if (!ok && fileMsg) {
      fileMsg.textContent = "등록증 이미지를 첨부해주세요.";
    }
    return ok;
  };

  // ===== 최종 제출 검증 =====
  form.addEventListener('submit', (e) => {
    const errors = [];

    const id = (lawyerId.value || "").trim();
    const pw = (lawyerPass.value || "").trim();
    const cf = (passConfirm.value || "").trim();
    const regNo = (lawyerRegNo.value || "").trim();
    const nm = (lawyerName.value || "").trim();
    const nick = (lawyerNickname.value || "").trim();
    const mail = (lawyerEmail.value || "").trim();
    const phone = digits(lawyerPhone.value || "");
    const interest = interestIdx?.value || "";

    // 기본 필수값
    if (!regNo) errors.push("등록번호를 입력하세요.");
    if (!nm)    errors.push("이름을 입력하세요.");
    if (!id)    errors.push("아이디를 입력하세요.");
    if (!pw)    errors.push("비밀번호를 입력하세요.");
    if (!cf)    errors.push("비밀번호 확인을 입력하세요.");
    if (!nick)  errors.push("닉네임을 입력하세요.");
    if (!mail || !emailValid(mail)) errors.push("올바른 이메일을 입력하세요.");
    if (phone.length < 10) errors.push("전화번호를 올바르게 입력하세요.");
    if (!interest) errors.push("전문 분야를 선택하세요.");

    // 아이디 중복확인
    if (id !== lastCheckedId) {
      errors.push("아이디 중복확인을 완료하세요.");
      dupMsg.textContent = "아이디 중복확인을 완료하세요.";
    }

    // 비밀번호 일치
    if (pw !== cf) {
      errors.push("비밀번호 확인이 일치하지 않습니다.");
      pwMsg.textContent = "비밀번호 확인이 일치하지 않습니다.";
    }

    // 개인정보 수신 동의
    if (lawyerAgree.value !== "1") {
      errors.push("개인 정보 수신 동의가 필요합니다.");
      if (agreeMsg) agreeMsg.textContent = "개인 정보 수신 동의가 필요합니다.";
    }

    // 등록증 파일
    if (!validateFile()) {
      errors.push("등록증 이미지를 첨부해주세요.");
    }

    if (errors.length > 0) {
      e.preventDefault();
      alert(errors[0]); // 첫 에러만 알림
      return;
    }

    // 전송 전에 전화번호 포맷 정리
    lawyerPhone.value = formatPhone(lawyerPhone.value);
  });

})();
