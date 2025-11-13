// chatIcon 스크립트
(function () {
  function initChatIcon() {
    var root = document.querySelector('.page-chatIcon');
    if (!root) {
      console.log('[DEBUG] page-chatIcon not found');
      return;
    }
    console.log('[DEBUG] chatIcon script initialized');

    var btn      = root.querySelector('.icon');
    var panel    = root.querySelector('#chatPanel');
    var list     = root.querySelector('#chatList');
    var badge    = root.querySelector('#chatBadge');
    var role     = btn ? btn.dataset.role : null; // MEMBER | LAWYER

    if (!btn) {
      console.log('[DEBUG] Button not found');
      return;
    }

    // 상태 문자열 → 네 패널에서 쓰는 data-status 값으로 변환
    function toStatus(state) {
      if (!state) return 'wait';
      state = state.toUpperCase();
      if (state === 'ACTIVE') return 'live';
      if (state === 'PENDING') return 'wait';
      // 그 외 DECLINED, EXPIRED, CANCELLED 등은 종료로
      return 'end';
    }

    // 리스트 비우고 다시 그리기
    function renderRooms(rooms) {
      if (!list) return;
      list.innerHTML = '';

      console.log('[DEBUG] renderRooms called with:', rooms);

      if (!rooms || rooms.length === 0) {
        console.log('[DEBUG] No rooms to render');
        list.innerHTML = '<p class="empty">채팅방이 없습니다.</p>';
        updateCounts([]);
        return;
      }

      rooms.forEach(function (room) {
        console.log('[DEBUG] Rendering room:', room.chatroomIdx, 'state:', room.state);
        var st = toStatus(room.state);
        var item = document.createElement('div');
        item.className = 'item';
        item.setAttribute('role', 'listitem');
        item.dataset.status = st;
        item.dataset.roomId = room.chatroomIdx;
        item.dataset.unread = 'false'; // 서버에서 안 줘서 일단 false

        item.innerHTML =
          '<div class="pico">' +
            '<img src="/img/testavatar.png" alt="프로필">' +
          '</div>' +
          '<a class="meta">' +
            '<div class="row">' +
              '<b>' + (room.chatroomName || '상담방') + '</b>' +
              '<span class="stamp">' + (room.lastMessageAt || '') + '</span>' +
            '</div>' +
            '<div class="preview">' + (room.lastMessage || '') + '</div>' +
          '</a>' +
          '<span class="label ' + st + '">' +
            (st === 'live' ? '상담중' : st === 'wait' ? '대기중' : '상담종료') +
          '</span>';

        item.addEventListener('click', function (e) {
          e.preventDefault();
          e.stopPropagation();
          var url = '/chat/room?roomId=' + room.chatroomIdx;
          var popup = window.open(
            url,
            'chatRoom_' + room.chatroomIdx,
            'width=800,height=900,scrollbars=yes,resizable=yes,location=yes,menubar=no,toolbar=no'
          );
          if (popup) {
            popup.focus();
          } else {
            alert('팝업이 차단되었습니다. 브라우저 설정에서 팝업을 허용해주세요.');
          }
        });

        list.appendChild(item);
      });

      updateCounts(rooms);
    }

    // 탭 숫자 갱신
    function updateCounts(rooms) {
      var all  = rooms.length;
      var live = rooms.filter(function (r) { return toStatus(r.state) === 'live'; }).length;
      var wait = rooms.filter(function (r) { return toStatus(r.state) === 'wait'; }).length;
      var end  = rooms.filter(function (r) { return toStatus(r.state) === 'end'; }).length;

      var elAll   = root.querySelector('.cnt-all');
      var elLive  = root.querySelector('.cnt-live');
      var elWait  = root.querySelector('.cnt-wait');
      var elEnd   = root.querySelector('.cnt-end');
      var elTotal = root.querySelector('.total-label');

      if (elAll)   elAll.textContent = all;
      if (elLive)  elLive.textContent = live;
      if (elWait)  elWait.textContent = wait;
      if (elEnd)   elEnd.textContent  = end;
      if (elTotal) elTotal.textContent = '총 ' + all + '건';
    }

    // 필터 적용
    function applyFilter(filter) {
      if (!list) return;
      var items = list.querySelectorAll('.item');
      items.forEach(function (it) {
        var st = it.dataset.status;
        if (filter === 'all' || filter === st) {
          it.style.display = '';
        } else {
          it.style.display = 'none';
        }
      });
      root.setAttribute('data-view', filter);
    }

    // 탭 클릭
    root.addEventListener('click', function (e) {
      var tab = e.target.closest('.tab');
      if (!tab) return;
      if (!panel || panel.hidden) return; // 닫혀있으면 무시
      root.querySelectorAll('.tab').forEach(function (t) {
        t.classList.remove('is-active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('is-active');
      tab.setAttribute('aria-selected', 'true');
      
      // 종료 탭 클릭 시 종료된 방(EXPIRED, DECLINED, CANCELLED) 로드
      if (tab.dataset.filter === 'end' && role === 'MEMBER') {
        console.log('[DEBUG] Loading ended rooms for end tab');
        fetch('/chat/api/member/rooms?state=ENDED')
          .then(function (r) { 
            console.log('[DEBUG] ENDED rooms response status:', r.status, r.ok);
            return r.ok ? r.json() : []; 
          })
          .then(function (endedRooms) {
            console.log('[DEBUG] Received ended rooms:', endedRooms.length, endedRooms);
            // 기존 방 목록에 종료된 방 추가 (중복 제거)
            var currentItems = Array.from(list.querySelectorAll('.item'));
            var existingRoomIds = currentItems.map(function(item) {
              return item.dataset.roomId;
            }).filter(Boolean);
            
            console.log('[DEBUG] Existing room IDs:', existingRoomIds);
            
            endedRooms.forEach(function (room) {
              var roomIdStr = String(room.chatroomIdx);
              if (!existingRoomIds.includes(roomIdStr)) {
                console.log('[DEBUG] Adding expired room:', room.chatroomIdx, room.state);
                var st = toStatus(room.state);
                var item = document.createElement('div');
                item.className = 'item';
                item.setAttribute('role', 'listitem');
                item.dataset.status = st;
                item.dataset.roomId = room.chatroomIdx;
                item.dataset.unread = 'false';

                item.innerHTML =
                  '<div class="pico">' +
                    '<img src="/img/testavatar.png" alt="프로필">' +
                  '</div>' +
                  '<a class="meta">' +
                    '<div class="row">' +
                      '<b>' + (room.chatroomName || '상담방') + '</b>' +
                      '<span class="stamp">' + (room.lastMessageAt || '') + '</span>' +
                    '</div>' +
                    '<div class="preview">' + (room.lastMessage || '') + '</div>' +
                  '</a>' +
                  '<span class="label ' + st + '">' +
                    (st === 'live' ? '상담중' : st === 'wait' ? '대기중' : '상담종료') +
                  '</span>';

                item.addEventListener('click', function (e) {
                  e.preventDefault();
                  e.stopPropagation();
                  var url = '/chat/room?roomId=' + room.chatroomIdx;
                  var popup = window.open(
                    url,
                    'chatRoom_' + room.chatroomIdx,
                    'width=800,height=900,scrollbars=yes,resizable=yes,location=yes,menubar=no,toolbar=no'
                  );
                  if (popup) {
                    popup.focus();
                  } else {
                    alert('팝업이 차단되었습니다. 브라우저 설정에서 팝업을 허용해주세요.');
                  }
                });

                list.appendChild(item);
              } else {
                console.log('[DEBUG] Skipping duplicate room:', room.chatroomIdx);
              }
            });
            
            // 카운트 업데이트를 위해 모든 아이템의 상태를 확인
            var allItems = Array.from(list.querySelectorAll('.item'));
            var roomsForCount = allItems.map(function(item) {
              var status = item.dataset.status;
              return { state: status === 'live' ? 'ACTIVE' : status === 'wait' ? 'PENDING' : 'EXPIRED' };
            });
            console.log('[DEBUG] Updating counts with', roomsForCount.length, 'rooms');
            updateCounts(roomsForCount);
            applyFilter('end');
          })
          .catch(function (err) { 
            console.error('[DEBUG] Error loading expired rooms:', err);
            applyFilter('end');
          });
      } else {
        applyFilter(tab.dataset.filter);
      }
    });

    // 패널 열기/닫기
    var refreshInterval = null;
    function togglePanel(open) {
      if (!panel) return;
      panel.hidden = !open;
      if (btn) btn.setAttribute('aria-expanded', open ? 'true' : 'false');
      
      // 패널이 열려있을 때만 주기적으로 갱신 (5초마다)
      if (open && role === 'MEMBER') {
        if (refreshInterval) clearInterval(refreshInterval);
        refreshInterval = setInterval(function() {
          if (!panel || panel.hidden) {
            clearInterval(refreshInterval);
            refreshInterval = null;
            return;
          }
          console.log('[DEBUG] Auto-refreshing rooms list');
          // 진행중 방과 종료된 방을 모두 로드
          Promise.all([
            fetch('/chat/api/member/rooms?state=ONGOING').then(function (r) { return r.ok ? r.json() : []; }),
            fetch('/chat/api/member/rooms?state=ENDED').then(function (r) { return r.ok ? r.json() : []; })
          ])
          .then(function (results) {
            var ongoingRooms = results[0];
            var endedRooms = results[1];
            // 두 목록을 합침 (중복 제거)
            var allRoomIds = new Set();
            var allRooms = [];
            ongoingRooms.forEach(function(room) {
              if (!allRoomIds.has(room.chatroomIdx)) {
                allRoomIds.add(room.chatroomIdx);
                allRooms.push(room);
              }
            });
            endedRooms.forEach(function(room) {
              if (!allRoomIds.has(room.chatroomIdx)) {
                allRoomIds.add(room.chatroomIdx);
                allRooms.push(room);
              }
            });
            renderRooms(allRooms);
            applyFilter(root.getAttribute('data-view') || 'all');
          })
          .catch(function (err) { console.error('[DEBUG] Auto-refresh error:', err); });
        }, 5000); // 5초마다 갱신
      } else {
        if (refreshInterval) {
          clearInterval(refreshInterval);
          refreshInterval = null;
        }
      }
    }

    // 외부 클릭시 닫기
    document.addEventListener('click', function (e) {
      if (!panel || panel.hidden) return;
      if (!root.contains(e.target)) {
        togglePanel(false);
      }
    });

    // ESC 닫기
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') {
        togglePanel(false);
      }
    });

    // 버튼 클릭 동작
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      console.log('[DEBUG] Button clicked, role:', role);
      
      if (role === 'LAWYER') {
        // 변호사는 리스트 안 쓰고 바로 메인으로
        window.location.href = '/chat/lawyer';
        return;
      }
      // 회원이면 패널 토글 + 열릴 때 방 목록 불러오기
      var willOpen = panel ? panel.hidden : false;
      togglePanel(willOpen);
      if (willOpen && panel) {
        console.log('[DEBUG] Fetching rooms from /chat/api/member/rooms?state=ONGOING');
        // 진행중 방과 종료된 방을 모두 로드
        Promise.all([
          fetch('/chat/api/member/rooms?state=ONGOING').then(function (r) { 
            console.log('[DEBUG] ONGOING response status:', r.status, r.ok);
            return r.ok ? r.json() : []; 
          }),
          fetch('/chat/api/member/rooms?state=ENDED').then(function (r) { 
            console.log('[DEBUG] ENDED response status:', r.status, r.ok);
            return r.ok ? r.json() : []; 
          })
        ])
        .then(function (results) {
          var ongoingRooms = results[0];
          var endedRooms = results[1];
          console.log('[DEBUG] Received ongoing rooms:', ongoingRooms.length, ongoingRooms);
          console.log('[DEBUG] Received ended rooms:', endedRooms.length, endedRooms);
          // 두 목록을 합침 (중복 제거)
          var allRoomIds = new Set();
          var allRooms = [];
          ongoingRooms.forEach(function(room) {
            if (!allRoomIds.has(room.chatroomIdx)) {
              allRoomIds.add(room.chatroomIdx);
              allRooms.push(room);
            }
          });
          endedRooms.forEach(function(room) {
            if (!allRoomIds.has(room.chatroomIdx)) {
              allRoomIds.add(room.chatroomIdx);
              allRooms.push(room);
            }
          });
          console.log('[DEBUG] Total rooms after merge:', allRooms.length);
          renderRooms(allRooms);
          applyFilter('all');
        })
        .catch(function (err) { 
          console.error('[DEBUG] Fetch error:', err);
        });
      }
    });

    // 처음에 뱃지 숫자만 채우기
    if (role === 'MEMBER') {
      fetch('/chat/api/member/rooms/badge')
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (data) {
          if (!data) return;
          var count = (data.unread || 0) + (data.pending || 0);
          if (count > 0 && badge) {
            badge.textContent = count;
            badge.hidden = false;
          }
        })
        .catch(function () {});
    } else if (role === 'LAWYER') {
      // 변호사는 대기중(PENDING)만 뱃지로
      fetch('/chat/api/lawyer/badge')
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (data) {
          if (!data) return;
          var count = (data.pending || 0);
          if (count > 0 && badge) {
            badge.textContent = count;
            badge.hidden = false;
          }
        })
        .catch(function () {});
    }
  }

  // DOM이 로드된 후 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initChatIcon);
  } else {
    // DOM이 이미 로드된 경우 즉시 실행
    initChatIcon();
  }
})();

