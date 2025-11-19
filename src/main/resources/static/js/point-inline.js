console.log('=== point-inline.js 로드됨 ===');

let tossPayments = null;
let paymentInstance = null;
let tossInitialized = false;

window.addEventListener('DOMContentLoaded', function() {
    console.log('✅ DOMContentLoaded');

    registerTabs();
    initPaymentButton();
    initTossPayments();
    console.log('=== 초기화 완료 ===');
});

function initTossPayments() {
    if (tossInitialized && paymentInstance) {
        return true;
    }

    if (typeof TossPayments === 'undefined') {
        console.warn('⚠️ TossPayments SDK가 로드되지 않았습니다.');
        return false;
    }

    try {
        const clientKey = "test_ck_oEjb0gm23P5GeZ2laN2WVpGwBJn5";
        const customerKey = window.btoa(Math.random()).slice(0, 20);
        tossPayments = TossPayments(clientKey);
        paymentInstance = tossPayments.payment({ customerKey });
        tossInitialized = true;
        console.log('✅ 토스 초기화 완료');
        return true;
    } catch (error) {
        console.error('❌ 토스 초기화 실패:', error);
        return false;
    }
}

function registerTabs() {
    const pointTab = document.getElementById('pointHistoryTab');
    const paymentTab = document.getElementById('paymentHistoryTab');
    const pointHistory = document.getElementById('pointHistory');
    const paymentHistory = document.getElementById('paymentHistory');

    if (!pointTab || !paymentTab || !pointHistory || !paymentHistory) {
        console.warn('⚠️ 탭 요소를 찾을 수 없습니다.');
        return;
    }

    pointTab.onclick = function() {
        console.log('📊 포인트 탭');
        pointTab.classList.add('active');
        paymentTab.classList.remove('active');
        pointHistory.style.display = 'block';
        paymentHistory.style.display = 'none';
    };

    paymentTab.onclick = function() {
        console.log('💳 결제 탭');
        pointTab.classList.remove('active');
        paymentTab.classList.add('active');
        pointHistory.style.display = 'none';
        paymentHistory.style.display = 'block';
    };

    console.log('✅ 탭 이벤트 등록');
}

function initPaymentButton() {
    const paymentButton = document.getElementById('payment-button');
    if (!paymentButton) {
        console.warn('⚠️ 결제 버튼을 찾을 수 없습니다.');
        return;
    }

    paymentButton.onclick = async function(e) {
        e.preventDefault();
        console.log('=== 💳 결제 시작 ===');

        if (!paymentInstance && !initTossPayments()) {
            alert('결제 모듈이 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.');
            return;
        }

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
                    productIdx: parseInt(selected.value, 10),
                    orderId: orderId,
                    memberIdx: parseInt(memberIdx, 10)
                })
            });

            if (!res.ok) throw new Error('주문 생성 실패');

            console.log('✅ 주문 생성 완료');

            await paymentInstance.requestPayment({
                method: "CARD",
                amount: {
                    currency: "KRW",
                    value: parseInt(selected.dataset.price.replace(/[^0-9]/g, ''), 10)
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
}