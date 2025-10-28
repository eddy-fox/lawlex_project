(function(){
  const telInput=document.getElementById('tel_g');
  const reqBtn=document.querySelector('.phone-flex .verify-btn');
  const codeInput=document.getElementById('code_g');
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

  const gPhotoInput=document.getElementById('gPhotoInput');
  const gPhotoPreview=document.getElementById('gPhotoPreview');
  const gPhotoRemoveBtn=document.getElementById('gPhotoRemoveBtn');

  if(gPhotoInput){
    gPhotoInput.addEventListener('change',function(){
      const file=this.files&&this.files[0];
      if(!file)return;
      const reader=new FileReader();
      reader.onload=function(e){
        gPhotoPreview.innerHTML='<img src="'+e.target.result+'" alt="미리보기">';
      };
      reader.readAsDataURL(file);
    });
  }

  if(gPhotoRemoveBtn){
    gPhotoRemoveBtn.addEventListener('click',function(){
      gPhotoInput.value='';
      gPhotoPreview.innerHTML='<span>사진</span>';
    });
  }
})();