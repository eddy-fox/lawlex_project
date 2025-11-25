// admin.js
document.addEventListener("DOMContentLoaded", function() {
  const currentPath = window.location.pathname;
  const sideLinks = document.querySelectorAll('.sideMenu a');

  sideLinks.forEach(link => {
    const href = link.getAttribute('href');

    // 🔹 광고 관리 관련 경로 묶기
    const adPaths = [
      '/admin/adManagement',
      '/admin/adInfo',
      '/admin/adRegistration',
      '/admin/adModify'
    ];

    // 🔹 회원 관리 관련 경로 묶기
    const memberPaths = [
      '/admin/memberManagement',
      '/admin/lawyerManagement'
    ];

    // 🔹 현재 링크가 광고 관리 항목이면, 위 목록 중 하나라도 URL에 포함될 때 활성화
    if (href.includes('/admin/adManagement')) {
      if (adPaths.some(path => currentPath.includes(path))) {
        link.classList.add('side-choice');
        link.classList.remove('side');
      } else {
        link.classList.remove('side-choice');
        link.classList.add('side');
      }
    } 
    // 🔹 현재 링크가 회원 관리 항목이면, 위 목록 중 하나라도 URL에 포함될 때 활성화
    else if (href.includes('/admin/memberManagement')) {
      if (memberPaths.some(path => currentPath.includes(path))) {
        link.classList.add('side-choice');
        link.classList.remove('side');
      } else {
        link.classList.remove('side-choice');
        link.classList.add('side');
      }
    }
    // 🔹 그 외 메뉴는 기존 방식 유지
    else if (currentPath.includes(href)) {
      link.classList.add('side-choice');
      link.classList.remove('side');
    } else {
      link.classList.remove('side-choice');
      link.classList.add('side');
    }
  });
});