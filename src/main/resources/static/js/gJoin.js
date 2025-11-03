function makeDone(btn, baseText){
  btn.textContent = baseText + ' 완료';
  btn.classList.add('complete-btn'); }
document.getElementById('dupBtn').addEventListener('click', ()=>{ makeDone(dupBtn,'중복확인'); });
document.getElementById('verifyBtn').addEventListener('click', ()=>{ makeDone(verifyBtn,'본인인증'); });
document.getElementById('privacyBtn').addEventListener('click', ()=>{ makeDone(privacyBtn,'개인정보 동의'); });