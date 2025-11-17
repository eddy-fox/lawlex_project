(function () {
  // ===== 기본 엘리먼트 =====
  var scope = document.querySelector('.page-gMain') || document.querySelector('.page-lChating');
  if (!scope) {
    console.log('[DEBUG] .page-gMain or .page-lChating not found');
    return;
  }
  console.log('[DEBUG] lChating.js initialized');

  var msgs      = document.getElementById('msgs');
  var input     = document.getElementById('inputMsg');
  var sendBtn   = document.getElementById('btnSend');
  var upBtn     = scope.querySelector('#btnUpload');
  var fileInput = scope.querySelector('#fileUpload');
  var previews  = scope.querySelector('#previews');
  
  if (!msgs) {
    console.error('[DEBUG] #msgs element not found');
  }
  if (!input) {
    console.error('[DEBUG] #inputMsg element not found');
  }
  if (!sendBtn) {
    console.error('[DEBUG] #btnSend element not found');
  }

  // 서버에서 내려준 방/사용자 정보
  var roomIdEl     = document.getElementById('roomId');
  var roomId       = roomIdEl ? roomIdEl.value : null;
  var senderType   = 'LAWYER'; // 변호사 화면이므로 고정
  var senderIdEl   = document.getElementById('meLawyerId');
  var senderId     = senderIdEl ? senderIdEl.value : null;

  // 업로드할 파일들을 임시로 들고 있을 배열
  var pendingFiles = [];

  // 웹소켓(STOMP)
  var stompClient = null;

  // ====== 유틸 ======
  function uid() {
    return 'f' + Math.random().toString(36).slice(2, 9);
  }
  function scrollBottom() {
    if (!msgs) return;
    msgs.scrollTop = msgs.scrollHeight;
  }
  function formatTime(isoOrNull) {
    try {
      var d = isoOrNull ? new Date(isoOrNull) : new Date();
      var hh = String(d.getHours()).padStart(2, '0');
      var mm = String(d.getMinutes()).padStart(2, '0');
      return hh + ':' + mm;
    } catch (e) {
      return '';
    }
  }

  // ======================================================
  // 1. WebSocket(STOMP) 연결해서 이 방 구독
  // ======================================================
  function connectWs() {
    if (!roomId) {
      console.log('[DEBUG] No roomId, skipping WebSocket connection');
      return;
    }
    if (typeof SockJS === 'undefined') {
      console.error('[DEBUG] SockJS not loaded');
      return;
    }
    if (typeof Stomp === 'undefined') {
      console.error('[DEBUG] Stomp not loaded. Available globals:', Object.keys(window).filter(function(k) { return k.toLowerCase().includes('stomp'); }));
      return;
    }
    console.log('[DEBUG] Connecting to WebSocket, roomId:', roomId);
    try {
      var socket = new SockJS('/ws');
      stompClient = Stomp.over(socket);
      stompClient.debug = null;

      stompClient.connect({}, function () {
        console.log('[DEBUG] WebSocket connected, subscribing to /topic/chat/' + roomId);
        stompClient.subscribe('/topic/chat/' + roomId, function (message) {
          var body = JSON.parse(message.body);
          console.log('[DEBUG] Received message:', body);
          appendMessageDom(body);
        });
      }, function(error) {
        console.error('[DEBUG] WebSocket connection error:', error);
      });
    } catch (e) {
      console.error('[DEBUG] WebSocket setup error:', e);
    }
  }

  // ======================================================
  // 2. 첨부파일 미리보기
  // ======================================================
  function addPreview(file) {
    var id = uid();
    var url = URL.createObjectURL(file);
    pendingFiles.push({ file: file, url: url, id: id });

    var chip = document.createElement('div');
    chip.className = 'chip';
    chip.dataset.id = id;

    var img = document.createElement('img');
    img.src = url;
    img.alt = '선택한 이미지 미리보기';

    var close = document.createElement('button');
    close.type = 'button';
    close.setAttribute('aria-label', '삭제');
    close.textContent = '×';
    close.addEventListener('click', function () {
      removePreview(id);
    });

    chip.appendChild(img);
    chip.appendChild(close);
    previews.appendChild(chip);
  }

  function removePreview(id) {
    var idx = pendingFiles.findIndex(function (x) { return x.id === id; });
    if (idx >= 0) {
      URL.revokeObjectURL(pendingFiles[idx].url);
      pendingFiles.splice(idx, 1);
    }
    var chip = previews.querySelector('.chip[data-id="' + id + '"]');
    if (chip) chip.remove();
  }

  function clearPreviews() {
    pendingFiles.forEach(function (p) { URL.revokeObjectURL(p.url); });
    pendingFiles = [];
    if (previews) {
      previews.innerHTML = '';
    }
  }

  // ======================================================
  // 3. 메시지 DOM에 추가 (서버/내가 보낸 거 공통)
  // ======================================================
  function appendMessageDom(msg) {
    if (!msgs) {
      console.error('[DEBUG] msgs element not found');
      return;
    }
    console.log('[DEBUG] appendMessageDom called with:', msg);
    // 변호사 화면 기준:
    // 내가 보낸(LAWYER) → 왼쪽 .lawyer
    // 일반회원이 보낸(MEMBER) → 오른쪽 .user
    var isMe = msg.senderType && msg.senderType.toUpperCase() === 'LAWYER';

    var row = document.createElement('div');
    row.className = 'msg-row ' + (isMe ? 'lawyer' : 'user');

    // 일반회원이 보낸 메시지(MEMBER) → 오른쪽, 시간 먼저
    if (!isMe) {
      var meta1 = document.createElement('div');
      meta1.className = 'meta';
      meta1.textContent = formatTime(msg.chatRegDate);
      row.appendChild(meta1);
    }

    // 말풍선
    if (msg.chatContent) {
      var bubble = document.createElement('div');
      bubble.className = 'bubble';
      var inner = document.createElement('div');
      inner.textContent = msg.chatContent;
      bubble.appendChild(inner);
      row.appendChild(bubble);
    }

    // 첨부 이미지 (텍스트와 같은 위치에 배치)
    if (msg.attachments && msg.attachments.length > 0) {
      var media = document.createElement('div');
      media.className = 'media';
      msg.attachments.forEach(function (att) {
        if (!att.fileUrl) return; // fileUrl이 없으면 스킵
        var a = document.createElement('a');
        a.href = att.fileUrl;
        a.target = '_blank';
        var im = document.createElement('img');
        im.src = att.fileUrl;
        im.alt = att.fileName || '첨부';
        a.appendChild(im);
        media.appendChild(a);
      });
      row.appendChild(media);
    }

    // 변호사가 보낸 메시지(LAWYER) → 왼쪽, 말풍선 뒤에 시간
    if (isMe) {
      var meta2 = document.createElement('div');
      meta2.className = 'meta';
      meta2.textContent = formatTime(msg.chatRegDate);
      row.appendChild(meta2);
    }

    msgs.appendChild(row);
    scrollBottom();
  }

  // ======================================================
  // 4. 메시지 보내기 (REST → 필요하면 STOMP로도 뿌리기)
  // ======================================================
  function sendMessage() {
    var text = (input.value || '').trim();

    // 텍스트도 없고 파일도 없으면 무시
    if (!text && pendingFiles.length === 0) {
      return;
    }
    if (!roomId) {
      alert('방 정보가 없습니다.');
      return;
    }

    var formData = new FormData();
    // @RequestParam을 사용하는 ChatMessageController 사용
    formData.append('roomId', roomId);
    if (text) {
      formData.append('content', text);
    }

    // 첨부
    pendingFiles.forEach(function (p) {
      formData.append('files', p.file);
    });

    fetch('/chat/messages', {
      method: 'POST',
      body: formData
      // FormData를 사용하면 Content-Type을 명시하지 않아야 브라우저가 자동으로 multipart/form-data로 설정
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (dto) {
        console.log('[DEBUG] Message sent, received DTO:', dto);
        // 서버에 저장된 최종 DTO를 화면에 반영
        appendMessageDom(dto);
        // 입력창/미리보기 초기화
        input.value = '';
        clearPreviews();

        // 다른 참여자에게도 websocket으로 쏘고 싶으면 여기서
        if (stompClient && stompClient.connected) {
          stompClient.send('/app/chat/' + roomId, {}, JSON.stringify(dto));
        }
      })
      .catch(function (err) {
        console.error('[DEBUG] Message send error:', err);
        alert('메시지 전송에 실패했습니다: ' + err.message);
      });
  }

  // ======================================================
  // 5. 남은시간 표시 (data-expires-at 읽어서 카운트다운)
  // ======================================================
  function startRemainTimer() {
    var el = document.getElementById('remainTime');
    if (!el) return;
    var exp = el.getAttribute('data-expires-at');
    if (!exp) return;
    var target = new Date(exp).getTime();

    function tick() {
      var now = Date.now();
      var diff = target - now;
      if (diff <= 0) {
        el.textContent = '만료됨';
        return;
      }
      var m = Math.floor(diff / 1000 / 60);
      var s = Math.floor(diff / 1000) % 60;
      el.textContent = '남은시간 ' + String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
      setTimeout(tick, 1000);
    }
    tick();
  }

  // ======================================================
  // 6. 라이트박스 (원래 있던 코드 살려서 그대로)
  // ======================================================
  var viewer   = scope.querySelector('#viewer'),
      vImg     = scope.querySelector('#viewerImg'),
      btnClose = scope.querySelector('#btnClose'),
      btnFit   = scope.querySelector('#btnFit'),
      btnPrev  = scope.querySelector('#btnPrev'),
      btnNext  = scope.querySelector('#btnNext');

  var group = [], idx = -1, fit = true;

  function openViewer(images, startIndex) {
    group = images;
    idx = startIndex;
    fit = true;
    viewer.classList.remove('fit-off');
    btnFit.textContent = '100%';
    viewer.setAttribute('aria-hidden', 'false');
    updateViewer();
  }
  function closeViewer() {
    viewer.setAttribute('aria-hidden', 'true');
    vImg.src = '';
    group = [];
    idx = -1;
  }
  function updateViewer() {
    vImg.src = group[idx];
    btnPrev.disabled = (idx <= 0);
    btnNext.disabled = (idx >= group.length - 1);
  }
  function toggleFit() {
    fit = !fit;
    if (fit) {
      viewer.classList.remove('fit-off');
      btnFit.textContent = '100%';
    } else {
      viewer.classList.add('fit-off');
      btnFit.textContent = '맞춤';
    }
  }

  msgs.addEventListener('click', function (e) {
    var t = e.target;
    if (t.tagName === 'IMG' && t.closest('.media')) {
      var tiles = Array.prototype.slice.call(t.closest('.media').querySelectorAll('img'))
        .map(function (img) { return img.src; });
      var start = tiles.indexOf(t.src);
      openViewer(tiles, Math.max(0, start));
    }
  });
  if (btnClose) btnClose.addEventListener('click', closeViewer);
  if (btnFit) btnFit.addEventListener('click', toggleFit);
  if (btnPrev) btnPrev.addEventListener('click', function () { if (idx > 0) { idx--; updateViewer(); } });
  if (btnNext) btnNext.addEventListener('click', function () { if (idx < group.length - 1) { idx++; updateViewer(); } });
  if (viewer) {
    viewer.addEventListener('click', function (e) {
      if (e.target === viewer || e.target.classList.contains('stage')) closeViewer();
    });
  }
  document.addEventListener('keydown', function (e) {
    if (!viewer || viewer.getAttribute('aria-hidden') === 'true') return;
    if (e.key === 'Escape') closeViewer();
    else if (e.key === 'ArrowLeft' && idx > 0) { idx--; updateViewer(); }
    else if (e.key === 'ArrowRight' && idx < group.length - 1) { idx++; updateViewer(); }
    else if (e.key === 'f' || e.key === 'F') { toggleFit(); }
  });

  // ======================================================
  // 7. 이벤트 바인딩
  // ======================================================
  if (input) {
    input.addEventListener('keydown', function (e) {
      // 엔터로 전송
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });
  }

  if (sendBtn) {
    console.log('[DEBUG] sendBtn found, adding click listener');
    sendBtn.addEventListener('click', function(e) {
      console.log('[DEBUG] sendBtn clicked');
      e.preventDefault();
      sendMessage();
    });
  } else {
    console.error('[DEBUG] sendBtn not found!');
  }

  if (upBtn && fileInput) {
    upBtn.addEventListener('click', function () {
      fileInput.click();
    });
    fileInput.addEventListener('change', function () {
      if (fileInput.files && fileInput.files.length) {
        var max = 6;
        Array.from(fileInput.files)
          .slice(0, max - pendingFiles.length)
          .forEach(addPreview);
        fileInput.value = '';
      }
    });
  }

  // 초기화
  if (roomId) {
    connectWs();
  } else {
    console.error('[DEBUG] No roomId found, cannot connect WebSocket');
  }
  startRemainTimer();
  scrollBottom();
})();
