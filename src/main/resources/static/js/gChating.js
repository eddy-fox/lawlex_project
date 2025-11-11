(function () {
  var scope = document.querySelector('.page-gChating');
  if (!scope) return;

  var msgs      = scope.querySelector('#msgs');
  var input     = scope.querySelector('#inputMsg');
  var sendBtn   = scope.querySelector('#btnSend');
  var upBtn     = scope.querySelector('#btnUpload');
  var fileInput = scope.querySelector('#fileUpload');
  var previews  = scope.querySelector('#previews');

  var roomIdEl     = document.getElementById('roomId');
  var senderTypeEl = document.getElementById('senderType');
  var senderIdEl   = document.getElementById('senderId');
  var expiresEl    = document.getElementById('expiresAt');

  var roomId     = roomIdEl ? roomIdEl.value : null;
  var senderType = senderTypeEl ? senderTypeEl.value : 'MEMBER';
  var senderId   = senderIdEl ? senderIdEl.value : null;

  var pendingFiles = [];
  var stompClient = null;

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

  // ================== WebSocket 연결 ==================
  function connectWs() {
    if (!roomId) return;
    var socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function () {
      stompClient.subscribe('/topic/chat/' + roomId, function (msg) {
        var body = JSON.parse(msg.body);
        appendMessageDom(body);
      });
    });
  }

  // ================== 미리보기 ==================
  function addPreview(file) {
    var id  = 'f' + Math.random().toString(36).slice(2, 9);
    var url = URL.createObjectURL(file);
    pendingFiles.push({ id: id, file: file, url: url });

    var chip = document.createElement('div');
    chip.className = 'chip';
    chip.dataset.id = id;

    var img = document.createElement('img');
    img.src = url;
    img.alt = '선택한 이미지';

    var close = document.createElement('button');
    close.type = 'button';
    close.textContent = '×';
    close.setAttribute('aria-label', '삭제');
    close.addEventListener('click', function () {
      removePreview(id);
    });

    chip.appendChild(img);
    chip.appendChild(close);
    previews.appendChild(chip);
  }
  function removePreview(id) {
    var idx = pendingFiles.findIndex(function (p) { return p.id === id; });
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
    previews.innerHTML = '';
  }

  // ================== DOM에 메시지 추가 ==================
  function appendMessageDom(dto) {
    // 일반회원 화면 기준:
    // 내가 보낸(MEMBER) → 오른쪽 .user
    // 변호사가 보낸(LAWYER) → 왼쪽 .lawyer
    var isMe = dto.senderType && dto.senderType.toUpperCase() === 'MEMBER';

    var row = document.createElement('div');
    row.className = 'msg-row ' + (isMe ? 'user' : 'lawyer');

    if (isMe) {
      var meta1 = document.createElement('div');
      meta1.className = 'meta';
      meta1.textContent = formatTime(dto.chatRegDate);
      row.appendChild(meta1);
    }

    if (dto.chatContent) {
      var bubble = document.createElement('div');
      bubble.className = 'bubble';
      var inner = document.createElement('div');
      inner.textContent = dto.chatContent;
      bubble.appendChild(inner);
      row.appendChild(bubble);
    }

    if (!isMe) {
      var meta2 = document.createElement('div');
      meta2.className = 'meta';
      meta2.textContent = formatTime(dto.chatRegDate);
      row.appendChild(meta2);
    }

    // 첨부 이미지
    if (dto.attachments && dto.attachments.length > 0) {
      var media = document.createElement('div');
      media.className = 'media';
      dto.attachments.forEach(function (att) {
        var a = document.createElement('a');
        a.href = '/chat/attachment/' + att.attachmentId;
        a.target = '_blank';
        var im = document.createElement('img');
        im.src = '/chat/attachment/' + att.attachmentId;
        im.alt = att.fileName || '첨부';
        a.appendChild(im);
        media.appendChild(a);
      });
      row.appendChild(media);
    }

    msgs.appendChild(row);
    scrollBottom();
  }

  // ================== 전송 ==================
  function sendMessage() {
    var text = (input.value || '').trim();
    if (!text && pendingFiles.length === 0) return;
    if (!roomId) {
      alert('방 정보가 없습니다.');
      return;
    }

    var fd = new FormData();
    fd.append('roomId', roomId);
    fd.append('senderType', senderType); // MEMBER
    fd.append('senderId', senderId);
    fd.append('content', text);

    pendingFiles.forEach(function (p) {
      fd.append('files', p.file);
    });

    fetch('/chat/messages', {
      method: 'POST',
      body: fd
    })
      .then(function (res) { return res.json(); })
      .then(function (dto) {
        appendMessageDom(dto);
        input.value = '';
        clearPreviews();

        if (stompClient && stompClient.connected) {
          stompClient.send('/app/chat/' + roomId, {}, JSON.stringify(dto));
        }
      })
      .catch(function (err) {
        console.error(err);
        alert('메시지 전송 실패');
      });
  }

  // ================== 남은 시간 표시 ==================
  function startRemainTimer() {
    var el = document.getElementById('remainTime');
    if (!el) return;
    var exp = expiresEl ? expiresEl.value : el.getAttribute('data-expires-at');
    if (!exp) return;

    var target = new Date(exp).getTime();

    function tick() {
      var now = Date.now();
      var diff = target - now;
      if (diff <= 0) {
        el.textContent = '상담시간이 종료되었습니다';
        return;
      }
      var m = Math.floor(diff / 1000 / 60);
      var s = Math.floor(diff / 1000) % 60;
      el.textContent = '남은시간 ' + String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
      setTimeout(tick, 1000);
    }
    tick();
  }

  // ================== 라이트박스 ==================
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

  // ================== 이벤트 바인딩 ==================
  if (input) {
    input.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });
  }
  if (sendBtn) {
    sendBtn.addEventListener('click', sendMessage);
  }
  if (upBtn && fileInput) {
    upBtn.addEventListener('click', function () { fileInput.click(); });
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

  connectWs();
  startRemainTimer();
  scrollBottom();
})();
