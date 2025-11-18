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

    // 포인트 선택
    options.forEach(opt => {
        opt.addEventListener('click', () => {
            options.forEach(o => o.classList.remove('selected'));
            opt.classList.add('selected');
            productInput.value = opt.dataset.product;
        });
    });

    // 첫번째 포인트 선택
    window.addEventListener('DOMContentLoaded', () => {
        options[0].classList.add('selected');
        productInput.value = options[0].dataset.product;
    });

    // 동의 체크박스 확인
    document.getElementById('payment-form').addEventListener('submit', function(e) {
        const agree = document.getElementById('agree');
        if (!agree.checked) {
            alert("결제 주의사항에 동의해주세요.");
            e.preventDefault();
            return;
        }
    });