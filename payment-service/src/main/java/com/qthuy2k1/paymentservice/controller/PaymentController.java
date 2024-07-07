package com.qthuy2k1.paymentservice.controller;

import com.qthuy2k1.paymentservice.config.Config;
import com.qthuy2k1.paymentservice.dto.OrderPaymentDTO;
import com.qthuy2k1.paymentservice.dto.PaymentResDTO;
import com.qthuy2k1.paymentservice.dto.TransactionStatusDTO;
import com.qthuy2k1.paymentservice.model.PaymentModel;
import com.qthuy2k1.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody OrderPaymentDTO orderPaymentDTO) throws UnsupportedEncodingException {
        long amount = orderPaymentDTO.getAmount().longValue() * 100;

        // unique --> use order id
        String vnp_TxnRef = orderPaymentDTO.getOrderId().toString();
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_Locale", "vn");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        PaymentResDTO paymentResDTO = new PaymentResDTO();
        paymentResDTO.setStatus("Ok");
        paymentResDTO.setMessage("Successfully");
        paymentResDTO.setURL(paymentUrl);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResDTO);
    }

    @GetMapping("/payment-info")
    public ResponseEntity<?> transaction(
            @RequestParam("vnp_Amount") String amount,
            @RequestParam("vnp_BankCode") String bankCode,
            @RequestParam("vnp_BankTranNo") String bankTranNo,
            @RequestParam("vnp_CardType") String cardType,
            @RequestParam("vnp_PayDate") String payDate,
            @RequestParam("vnp_OrderInfo") String orderInfo,
            @RequestParam("vnp_TransactionNo") BigDecimal transactionNo,
            @RequestParam("vnp_TransactionStatus") Integer transactionStatus,
            @RequestParam("vnp_TxnRef") Integer orderID,
            @RequestParam("vnp_ResponseCode") String respCode
    ) {
        TransactionStatusDTO transactionStatusDTO = new TransactionStatusDTO();
        if (respCode.equals("00")) {
            transactionStatusDTO.setStatus("Ok");
            transactionStatusDTO.setMessage("Successfully");

            PaymentModel paymentModel = new PaymentModel();
            paymentModel.setAmount(Long.parseLong(amount) / 100);
            paymentModel.setBankCode(bankCode);
            paymentModel.setBankTranNo(bankTranNo);
            paymentModel.setCardType(cardType);
            paymentModel.setOrderInfo(orderInfo);
            paymentModel.setTransactionNo(transactionNo);
            paymentModel.setTransactionStatus(transactionStatus);
            paymentModel.setOrderID(orderID);


            int year = Integer.parseInt(payDate.substring(0, 4));
            int month = Integer.parseInt(payDate.substring(4, 6));
            int dayOfMonth = Integer.parseInt(payDate.substring(6, 8));
            int hour = Integer.parseInt(payDate.substring(8, 10));
            int minute = Integer.parseInt(payDate.substring(10, 12));
            int second = Integer.parseInt(payDate.substring(12, 14));
            paymentModel.setPayDate(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second));

            paymentService.createPayment(paymentModel);
        } else {
            log.info("CODE: {}", respCode);
            transactionStatusDTO.setStatus("No");
            transactionStatusDTO.setMessage("Failed");
        }

        return ResponseEntity.status(HttpStatus.OK).body(transactionStatusDTO);
    }
}
