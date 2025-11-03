// 탭
const tabUser=document.getElementById('tab-user');
const tabLawyer=document.getElementById('tab-lawyer');
const secUser=document.getElementById('sec-user');
const secLawyer=document.getElementById('sec-lawyer');
tabUser.onclick=()=>{tabUser.classList.add('active');tabLawyer.classList.remove('active');secUser.style.display='block';secLawyer.style.display='none';};
tabLawyer.onclick=()=>{tabLawyer.classList.add('active');tabUser.classList.remove('active');secLawyer.style.display='block';secUser.style.display='none';};

// 검색
const field=document.getElementById('field');
const query=document.getElementById('query');
const btn=document.getElementById('btnSearch');
btn.onclick=()=>{
  const active=tabUser.classList.contains('active')?'user':'lawyer';
  const map={
    user:{tbody:document.getElementById('tbody-user'),id:1,no:0,name:2,nick:3,birth:4},
    lawyer:{tbody:document.getElementById('tbody-lawyer'),id:1,no:0,name:2,birth:4,nick:2}
  };
  const info=map[active];const col=info[field.value]??0;
  const key=query.value.trim().toLowerCase();
  info.tbody.querySelectorAll('tr').forEach(tr=>{
    const text=(tr.children[col]?.textContent||'').toLowerCase();
    tr.style.display=(!key||text.includes(key))?'':'none';
  });
};
query.addEventListener('keydown',e=>{if(e.key==='Enter')btn.click();});

// 행 전체 클릭 이동 (a로 tr 감싸지 않음)
function wireRowNav(tbody, base){
  tbody.querySelectorAll('tr[role="link"]').forEach(tr=>{
    const id = tr.dataset.id;
    tr.addEventListener('click',()=>{ location.href = `${base}${encodeURIComponent(id)}`; });
    tr.addEventListener('keydown',e=>{ if(e.key==='Enter' || e.key===' '){ e.preventDefault(); tr.click(); } });
  });
}
wireRowNav(document.getElementById('tbody-user'), '/member/mypage.html?id=');
wireRowNav(document.getElementById('tbody-lawyer'), '/lawyer/profile.html?id=');