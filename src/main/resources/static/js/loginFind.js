// 템플 디버그용 로그
console.log("[loginFind.js] loaded");

function $(id){ return document.getElementById(id); }

const overlay = $("modalOverlay");
const modalMsg = $("modalMessage");
const modalCloseBtn = $("modalCloseBtn");

function openModal(msg){
  modalMsg.textContent = msg;
  overlay.style.display = "block";
}
function closeModal(){
  overlay.style.display = "none";
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

// ===== 아이디 찾기 =====
const findIdForm = $("findIdForm");
if (findIdForm){
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
        openModal("조회 중 오류가 발생했습니다. (status " + res.status + ")");
        return;
      }

      if (text === "NOT_FOUND"){
        openModal("일치하는 계정을 찾을 수 없습니다.\n전화번호와 생년월일(YYMMDD)을 다시 확인해주세요.");
        return;
      }

      // 레거시 대응: "123/username" 혹은 그냥 "username"
      let userId = text;
      if (text.includes("/")){
        const parts = text.split("/");
        userId = parts[1] || parts[0];
      }
      openModal("찾으신 아이디는\n" + userId + " 입니다.");

    }catch(err){
      console.error(err);
      openModal("조회 중 오류가 발생했습니다.");
    }
  });
}

// ===== 비밀번호 재설정 =====
const resetPwForm = $("resetPwForm");
if (resetPwForm){
  resetPwForm.addEventListener("submit", async (e)=>{
    e.preventDefault();

    const newPw = trimText($("fp-newPassword").value);
    const cfPw  = trimText($("fp-confirmPassword").value);

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
        openModal("처리 중 오류가 발생했습니다. (status " + res.status + ")");
        return;
      }

      if (text === "OK"){
        openModal("비밀번호가 재설정되었습니다.\n새 비밀번호로 로그인 해주세요.");
        // 성공 시 입력값 초기화
        resetPwForm.reset();
        return;
      }
      if (text === "MISMATCH"){
        openModal("새 비밀번호와 확인이 일치하지 않습니다.");
        return;
      }
      // FAIL
      openModal("본인 확인에 실패했습니다.\n아이디/전화번호/생년월일(YYMMDD)을 다시 확인해주세요.");

    }catch(err){
      console.error(err);
      openModal("처리 중 오류가 발생했습니다.");
    }
  });
}
