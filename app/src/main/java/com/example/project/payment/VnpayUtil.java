package com.example.project.payment;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnpayUtil {

    /**
     * Tạo URL thanh toán VNPAY đầy đủ và đúng chuẩn
     *
     * @param amountStr     Số tiền (VND) dạng chuỗi, VD: "100000"
     * @param orderInfo     Thông tin đơn hàng hiển thị cho người dùng
     * @param orderId       Mã giao dịch/đơn hàng của hệ thống
     * @param vnp_TmnCode   Mã terminal của bạn được cấp bởi VNPAY
     * @param vnp_HashSecret Chuỗi ký bí mật được cấp bởi VNPAY
     * @param vnp_Url       URL thanh toán của VNPAY (sandbox hoặc live)
     * @param returnUrl     URL callback khi thanh toán xong
     * @return              Chuỗi URL để redirect người dùng đến cổng thanh toán
     */
    public static String buildCheckoutUrl(String amountStr, String orderInfo, String orderId,
                                          String vnp_TmnCode, String vnp_HashSecret,
                                          String vnp_Url, String returnUrl) {
        try {
            // ✅ Parse tiền: chuỗi → double → nhân 100 → long
            double amount = Double.parseDouble(amountStr);
            long vnpAmount = (long) (amount * 100); // VNPAY yêu cầu *100

            // ✅ Tạo danh sách tham số gửi sang VNPAY
            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", vnp_TmnCode);
            params.put("vnp_Amount", String.valueOf(vnpAmount));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", orderId);
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", "other"); // hoặc: "billpayment", "fashion", ...
            params.put("vnp_Locale", "vn"); // hoặc "en"
            params.put("vnp_ReturnUrl", returnUrl);
            params.put("vnp_IpAddr", "127.0.0.1"); // hoặc IP client thực

            // ✅ Tạo thời gian hiện tại theo định dạng yêu cầu
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String vnp_CreateDate = formatter.format(new Date());
            params.put("vnp_CreateDate", vnp_CreateDate);

            // ✅ Sắp xếp thứ tự key alphabet
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            // ✅ Tạo hashData (không encode) và query string (có encode)
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = params.get(key);

                // ⚠️ KHÔNG encode trong hashData
                hashData.append(key).append('=').append(value);

                // ✅ ENCODE trong query
                query.append(URLEncoder.encode(key, "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(value, "UTF-8"));

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }

            // ✅ Tạo chữ ký SHA512
            String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            // ✅ Gắn chữ ký vào query string
            query.append("&vnp_SecureHash=").append(secureHash);

            // ✅ Trả về URL hoàn chỉnh
            return vnp_Url + "?" + query.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo chữ ký SHA512 từ dữ liệu và key bí mật
     */
    private static String hmacSHA512(String key, String data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));

        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }
}
