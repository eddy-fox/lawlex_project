function resetImage(){
  const thumb = document.getElementById('thumbBox');
  const url = document.getElementById('imgUrl');
  const file = document.getElementById('imgFile');
  thumb.style.backgroundImage = '';
  thumb.style.background = '#e9ecf3';
  thumb.textContent = '이미지';
  url.value='';
  file.value='';
  const fileInfo = document.getElementById('fileInfo');
  fileInfo.innerHTML = '<span class="small">선택된 파일:</span> <span id="fileName" class="file-pill">없음</span>';
}

function setFilePill(name){
  const fileInfo = document.getElementById('fileInfo');
  const pill = document.createElement('span');
  pill.className = 'file-pill';
  pill.innerHTML = name + ' <span class="x" role="button" aria-label="파일 지우기" title="파일 지우기">×</span>';
  pill.querySelector('.x').addEventListener('click', resetImage);
  const label = document.createElement('span');
  label.className = 'small';
  label.textContent = '선택된 파일:';
  fileInfo.innerHTML = '';
  fileInfo.appendChild(label);
  fileInfo.appendChild(pill);
}

document.getElementById('imgUrl')?.addEventListener('change', e=>{
  const url = e.target.value.trim();
  if(!url) return;
  const box = document.getElementById('thumbBox');
  box.style.backgroundImage = `url("${url}")`;
  box.style.backgroundSize = 'cover';
  box.style.backgroundPosition = 'center';
  box.textContent = '';
  const fname = url.split('/').pop() || 'URL 이미지';
  setFilePill(fname);
});

document.getElementById('imgFile')?.addEventListener('change', e=>{
  const file = e.target.files?.[0];
  if(!file) return;
  const reader = new FileReader();
  reader.onload = ev=>{
    const box = document.getElementById('thumbBox');
    box.style.backgroundImage = `url("${ev.target.result}")`;
    box.style.backgroundSize = 'cover';
    box.style.backgroundPosition = 'center';
    box.textContent = '';
  };
  reader.readAsDataURL(file);
  setFilePill(file.name);
});

document.getElementById('previewBtn')?.addEventListener('click', ()=>{
  const url = document.getElementById('linkInput').value.trim();
  if(!url){
    alert('링크를 입력해주세요.');
    return;
  }
  window.open(url, '_blank');
});