// 히어로 슬라이드와 뉴스 탭 전환은 index.html의 인라인 스크립트에서 처리됨
// currentSlide와 nextSlide는 index.html에서 정의됨
// 이 파일은 setLoginState 같은 다른 기능만 처리

function setLoginState(isLoggedIn){
  document.querySelectorAll('.guest-only').forEach(el=>{el.style.display=isLoggedIn?'none':'';});
  document.querySelectorAll('.logged-only').forEach(el=>{el.style.display=isLoggedIn?'':'none';});
}
setLoginState(false);