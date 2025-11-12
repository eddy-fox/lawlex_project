(function () {
  // ===== 기본 엘리먼트 =====
  var scope = document.querySelector('.page-lChating');
  if (!scope) return;

  var msgs      = scope.querySelector('#msgs');
  var input     = scope.querySelector('#inputMsg');
  var sendBtn   = scope.querySelector('#btnSend');
  var upBtn     = scope.querySelector('#btnUpload');
  var fileInput = scope.querySelector('#fileUpload');
  var previews  = scope.querySelector('#previews');

  // 서버에서 내려준 방/사용자 정보
  var roomId     = document.getElementById('roomId')     ? document.getElementById('roomId').value : null;
  var senderType = document.getElementById('senderType') ? document.getElementById('senderType').value : 'LAWYER';
  var senderIdEl = document.getElementById('senderId');
  var senderId   = senderIdEl ? senderIdEl.value : null;

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
    if (!roomId) return;
    // SockJS, Stomp는 html에서 불러왔다고 가정
    var socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    // 로그 싫으면 끄자
    stompClient.debug = null;

    stompClient.connect({}, function () {
      // /topic/chat/{roomId} 구독
      stompClient.subscribe('/topic/chat/' + roomId, function (message) {
        var body = JSON.parse(message.body);
        // 서버가 ChatdataDTO 형태로 보내준다고 가정
        appendMessageDom(body);
      });
    });
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
    previews.innerHTML = '';
  }

  // ======================================================
  // 3. 메시지 DOM에 추가 (서버/내가 보낸 거 공통)
  // ======================================================
  function appendMessageDom(msg) {
    // msg: ChatdataDTO
    // 변호사 화면이니까 내가(LAWYER) 보낸 건 오른쪽(user), member가 보낸 건 왼쪽(lawyer)
    var isMe = msg.senderType && msg.senderType.toUpperCase() === 'LAWYER';

    var row = document.createElement('div');
    row.className = 'msg-row ' + (isMe ? 'user' : 'lawyer');

    // 내가 보낸 거면 시간 먼저
    if (isMe) {
      var meta1 = document.createElement('div');
      meta1.className = 'meta';
      meta1.textContent = formatTime(msg.chatRegDate);
      row.appendChild(meta1);
    }

    // 텍스트 내용
    if (msg.chatContent) {
      var bubble = document.createElement('div');
      bubble.className = 'bubble';
      var contentDiv = document.createElement('div');
      contentDiv.textContent = msg.chatContent;
      bubble.appendChild(contentDiv);
      row.appendChild(bubble);
    }

    // 상대가 보낸 거면 시간 뒤에
    if (!isMe) {
      var meta2 = document.createElement('div');
      meta2.className = 'meta';
      meta2.textContent = formatTime(msg.chatRegDate);
      row.appendChild(meta2);
    }

    // 첨부 이미지가 있으면 media로
    if (msg.attachments && msg.attachments.length > 0) {
      var media = document.createElement('div');
      media.className = 'media';
      msg.attachments.forEach(function (att) {
        var a = document.createElement('a');
        a.href = '/chat/attachment/' + att.attachmentId;
        a.target = '_blank';
        var im = document.createElement('img');
        im.src = '/chat/attachment/' + att.attachmentId;
        im.alt = att.fileName || '첨부파일';
        a.appendChild(im);
        media.appendChild(a);
      });
      row.appendChild(media);
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
    formData.append('roomId', roomId);
    formData.append('senderType', senderType); // "LAWYER"
    formData.append('senderId', senderId);
    formData.append('content', text);

    // 첨부
    pendingFiles.forEach(function (p) {
      formData.append('files', p.file);
    });

    fetch('/chat/messages', {
      method: 'POST',
      body: formData
    })
      .then(function (res) { return res.json(); })
      .then(function (dto) {
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
        console.error(err);
        alert('메시지 전송에 실패했습니다.');
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
      // 엔터로 전송 (쉬프트+엔터는 줄바꿈으로 남겨두고 싶으면 조건 바꾸면 됨)
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
  connectWs();
  startRemainTimer();
  scrollBottom();
})();
