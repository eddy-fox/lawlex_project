const form = document.getElementById('qnaForm');
const titleInput = document.getElementById('titleInput');
const contentTextarea = document.getElementById('contentTextarea');
const secretCheck = document.getElementById('secretCheck');
const submitBtn = document.getElementById('submitBtn');
const cancelBtn = document.getElementById('cancelBtn');

function updateSubmitState(){
  const ok = titleInput.value.trim().length > 0 &&
             contentTextarea.value.trim().length > 0;
  submitBtn.disabled = !ok;
}

titleInput.addEventListener('input', updateSubmitState);
contentTextarea.addEventListener('input', updateSubmitState);

form.addEventListener('submit', (e)=>{
  updateSubmitState();
  if (submitBtn.disabled) {
    e.preventDefault();
    return;
  }

  const warning =
    '작성하신 문의는 본인이 삭제할 수 없습니다.\n관리자에 의해 삭제될 수 있습니다.\n\n등록을 계속하시겠습니까?';

  if (!confirm(warning)) {
    e.preventDefault();
    return;
  }

  // 중복 제출 방지(연타/더블클릭 대비)
  submitBtn.disabled = true;
  submitBtn.textContent = '등록 중...';
  // 기본 제출 진행(추가 submit() 호출 불필요)
});

cancelBtn.addEventListener('click', ()=>{
  if (confirm('작성을 취소하시겠습니까?')) {
    history.back();
  }
});

updateSubmitState();
