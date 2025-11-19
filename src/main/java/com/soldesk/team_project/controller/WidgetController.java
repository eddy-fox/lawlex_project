package com.soldesk.team_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.soldesk.team_project.config.TossProperties;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.service.PurchaseService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequestMapping("/payment")  // ✅ 이거 유지!
@RequiredArgsConstructor
public class WidgetController {

    private final TossProperties tossProperties;
    private final PurchaseService purchaseService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody, HttpServletResponse response) throws Exception {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
            
            // ✅ 받은 데이터 로그
            logger.info("=== 결제 승인 요청 데이터 ===");
            logger.info("paymentKey: {}", paymentKey);
            logger.info("orderId: {}", orderId);
            logger.info("amount: {}", amount);
            
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // 구매 요청 검증
        PurchaseDTO purchase = purchaseService.getOrderInfo(orderId);
        if (purchase == null) {
            JSONObject error = new JSONObject();
            error.put("message", "존재하지 않는 주문입니다.");
            error.put("code", "NOT_FOUND_ORDER");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        String productPrice = purchaseService.getProductPrice(purchase.getProductIdx());
        
        // 숫자만 추출해서 비교
        String cleanAmount = amount.replaceAll("[^0-9]", "");
        String cleanProductPrice = productPrice.replaceAll("[^0-9]", "");
        
        if (!cleanAmount.equals(cleanProductPrice)) {
            JSONObject error = new JSONObject();
            error.put("message", "결제 금액이 일치하지 않습니다.");
            error.put("code", "AMOUNT_MISMATCH");
            error.put("expected", cleanProductPrice);
            error.put("received", cleanAmount);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", cleanAmount);  // ✅ 숫자만 전송
        obj.put("paymentKey", paymentKey);
        
        // ✅ 토스에 보낼 데이터 로그
        logger.info("=== 토스 API 요청 데이터 ===");
        logger.info("Request Body: {}", obj.toString());

        String widgetSecretKey = tossProperties.getSecretKey();

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;
        
        // ✅ 토스 API 응답 로그
        logger.info("=== 토스 API 응답 ===");
        logger.info("Response Code: {}", code);
        logger.info("Success: {}", isSuccess);

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();
        
        // ✅ 토스 응답 내용 로그
        logger.info("Response Body: {}", jsonObject.toString());

        JSONObject result = new JSONObject();
        if (isSuccess) {
            purchaseService.purchasePoint(purchase.getMemberIdx(), purchase.getProductIdx());
            purchaseService.updatePurchaseStatus(orderId, "success");

            result.put("status", "success");
            result.put("message", "결제가 성공했습니다.");
            result.put("data", jsonObject);
        } else {
            purchaseService.updatePurchaseStatus(orderId, "fail");

            result.put("status", "fail");
            result.put("message", "결제에 실패했습니다.");
            result.put("data", jsonObject);
            
            // ✅ 실패 상세 로그
            logger.error("=== 결제 실패 상세 ===");
            logger.error("Error Code: {}", jsonObject.get("code"));
            logger.error("Error Message: {}", jsonObject.get("message"));
        }

        return ResponseEntity.status(code).body(result);
    }

    /**
     * 인증성공처리
     */
    @GetMapping("/success")
    public String paymentSuccess(HttpServletRequest request, Model model) throws Exception {
        logger.info("결제 성공 페이지 진입");
        return "payment/success";
    }

    /**
     * 인증실패처리
     */
    @GetMapping("/fail")
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");
        
        logger.info("결제 실패: code={}, message={}", failCode, failMessage);

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "payment/fail";
    }
}