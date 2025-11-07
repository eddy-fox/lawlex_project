  const form = document.getElementById('qnaForm');
  const titleInput = document.getElementById('titleInput');
  const contentTextarea = document.getElementById('contentTextarea');
  const secretCheck = document.getElementById('secretCheck');
  const submitBtn = document.getElementById('submitBtn');
  const cancelBtn = document.getElementById('cancelBtn');

  function updateSubmitState(){
    const ok = titleInput.value.trim() && contentTextarea.value.trim();
    submitBtn.disabled = !ok;
  }

  titleInput.addEventListener('input', updateSubmitState);
  contentTextarea.addEventListener('input', updateSubmitState);

  form.addEventListener('submit', (e)=>{
    e.preventDefault();
    updateSubmitState();
    if (submitBtn.disabled) return;

    const warning =
      '작성하신 문의는 본인이 삭제할 수 없습니다.\n관리자에 의해 삭제될 수 있습니다.\n\n등록을 계속하시겠습니까?';

    if (!confirm(warning)) return;

    const payload = {
      title: titleInput.value.trim(),
      content: contentTextarea.value.trim(),
      isSecret: secretCheck.checked
    };

    // TODO: 실제 전송 로직 연결
    // form.action = '/qna/write'; form.method = 'post'; form.submit();

    alert('문의가 등록되었습니다.');
  });

  cancelBtn.addEventListener('click', ()=>{
    if (confirm('작성을 취소하시겠습니까?')) {
      history.back();
    }
  });

  updateSubmitState();