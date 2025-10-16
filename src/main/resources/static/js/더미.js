// 실험용 지워질 예정

document.addEventListener('DOMContentLoaded', () => {
    // 버튼 요소들을 ID를 이용해 선택
    const freeConsultHero = document.getElementById('free-consult-hero');
    const viewServicesHero = document.getElementById('view-services-hero');
    const freeConsultCta = document.getElementById('free-consult-cta');
    const callConsultCta = document.getElementById('call-consult-cta');
    const serviceLinks = document.querySelectorAll('.service-link');

    // '무료 상담 신청' 버튼 클릭 시 알림 표시
    if (freeConsultHero) {
        freeConsultHero.addEventListener('click', () => {
            alert('무료 상담을 신청합니다.');
        });
    }

    if (freeConsultCta) {
        freeConsultCta.addEventListener('click', () => {
            alert('무료 상담을 신청합니다.');
        });
    }

    // '서비스 둘러보기' 버튼 클릭 시 Services 섹션으로 부드럽게 스크롤
    if (viewServicesHero) {
        viewServicesHero.addEventListener('click', (e) => {
            const servicesSection = document.querySelector('.services');
            if (servicesSection) {
                servicesSection.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    // '전화 상담' 버튼 클릭 시 알림 표시
    if (callConsultCta) {
        callConsultCta.addEventListener('click', () => {
            alert('전화 상담을 시도합니다: 1588-0000');
        });
    }

    // 서비스 카드 '자세히 보기' 링크 클릭 시 알림 표시
    serviceLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault(); 
            const serviceName = link.previousElementSibling.previousElementSibling.textContent;
            alert(`"${serviceName}" 서비스에 대한 자세한 정보 페이지로 이동합니다.`);
        });
    });
});