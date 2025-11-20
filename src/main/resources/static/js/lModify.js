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

    if (res.type === "opaqueredirect" || res.status === 0 || res.redirected) {
      return "REDIRECT";
    }

    const text = (await res.text()).trim();
    return text || "OK";
  }

  // ===== 상담 가능 요일/시간 관리 =====
  // availability: [{weekdays:[0,2,4], start:"09:00", end:"12:00"}, ...]
  const availability = [];
  const dayCheckboxes = $("#dayCheckboxes");
  const timeStart = $("#timeStart");
  const timeEnd = $("#timeEnd");
  const addTimeBtn = $("#addTime");
  const selectedList = $("#selectedList");
  const calendarJsonInput = $("#calendarJson");

  const refreshCalendarJson = () => {
    if (calendarJsonInput) {
      calendarJsonInput.value = JSON.stringify(availability);
    }
  };

  // 기존 상담 가능 시간 불러오기 (calendarList: DTO 리스트)
  const loadExistingCalendar = () => {
    if (
      typeof window.calendarListData !== "undefined" &&
      window.calendarListData &&
      window.calendarListData.length > 0
    ) {
      const dayNames = ["월", "화", "수", "목", "금", "토", "일"];

      // 요일별로 그룹화
      const groupedByDay = {};
      window.calendarListData.forEach((cal) => {
        const day = cal.calendarWeekname;
        if (!groupedByDay[day]) {
          groupedByDay[day] = [];
        }
        groupedByDay[day].push({
          start:
            Math.floor(cal.calendarStartTime / 60) +
            ":" +
            String(cal.calendarStartTime % 60).padStart(2, "0"),
          end:
            Math.floor(cal.calendarEndTime / 60) +
            ":" +
            String(cal.calendarEndTime % 60).padStart(2, "0"),
        });
      });

      // 같은 시간대의 요일들을 그룹화
      const timeGroups = {};
      Object.keys(groupedByDay).forEach((day) => {
        groupedByDay[day].forEach((timeSlot) => {
          const key = timeSlot.start + "-" + timeSlot.end;
          if (!timeGroups[key]) {
            timeGroups[key] = {
              weekdays: [],
              start: timeSlot.start,
              end: timeSlot.end,
            };
          }
          timeGroups[key].weekdays.push(parseInt(day, 10));
        });
      });

      // availability 배열에 추가 + UI 표시
      Object.values(timeGroups).forEach((group) => {
        availability.push(group);
        refreshCalendarJson();

        if (selectedList) {
          const tag = document.createElement("span");
          tag.className = "tag-slot";

          const label = document.createElement("span");
          const dayLabels = group.weekdays
            .map((wd) => dayNames[wd])
            .join(", ");
          label.textContent = `${dayLabels} ${group.start}~${group.end}`;

          const removeBtn = document.createElement("button");
          removeBtn.className = "tag-remove";
          removeBtn.type = "button";
          removeBtn.innerHTML = "✕";
          removeBtn.addEventListener("click", () => {
            const idx = availability.indexOf(group);
            if (idx >= 0) availability.splice(idx, 1);
            tag.remove();
            refreshCalendarJson();
          });

          tag.append(label, removeBtn);
          selectedList.append(tag);
        }
      });
    }
  };

  // 페이지 로드 시 기존 데이터 불러오기
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", loadExistingCalendar);
  } else {
    loadExistingCalendar();
  }

  const addSlot = () => {
    if (!dayCheckboxes || !timeStart || !timeEnd) return;

    const checked = Array.from(
      dayCheckboxes.querySelectorAll('input[type="checkbox"]:checked')
    );
    const weekdays = checked.map((c) => parseInt(c.value, 10));
    const start = timeStart.value; // "HH:mm"
    const end = timeEnd.value;

    if (weekdays.length === 0 || !start || !end) {
      alert("요일과 시간을 모두 선택해주세요.");
      return;
    }

    const item = { weekdays, start, end };
    availability.push(item);
    refreshCalendarJson();

    const tag = document.createElement("span");
    tag.className = "tag-slot";
    const label = document.createElement("span");
    const dayNames = ["월", "화", "수", "목", "금", "토", "일"];
    const dayLabels = weekdays.map((wd) => dayNames[wd]).join(", ");
    label.textContent = `${dayLabels} ${start}~${end}`;

    const removeBtn = document.createElement("button");
    removeBtn.className = "tag-remove";
    removeBtn.type = "button";
    removeBtn.innerHTML = "✕";
    removeBtn.addEventListener("click", () => {
      const idx = availability.indexOf(item);
      if (idx >= 0) availability.splice(idx, 1);
      tag.remove();
      refreshCalendarJson();
    });

    tag.append(label, removeBtn);
    if (selectedList) selectedList.append(tag);

    checked.forEach((c) => {
      c.checked = false;
    });
    timeStart.value = "";
    timeEnd.value = "";
  };

  if (addTimeBtn) addTimeBtn.addEventListener("click", addSlot);

  // ===== 상담시간만 따로 /member/api/lawyer/calendar 로 저장 =====
  async function saveCalendarToServer() {
    // availability 배열 그대로 JSON으로 전송
    const url = "/member/api/lawyer/calendar";
    const csrfInput = document.querySelector('input[name="_csrf"]');

    const headers = {
      "Content-Type": "application/json",
    };
    if (csrfInput && csrfInput.value) {
      // 스프링 CSRF 헤더 (기본값: X-CSRF-TOKEN)
      headers["X-CSRF-TOKEN"] = csrfInput.value;
    }

    const res = await fetch(url, {
      method: "POST",
      headers,
      body: JSON.stringify(availability),
    });

    if (res.type === "opaqueredirect" || res.status === 0 || res.redirected) {
      // 로그인 만료 등으로 리다이렉트 되는 경우
      location.reload();
      return;
    }

    const data = await res.json().catch(() => ({}));

    if (!res.ok || !data.success) {
      const msg =
        (data && data.message) ||
        "상담 가능 시간을 저장하는 중 오류가 발생했습니다.";
      throw new Error(msg);
    }
  }

  // ===== 사진 미리보기 =====
  const photoInput = $("#photoInput");
  const photoPreview = $("#photoPreview");
  const photoRemoveBtn = $("#photoRemoveBtn");

  if (photoInput && photoPreview) {
    photoInput.addEventListener("change", function () {
      const file = this.files && this.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = function (e) {
        photoPreview.innerHTML =
          '<img src="' +
          e.target.result +
          '" style="width:100%; height:100%; object-fit:cover; display:block;" alt="미리보기">';
      };
      reader.readAsDataURL(file);
    });
  }

  if (photoRemoveBtn && photoInput && photoPreview) {
    photoRemoveBtn.addEventListener("click", function () {
      photoInput.value = "";
      photoPreview.innerHTML =
        '<span style="display:flex; align-items:center; justify-content:center; width:100%; height:100%;">사진</span>';
    });
  }

  // ===== 프로필 + 상담시간 저장 =====
  const formProfile = $("#formProfile");
  if (formProfile) {
    formProfile.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearMsg();

      // hidden에도 availability JSON 복사 (혹시 나중에 서버에서 참고할 수 있게)
      refreshCalendarJson();

      const interestIdx = $("#interestIdx")?.value;
      if (!interestIdx) {
        showMsg("전문분야를 선택해 주세요.");
        return;
      }

      try {
        // 1) 상담시간 먼저 저장
        await saveCalendarToServer();

        // 2) 프로필(닉네임/이메일/주소/전문분야/사진/한줄소개) 저장
        const result = await postForm(formProfile);

        if (result === "OK") {
          showMsg("프로필이 수정되었습니다.", true);
          setTimeout(() => {
            location.href = "/member/mypage";
          }, 1500);
        } else if (result === "REDIRECT") {
          location.reload();
        } else {
          showMsg(result);
        }
      } catch (err) {
        console.error(err);
        showMsg(err.message || "서버 통신 중 오류가 발생했습니다.");
      }
    });
  }

  // ===== 비밀번호 변경 (아이디/전화번호/생년월일 체크) =====
  const formPw = $("#formPw");
  if (formPw) {
    formPw.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearMsg();

      const id = ($("#pw_id")?.value || "").trim();
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
          const idInput = $("#pw_id");
          if (idInput) idInput.value = id;
        } else if (result === "MISMATCH" || result.includes("비밀번호 확인")) {
          showMsg("비밀번호 확인이 일치하지 않습니다.");
        } else if (result.includes("본인 확인") || result.includes("일치하지 않습니다")) {
          showMsg("아이디/전화번호/생년월일이 일치하지 않습니다.");
        } else if (result === "REDIRECT") {
          location.reload();
        } else {
          showMsg(result || "비밀번호 변경 중 오류가 발생했습니다.");
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
        } else if (result.includes("본인 확인") || result.includes("일치하지 않습니다")) {
          showMsg("전화번호/생년월일이 일치하지 않습니다.");
        } else if (result === "REDIRECT") {
          location.reload();
        } else {
          showMsg(result || "회원탈퇴 중 오류가 발생했습니다.");
        }
      } catch (err) {
        console.error(err);
        showMsg("서버 통신 중 오류가 발생했습니다.");
      }
    });
  }
})();
