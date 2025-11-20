// 히어로 슬라이드와 뉴스 탭 전환은 index.html의 인라인 스크립트에서 처리됨
// currentSlide와 nextSlide는 index.html에서 정의됨
// 이 파일은 setLoginState 같은 다른 기능만 처리

function setLoginState(isLoggedIn){
  document.querySelectorAll('.guest-only').forEach(el=>{el.style.display=isLoggedIn?'none':'';});
  document.querySelectorAll('.logged-only').forEach(el=>{el.style.display=isLoggedIn?'':'none';});
}
setLoginState(false);

// 변호사 카드 슬라이더
(function(){
  const slider = document.querySelector('.lawyer-slider');
  if(!slider) return;
  
  const group = slider.querySelector('.group');
  const prevBtn = slider.querySelector('.slider-prev');
  const nextBtn = slider.querySelector('.slider-next');
  
  if(!group || !prevBtn || !nextBtn) return;
  
  const cards = group.querySelectorAll('.card');
  const cardCount = cards.length;
  
  // 카드가 1개일 때 가운데 정렬
  if(cardCount === 1){
    group.style.justifyContent = 'center';
  }
  
  // 카드가 3개 이하이면 화살표 숨기기
  if(cardCount <= 3){
    prevBtn.style.display = 'none';
    nextBtn.style.display = 'none';
    return;
  }
  
  const cardWidth = 300;
  const gap = 16;
  const scrollAmount = cardWidth + gap;
  
  function updateButtons(){
    const scrollLeft = group.scrollLeft;
    const maxScroll = group.scrollWidth - group.clientWidth;
    
    prevBtn.disabled = scrollLeft <= 0;
    nextBtn.disabled = scrollLeft >= maxScroll - 1;
  }
  
  prevBtn.addEventListener('click',()=>{
    group.scrollBy({left:-scrollAmount,behavior:'smooth'});
  });
  
  nextBtn.addEventListener('click',()=>{
    group.scrollBy({left:scrollAmount,behavior:'smooth'});
  });
  
  group.addEventListener('scroll',updateButtons);
  updateButtons();
})();