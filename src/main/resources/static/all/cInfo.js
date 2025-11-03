(function(){
  const cmt=document.querySelector('.cmt');
  const body=document.getElementById('cBody');
  const text=document.getElementById('cText');
  const btn=document.getElementById('cBtn');
  const toggle=document.getElementById('cToggle');
  const nick=(document.body.dataset.nickname||'방문자').trim();

  function escapeHTML(s){return s.replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));}
  function ymd(d){const p=n=>String(n).padStart(2,'0');return d.getFullYear()+'-'+p(d.getMonth()+1)+'-'+p(d.getDate());}
  function openBox(){if(!cmt.classList.contains('open')){cmt.classList.add('open');toggle.textContent='닫기';setTimeout(()=>text.focus(),0);}}
  function closeBox(){if(cmt.classList.contains('open')){cmt.classList.remove('open');toggle.textContent='댓글 적기';}}
  function submit(){const t=text.value.trim();if(!t){text.focus();return;}const tr=document.createElement('tr');const td1=document.createElement('td');td1.textContent=nick;const td2=document.createElement('td');td2.innerHTML=escapeHTML(t).replace(/\n/g,'<br>');const td3=document.createElement('td');td3.textContent=ymd(new Date());tr.append(td1,td2,td3);body.insertBefore(tr,body.firstChild);text.value='';text.focus();}
  toggle.addEventListener('click',()=>{if(cmt.classList.contains('open'))closeBox();else openBox();});
  btn.addEventListener('click',submit);
  text.addEventListener('keydown',e=>{if(e.ctrlKey&&e.key==='Enter')submit();});
})();