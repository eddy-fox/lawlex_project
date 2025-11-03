const addBtn = document.getElementById('addTime');
const list = document.getElementById('selectedList');
if (addBtn) {
  addBtn.addEventListener('click', () => {
    const checked = [...document.querySelectorAll('.days input:checked')];
    const days = checked.map(d => d.value);
    const start = document.getElementById('timeStart').value;
    const end = document.getElementById('timeEnd').value;
    if (days.length === 0 || !start || !end) {
      alert('요일과 시간을 모두 선택해주세요.');
      return; }
    const tag = document.createElement('div');
    tag.className = 'tag';
    tag.textContent = `${days.join(', ')} ${start}~${end}`;
    const del = document.createElement('button');
    del.textContent = '×';
    del.onclick = () => tag.remove();
    tag.appendChild(del);
    list.appendChild(tag);
    checked.forEach(c => { c.checked = false; });
    document.getElementById('timeStart').value = '';
    document.getElementById('timeEnd').value = '';
  }); }

function makeDone(btn, baseText){
  btn.textContent = baseText + ' 완료';
  btn.classList.add('complete-btn');} 

document.getElementById('dupBtn').addEventListener('click', ()=>{ makeDone(dupBtn,'중복확인'); });
document.getElementById('verifyBtn').addEventListener('click', ()=>{ makeDone(verifyBtn,'본인인증'); });
document.getElementById('qualifyBtn').addEventListener('click', ()=>{ makeDone(qualifyBtn,'자격확인'); });
document.getElementById('privacyBtn').addEventListener('click', ()=>{ makeDone(privacyBtn,'개인정보 동의'); });