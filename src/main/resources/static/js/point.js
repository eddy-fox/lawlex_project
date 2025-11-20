// 페이지 로드 대기
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

let tossPayments = null;
let paymentInstance = null;

function init() {
    console.log('=== 초기화 시작 ===');
    
    // 1. 토스페이먼츠 초기화
    try {
        if (typeof TossPayments === 'undefined') {
            console.error('TossPayments SDK 로드 실패');
            return;
        }
        
        const clientKey = "test_ck_oEjb0gm23P5GeZ2laN2WVpGwBJn5";
        const customerKey = window.btoa(Math.random()).slice(0, 20);
        tossPayments = TossPayments(clientKey);
        paymentInstance = tossPayments.payment({ customerKey });
        console.log('✅ 토스 초기화 완료');
    } catch (e) {
        console.error('토스 초기화 실패:', e);
    }
    
    // 2. 탭 전환 이벤트
    const pointTab = document.getElementById('pointHistoryTab');
    const paymentTab = document.getElementById('paymentHistoryTab');
    const pointDiv = document.getElementById('pointHistory');
    const paymentDiv = document.getElementById('paymentHistory');
    
    if (pointTab && paymentTab && pointDiv && paymentDiv) {
        pointTab.onclick = function() {
            console.log('포인트 탭 클릭');
            pointTab.classList.add('active');
            paymentTab.classList.remove('active');
            pointDiv.style.display = 'block';
            paymentDiv.style.display = 'none';
        };
        
        paymentTab.onclick = function() {
            console.log('결제 탭 클릭');
            pointTab.classList.remove('active');
            paymentTab.classList.add('active');
            pointDiv.style.display = 'none';
            paymentDiv.style.display = 'block';
        };
        console.log('✅ 탭 이벤트 등록 완료');
    } else {
        console.error('탭 요소 없음:', {pointTab, paymentTab, pointDiv, paymentDiv});
    }
    
    // 3. 결제 버튼 이벤트
    const payBtn = document.getElementById('payment-button');
    if (payBtn) {
        payBtn.onclick = handlePayment;
        console.log('✅ 결제 버튼 이벤트 등록 완료');
    } else {
        console.error('결제 버튼 없음');
    }
}

async function handlePayment(e) {
    e.preventDefault();
    console.log('=== 결제 시작 ===');
    
    // 1. 선택된 상품 확인
    const selected = document.querySelector('input[name="selectedProduct"]:checked');
    if (!selected) {
        alert('상품을 선택해주세요');
        return;
    }
    
    const productIdx = selected.value;
    const price = selected.dataset.price;
    const content = selected.dataset.content;
    console.log('선택 상품:', {productIdx, price, content});
    
    // 2. 동의 확인
    const agree = document.getElementById('agree');
    if (!agree || !agree.checked) {
        alert('결제 주의사항에 동의해주세요');
        return;
    }
    
    // 3. 회원 정보
    const memberData = document.getElementById('member-data');
    if (!memberData) {
        alert('회원 정보를 찾을 수 없습니다');
        return;
    }
    
    const memberIdx = memberData.dataset.memberIdx;
    const memberEmail = memberData.dataset.memberEmail;
    const memberName = memberData.dataset.memberName;
    const memberPhone = memberData.dataset.memberPhone;
    
    const phone = (memberPhone || '').replace(/\D/g, '');
    if (phone.length < 10) {
        alert('올바른 전화번호가 아닙니다');
        return;
    }
    
    console.log('회원:', {memberIdx, memberEmail, memberName, phone});
    
    try {
        // 4. 주문 생성
        const orderId = 'order-' + Date.now();
        console.log('주문 생성:', orderId);
        
        const res = await fetch('/member/point/prepare', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                productIdx: parseInt(productIdx),
                orderId: orderId,
                memberIdx: parseInt(memberIdx)
            })
        });
        
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || '주문 생성 실패');
        }
        
        console.log('주문 생성 완료');
        
        // 5. 결제 요청
        if (!paymentInstance) {
            throw new Error('결제 시스템 초기화 안됨');
        }
        
        const amount = parseInt(price.replace(/[^0-9]/g, ''));
        console.log('결제 요청:', {amount, orderId, content});
        
        await paymentInstance.requestPayment({
            method: "CARD",
            amount: {
                currency: "KRW",
                value: amount
            },
            orderId: orderId,
            orderName: content,
            successUrl: window.location.origin + "/payment/success",
            failUrl: window.location.origin + "/payment/fail",
            customerEmail: memberEmail || '',
            customerName: memberName || '',
            customerMobilePhone: phone,
            card: {
                useEscrow: false,
                flowMode: "DEFAULT",
                useCardPoint: false,
                useAppCardOnly: false
            }
        });
        
        console.log('결제창 호출 완료');
        
    } catch (error) {
        console.error('결제 오류:', error);
        alert('결제 중 오류: ' + error.message);
    }
}