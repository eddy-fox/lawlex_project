// 템플 디버그용 로그
console.log("[loginFind.js] loaded");

function $(id){ return document.getElementById(id); }

const overlay = $("modalOverlay");
const modalMsg = $("modalMessage");
const modalTitle = $("modalTitle");
const modalHeader = overlay?.querySelector(".modal-header");
const modalCloseBtn = $("modalCloseBtn");

function openModal(msg, type = "info"){
  if (!overlay || !modalMsg) return;
  
  modalMsg.textContent = msg;
  
  // 타입에 따라 헤더 스타일 변경
  if (modalHeader) {
    modalHeader.classList.remove("success", "error");
    if (type === "success") {
      modalHeader.classList.add("success");
      if (modalTitle) modalTitle.textContent = "완료";
    } else if (type === "error") {
      modalHeader.classList.add("error");
      if (modalTitle) modalTitle.textContent = "오류";
    } else {
      if (modalTitle) modalTitle.textContent = "알림";
    }
  }
  
  overlay.style.display = "flex";
  overlay.style.alignItems = "center";
  overlay.style.justifyContent = "center";
  overlay.classList.add("active");
}
function closeModal(){
  if (overlay) {
    overlay.style.display = "none";
    overlay.classList.remove("active");
  }
}
if (modalCloseBtn) modalCloseBtn.addEventListener("click", closeModal);
if (overlay) overlay.addEventListener("click", (e)=>{ if(e.target===overlay) closeModal(); });

function toFormBody(formEl){
  const fd = new FormData(formEl);
  // application/x-www-form-urlencoded로 전송
  return new URLSearchParams(fd);
}

function trimText(v){ return (v||"").trim(); }
function digitsOnly(v){ return (v||"").replace(/\D/g,""); }

// 전화번호 010-1234-5678 포맷
function formatPhone(raw) {
  const d = digitsOnly(raw).slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 7) return `${d.slice(0,3)}-${d.slice(3)}`;
  return `${d.slice(0,3)}-${d.slice(3,7)}-${d.slice(7)}`;
}

// 생년월일 YYMMDD 포맷 (슬래시 제거)
function formatIdnum(raw) {
  return digitsOnly(raw).slice(0, 6);
}

// ===== 아이디 찾기 =====
const findIdForm = $("findIdForm");
if (findIdForm){
  // 전화번호 자동 포맷팅
  const fiPhone = $("fi-memberPhone");
  if (fiPhone) {
    fiPhone.addEventListener("input", (e) => {
      const before = e.target.value;
      const after = formatPhone(before);
      e.target.value = after;
    });
  }

  // 생년월일 자동 포맷팅 (숫자만, 최대 6자리)
  const fiIdnum = $("fi-memberIdnum");
  if (fiIdnum) {
    fiIdnum.addEventListener("input", (e) => {
      e.target.value = formatIdnum(e.target.value);
    });
  }

  findIdForm.addEventListener("submit", async (e)=>{
    e.preventDefault();

    // 간단 검증
    const phone = trimText($("fi-memberPhone").value);
    const idnum = trimText($("fi-memberIdnum").value);
    if (!phone){ $("err-fi-phone").textContent = "전화번호를 입력해주세요."; return; }
    else $("err-fi-phone").textContent = "";

    if (!idnum){ $("err-fi-idnum").textContent = "생년월일(YYMMDD)을 입력해주세요."; return; }
    else $("err-fi-idnum").textContent = "";

    try{
      const body = toFormBody(findIdForm); // memberPhone, memberIdnum 그대로
      const res = await fetch(findIdForm.getAttribute("action"), {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body
      });

      const text = (await res.text()).trim(); // "NOT_FOUND" 또는 "123/username" 또는 "username"
      console.log("[findId] response:", text);

      if (!res.ok){
        openModal("조회 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.", "error");
        return;
      }

      if (text === "NOT_FOUND"){
        openModal("일치하는 계정을 찾을 수 없습니다.\n전화번호와 생년월일(YYMMDD)을 다시 확인해주세요.", "error");
        return;
      }

      // 레거시 대응: "123/username" 혹은 그냥 "username"
      let userId = text;
      if (text.includes("/")){
        const parts = text.split("/");
        userId = parts[1] || parts[0];
      }
      openModal("찾으신 아이디는\n" + userId + " 입니다.", "success");

    }catch(err){
      console.error(err);
      openModal("조회 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.", "error");
    }
  });
}

