(function(){
  const MAX_FILES = 10;

  const form = document.getElementById('writeForm');
  const title = document.getElementById('title');
  const content = document.getElementById('content');
  const submitBtn = document.getElementById('submitBtn');

  const uploadBtn = document.getElementById('uploadBtn');
  const drop = document.getElementById('drop');
  const fileInput = document.getElementById('file');
  const thumbs = document.getElementById('thumbs');
  const count = document.getElementById('count');
  const maxSpan = document.getElementById('max');

  maxSpan.textContent = MAX_FILES;

  function validate(){
    const ok = title.value.trim().length >= 2 && content.value.trim().length >= 20;
    submitBtn.disabled = !ok;
  }
  title.addEventListener('input', validate);
  content.addEventListener('input', validate);
  content.addEventListener('keydown', (e)=>{
    if((e.ctrlKey||e.metaKey)&&e.key==='Enter'){
      if(!submitBtn.disabled) form.requestSubmit();
    }
  });

  // Chips UI
  document.getElementById('chips').addEventListener('change', (e)=>{
    if(e.target && e.target.type === 'checkbox'){
      e.target.closest('.chip').classList.toggle('active', e.target.checked);
    }
  });

  function updateCounter(){
    count.textContent = thumbs.querySelectorAll('.thumb').length;
  }

  function createThumb(dataURL){
    const div = document.createElement('div');
    div.className = 'thumb';
    div.innerHTML = '<img alt="업로드 이미지"><button type="button" class="remove" aria-label="삭제">×</button>';
    div.querySelector('img').src = dataURL;
    div.querySelector('.remove').addEventListener('click', ()=>{
      div.remove();
      updateCounter();
    });
    thumbs.appendChild(div);
    updateCounter();
  }

  function addFiles(files){
    const already = thumbs.querySelectorAll('.thumb').length;
    const remain = MAX_FILES - already;
    if(remain <= 0){ alert('이미지는 최대 ' + MAX_FILES + '장까지 가능합니다.'); return; }
    const list = Array.from(files).slice(0, remain);
    list.forEach(file=>{
      if(!file.type.startsWith('image/')) return;
      const reader = new FileReader();
      reader.onload = e => createThumb(e.target.result);
      reader.readAsDataURL(file);
    });
    if(files.length > remain){
      alert('최대 ' + MAX_FILES + '장까지만 추가됩니다.');
    }
  }

  function openPicker(){ fileInput.click(); }
  uploadBtn.addEventListener('click', openPicker);
  drop.addEventListener('click', openPicker);

  fileInput.addEventListener('change', (e)=> addFiles(e.target.files));

  // Drag & Drop support
  ;['dragenter','dragover'].forEach(evt=>{
    drop.addEventListener(evt, e=>{ e.preventDefault(); e.stopPropagation(); drop.style.borderColor = '#94a3b8'; });
  });
  ;['dragleave','drop'].forEach(evt=>{
    drop.addEventListener(evt, e=>{ e.preventDefault(); e.stopPropagation(); drop.style.borderColor = 'var(--line)'; });
  });
  drop.addEventListener('drop', e=>{
    const files = e.dataTransfer.files;
    addFiles(files);
  });

  document.getElementById('cancelBtn').addEventListener('click', ()=>{
    if(confirm('작성한 내용을 모두 지울까요?')){
      form.reset();
      thumbs.innerHTML = '';
      updateCounter();
      document.querySelectorAll('.chip').forEach(c=>c.classList.remove('active'));
      validate();
    }
  });

  form.addEventListener('submit', (e)=>{
    e.preventDefault();
    alert('작성 완료되었습니다. (데모)');
  });

  validate();
  updateCounter();
})();