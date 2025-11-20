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

  const form = $("#gJoinForm");
  const submitBtn = $("#submitBtn");

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
    if (!(d.length === 10 || d.length === 11)) {
      show(phoneMsg, "전화번호 자릿수가 올바르지 않습니다.", false);
      return false;
    }
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
    if (!birth6.test(v)) {
      show(birthMsg, "생년월일은 6자리(YYMMDD)로 입력하세요.", false);
      return false;
    }
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
    if (!a || !b || !c) {
      show(interestMsg, "관심 분야 3개를 모두 선택하세요.", false);
      return false;
    }
    if (a === b || a === c || b === c) {
      show(interestMsg, "서로 다른 항목으로 선택하세요.", false);
      return false;
    }
    show(interestMsg, "");
    return true;
  }
  
  [interest1, interest2, interest3].forEach(el => el.addEventListener("change", validateInterests));

  // 개인정보 수신 동의 토글
  const privacyBtn = $("#privacyBtn");
  const memberAgree = $("#memberAgree");
  const agreeMsg = $("#agreeMsg");

  function validateAgree() {
    if (memberAgree.value !== "1") {
      show(agreeMsg, "개인정보 수신동의(필수) 필요", false);
      return false;
    }
    show(agreeMsg, "");
    return true;
  }

  privacyBtn.addEventListener("click", () => {
    const on = memberAgree.value === "1";
    memberAgree.value = on ? "0" : "1";

    privacyBtn.classList.toggle("active", !on);
    privacyBtn.classList.toggle("agreed", !on);
    privacyBtn.setAttribute("aria-pressed", String(!on));
    privacyBtn.textContent = !on ? "개인 정보 수신 동의(동의됨)" : "개인 정보 수신 동의";

    validateAgree();
  });

  // 제출
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // 필수 검증
    if (!validatePhone()) {
      memberPhone.focus();
      return;
    }
    if (!validateBirth()) {
      memberIdnum.focus();
      return;
    }
    if (!validateInterests()) {
      interest1.focus();
      return;
    }
    if (!validateAgree()) {
      privacyBtn.focus();
      return;
    }

    submitBtn.disabled = true;
    const orig = submitBtn.textContent;
    submitBtn.textContent = "처리 중…";

    try {
      const action = form.getAttribute("action") || "/member/joinMember-oauth";
      const res = await fetch(action, {
        method: "POST",
        body: new FormData(form),
        credentials: "same-origin",
        redirect: "follow",
      });

      // 302 리다이렉트 처리
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