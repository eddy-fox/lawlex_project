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

  // 추천 카테고리 영역 이벤트 리스너
  const recommendedChips = document.getElementById('recommendedChips');
  if(recommendedChips) {
    recommendedChips.addEventListener('change', (e)=>{
      if(e.target && e.target.type === 'checkbox'){
        // 다른 체크박스 해제 (단일 선택)
        document.querySelectorAll('#recommendedChips input[type="checkbox"]').forEach(cb => {
          if(cb !== e.target) {
            cb.checked = false;
            cb.closest('.chip').classList.remove('active');
          }
        });
        e.target.closest('.chip').classList.toggle('active', e.target.checked);
        updateSelectedCategory();
      }
    });
  }

  // 카테고리 -> categoryIdx 매핑
  function getCategoryIdx(category) {
    if (!category) return null;
    
    const categoryMap = {
      // categoryIdx = 1
      '성매매': 1,
      '성폭력/강제추행 등': 1,
      '미성년 대상 성범죄': 1,
      '디지털 성범죄': 1,
      // categoryIdx = 2
      '횡령/배임': 2,
      '사기/공갈': 2,
      '기타 재산범죄': 2,
      // categoryIdx = 3
      '교통사고/도주': 3,
      '음주/무면허': 3,
      // categoryIdx = 4
      '고소/소송절차': 4,
      '수사/체포/구속': 4,
      // categoryIdx = 5
      '폭행/협박/상해 일반': 5,
      // categoryIdx = 6
      '명예훼손/모욕 일반': 6,
      '사이버 명예훼손/모욕': 6,
      // categoryIdx = 7
      '마약/도박': 7,
      '소년범죄/학교폭력': 7,
      '형사일반/기타범죄': 7,
      // categoryIdx = 8
      '건축/부동산 일반': 8,
      '재개발/재건축': 8,
      '매매/소유권 등': 8,
      '임대차': 8,
      // categoryIdx = 9
      '손해배상': 9,
      '대여금/채권추심': 9,
      '계약일반/매매': 9,
      // categoryIdx = 10
      '소송/집행절차': 10,
      '가압류/가처분': 10,
      '회생/파산': 10,
      // categoryIdx = 11
      '공증/내용증명/조합/국제문제 등': 11,
      // categoryIdx = 12
      '이혼': 12,
      '상속': 12,
      '가사 일반': 12,
      // categoryIdx = 13
      '기업법무': 13,
      '노동/인사': 13,
      // categoryIdx = 14
      '세금/행정/헌법': 14,
      '의료/식품의약': 14,
      '병역/군형법': 14,
      // categoryIdx = 15
      '소비자/공정거래': 15,
      'IT/개인정보': 15,
      '지식재산권/엔터': 15,
      '금융/보험': 15
    };
    
    return categoryMap[category] || null;
  }

  // 선택된 카테고리 업데이트
  function updateSelectedCategory(){
    const selected = [];
    document.querySelectorAll('#recommendedChips input[type="checkbox"]:checked').forEach(cb => {
      selected.push(cb.value);
    });
    
    const category = selected.length > 0 ? selected[0] : '';
    const categoryIdx = getCategoryIdx(category);
    
    document.getElementById('boardCategory').value = category;
    const interestIdxInput = document.getElementById('interestIdx');
    if(interestIdxInput) {
      interestIdxInput.value = categoryIdx || '';
    }
  }

  // 카테고리 추천 기능
  let recommendTimeout;
  title.addEventListener('input', ()=>{
    validate();
    // 디바운싱: 1초 후에 API 호출
    clearTimeout(recommendTimeout);
    recommendTimeout = setTimeout(() => {
      const titleValue = title.value.trim();
      if(titleValue.length >= 2) {
        fetchCategoryRecommendations(titleValue);
      } else {
        hideRecommendedCategories();
      }
    }, 1000);
  });

  function fetchCategoryRecommendations(titleText) {
    fetch(`/board/api/recommend-category?title=${encodeURIComponent(titleText)}`)
      .then(res => res.json())
      .then(data => {
        if(data.success && data.categories && data.categories.length > 0) {
          displayRecommendedCategories(data.categories);
        } else {
          hideRecommendedCategories();
        }
      })
      .catch(err => {
        console.error('카테고리 추천 오류:', err);
        hideRecommendedCategories();
      });
  }

  function displayRecommendedCategories(categories) {
    const container = document.getElementById('recommendedCategories');
    const chipsContainer = document.getElementById('recommendedChips');
    
    if(!container || !chipsContainer) return;
    
    chipsContainer.innerHTML = '';
    categories.forEach(category => {
      const label = document.createElement('label');
      label.className = 'chip';
      label.style.backgroundColor = '#eff6ff';
      label.style.borderColor = '#3b82f6';
      label.innerHTML = `<input type="checkbox" value="${escapeHtml(category)}"> ${escapeHtml(category)}`;
      chipsContainer.appendChild(label);
    });
    
    container.style.display = 'block';
  }

  function hideRecommendedCategories() {
    const container = document.getElementById('recommendedCategories');
    if(container) {
      container.style.display = 'none';
    }
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

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
      document.querySelectorAll('#recommendedChips .chip').forEach(c=>c.classList.remove('active'));
      hideRecommendedCategories();
      validate();
    }
  });

  form.addEventListener('submit', (e)=>{
    // 선택된 카테고리 업데이트
    updateSelectedCategory();
    // 실제 제출은 서버에서 처리
  });

  validate();
  updateCounter();
})();