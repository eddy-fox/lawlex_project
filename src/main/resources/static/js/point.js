// 탭 활성 토글
document.querySelectorAll('.tabs .tab').forEach(tab=>{
    tab.addEventListener('click',()=>{
        if(tab.classList.contains('divider')) return;
        document.querySelectorAll('.tabs .tab').forEach(t=>t.classList.remove('active'));
        tab.classList.add('active');
    });
});

function point() {
    document.getElementById('point').style.display = 'block';
    document.getElementById('payment').style.display = 'none';
}

function payment() {
    document.getElementById('point').style.display = 'none';
    document.getElementById('payment').style.display = 'block';
}

const options = document.querySelectorAll('.point-option');
const productInput = document.getElementById('selectedProduct');

let selectedProductIdx = null;
let selectedPrice = null;
let selectedContent = null;
let tossPayments = null;
let paymentInstance = null;

// 포인트 선택
options.forEach(opt => {
    opt.addEventListener('click', () => {
        options.forEach(o => o.classList.remove('selected'));
        opt.classList.add('selected');
        
        selectedProductIdx = opt.dataset.product;
        selectedPrice = opt.dataset.price;
        selectedContent = opt.dataset.content;
        productInput.value = selectedProductIdx;
    });
});

// 첫번째 포인트 선택
window.addEventListener('DOMContentLoaded', () => {
    if (options.length > 0) {
        options[0].classList.add('selected');
        selectedProductIdx = options[0].dataset.product;
        selectedPrice = options[0].dataset.price;
        selectedContent = options[0].dataset.content;
        productInput.value = selectedProductIdx;
    }
    
    // 토스페이먼츠 초기화 (페이지 로드시 한번)
    initTossPayments();
});

// 토스페이먼츠 초기화 (API 개별 연동 방식)
function initTossPayments() {
    const clientKey = "test_ck_oEjb0gm23P5GeZ2laN2WVpGwBJn5";
    const customerKey = generateRandomString();
    tossPayments = TossPayments(clientKey);
    paymentInstance = tossPayments.payment({ customerKey });
}

function generateRandomString() {
    return window.btoa(Math.random()).slice(0, 20);
}

// 전화번호에서 특수문자 제거 (숫자만) - 더 안전하게
function formatPhoneNumber(phone) {
    if (!phone) {
        console.error('전화번호가 없습니다:', phone);
        return '';
    }
    
    // 문자열로 변환 후 숫자만 추출
    const str = String(phone);
    const onlyNumbers = str.replace(/\D/g, ''); // \D는 숫자가 아닌 모든 것
    
    console.log('전화번호 변환:', str, '->', onlyNumbers);
    
    return onlyNumbers;
}

// 결제하기 버튼 클릭
document.getElementById('payment-button').addEventListener('click', async function(e) {
    e.preventDefault();
    
    // 1. 상품 선택 확인
    if (!selectedProductIdx) {
        alert('결제할 상품을 선택해주세요.');
        return;
    }

    // 2. 동의 확인
    const agreeCheckbox = document.getElementById('agree');
    if (!agreeCheckbox.checked) {
        alert('결제 주의사항에 동의해주세요.');
        return;
    }

    try {
        // 3. 회원 정보 가져오기
        const memberDataDiv = document.getElementById('member-data');
        
        if (!memberDataDiv) {
            alert('회원 정보를 찾을 수 없습니다.');
            return;
        }
        
        const memberIdx = memberDataDiv.dataset.memberIdx;
        const memberEmail = memberDataDiv.dataset.memberEmail;
        const memberName = memberDataDiv.dataset.memberName;
        const memberPhone = memberDataDiv.dataset.memberPhone;
        
        console.log('=== 원본 회원 정보 ===');
        console.log('memberPhone (원본):', memberPhone);
        
        // 전화번호 포맷팅 - 먼저!
        const formattedPhone = formatPhoneNumber(memberPhone);
        
        console.log('=== 포맷팅 후 ===');
        console.log('formattedPhone:', formattedPhone);
        console.log('길이:', formattedPhone.length);
        
        if (!formattedPhone || formattedPhone.length < 10) {
            alert('올바른 전화번호 형식이 아닙니다.');
            return;
        }

        // 4. 서버에 주문 생성 요청
        const orderId = 'order-' + Date.now();
        
        const response = await fetch('/member/point/prepare', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                productIdx: parseInt(selectedProductIdx),
                orderId: orderId,
                memberIdx: parseInt(memberIdx)
            })
        });

        if (!response.ok) {
            throw new Error('주문 생성 실패');
        }

        // 5. 결제 요청 데이터 준비
        const paymentData = {
            method: "CARD",
            amount: {
                currency: "KRW",
                value: parseInt(selectedPrice.replace(/,/g, ''))
            },
            orderId: orderId,
            orderName: selectedContent,
            // ✅ /payment/ 유지
            successUrl: window.location.origin + "/payment/success",
            failUrl: window.location.origin + "/payment/fail",
            customerEmail: memberEmail || '',
            customerName: memberName || '',
            customerMobilePhone: formattedPhone,
            card: {
                useEscrow: false,
                flowMode: "DEFAULT",
                useCardPoint: false,
                useAppCardOnly: false,
            }
        };
        
        console.log('=== 최종 결제 요청 데이터 ===');
        console.log(JSON.stringify(paymentData, null, 2));

        // 6. 결제 요청
        await paymentInstance.requestPayment(paymentData);

    } catch (error) {
        console.error('결제 오류:', error);
        console.error('에러 상세:', JSON.stringify(error, null, 2));
        alert('결제 중 오류가 발생했습니다: ' + error.message);
    }
});