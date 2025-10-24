// 탭 활성 토글
document.querySelectorAll('.tabs .tab').forEach(tab=>{
  tab.addEventListener('click',()=>{
    if(tab.classList.contains('divider')) return;
    document.querySelectorAll('.tabs .tab').forEach(t=>t.classList.remove('active'));
    tab.classList.add('active');
  });
});