// 유틸
const $ = (sel) => document.querySelector(sel);
const digits = (s) => (s || "").replace(/\D/g, "");

const idInput = $("#memberId");
const dupBtn = $("#dupBtn");
const idHelp = $("#idHelp");

const pass1 = $("#memberPass");
const pass2 = $("#memberPass2");
const passHelp = $("#passHelp");

const phone = $("#memberPhone");
const idnum = $("#memberIdnum");

const i1 = $("#interestIdx1");
const i2 = $("#interestIdx2");
const i3 = $("#interestIdx3");
const interestHelp = $("#interestHelp");

const agreeHidden = $("#memberAgree");
const privacyBtn = $("#privacyBtn");
const privacyHelp = $("#privacyHelp");

const form = $("#gJoinForm");
const submitHelp = $("#submitHelp");

let dupChecked = false; // 현재 입력된 아이디 기준 중복검사 통과 여부

// 아이디 입력이 바뀌면 중복검사 상태 초기화
idInput.addEventListener("input", () => {
  dupChecked = false;
  idHelp.textContent = "";
  dupBtn.classList.remove("complete-btn");
  dupBtn.textContent = "중복확인";
});

// 아이디 중복확인
dupBtn.addEventListener("click", async () => {
  const val = idInput.value.trim();
  idHelp.className = "help";

  if (!val) {
    idHelp.textContent = "아이디를 입력해주세요.";
    idHelp.classList.add("error");
    return;
  }

  try {
    const res = await fetch(`/member/api/checkId?memberId=${encodeURIComponent(val)}`, {
      method: "GET",
      credentials: "same-origin"
    });
    const text = await res.text();

    if (text === "OK") {
      dupChecked = true;
      idHelp.textContent = "사용 가능한 아이디입니다.";
      idHelp.classList.add("success");
      dupBtn.classList.add("complete-btn");
      dupBtn.textContent = "중복확인 완료";
    } else {
      dupChecked = false;
      idHelp.textContent = "이미 사용 중인 아이디입니다.";
      idHelp.classList.add("error");
      dupBtn.classList.remove("complete-btn");
      dupBtn.textContent = "중복확인";
    }
  } catch (e) {
    dupChecked = false;
    idHelp.textContent = "중복 확인 중 오류가 발생했습니다.";
    idHelp.classList.add("error");
  }
});

// 비밀번호 일치 즉시 표시
function updatePassMatch() {
  passHelp.className = "help";
  if (!pass1.value || !pass2.value) {
    passHelp.textContent = "";
    return;
  }
  if (pass1.value === pass2.value) {
    passHelp.textContent = "비밀번호가 일치합니다.";
    passHelp.classList.add("success");
  } else {
    passHelp.textContent = "비밀번호가 일치하지 않습니다.";
    passHelp.classList.add("error");
  }
}
pass1.addEventListener("input", updatePassMatch);
pass2.addEventListener("input", updatePassMatch);

// 관심분야 3개 중복 방지 체크
function distinctInterestsOK() {
  interestHelp.className = "help";
  const v1 = i1.value, v2 = i2.value, v3 = i3.value;
  if (!v1 || !v2 || !v3) return false;
  const set = new Set([v1, v2, v3]);
  const ok = set.size === 3;
  if (!ok) {
    interestHelp.textContent = "관심 분야는 서로 다른 항목으로 선택해주세요.";
    interestHelp.classList.add("error");
  } else {
    interestHelp.textContent = "";
  }
  return ok;
}
[i1, i2, i3].forEach(el => el.addEventListener("change", distinctInterestsOK));

// 개인정보 동의 토글
privacyBtn.addEventListener("click", () => {
  const on = agreeHidden.value === "Y";
  if (on) {
    agreeHidden.value = "N";
    privacyBtn.classList.remove("complete-btn");
    privacyBtn.textContent = "개인 정보 수신 동의";
    privacyHelp.textContent = "동의가 해제되었습니다.";
    privacyHelp.className = "help";
  } else {
    agreeHidden.value = "Y";
    privacyBtn.classList.add("complete-btn");
    privacyBtn.textContent = "개인정보 동의 완료";
    privacyHelp.textContent = "";
  }
});

// 폼 제출(fetch + FormData). 서버 리다이렉트 따라가기
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  submitHelp.className = "help";
  submitHelp.textContent = "";

  // 최소 검증
  if (!dupChecked) {
    idHelp.textContent = "아이디 중복확인을 해주세요.";
    idHelp.className = "help error";
    return;
  }
  if (pass1.value !== pass2.value) {
    passHelp.textContent = "비밀번호가 일치하지 않습니다.";
    passHelp.className = "help error";
    return;
  }
  if (!distinctInterestsOK()) {
    return;
  }
  if (agreeHidden.value !== "Y") {
    privacyHelp.textContent = "개인정보 수신 동의가 필요합니다.";
    privacyHelp.className = "help error";
    return;
  }

  // FormData 준비 (전화/생년월일은 숫자만)
  const fd = new FormData(form);
  fd.set("memberPhone", digits(fd.get("memberPhone")));
  // date 값(YYYY-MM-DD)을 숫자로(YYYYMMDD). 서버도 digits()하니 안전차원에서 처리
  fd.set("memberIdnum", digits(fd.get("memberIdnum")));

  try {
    const res = await fetch(form.action, {
      method: "POST",
      body: fd,
      credentials: "same-origin"
    });

    if (res.redirected) {
      // 성공 시 컨트롤러가 /member/login?joined 로 리다이렉트
      window.location.href = res.url;
      return;
    }
    // 리다이렉트가 없으면(예외적으로) 응답 텍스트 표기
    const text = await res.text();
    submitHelp.textContent = text || "처리 중 문제가 발생했습니다.";
    submitHelp.classList.add("error");
  } catch (err) {
    submitHelp.textContent = "네트워크 오류가 발생했습니다.";
    submitHelp.classList.add("error");
  }
});
