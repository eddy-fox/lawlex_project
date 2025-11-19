console.log('=== point-inline.js 로드됨 ===');

let tossPayments = null;
let paymentInstance = null;

window.addEventListener('DOMContentLoaded', function() {
    console.log('✅ DOMContentLoaded');
    
    // 토스페이먼츠 초기화
    if (typeof TossPayments === 'undefined') {
        console.error('❌ TossPayments SDK 없음');
        return;
    }
    
    const clientKey = "test_ck_oEjb0gm23P5GeZ2laN2WVpGwBJn5";
    const customerKey = window.btoa(Math.random()).slice(0, 20);
    tossPayments = TossPayments(clientKey);
    paymentInstance = tossPayments.payment({ customerKey });
    console.log('✅ 토스 초기화 완료');
    
    // 탭 전환
    document.getElementById('pointHistoryTab').onclick = function() {
        console.log('📊 포인트 탭');
        this.classList.add('active');
        document.getElementById('paymentHistoryTab').classList.remove('active');
        document.getElementById('pointHistory').style.display = 'block';
        document.getElementById('paymentHistory').style.display = 'none';
    };
    
    document.getElementById('paymentHistoryTab').onclick = function() {
        console.log('💳 결제 탭');
        document.getElementById('pointHistoryTab').classList.remove('active');
        this.classList.add('active');
        document.getElementById('pointHistory').style.display = 'none';
        document.getElementById('paymentHistory').style.display = 'block';
    };
    
    console.log('✅ 탭 이벤트 등록');
    
    // 결제 버튼
    document.getElementById('payment-button').onclick = async function(e) {
        e.preventDefault();
        console.log('=== 💳 결제 시작 ===');
        
        const selected = document.querySelector('input[name="selectedProduct"]:checked');
        if (!selected) {
            alert('상품을 선택해주세요');
            return;
        }
        
        const agree = document.getElementById('agree');
        if (!agree.checked) {
            alert('결제 주의사항에 동의해주세요');
            return;
        }
        
        const memberData = document.getElementById('member-data');
        const memberIdx = memberData.dataset.memberIdx;
        const memberEmail = memberData.dataset.memberEmail;
        const memberName = memberData.dataset.memberName;
        const memberPhone = (memberData.dataset.memberPhone || '').replace(/\D/g, '');
        
        const orderId = 'order-' + Date.now();
        
        try {
            const res = await fetch('/member/point/prepare', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    productIdx: parseInt(selected.value),
                    orderId: orderId,
                    memberIdx: parseInt(memberIdx)
                })
            });
            
            if (!res.ok) throw new Error('주문 생성 실패');
            
            console.log('✅ 주문 생성 완료');
            
            await paymentInstance.requestPayment({
                method: "CARD",
                amount: {
                    currency: "KRW",
                    value: parseInt(selected.dataset.price.replace(/[^0-9]/g, ''))
                },
                orderId: orderId,
                orderName: selected.dataset.content,
                successUrl: window.location.origin + "/payment/success",
                failUrl: window.location.origin + "/payment/fail",
                customerEmail: memberEmail,
                customerName: memberName,
                customerMobilePhone: memberPhone,
                card: {
                    useEscrow: false,
                    flowMode: "DEFAULT",
                    useCardPoint: false,
                    useAppCardOnly: false
                }
            });
            
            console.log('✅ 결제창 호출');
            
        } catch (error) {
            console.error('❌ 에러:', error);
            alert('결제 중 오류: ' + error.message);
        }
    };
    
    console.log('✅ 결제 버튼 이벤트 등록');
    console.log('=== 초기화 완료 ===');
});