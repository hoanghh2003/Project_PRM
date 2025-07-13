package com.example.project.payment;

import android.util.Log;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Utility ký và tạo URL thanh toán VNPAY (SHA‑512). */
public class VnpayUtil {

    private final SortedMap<String, String> data = new TreeMap<>();

    public void addRequestData(String key, String value) {
        if (value != null && !value.isEmpty()) data.put(key, value);
    }

    /** Tạo URL redirect kèm vnp_SecureHash. */
    public String createRequestUrl(String baseUrl, String secretKey) throws Exception {
        /* 1. build chuỗi hashData (đúng thứ tự key) */

        StringBuilder hashData = new StringBuilder();
        Iterator<SortedMap.Entry<String, String>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            SortedMap.Entry<String, String> e = it.next();
            hashData.append(e.getKey()).append('=')
                    .append(URLEncoder.encode(e.getValue(), "UTF-8")
                            .replace("+", "%20"));      // giữ đúng %20
            if (it.hasNext()) hashData.append('&');
        }
        /* 2. tính HMAC‑SHA512 */
        String secureHash = hmacSHA512(secretKey, hashData.toString());

        /* 3. build query string (URL‑encode; đổi + → %20) */
        StringBuilder query = new StringBuilder();
        for (SortedMap.Entry<String, String> e : data.entrySet()) {
            query.append(URLEncoder.encode(e.getKey(), "UTF-8"))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), "UTF-8").replace("+", "%20"))
                    .append('&');
        }
        query.append("vnp_SecureHashType=SHA512")
                .append("&vnp_SecureHash=")
                .append(URLEncoder.encode(secureHash, "UTF-8"));

        /* Log hashData để debug */
        Log.d("VNPAY", "hashData: " + hashData);

        return baseUrl + '?' + query;
    }

    /* HMAC‑SHA512 → HEX uppercase */
    private String hmacSHA512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512"));
        byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
