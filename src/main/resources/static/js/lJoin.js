// static/js/lJoin.js
(() => {
  // ===== element refs =====
  const form = document.getElementById('lawyerJoinForm');

  const lawyerId     = document.getElementById('lawyerId');
  const lawyerPass   = document.getElementById('lawyerPass');
  const passConfirm  = document.getElementById('passConfirm');
  const lawyerName   = document.getElementById('lawyerName');
  const lawyerEmail  = document.getElementById('lawyerEmail');
  const lawyerAddress= document.getElementById('lawyerAddress');
  const lawyerPhone  = document.getElementById('lawyerPhone');
  const lawyerIdnum  = document.getElementById('lawyerIdnum');

  const dupBtn   = document.getElementById('dupBtn');
  const dupMsg   = document.getElementById('dupMsg');
  const pwMsg    = document.getElementById('pwMsg');

  const addTime  = document.getElementById('addTime');
  const list     = document.getElementById('selectedList');
  const timeStart= document.getElementById('timeStart');
  const timeEnd  = document.getElementById('timeEnd');
  const availabilityJson = document.getElementById('availabilityJson');

  const privacyBtn   = document.getElementById('privacyBtn');
  const lawyerAgree  = document.getElementById('lawyerAgree');
  const agreeMsg     = document.getElementById('agreeMsg'); // 없으면 무시

  const idFile     = document.getElementById('idImage');
  const certFile   = document.getElementById('certImage');
  const idPickBtn  = document.getElementById('idPickBtn');
  const certPickBtn= document.getElementById('certPickBtn');
  const idPreview  = document.getElementById('idImagePreview');
  const certPreview= document.getElementById('certImagePreview');
  const fileMsg    = document.getElementById('fileMsg');

  if (!form) return; // 다른 페이지에서 로드됐을 때 안전장치

  // ===== helpers =====
  const digits = s => (s || "").replace(/\D/g, "");
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
  const emailValid = value =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((value || "").trim());

  // 전화번호 010-1234-5678 포맷팅
  const formatPhone = raw => {
    const d = digits(raw).slice(0, 11);
    if (d.length <= 3) return d;
    if (d.length <= 7) return `${d.slice(0,3)}-${d.slice(3)}`;
    return `${d.slice(0,3)}-${d.slice(3,7)}-${d.slice(7)}`;
  };

  // 생년월일 6자리 제한
  const formatIdnum = raw => digits(raw).slice(0, 6);

  // 파일 미리보기
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

  // ===== ID duplicate check =====
  let lastCheckedId = ""; // 중복확인 성공 시점의 아이디 보관

  const runDupCheck = async () => {
    const id = (lawyerId.value || "").trim();
    if (!id) {
      dupMsg.textContent = "아이디를 입력하세요.";
      lastCheckedId = "";
      return;
    }
    dupMsg.textContent = "확인 중...";
    try {
      // 서버는 memberId 파라미터를 받도록 통합되어 있음
      const r = await fetch(`/member/api/checkId?memberId=${encodeURIComponent(id)}`);
      const t = await r.text();
      if (t === "OK") {
        dupMsg.textContent = "사용 가능한 아이디입니다.";
        lastCheckedId = id;
        setDone(dupBtn, "중복확인");
      } else if (t === "DUP") {
        dupMsg.textContent = "이미 사용 중인 아이디입니다.";
        lastCheckedId = "";
        setUndo(dupBtn, "중복확인");
      } else {
        dupMsg.textContent = "확인 실패. 잠시 후 다시 시도하세요.";
        lastCheckedId = "";
        setUndo(dupBtn, "중복확인");
      }
    } catch (e) {
      dupMsg.textContent = "네트워크 오류로 확인에 실패했습니다.";
      lastCheckedId = "";
      setUndo(dupBtn, "중복확인");
    }
  };

  dupBtn?.addEventListener('click', runDupCheck);
  lawyerId?.addEventListener('input', () => {
    if (!dupMsg) return;
    dupMsg.textContent = "";       // 입력 중엔 메시지 숨김
    lastCheckedId = "";            // 다시 확인 필요
    setUndo(dupBtn, "중복확인");
  });

  // ===== password confirm live check =====
  const syncPwMsg = () => {
    const a = lawyerPass.value;
    const b = passConfirm.value;
    if (!a || !b) {
      pwMsg.textContent = "";
      return;
    }
    pwMsg.textContent = a === b ? "비밀번호가 일치합니다." : "비밀번호 확인이 일치하지 않습니다.";
  };
  lawyerPass?.addEventListener('input', syncPwMsg);
  passConfirm?.addEventListener('input', syncPwMsg);

  // ===== phone / idnum formatting =====
  lawyerPhone?.addEventListener('input', e => {
    const caret = e.target.selectionStart;
    const before = e.target.value;
    const after = formatPhone(before);
    e.target.value = after;
  });
  lawyerIdnum?.addEventListener('input', e => {
    e.target.value = formatIdnum(e.target.value);
  });

  // ===== availability (요일/시간) 관리 =====
  const availability = []; // {days:[...], start:'09:00', end:'18:00'}
  const refreshAvailabilityJson = () => {
    availabilityJson.value = JSON.stringify(availability);
  };

  const addAvailability = () => {
    const checked = [...document.querySelectorAll('.days input:checked')];
    const days = checked.map(d => d.value);
    const start = timeStart.value;
    const end = timeEnd.value;
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
    timeEnd.value = '';
  };
  addTime?.addEventListener('click', addAvailability);

  // ===== privacy (동의) 토글 =====
  privacyBtn?.addEventListener('click', () => {
    if (lawyerAgree.value === "Y") {
      lawyerAgree.value = "N";
      setUndo(privacyBtn, "개인 정보 수신 동의");
      if (agreeMsg) agreeMsg.textContent = "";
    } else {
      lawyerAgree.value = "Y";
      setDone(privacyBtn, "개인 정보 수신 동의");
      if (agreeMsg) agreeMsg.textContent = "";
    }
  });

  // ===== file pick & preview =====
  idPickBtn?.addEventListener('click', () => idFile.click());
  certPickBtn?.addEventListener('click', () => certFile.click());

  idFile?.addEventListener('change', () => {
    previewFile(idFile, idPreview, "신분증 업로드", idPickBtn);
  });
  certFile?.addEventListener('change', () => {
    previewFile(certFile, certPreview, "등록증 업로드", certPickBtn);
  });

  const validateFiles = () => {
    const ok = !!(idFile.files.length && certFile.files.length);
    if (!ok && fileMsg) fileMsg.textContent = "신분증과 등록증 이미지를 모두 첨부해야 합니다.";
    return ok;
  };

  // ===== submit validation =====
  form.addEventListener('submit', (e) => {
    const errors = [];

    // 필수값
    if (!(lawyerId.value || "").trim())     errors.push("아이디를 입력하세요.");
    if (!(lawyerPass.value || "").trim())   errors.push("비밀번호를 입력하세요.");
    if (!(passConfirm.value || "").trim())  errors.push("비밀번호 확인을 입력하세요.");
    if (!(lawyerName.value || "").trim())   errors.push("닉네임을 입력하세요.");
    if (!emailValid(lawyerEmail.value))     errors.push("올바른 이메일 형식을 입력하세요.");
    if (digits(lawyerPhone.value).length < 10) errors.push("전화번호를 올바르게 입력하세요.");
    if (digits(lawyerIdnum.value).length !== 6) errors.push("생년월일(6자리)을 입력하세요.");

    // 중복확인
    if ((lawyerId.value || "").trim() !== lastCheckedId) {
      errors.push("아이디 중복확인을 완료하세요.");
      if (dupMsg) dupMsg.textContent = "아이디 중복확인을 완료하세요.";
    }

    // 비밀번호 확인
    if (lawyerPass.value !== passConfirm.value) {
      errors.push("비밀번호 확인이 일치하지 않습니다.");
      if (pwMsg) pwMsg.textContent = "비밀번호 확인이 일치하지 않습니다.";
    }

    // 동의
    if (lawyerAgree.value !== "Y") {
      errors.push("개인 정보 수신 동의가 필요합니다.");
      if (agreeMsg) agreeMsg.textContent = "개인 정보 수신 동의가 필요합니다.";
    }

    // 파일
    if (!validateFiles()) errors.push("신분증/등록증 이미지를 첨부하세요.");

    if (errors.length > 0) {
      e.preventDefault();
      alert(errors[0]); // 과도한 경고 방지: 첫 메시지만
      return;
    }

    // 최종 전송 전 마무리: 전화번호 포맷 정리
    lawyerPhone.value = formatPhone(lawyerPhone.value);
    // 그대로 submit (multipart/form-data)
  });

})();
