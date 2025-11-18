(function(){
  const cmt=document.querySelector('.cmt');
  if(!cmt) return; // 댓글 섹션이 없으면 종료
  
  const body=document.getElementById('cBody');
  const text=document.getElementById('cText');
  const btn=document.getElementById('cBtn');
  
  // newsIdx 가져오기 (HTML에서 data 속성 또는 URL에서)
  const newsIdxFromData = document.body.dataset.newsidx || document.body.getAttribute('data-newsIdx');
  const newsIdxFromUrl = new URLSearchParams(window.location.search).get('newsIdx');
  const newsIdx = newsIdxFromData || newsIdxFromUrl;
  
  // 현재 로그인 사용자 정보 가져오기
  const loginMemberIdx = document.body.dataset.memberidx || document.body.getAttribute('data-memberIdx');
  const loginLawyerIdx = document.body.dataset.lawyeridx || document.body.getAttribute('data-lawyerIdx');
  const isAdmin = document.body.dataset.isadmin === 'true';
  const isLoggedIn = (loginMemberIdx && loginMemberIdx !== '') || (loginLawyerIdx && loginLawyerIdx !== '');
  
  if(!newsIdx) return;
  
  // 로그인하지 않은 경우 입력창 비활성화
  if(!isLoggedIn){
    text.disabled = true;
    btn.disabled = true;
  }
  
  function escapeHTML(s){return s.replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));}
  function ymd(d){
    if(typeof d === 'string') {
      return d.substring(0, 10); // "2025-01-01" 형식
    }
    const p=n=>String(n).padStart(2,'0');
    return d.getFullYear()+'-'+p(d.getMonth()+1)+'-'+p(d.getDate());
  }
  
  // 댓글 섹션은 항상 열려있음 (버튼 제거됨)
  
  // 댓글 목록 불러오기
  function loadComments(){
    fetch(`/comment/list/${newsIdx}`)
      .then(r=>r.json())
      .then(comments=>{
        body.innerHTML='';
        if(comments.length===0){
          const tr=document.createElement('tr');
          const td=document.createElement('td');
          td.setAttribute('colspan','3');
          td.textContent='댓글이 없습니다.';
          td.style.textAlign='center';
          td.style.padding='20px';
          tr.append(td);
          body.append(tr);
          return;
        }
        comments.forEach(c=>{
          const tr=document.createElement('tr');
          
          // 닉네임
          const td1=document.createElement('td');
          td1.textContent=c.nickname || '익명';
          
          // 내용
          const td2=document.createElement('td');
          td2.innerHTML=escapeHTML(c.commentContent).replace(/\n/g,'<br>');
          
          // 날짜와 삭제 버튼
          const td3=document.createElement('td');
          const dateSpan=document.createElement('span');
          dateSpan.textContent=ymd(c.commentRegDate);
          td3.appendChild(dateSpan);
          
          // 삭제 버튼 표시 조건: 작성자이거나 관리자
          const canDelete = isAdmin || 
                           (loginMemberIdx && c.memberIdx && parseInt(loginMemberIdx) === c.memberIdx) ||
                           (loginLawyerIdx && c.lawyerIdx && parseInt(loginLawyerIdx) === c.lawyerIdx);
          
          if(canDelete){
            const deleteBtn=document.createElement('button');
            deleteBtn.textContent='삭제';
            deleteBtn.className='btn-delete';
            deleteBtn.style.marginLeft='10px';
            deleteBtn.style.padding='4px 8px';
            deleteBtn.style.fontSize='12px';
            deleteBtn.style.border='1px solid #ccc';
            deleteBtn.style.borderRadius='4px';
            deleteBtn.style.background='#fff';
            deleteBtn.style.cursor='pointer';
            deleteBtn.onclick=()=>deleteComment(c.commentIdx);
            td3.appendChild(deleteBtn);
          }
          
          tr.append(td1,td2,td3);
          body.append(tr);
        });
      })
      .catch(err=>{
        console.error('댓글 로딩 실패:',err);
        body.innerHTML='<tr><td colspan="3" style="text-align:center;padding:20px;">댓글을 불러올 수 없습니다.</td></tr>';
      });
  }
  
  // 댓글 작성
  function submit(){
    // 로그인하지 않은 경우
    if(!isLoggedIn){
      alert('로그인 후 댓글을 남길 수 있습니다.');
      return;
    }
    
    const t=text.value.trim();
    if(!t){
      text.focus();
      alert('댓글 내용을 입력해주세요.');
      return;
    }
    
    fetch('/comment/create',{
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify({
        newsIdx:parseInt(newsIdx),
        commentContent:t
      })
    })
    .then(r=>r.json())
    .then(result=>{
      if(result.success){
        text.value='';
        loadComments(); // 댓글 목록 새로고침
        text.focus();
      }else{
        alert(result.message || '댓글 작성에 실패했습니다.');
        if(result.message && result.message.includes('로그인')){
          if(confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')){
            window.location.href='/member/login';
          }
        }
      }
    })
    .catch(err=>{
      console.error('댓글 작성 실패:',err);
      alert('댓글 작성에 실패했습니다.');
    });
  }
  
  btn.addEventListener('click',submit);
  
  text.addEventListener('keydown',e=>{
    if(e.ctrlKey&&e.key==='Enter') submit();
  });
  
  // 댓글 삭제
  function deleteComment(commentIdx){
    if(!confirm('정말 삭제하시겠습니까?')) return;
    
    fetch(`/comment/delete/${commentIdx}`,{
      method:'POST',
      headers:{'Content-Type':'application/json'}
    })
    .then(r=>r.json())
    .then(result=>{
      if(result.success){
        loadComments(); // 댓글 목록 새로고침
      }else{
        alert(result.message || '댓글 삭제에 실패했습니다.');
      }
    })
    .catch(err=>{
      console.error('댓글 삭제 실패:',err);
      alert('댓글 삭제에 실패했습니다.');
    });
  }
  
  // 페이지 로드 시 댓글 목록 불러오기
  loadComments();
})();