// ===== 비밀번호 재설정 =====
const resetPwForm = $("resetPwForm");
if (resetPwForm){
  // 전화번호 자동 포맷팅
  const fpPhone = $("fp-memberPhone");
  if (fpPhone) {
    fpPhone.addEventListener("input", (e) => {
      const before = e.target.value;
      const after = formatPhone(before);
      e.target.value = after;
    });
  }

  // 생년월일 자동 포맷팅 (숫자만, 최대 6자리)
  const fpIdnum = $("fp-memberIdnum");
  if (fpIdnum) {
    fpIdnum.addEventListener("input", (e) => {
      e.target.value = formatIdnum(e.target.value);
    });
  }

  resetPwForm.addEventListener("submit", async (e)=>{
    e.preventDefault();

    // 간단 검증
    const memberId = trimText($("fp-memberId").value);
    const phone = trimText($("fp-memberPhone").value);
    const idnum = trimText($("fp-memberIdnum").value);
    const newPw = trimText($("fp-newPassword").value);
    const cfPw  = trimText($("fp-confirmPassword").value);

    if (!memberId){
      $("err-fp-id").textContent = "아이디를 입력해주세요.";
      return;
    } else $("err-fp-id").textContent = "";

    if (!phone){
      $("err-fp-phone").textContent = "전화번호를 입력해주세요.";
      return;
    } else $("err-fp-phone").textContent = "";

    // 전화번호 숫자만 추출하여 11자리인지 확인
    const phoneDigits = digitsOnly(phone);
    if (phoneDigits.length !== 11){
      $("err-fp-phone").textContent = "전화번호를 올바르게 입력해주세요. (예: 010-1234-5678)";
      return;
    } else $("err-fp-phone").textContent = "";

    if (!idnum){
      $("err-fp-idnum").textContent = "생년월일(YYMMDD)을 입력해주세요.";
      return;
    } else $("err-fp-idnum").textContent = "";

    // 생년월일 숫자만 추출하여 6자리인지 확인
    const idnumDigits = digitsOnly(idnum);
    if (idnumDigits.length !== 6){
      $("err-fp-idnum").textContent = "생년월일을 올바르게 입력해주세요. (YYMMDD)";
      return;
    } else $("err-fp-idnum").textContent = "";

    if (newPw.length < 3){
      $("err-fp-new").textContent = "비밀번호는 3자 이상이어야 합니다.";
      return;
    } else $("err-fp-new").textContent = "";

    if (newPw !== cfPw){
      $("err-fp-confirm").textContent = "비밀번호가 일치하지 않습니다.";
      return;
    } else $("err-fp-confirm").textContent = "";

    try{
      const body = toFormBody(resetPwForm); // memberId, memberPhone, memberIdnum, newPassword, confirmPassword
      const res = await fetch(resetPwForm.getAttribute("action"), {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body
      });

      const text = (await res.text()).trim(); // "OK" | "FAIL" | "MISMATCH"
      console.log("[resetPw] response:", text);

      if (!res.ok){
        openModal("처리 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.", "error");
        return;
      }

      if (text === "OK"){
        openModal("비밀번호가 재설정되었습니다.\n새 비밀번호로 로그인 해주세요.", "success");
        // 성공 시 입력값 초기화
        resetPwForm.reset();
        return;
      }
      if (text === "MISMATCH"){
        openModal("새 비밀번호와 확인이 일치하지 않습니다.", "error");
        return;
      }
      // FAIL
      openModal("본인 확인에 실패했습니다.\n아이디/전화번호/생년월일(YYMMDD)을 다시 확인해주세요.", "error");

    }catch(err){
      console.error(err);
      openModal("처리 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.", "error");
    }
  });
}
