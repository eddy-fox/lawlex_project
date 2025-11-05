    // 아코디언 기능
    document.querySelectorAll('.faq-question').forEach(question => {
      question.addEventListener('click', () => {
        const item = question.parentElement;
        const isActive = item.classList.contains('active');
        
        // 모든 아이템 닫기
        document.querySelectorAll('.faq-item').forEach(i => {
          i.classList.remove('active');
        });
        
        // 클릭한 아이템만 열기 (이미 열려있던 것은 닫기)
        if (!isActive) {
          item.classList.add('active');
        }
      });
    });