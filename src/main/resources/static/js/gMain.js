    document.querySelectorAll('.btn').forEach(btn=>{
      btn.addEventListener('click',()=>{
        const isApply = btn.textContent === '신청';
        btn.textContent = isApply ? '신청취소' : '신청';
        btn.classList.toggle('cancel', isApply);
      });
    });

    const chips = document.querySelectorAll('.chip[data-day]');
    const cards = document.querySelectorAll('.card[data-days]');
    const empty = document.getElementById('empty');

    function applyFilter(activeDay){
      let visibleCount = 0;
      cards.forEach(card => {
        const days = card.dataset.days.split(',');
        const show = days.includes(activeDay);
        card.style.display = show ? '' : 'none';
        if(show) visibleCount++;
      });
      empty.hidden = visibleCount !== 0;
    }

    chips.forEach(chip => {
      chip.addEventListener('click', () => {
        chips.forEach(c => { c.classList.remove('is-active'); c.removeAttribute('aria-pressed'); });
        chip.classList.add('is-active');
        chip.setAttribute('aria-pressed','true');
        applyFilter(chip.dataset.day);
      });
    });

    applyFilter('수');