(() => {
  const $ = (sel, el = document) => el.querySelector(sel);

  const digitsOnly = (value) => (value || "").replace(/\D/g, "");

  const msg = $("#msgArea");
  const showMsg = (text, ok = false) => {
    if (!msg) return;

    msg.textContent = text;
    msg.classList.toggle("ok", ok);

    msg.style.display = "block";
    msg.style.textAlign = "center";
    msg.style.margin = "1.5rem auto";

    msg.scrollIntoView({
      behavior: "smooth",
      block: "center"
    });
  };

  const clearMsg = () => {
    if (msg) {
      msg.style.display = "none";
      msg.textContent = "";
      msg.classList.remove("ok");
    }
  };

  async function postForm(form) {
    const action = form.getAttribute("action");
    const fd = new FormData(form);

    const res = await fetch(action, {
      method: "POST",
      body: fd,
      redirect: "manual"
    });

    console.log("응답 상태:", res.status, "타입:", res.type, "redirected:", res.redirected);

    if (res.type === "opaqueredirect" || res.status === 0 || res.redirected) {
      console.log("리다이렉트 감지됨");
      return "REDIRECT";
    }

    const text = (await res.text()).trim();
    console.log("응답 텍스트:", text);
    return text || "OK";
  }

  const threeDistinct = (a, b, c) => {
    const vals = [a, b, c];
    if (vals.some(v => !v)) return false;
    return new Set(vals).size === 3;
  };

  // ===== 프로필 저장 =====
  const formProfile = $("#formProfile");
  if (formProfile) {
    formProfile.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearMsg();

      const i1 = $("#interestIdx1").value;
      const i2 = $("#interestIdx2").value;
      const i3 = $("#interestIdx3").value;

      if (!threeDistinct(i1, i2, i3)) {
        showMsg("관심분야 3개를 서로 다르게 선택해 주세요.");
        return;
      }

      try {
        const result = await postForm(formProfile);
        console.log("프로필 수정 결과:", result);

        if (result === "OK") {
          showMsg("프로필이 수정되었습니다.", true);
          setTimeout(() => {
            // 관리자가 수정한 경우 수정된 회원의 마이페이지로, 일반 회원이 수정한 경우 자신의 마이페이지로
            // URL 파라미터 또는 폼의 hidden input에서 memberIdx 가져오기
            const memberIdxFromUrl = new URLSearchParams(window.location.search).get("memberIdx");
            const memberIdxFromForm = formProfile.querySelector('input[name="memberIdx"]')?.value;
            const memberIdxParam = memberIdxFromUrl || memberIdxFromForm;
            console.log("memberIdxParam (URL):", memberIdxFromUrl);
            console.log("memberIdxParam (Form):", memberIdxFromForm);
            console.log("memberIdxParam (최종):", memberIdxParam);
            if (memberIdxParam) {
              const redirectUrl = "/member/mypage?memberIdx=" + memberIdxParam;
              console.log("리다이렉트 URL:", redirectUrl);
              location.href = redirectUrl;
            } else {
              console.log("일반 회원 - 마이페이지로 이동");
              location.href = "/member/mypage";
            }
          }, 1500);
        } else if (result === "REDIRECT") {
          console.log("REDIRECT 감지 - 프로필 수정");
          // 리다이렉트가 감지되면 관리자가 수정한 경우 회원 마이페이지로, 아니면 마이페이지로
          const memberIdxFromUrl = new URLSearchParams(window.location.search).get("memberIdx");
          const memberIdxFromForm = formProfile.querySelector('input[name="memberIdx"]')?.value;
          const memberIdxParam = memberIdxFromUrl || memberIdxFromForm;
          if (memberIdxParam) {
            location.href = "/member/mypage?memberIdx=" + memberIdxParam;
          } else {
            location.href = "/member/mypage";
          }
        } else {
          showMsg(result);
        }
      } catch (err) {
        console.error(err);
        showMsg("서버 통신 중 오류가 발생했습니다.");
      }
    });
  }

  // ===== 비밀번호 변경 (아이디/전화번호/생년월일 체크) =====
  const formPw = $("#formPw");
  if (formPw) {
    formPw.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearMsg();

      const id  = ($("#pw_memberId")?.value || "").trim();
      const phone = digitsOnly($("#pw_phone").value);
      const idnum = digitsOnly($("#pw_idnum").value);
      const pw1 = $("#newPassword").value;
      const pw2 = $("#confirmPassword").value;

      if (!id) {
        showMsg("아이디를 확인할 수 없습니다.");
        return;
      }
      if (phone.length < 10) {
        showMsg("전화번호를 정확히 입력해 주세요.");
        return;
      }
      if (idnum.length !== 6) {
        showMsg("생년월일(6자리)을 정확히 입력해 주세요.");
        return;
      }
      if (!pw1 || !pw2) {
        showMsg("새 비밀번호를 입력해 주세요.");
        return;
      }
      if (pw1.length < 3) {
        showMsg("비밀번호는 3자 이상이어야 합니다.");
        return;
      }
      if (pw1 !== pw2) {
        showMsg("비밀번호 확인이 일치하지 않습니다.");
        return;
      }

      try {
        const result = await postForm(formPw);

        if (result === "OK") {
          showMsg("비밀번호가 변경되었습니다.", true);
          formPw.reset();
          // 아이디 필드는 세션 값이니 다시 채워줌
          const meId = id;
          const idInput = $("#pw_memberId");
          if (idInput) idInput.value = meId;
        } else if (result === "MISMATCH") {
          showMsg("비밀번호 확인이 일치하지 않습니다.");
        } else if (result === "FAIL") {
          showMsg("아이디/전화번호/생년월일이 일치하지 않습니다.");
        } else if (result === "REDIRECT") {
          location.reload();
        } else {
          showMsg(result);
        }
      } catch (err) {
        console.error(err);
        showMsg("서버 통신 중 오류가 발생했습니다.");
      }
    });
  }

  // ===== 회원탈퇴 =====
  const agreeDelete = $("#agreeDelete");
  const btnDelete = $("#btnDelete");
  const formDelete = $("#formDelete");

  if (agreeDelete && btnDelete) {
    const syncDeleteBtn = () => {
      btnDelete.disabled = !agreeDelete.checked;
    };
    agreeDelete.addEventListener("change", syncDeleteBtn);
    syncDeleteBtn();
  }

  if (formDelete) {
    formDelete.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearMsg();

      if (!agreeDelete || !agreeDelete.checked) {
        showMsg("탈퇴 안내에 동의해야 진행할 수 있습니다.");
        return;
      }

      const phone = digitsOnly($("#del_phone").value);
      const idnum = digitsOnly($("#del_idnum").value);

      if (phone.length < 10) {
        showMsg("전화번호를 정확히 입력해 주세요.");
        return;
      }
      if (idnum.length !== 6) {
        showMsg("생년월일(6자리)을 정확히 입력해 주세요.");
        return;
      }

      try {
        const result = await postForm(formDelete);

        if (result === "OK") {
          showMsg("회원탈퇴가 완료되었습니다.", true);
          setTimeout(() => {
            location.href = "/member/login?deactivated=true";
          }, 1500);
        } else if (result === "FAIL") {
          showMsg("전화번호/생년월일이 일치하지 않습니다.");
        } else if (result === "REDIRECT") {
          location.reload();
        } else {
          showMsg(result);
        }
      } catch (err) {
        console.error(err);
        showMsg("서버 통신 중 오류가 발생했습니다.");
      }
    });
  }
})();
