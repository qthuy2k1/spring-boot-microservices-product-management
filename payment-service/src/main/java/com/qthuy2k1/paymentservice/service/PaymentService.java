package com.qthuy2k1.paymentservice.service;

import com.qthuy2k1.paymentservice.config.VNPayConfig;
import com.qthuy2k1.paymentservice.dto.OrderPaymentRequest;
import com.qthuy2k1.paymentservice.dto.PaymentResponse;
import com.qthuy2k1.paymentservice.dto.TransactionStatusResponse;
import com.qthuy2k1.paymentservice.dto.UpdatePaidOrderRequest;
import com.qthuy2k1.paymentservice.model.PaymentModel;
import com.qthuy2k1.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, UpdatePaidOrderRequest> updatePaidOrderRequestKafkaTemplate;

    public PaymentResponse getUrl(OrderPaymentRequest orderPaymentRequest) {
        long amount = orderPaymentRequest.getAmount().longValue() * 100;

        // unique --> use order id
        String vnp_TxnRef = orderPaymentRequest.getOrderId().toString();
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_BankCode", "VNBANK");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_Locale", "vn");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
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
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("Ok");
        paymentResponse.setMessage("Successfully");
        paymentResponse.setURL(paymentUrl);

        return paymentResponse;
    }

    public TransactionStatusResponse transaction(
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
        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        if (respCode.equals("00")) {
            transactionStatusResponse.setStatus("Ok");
            transactionStatusResponse.setMessage("Successfully");

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

            paymentRepository.save(paymentModel);
            // Send a kafka message to order client
            updatePaidOrderRequestKafkaTemplate.send("update-status-order", new UpdatePaidOrderRequest(true));
        } else {
            log.info("CODE: {}", respCode);
            transactionStatusResponse.setStatus("No");
            transactionStatusResponse.setMessage("Failed");
        }

        return transactionStatusResponse;
    }
}
