    (function(){
      const perPage = 10;
      const rows = [...document.querySelectorAll('.page-nList .item')];
      const pagination = document.getElementById('pagination');

      function render(page){
        const start = (page - 1) * perPage, end = start + perPage;
        rows.forEach((r,i) => r.style.display = (i >= start && i < end) ? 'grid' : 'none');
        draw(page);
      }

      function draw(current){
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