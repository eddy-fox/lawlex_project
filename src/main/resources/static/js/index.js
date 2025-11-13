let currentSlide=0;
const slides=[
  {title:'바쁜 <span class="hl">일상</span> 속<br>지금 가장 필요한 답을',desc:'상담은 빠르게, 정보는 정확하게. 분야별 전문 변호사와 믿을 수 있는 법률 정보를 한 곳에서.',button1:'실시간 상담하기',button2:'바로 상담받기'},
  {title:'전문 <span class="hl">변호사</span>에게<br>지금 바로 상담받으세요',desc:'분야별 전문 변호사, 평균 응답 시간 수 분 이내. 말하기 어려운 내용도 비공개로 안전하게.',button1:'변호사 찾기',button2:'상담 신청'},
  {title:'지금 꼭 알아야 할<br><span class="hl">법률 정보</span>만 골랐어요',desc:'어려운 법률 용어 말고, 상황별로 정리된 실전 가이드만 보여드립니다.',button1:'법률 가이드 보기',button2:'상세 정보'}
];
function updateHeroSlide(){
  const t=document.querySelector('.ttl-big');
  const d=document.querySelector('.desc');
  const b=document.querySelectorAll('.actions .btn');
  t.innerHTML=slides[currentSlide].title;
  d.textContent=slides[currentSlide].desc;
  if(b[0]) b[0].textContent=slides[currentSlide].button1;
  if(b[2]) b[2].textContent=slides[currentSlide].button2;
}
// function prevSlide(){currentSlide=(currentSlide-1+slides.length)%slides.length;updateHeroSlide();}
// function nextSlide(){currentSlide=(currentSlide+1)%slides.length;updateHeroSlide();}
setInterval(nextSlide,5000);

document.addEventListener('click',e=>{
  const trg=e.target.closest('.tab');
  if(!trg) return;
  const tab=trg.dataset.tab;
  document.querySelectorAll('.tab').forEach(el=>el.classList.remove('active'));
  trg.classList.add('active');
  document.getElementById('news-popular').style.display=(tab==='popular')?'block':'none';
  document.getElementById('news-latest').style.display=(tab==='latest')?'block':'none';
});

function setLoginState(isLoggedIn){
  document.querySelectorAll('.guest-only').forEach(el=>{el.style.display=isLoggedIn?'none':'';});
  document.querySelectorAll('.logged-only').forEach(el=>{el.style.display=isLoggedIn?'':'none';});
}
setLoginState(false);