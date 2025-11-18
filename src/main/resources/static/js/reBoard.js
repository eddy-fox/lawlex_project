
const delete_elements = document.getElementsByClassName("delete");
  Array.from(delete_elements).forEach(function(element) {
    element.addEventListener('click', function() {
      if(confirm("정말로 삭제하시겠습니까?")) {
        location.href = this.dataset.uri;
      };
    });
  });

  const recommend_elements = document.getElementsByClassName("recommend");
  Array.from(recommend_elements).forEach(function(element) {
    element.addEventListener('click', function() {
      if(confirm("정말로 추천하시겠습니까?")) {
        location.href = this.dataset.uri;
      };
    });
  });

  // 답변 달기 버튼 토글 기능
  (function() {
    // DOM이 완전히 로드된 후 실행
    function initAnswerToggle() {
      const answerToggleBtn = document.getElementById('answerToggleBtn');
      const answerFormWrapper = document.getElementById('answerFormWrapper');
      const answerCancelBtn = document.getElementById('answerCancelBtn');
      const answerTextarea = document.querySelector('.answer-textarea');

      if (answerToggleBtn && answerFormWrapper) {
        answerToggleBtn.addEventListener('click', function(e) {
          e.preventDefault();
          const currentDisplay = window.getComputedStyle(answerFormWrapper).display;
          if (currentDisplay === 'none' || answerFormWrapper.style.display === 'none') {
            answerFormWrapper.style.display = 'flex';
            if (answerTextarea) {
              setTimeout(() => answerTextarea.focus(), 100);
            }
          } else {
            answerFormWrapper.style.display = 'none';
          }
        });

        if (answerCancelBtn) {
          answerCancelBtn.addEventListener('click', function(e) {
            e.preventDefault();
            answerFormWrapper.style.display = 'none';
            if (answerTextarea) {
              answerTextarea.value = '';
            }
          });
        }
      }
    }

    // DOM 로드 완료 후 실행
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', initAnswerToggle);
    } else {
      initAnswerToggle();
    }
  })();
  
  (function(){
    const openBtn = document.getElementById('open');
    const replyArea = document.getElementById('replyArea');
    const submitBtn = document.getElementById('submit');
    const cancelBtn = document.getElementById('cancel');
    const cnt = document.getElementById('count');
    const min = document.getElementById('min');
    const ok = document.getElementById('ok');
    const ta = document.getElementById('ta');
    const statusText = document.getElementById('statusText');
    const comments = document.getElementById('comments');
    const MIN = parseInt(min.textContent,10) || 10;

    const LAWYER_NAME = '변호사 변호사';

    function openEditor(){ replyArea.classList.remove('hidden'); statusText.textContent = '작성 중'; openBtn.classList.add('hidden'); ta.focus(); }
    function closeEditor(){ replyArea.classList.add('hidden'); statusText.textContent = '김현민 변호사님의 답변을 기다리고 있어요.'; openBtn.classList.remove('hidden'); ta.value = ''; updateCounter(); }
    function updateCounter(){ const len = ta.value.trim().length; cnt.textContent = len; const good = len >= MIN; submitBtn.disabled = !good; ok.classList.toggle('hidden', !good); }
    function formatNow(){ const d = new Date(); const z = n => String(n).padStart(2,'0'); return `${d.getFullYear()}-${z(d.getMonth()+1)}-${z(d.getDate())} ${z(d.getHours())}:${z(d.getMinutes())}`; }
    function buildComment(text){
      const lines = text.split(/\r?\n/).map(s=>s.trim()).filter(Boolean);
      const items = lines.length ? lines : [text.trim()];
      const el = document.createElement('article');
      el.className = 'cmt';
      el.innerHTML = `
        <div class="cmt-head">
          <div class="cmt-avatar"><img src="https://via.placeholder.com/72x96" alt="프로필"></div>
          <div class="chip">${LAWYER_NAME}</div>
          <div class="chip">${formatNow()}</div>
          <div class="chip">😊 도움됐어요 <strong>0</strong></div>
        </div>
        <div class="cmt-body">
          ${items.map(li=>`${li.replace(/^\d+\.\s*/, '')}`).join('')}
        </div>`;
      return el;    }

    openBtn.addEventListener('click', (e)=>{ e.preventDefault(); openEditor(); });
    cancelBtn.addEventListener('click', (e)=>{ e.preventDefault(); closeEditor(); });
    ta.addEventListener('input', updateCounter);
    ta.addEventListener('keydown', (e)=>{ if((e.ctrlKey||e.metaKey)&&e.key==='Enter'){ if(!submitBtn.disabled) submitBtn.click(); } if(e.key==='Escape'){ closeEditor(); } });
    submitBtn.addEventListener('click', (e)=>{ e.preventDefault(); const text = ta.value.trim(); if(!text) return; const c = buildComment(text); comments.prepend(c); closeEditor(); c.scrollIntoView({behavior:'smooth', block:'center'}); });

    updateCounter();  })();