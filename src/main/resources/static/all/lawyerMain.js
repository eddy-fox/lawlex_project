    const acceptModal = document.getElementById('confirmModalAccept');
    const rejectModal = document.getElementById('confirmModalReject');
    const caseAccept = document.getElementById('caseAccept');
    const caseReject = document.getElementById('caseReject');
    let currentCaseId = null;

    document.querySelectorAll('.open-accept').forEach(btn => {
      btn.addEventListener('click', () => {
        currentCaseId = btn.dataset.case || null;
        caseAccept.textContent = currentCaseId ? ('선택된 케이스: ' + currentCaseId) : '';
        acceptModal.setAttribute('aria-hidden','false'); }); });

    document.querySelectorAll('.open-reject').forEach(btn => {
      btn.addEventListener('click', () => {
        currentCaseId = btn.dataset.case || null;
        caseReject.textContent = currentCaseId ? ('선택된 케이스: ' + currentCaseId) : '';
        rejectModal.setAttribute('aria-hidden','false'); }); });

    function closeModal(which){
      const target = which === 'accept' ? acceptModal : rejectModal;
      target.setAttribute('aria-hidden','true');
      currentCaseId = null; }

    document.querySelectorAll('[data-close="accept"]').forEach(b => b.addEventListener('click', () => closeModal('accept')));
    document.querySelectorAll('[data-close="reject"]').forEach(b => b.addEventListener('click', () => closeModal('reject')));

    document.querySelectorAll('[data-ok="accept"]').forEach(b => b.addEventListener('click', () => closeModal('accept')));
    document.querySelectorAll('[data-ok="reject"]').forEach(b => b.addEventListener('click', () => closeModal('reject')));

    acceptModal.addEventListener('click', (e)=>{ if(e.target === acceptModal) closeModal('accept'); });
    rejectModal.addEventListener('click', (e)=>{ if(e.target === rejectModal) closeModal('reject'); });

    document.addEventListener('keydown', (e)=>{
      if(e.key === 'Escape'){
        if(acceptModal.getAttribute('aria-hidden') === 'false') closeModal('accept');
        if(rejectModal.getAttribute('aria-hidden') === 'false') closeModal('reject'); } });