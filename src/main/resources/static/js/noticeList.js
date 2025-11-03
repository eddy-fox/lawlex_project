  (function(){
    const perPage = 10;
    let currentPage = 1;
    const rows = Array.from(document.querySelectorAll('.page-noticeList .notice-link'));
    const paginationEl = document.getElementById('pagination');

    function renderPage(page){
      currentPage = page;
      const start = (currentPage-1)*perPage;
      const end = start+perPage;
      rows.forEach((row,idx)=> row.style.display = (idx>=start && idx<end) ? "flex" : "none");
      renderPagination();
    }

    function renderPagination(){
      const totalPages = Math.ceil(rows.length / perPage);
      paginationEl.innerHTML = "";
      const prev = document.createElement('a');
      prev.textContent = "<";
      prev.className = "arrow";
      if(currentPage>1) prev.onclick = e=>{e.preventDefault();renderPage(currentPage-1);}
      paginationEl.appendChild(prev);

      for(let i=1;i<=totalPages;i++){
        const btn=document.createElement('a');
        btn.textContent=i;
        if(i===currentPage) btn.classList.add('active');
        btn.onclick=e=>{e.preventDefault();renderPage(i);}
        paginationEl.appendChild(btn);
      }

      const next = document.createElement('a');
      next.textContent = ">";
      next.className = "arrow";
      if(currentPage<totalPages) next.onclick = e=>{e.preventDefault();renderPage(currentPage+1);}
      paginationEl.appendChild(next);
    }

    renderPage(1);
  })();