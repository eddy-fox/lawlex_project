const page_elements = document.getElementsByClassName("page-link");
Array.from(page_elements).forEach(function(element) {
  element.addEventListener('click', function(e) {
    e.preventDefault();
    let page = this.dataset.page;
    
    // data-page가 없거나 undefined인 경우 href에서 페이지 번호 추출
    if (!page || page === 'undefined') {
      const href = this.getAttribute('href');
      if (href) {
        const match = href.match(/[?&]page=(\d+)/);
        if (match) {
          page = match[1];
        }
      }
    }
    
    // 여전히 페이지 번호를 찾지 못한 경우 기본값 0 사용
    if (!page || page === 'undefined') {
      page = '0';
    }
    
    const kw = document.getElementById('kw') ? document.getElementById('kw').value : '';
    const interestHidden = document.getElementById('interestIdx');
    const interestIdx = interestHidden ? interestHidden.value : '';
    console.log('CLICK PAGE -> page:', page, 'kw:', kw, 'interestIdx:', interestIdx);
    document.getElementById('page').value = page;
    document.getElementById('searchForm').submit();
  });
});

const searchBtn = document.getElementById('searchBtn');
const keywordInput = document.getElementById('keyword');
const tbody = document.getElementById('tbody');
const allRows = Array.from(tbody.querySelectorAll('tr'));

searchBtn.addEventListener('click', () => {
  const keyword = keywordInput.value.trim().toLowerCase();
  allRows.forEach(row => {
    const title = row.children[1].textContent.toLowerCase();
    if (title.includes(keyword)) row.style.display = '';
    else row.style.display = 'none';
  });
});

keywordInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') searchBtn.click();
});

document.querySelectorAll('.number .num').forEach(num => {
  num.addEventListener('click', e => {
    document.querySelectorAll('.number .num').forEach(n => n.classList.remove('active'));
    e.target.classList.add('active');
  });
});

// 글쓰기 버튼 클릭 시 로그인 체크
(function(){
  const writeBtn = document.getElementById('write');
  if(writeBtn){
    writeBtn.addEventListener('click', function(e){
      // 로그인 상태 확인
      const isLoggedIn = document.body.getAttribute('data-logged-in') === 'true';
      
      if(!isLoggedIn){
        e.preventDefault();
        e.stopPropagation();
        alert('로그인 후 이용이 가능합니다.');
        return false;
      }
    });
  }
})();