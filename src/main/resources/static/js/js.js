// index
// header
// footer
// chatIcon - 주석 처리: chatIcon.html의 스크립트를 사용하므로 충돌 방지
/*
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
*/

// chatIcon 밑

// side 위
(function(){
  var BASES   = ['side','aSide','nSide','bSide'];
  var ACTIVES = ['side-choice','aSide-choice','nSide-choice','bSide-choice'];

  function normalize(menu){
    menu.querySelectorAll('a').forEach(a=>{
      if (ACTIVES.some(c=>a.classList.contains(c))) a.classList.add('side-choice');
      else if (BASES.some(c=>a.classList.contains(c))) a.classList.add('side');
      else a.classList.add('side');
    });
  }

  function setActive(menu,a){
    var cur = menu.querySelector(ACTIVES.map(c=>'.'+c).join(','));
    if(cur && cur!==a){
      cur.classList.remove(...ACTIVES);
      cur.classList.add('side');
      cur.removeAttribute('aria-current');
    }
    a.classList.remove(...BASES, ...ACTIVES);
    a.classList.add('side-choice');
    a.setAttribute('aria-current','page');
  }

  function init(menu){
    if(!menu) return;
    
    // 메인 페이지(/newsBoard/main, /board/main)에서는 normalize와 복원 로직을 건너뛰고 모든 링크를 side로 설정
    var isMainPage = window.location.pathname === '/newsBoard/main' || window.location.pathname === '/board/main';
    
    if(isMainPage){
      // 메인 페이지에서는 모든 링크를 side로 설정하고, side-choice와 aria-current 제거
      menu.querySelectorAll('a').forEach(function(link){
        link.classList.remove('side-choice', 'is-active');
        link.classList.add('side');
        link.removeAttribute('aria-current');
      });
      return; // 메인 페이지에서는 여기서 종료
    }
    
    normalize(menu);

    var scope = menu.dataset.scope || 'side';
    var KEY   = 'side.clean.activeIndex.' + scope;
    var links = Array.from(menu.querySelectorAll('a'));

    // 서버 사이드에서 이미 aria-current가 설정된 링크가 있으면 그것을 유지하고 localStorage 복원하지 않음
    try{
      var hasActive = menu.querySelector('a[aria-current="page"]');
      if(hasActive){
        // 서버에서 설정한 active 링크가 있으면 그것을 유지하고 클래스 정리
        hasActive.classList.remove('side');
        if(!hasActive.classList.contains('side-choice')){
          hasActive.classList.add('side-choice');
        }
      } else {
        // aria-current가 없으면 localStorage에서 복원
        var saved = localStorage.getItem(KEY);
        if(saved!==null && links[+saved]) setActive(menu, links[+saved]);
      }
    }catch(e){}

    // 클릭 토글
    menu.addEventListener('click', e=>{
      var a = e.target.closest('a'); if(!a || !menu.contains(a)) return;
      var href = (a.getAttribute('href')||'').trim();
      if(!href || href==='#') e.preventDefault(); // 데모 점프 방지
      setActive(menu,a);
      try{ localStorage.setItem(KEY, String(links.indexOf(a))); }catch(e){}
    });

    // 키보드 접근성
    menu.addEventListener('keydown', e=>{
      var a = e.target.closest('a'); if(!a) return;
      if(e.key==='Enter' || e.key===' '){ e.preventDefault(); a.click(); }
    });
  }

  function boot(){ document.querySelectorAll('.sideMenu').forEach(init); }
  if(document.readyState==='loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();

  // 동적 삽입했을 때 수동 초기화용
  window.initSideMenus = boot;
})();
//  side 밑

