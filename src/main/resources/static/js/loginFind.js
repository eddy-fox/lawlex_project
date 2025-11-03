    const overlay=document.getElementById('modalOverlay');
    const modalMessage=document.getElementById('modalMessage');
    const modalCloseBtn=document.getElementById('modalCloseBtn');

    function openModal(messageText){
      modalMessage.textContent=messageText;
      overlay.classList.add('active');
    }
    function closeModal(){ overlay.classList.remove('active'); }

    modalCloseBtn.addEventListener('click',closeModal);
    overlay.addEventListener('click',(e)=>{ if(e.target===overlay) closeModal(); });

    const formFindId=document.getElementById('formFindId');
    formFindId.addEventListener('submit',(e)=>{
      e.preventDefault();
      openModal('찾으시는 아이디는\nasdfasdf 입니다');
    });

    const formFindPw=document.getElementById('formFindPw');
    formFindPw.addEventListener('submit',(e)=>{
      e.preventDefault();
      const pw1=document.getElementById('newPw').value.trim();
      const pw2=document.getElementById('newPw2').value.trim();
      if(!pw1||!pw2){ openModal('비밀번호를 입력해주세요.'); return; }
      if(pw1!==pw2){ openModal('비밀번호가 일치하지 않습니다.'); return; }
      openModal('비밀번호가 재설정되었습니다.\n다시 로그인 해주세요.');
    });

    function toggleVerify(btn){
      if(btn.classList.contains('done')){
        btn.textContent='인증번호';
        btn.classList.remove('done');
      }else{
        btn.textContent='인증완료';
        btn.classList.add('done');
      }
    }
    document.getElementById('btnIdCode').addEventListener('click',function(){ toggleVerify(this); });
    document.getElementById('btnPwCode').addEventListener('click',function(){ toggleVerify(this); });