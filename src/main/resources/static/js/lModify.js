(function(){
  const telInput=document.getElementById('tel_l');
  const reqBtn=document.querySelector('.phone-flex .verify-btn');
  const codeInput=document.getElementById('code_l');
  const confirmBtn=document.querySelector('.verify-row .verify-btn.confirm');
  let requested=false,verified=false;

  reqBtn.addEventListener('click',function(){
    if(verified)return;
    requested=true;
    telInput.readOnly=true;
    reqBtn.disabled=true;
    reqBtn.textContent='요청됨';
  });

  confirmBtn.addEventListener('click',function(){
    if(!requested||verified)return;
    const codeVal=codeInput.value.trim();
    if(!codeVal){alert('인증번호를 입력하세요');return;}
    verified=true;
    confirmBtn.disabled=true;
    confirmBtn.textContent='인증완료';
    telInput.readOnly=true;
    reqBtn.disabled=true;
    reqBtn.textContent='완료됨';
  });

  telInput.addEventListener('input',function(){
    requested=false;
    verified=false;
    reqBtn.disabled=false;
    reqBtn.textContent='인증 요청';
    confirmBtn.disabled=false;
    confirmBtn.textContent='확인';
    codeInput.value='';
  });

  const addBtn=document.getElementById('addTime');
  const startEl=document.getElementById('timeStart');
  const endEl=document.getElementById('timeEnd');
  const dayWrap=document.getElementById('dayCheckboxes');
  const listEl=document.getElementById('selectedList');

  function getCheckedDays(){
    return Array.from(dayWrap.querySelectorAll('input[type="checkbox"]:checked')).map(c=>c.value);
  }

  function addSlot(){
    const days=getCheckedDays(),s=startEl.value,e=endEl.value;
    if(!days.length||!s||!e)return;
    const tag=document.createElement('span');
    tag.className='tag-slot';
    const label=document.createElement('span');
    label.textContent=days.join(', ')+' '+s+'~'+e;
    const removeBtn=document.createElement('button');
    removeBtn.className='tag-remove';
    removeBtn.type='button';
    removeBtn.innerHTML='✕';
    removeBtn.addEventListener('click',()=>tag.remove());
    tag.append(label,removeBtn);
    listEl.append(tag);
    startEl.value='';
    endEl.value='';
    dayWrap.querySelectorAll('input[type="checkbox"]').forEach(c=>c.checked=false);
  }

  if(addBtn) addBtn.addEventListener('click',addSlot);

  const photoInput=document.getElementById('photoInput');
  const photoPreview=document.getElementById('photoPreview');
  const photoRemoveBtn=document.getElementById('photoRemoveBtn');

  if(photoInput){
    photoInput.addEventListener('change',function(){
      const file=this.files&&this.files[0];
      if(!file)return;
      const reader=new FileReader();
      reader.onload=function(e){
        photoPreview.innerHTML='<img src="'+e.target.result+'" alt="미리보기">';
      };
      reader.readAsDataURL(file);
    });
  }

  if(photoRemoveBtn){
    photoRemoveBtn.addEventListener('click',function(){
      photoInput.value='';
      photoPreview.innerHTML='<span>사진</span>';
    });
  }
})();