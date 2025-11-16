    (function(){
      const perPage = 10;
      const rows = [...document.querySelectorAll('.page-nList .item')];
      const pagination = document.getElementById('pagination');

      // pagination 요소가 없으면 실행하지 않음 (서버 사이드 페이지네이션 사용 시)
      if(!pagination) return;

      function render(page){
        const start = (page - 1) * perPage, end = start + perPage;
        rows.forEach((r,i) => {
          if (i >= start && i < end) {
            r.style.display = ''; // CSS 파일의 display: grid 사용
          } else {
            r.style.display = 'none';
          }
        });
        draw(page);
      }

      function draw(current){
        // pagination 요소가 없으면 실행하지 않음
        if(!pagination) return;
        
        const total = Math.ceil(rows.length / perPage);
        pagination.innerHTML = '';

        const mk = (t, cls, on) => {
          const a = document.createElement('a');
          a.textContent = t;
          if (cls) a.className = cls;
          if (on) a.onclick = e => { e.preventDefault(); on(); };
          return a;
        };

        pagination.appendChild(mk('<', 'arrow', current > 1 ? () => render(current - 1) : null));
        for(let i = 1; i <= total; i++){
          pagination.appendChild(mk(i, i === current ? 'active' : '', () => render(i)));
        }
        pagination.appendChild(mk('>', 'arrow', current < total ? () => render(current + 1) : null));
      }

      render(1);
    })();