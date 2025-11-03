(function(){
  var scope=document.querySelector('.page-lChating');
  var msgs=scope.querySelector('#msgs'), input=scope.querySelector('#inputMsg'), send=scope.querySelector('#btnSend');
  var upBtn=scope.querySelector('#btnUpload'), fileInput=scope.querySelector('#fileUpload'), previews=scope.querySelector('#previews');
  var pending=[]; function uid(){ return 'f'+Math.random().toString(36).slice(2,9); }
  function scrollBottom(){ msgs.scrollTop=msgs.scrollHeight; }
  function setSendDisabled(){ send.disabled=false; }

  function addPreview(file){
    var id=uid(), url=URL.createObjectURL(file); pending.push({file:file, url:url, id:id});
    var chip=document.createElement('div'); chip.className='chip'; chip.dataset.id=id;
    var img=document.createElement('img'); img.src=url; img.alt='선택한 이미지 미리보기';
    var close=document.createElement('button'); close.type='button'; close.setAttribute('aria-label','삭제'); close.textContent='×';
    close.addEventListener('click', function(){ removePreview(id); });
    chip.appendChild(img); chip.appendChild(close); previews.appendChild(chip);
  }
  function removePreview(id){
    var idx=pending.findIndex(function(x){return x.id===id;}); if(idx>=0){ URL.revokeObjectURL(pending[idx].url); pending.splice(idx,1); }
    var chip=previews.querySelector('.chip[data-id="'+id+'"]'); if(chip){ chip.remove(); } setSendDisabled();
  }
  function clearPreviews(){ pending.forEach(function(p){ URL.revokeObjectURL(p.url); }); pending=[]; previews.innerHTML=''; }

  function addMyMessage(text, images){
    if(!text && (!images || images.length===0)){ return; }
    var row=document.createElement('div'); row.className='msg-row user';
    var meta=document.createElement('div'); meta.className='meta';
    var now=new Date(), hh=String(now.getHours()).padStart(2,'0'), mm=String(now.getMinutes()).padStart(2,'0');
    meta.textContent=hh+':'+mm+(images&&images.length?' · 이미지':'');
    row.appendChild(meta);

    if(images && images.length){
      var media=document.createElement('div'); media.className='media';
      images.forEach(function(src){ var im=document.createElement('img'); im.src=src; im.alt='전송 이미지'; media.appendChild(im); });
      row.appendChild(media);
    }
    if(text){
      var bubble=document.createElement('div'); bubble.className='bubble';
      var p=document.createElement('div'); p.textContent=text; bubble.appendChild(p);
      row.appendChild(bubble);
    }
    msgs.appendChild(row); scrollBottom();
  }

  input.addEventListener('input', setSendDisabled);
  send.addEventListener('click', function(){
    var t=(input.value||'').trim(), imgs=pending.map(function(p){return p.url;});
    addMyMessage(t, imgs); input.value=''; clearPreviews(); setSendDisabled();
  });
  input.addEventListener('keydown', function(e){ if((e.ctrlKey||e.metaKey)&&e.key==='Enter'){ send.click(); }});
  upBtn.addEventListener('click', function(){ fileInput.click(); });
  fileInput.addEventListener('change', function(){
    if(fileInput.files && fileInput.files.length){ var max=6; Array.from(fileInput.files).slice(0,max-pending.length).forEach(addPreview); setSendDisabled(); fileInput.value=''; }
  });

  // 라이트박스
  var viewer=scope.querySelector('#viewer'), vImg=scope.querySelector('#viewerImg');
  var btnClose=scope.querySelector('#btnClose'), btnFit=scope.querySelector('#btnFit');
  var btnPrev=scope.querySelector('#btnPrev'), btnNext=scope.querySelector('#btnNext');
  var group=[], idx=-1, fit=true;

  function openViewer(images, startIndex){
    group=images; idx=startIndex; fit=true; viewer.classList.remove('fit-off'); btnFit.textContent='100%';
    viewer.setAttribute('aria-hidden','false'); updateViewer();
  }
  function closeViewer(){
    viewer.setAttribute('aria-hidden','true'); vImg.src=''; group=[]; idx=-1;
  }
  function updateViewer(){
    vImg.src=group[idx]; btnPrev.disabled=(idx<=0); btnNext.disabled=(idx>=group.length-1);
  }
  function toggleFit(){
    fit=!fit; if(fit){ viewer.classList.remove('fit-off'); btnFit.textContent='100%'; }
    else{ viewer.classList.add('fit-off'); btnFit.textContent='맞춤'; }
  }

  msgs.addEventListener('click', function(e){
    var t=e.target;
    if(t.tagName==='IMG' && t.closest('.media')){
      var tiles=Array.prototype.slice.call(t.closest('.media').querySelectorAll('img')).map(function(img){ return img.src; });
      var start=tiles.indexOf(t.src);
      openViewer(tiles, Math.max(0,start));
    }
  });
  btnClose.addEventListener('click', closeViewer);
  btnFit.addEventListener('click', toggleFit);
  btnPrev.addEventListener('click', function(){ if(idx>0){ idx--; updateViewer(); }});
  btnNext.addEventListener('click', function(){ if(idx<group.length-1){ idx++; updateViewer(); }});
  viewer.addEventListener('click', function(e){ if(e.target===viewer || e.target.classList.contains('stage')) closeViewer(); });

  document.addEventListener('keydown', function(e){
    if(viewer.getAttribute('aria-hidden')==='true') return;
    if(e.key==='Escape') closeViewer();
    else if(e.key==='ArrowLeft' && idx>0){ idx--; updateViewer(); }
    else if(e.key==='ArrowRight' && idx<group.length-1){ idx++; updateViewer(); }
    else if(e.key==='f' || e.key==='F'){ toggleFit(); }
  });

  scrollBottom();
})();