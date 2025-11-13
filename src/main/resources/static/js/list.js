const page_elements = document.getElementsByClassName("page-link");
Array.from(page_elements).forEach(function(element) {
  element.addEventListener('click', function() {
    document.getElementById('page').value = this.dataset.page;
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