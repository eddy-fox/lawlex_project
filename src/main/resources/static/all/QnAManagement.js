    const tabNew=document.getElementById('tab-new'), tabDone=document.getElementById('tab-done');
    const newSec=document.getElementById('newSection'), doneSec=document.getElementById('doneSection');
    tabNew.onclick=()=>{tabNew.classList.add('active');tabDone.classList.remove('active');newSec.style.display='block';doneSec.style.display='none'};
    tabDone.onclick=()=>{tabDone.classList.add('active');tabNew.classList.remove('active');newSec.style.display='none';doneSec.style.display='block'};

    function toDetail(row){
      const qid=row.dataset.qid||'';
      const title=encodeURIComponent(row.dataset.title||row.cells[2].textContent.trim());
      const member=encodeURIComponent(row.dataset.member||row.cells[1].textContent.trim());
      const date=encodeURIComponent(row.dataset.date||row.cells[3].textContent.trim());
      const secret=encodeURIComponent(row.dataset.secret||row.cells[4].textContent.trim());
      location.href=`admin-qna-answer.html?qid=${qid}&title=${title}&member=${member}&date=${date}&secret=${secret}`;
    }
    document.querySelectorAll('.answer-btn').forEach(btn=>{
      btn.addEventListener('click',e=>toDetail(e.target.closest('tr')));
    });
    function wireRowNav(tbody){
      tbody.querySelectorAll('tr[role="link"]').forEach(tr=>{
        tr.addEventListener('click',()=>toDetail(tr));
        tr.addEventListener('keydown',e=>{ if(e.key==='Enter'||e.key===' '){ e.preventDefault(); toDetail(tr); } });
      });
    }
    wireRowNav(document.getElementById('tbodyNew'));
    wireRowNav(document.getElementById('tbodyDone'));