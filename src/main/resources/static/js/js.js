// index
// header
// footer
// chatIcon 
    (function () {
      document.querySelectorAll('.page-chatIcon').forEach(function (root) {
        var btn = root.querySelector('.icon'),
            panel = root.querySelector('.panel'),
            badge = root.querySelector('.badge'),
            tabs = [].slice.call(root.querySelectorAll('.tab')),
            list = root.querySelector('.list');
        if (!btn || !panel || !list) return;
        function setBadgeToUnread() {
          if (!badge) return;
          var n = list.querySelectorAll('.item[data-unread="true"]').length;
          if (n <= 0) {
            badge.style.display = 'none';
          } else {
            badge.style.display = '';
            badge.textContent = n > 99 ? '99+' : String(n);
          }
        }
        function setView(k) { root.setAttribute('data-view', k); }
        function toggle(open) {
          if (open) {
            panel.hidden = false;
            btn.setAttribute('aria-expanded', 'true');
          } else {
            panel.hidden = true;
            btn.setAttribute('aria-expanded', 'false');
          }
        }
        function counts() {
          var items = [].slice.call(list.querySelectorAll('.item')),
              all = items.length, live = 0, wait = 0, end = 0;
          items.forEach(function (it) {
            var s = it.getAttribute('data-status');
            if (s === 'live') live++;
            else if (s === 'wait') wait++;
            else if (s === 'end') end++;
          });
          var q = function (s) { return root.querySelector(s); };
          var elAll = q('.cnt-all'), elLive = q('.cnt-live'), elWait = q('.cnt-wait'), elEnd = q('.cnt-end'), elTotal = q('.total-label');
          if (elAll) elAll.textContent = all;
          if (elLive) elLive.textContent = live;
          if (elWait) elWait.textContent = wait;
          if (elEnd) elEnd.textContent = end;
          if (elTotal) elTotal.textContent = '총 ' + all + '건';
        }
        function applyFilter(k) {
          [].slice.call(list.querySelectorAll('.item')).forEach(function (it) {
            var ok = (k === 'all') || (it.getAttribute('data-status') === k);
            it.style.display = ok ? '' : 'none';
          });
          setView(k);
        }
        list.addEventListener('click', function (e) {
          var a = e.target.closest('.meta');
          if (!a) return;
          e.preventDefault();
          var it = a.closest('.item');
          if (!it) return;
          it.setAttribute('data-unread', 'false');
          setBadgeToUnread();
        });
        btn.addEventListener('click', function () { toggle(panel.hidden); });
        document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { toggle(false); } });
        document.addEventListener('click', function (e) {
          if (panel.hidden) return;
          if (!root.contains(e.target)) toggle(false);
        });
        tabs.forEach(function (t) {
          t.addEventListener('click', function () {
            tabs.forEach(function (x) { x.classList.remove('is-active'); x.setAttribute('aria-selected', 'false'); });
            t.classList.add('is-active');
            t.setAttribute('aria-selected', 'true');
            applyFilter(t.getAttribute('data-filter'));
          });
        });
        counts();
        setBadgeToUnread();
        applyFilter('all');
      });
    })();

// chatIcon 밑

// side 위
(function(){
  function setup(menu){
    if(!menu) return;
    var scope = menu.getAttribute('data-scope') || 'side';
    var KEY = 'side.clean.activeIndex.' + scope;
    var links = function(){ return Array.prototype.slice.call(menu.querySelectorAll('a')); };

    function setActive(a){
      var current = menu.querySelector('.side-choice');
      if(current && current !== a){
        current.classList.remove('side-choice');
        current.classList.add('side');
        current.removeAttribute('aria-current');
      }
      a.classList.remove('side');
      a.classList.add('side-choice');
      a.setAttribute('aria-current','page');
    }

    // restore saved active
    try{
      var saved = localStorage.getItem(KEY);
      if(saved !== null){
        var idx = parseInt(saved,10);
        var items = links();
        if(items[idx]) setActive(items[idx]);
      }
    }catch(e){ /* ignore storage errors */ }

    // click: prevent jump for # / empty, toggle active, persist
    menu.addEventListener('click', function(e){
      var a = e.target.closest('a'); if(!a || !menu.contains(a)) return;
      var href = (a.getAttribute('href')||'').trim();
      if(href === '' || href === '#') e.preventDefault();
      setActive(a);
      try{
        var idx = links().indexOf(a);
        if(idx > -1) localStorage.setItem(KEY, String(idx));
      }catch(e){ /* ignore */ }
    });

    // keyboard: Enter/Space to activate
    menu.addEventListener('keydown', function(e){
      var a = e.target.closest('a'); if(!a) return;
      if(e.key === 'Enter' || e.key === ' '){
        e.preventDefault();
        a.click();
      }
    });
  }
  document.addEventListener('DOMContentLoaded', function(){
    document.querySelectorAll('.sideMenu').forEach(setup);
  });
})();
//  side 밑