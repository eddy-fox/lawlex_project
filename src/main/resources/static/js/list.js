const page_elements = document.getElementsByClassName("page-link");
Array.from(page_elements).forEach(function(element) {
  element.addEventListener('click', function(e) {
    e.preventDefault();
    const page = this.dataset.page;
    const kw = document.getElementById('kw') ? document.getElementById('kw').value : '';
    const interestIdx = document.getElementById('interestIdx') ? document.getElementById('intersetIdx').value : '';
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