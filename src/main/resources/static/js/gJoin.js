(() => {
  const $ = (s) => document.querySelector(s);
  const show = (el, msg, isOk) => {
    if (!el) return;
    if (!msg) {
      el.textContent = "";
      el.classList.remove("error", "success");
      return;
    }
    el.textContent = msg;
    el.classList.toggle("error", isOk === false);
    el.classList.toggle("success", isOk === true);
  };

  const form = $("#joinForm");
  const submitBtn = $("#submitBtn");

  // 아이디 & 중복확인
  const memberId = $("#memberId");
  const dupBtn = $("#dupBtn");
  const dupMsg = $("#dupMsg");
  let idCheckedOk = false;
  let idTouched = false;

  memberId.addEventListener("input", () => {
    idTouched = true;
    idCheckedOk = false;
    const v = memberId.value.trim();
    if (!v) { show(dupMsg, ""); return; }
    if (v.length < 4) { show(dupMsg, "아이디는 3자 이상 입력하세요.", false); return; }
    show(dupMsg, "중복확인을 눌러주세요.");
  });

  dupBtn.addEventListener("click", async () => {
    const v = memberId.value.trim();
    if (v.length < 3) { show(dupMsg, "아이디는 3자 이상 입력하세요.", false); memberId.focus(); return; }
    try {
      show(dupMsg, "확인 중…");
      const res = await fetch(`/member/api/checkId?memberId=${encodeURIComponent(v)}`, { credentials: "same-origin" });
      const text = (await res.text()).trim();
      if (text === "OK") { idCheckedOk = true; show(dupMsg, "사용 가능한 아이디입니다.", true); }
      else if (text === "DUP") { idCheckedOk = false; show(dupMsg, "이미 사용 중인 아이디입니다.", false); }
      else { idCheckedOk = false; show(dupMsg, `확인 실패: ${text}`, false); }
    } catch (e) {
      idCheckedOk = false;
      show(dupMsg, `오류: ${e.message}`, false);
    }
  });

  // 비밀번호
  const memberPass = $("#memberPass");
  const passConfirm = $("#passConfirm");
  const pwMsg = $("#pwMsg");
  function validatePw() {
    const a = memberPass.value;
    const b = passConfirm.value;
    if (a.length < 3) { show(pwMsg, "비밀번호는 3자 이상이어야 합니다.", false); return false; }
    if (a !== b) { show(pwMsg, "비밀번호 확인이 일치하지 않습니다.", false); return false; }
    show(pwMsg, "");
    return true;
  }
  memberPass.addEventListener("input", validatePw);
  passConfirm.addEventListener("input", validatePw);

  // ✅ 이름
  const memberName = $("#memberName");
  const nameMsg = $("#nameMsg");
  function validateName() {
    const v = (memberName.value || "").trim();
    if (!v) {
      show(nameMsg, "이름을 입력해주세요.", false);
      return false;
    }
    show(nameMsg, "");
    return true;
  }
  memberName.addEventListener("blur", validateName);
  memberName.addEventListener("input", () => show(nameMsg, ""));

  // 이메일
  const memberEmail = $("#memberEmail");
  const emailMsg = $("#emailMsg");
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  function validateEmail() {
    const v = (memberEmail.value || "").trim();
    if (!emailRegex.test(v)) { show(emailMsg, "올바른 이메일 형식이 아닙니다. 예) user@example.com", false); return false; }
    show(emailMsg, "");
    return true;
  }
  memberEmail.addEventListener("blur", validateEmail);
  memberEmail.addEventListener("input", () => show(emailMsg, ""));

  // 전화번호
  const memberPhone = $("#memberPhone");
  const phoneMsg = $("#phoneMsg");
  function fmtPhone(raw) {
    const d = (raw || "").replace(/\D/g, "");
    if (d.length <= 3) return d;
    if (d.length <= 7) return d.slice(0,3) + "-" + d.slice(3);
    return d.slice(0,3) + "-" + d.slice(3,7) + "-" + d.slice(7,11);
  }
  function validatePhone() {
    const d = (memberPhone.value || "").replace(/\D/g, "");
    if (!(d.length === 10 || d.length === 11)) { show(phoneMsg, "전화번호 자릿수가 올바르지 않습니다.", false); return false; }
    show(phoneMsg, "");
    return true;
  }
  memberPhone.addEventListener("input", () => {
    memberPhone.value = fmtPhone(memberPhone.value);
    show(phoneMsg, "");
  });
  memberPhone.addEventListener("blur", validatePhone);

  // 생년월일(YYMMDD 6자리)
  const memberIdnum = $("#memberIdnum");
  const birthMsg = $("#birthMsg");
  const birth6 = /^[0-9]{6}$/;
  function validateBirth() {
    const v = (memberIdnum.value || "").replace(/\D/g, "");
    if (!birth6.test(v)) { show(birthMsg, "생년월일은 6자리(YYMMDD)로 입력하세요.", false); return false; }
    show(birthMsg, "");
    return true;
  }
  memberIdnum.addEventListener("input", () => {
    memberIdnum.value = (memberIdnum.value || "").replace(/\D/g, "").slice(0,6);
    show(birthMsg, "");
  });
  memberIdnum.addEventListener("blur", validateBirth);

  // 관심분야
  const interest1 = $("#interest1");
  const interest2 = $("#interest2");
  const interest3 = $("#interest3");
  const interestMsg = $("#interestMsg");
  function validateInterests() {
    const a = interest1.value, b = interest2.value, c = interest3.value;
    if (!a || !b || !c) { show(interestMsg, "관심 분야 3개를 모두 선택하세요.", false); return false; }
    if (a === b || a === c || b === c) { show(interestMsg, "서로 다른 항목으로 선택하세요.", false); return false; }
    show(interestMsg, "");
    return true;
  }
  [interest1, interest2, interest3].forEach(el => el.addEventListener("change", validateInterests));

  // 개인정보 수신 동의 토글
  const privacyBtn = document.querySelector("#privacyBtn");
  const memberAgree = document.querySelector("#memberAgree");
  const agreeMsg = document.querySelector("#agreeMsg");

  function validateAgree() {
    if (memberAgree.value !== "1") {
      agreeMsg.textContent = "개인정보 수신동의(필수) 필요";
      agreeMsg.classList.add("error"); agreeMsg.classList.remove("success");
      return false;
    }
    agreeMsg.textContent = "";
    agreeMsg.classList.remove("error","success");
    return true;
  }

  privacyBtn.addEventListener("click", () => {
    const on = memberAgree.value === "1";
    memberAgree.value = on ? "0" : "1";

    privacyBtn.classList.toggle("active", !on);
    privacyBtn.setAttribute("aria-pressed", String(!on));
    privacyBtn.textContent = !on ? "개인 정보 수신 동의(동의됨)" : "개인 정보 수신 동의";

    validateAgree();
  });

  // 제출
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // 필수 검증
    if ((memberId.value || "").trim().length < 4) { show(dupMsg, "아이디는 4자 이상 입력하세요.", false); memberId.focus(); return; }
    if (!idCheckedOk) { if (idTouched) show(dupMsg, "중복확인을 진행하세요.", false); memberId.focus(); return; }
    if (!validatePw()) { passConfirm.focus(); return; }
    if (!validateName()) { memberName.focus(); return; }
    if (!validateEmail()) { memberEmail.focus(); return; }
    if (!validatePhone()) { memberPhone.focus(); return; }
    if (!validateBirth()) { memberIdnum.focus(); return; }
    if (!validateInterests()) { interest1.focus(); return; }
    if (!validateAgree()) { privacyBtn.focus(); return; }

    const fd = new FormData(form);
    submitBtn.disabled = true;
    const orig = submitBtn.textContent;
    submitBtn.textContent = "처리 중…";

    try {
      const action = form.getAttribute("action") || "/member/join/normal";
      const res = await fetch(action, {
        method: "POST",
        body: fd,
        credentials: "same-origin",
        redirect: "follow",
      });

      // 302 → login?joined=true 로 이동 처리
      if (res.redirected) {
        window.location.href = res.url;
        return;
      }

      const text = await res.text();

      if (res.ok) {
        if (/^OK$/i.test(text.trim())) {
          window.location.href = "/member/login?joined=true";
          return;
        }
        alert(text.trim());
        return;
      }

      alert(text || "가입 처리 중 오류가 발생했습니다.");
    } catch (err) {
      alert(`네트워크 오류: ${err.message}`);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = orig;
    }
  });
})();
