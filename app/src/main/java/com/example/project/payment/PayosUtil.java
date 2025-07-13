package com.example.project.payment;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PayosUtil {

    // Cấu hình từ PayOS
    private static final String CLIENT_ID = "d1a80dc1-3451-46b6-b5f9-9590394c3829";
    private static final String API_KEY = "e784aa1d-abc0-4047-a19b-82dc9e89c11b";
    private static final String CHECKOUT_URL = "https://api-sandbox.payos.vn/v2/payment-requests";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface CreateOrderLinkCallback {
        void onSuccess(String paymentUrl);
        void onError(Exception e);
    }

    /**
     * Gọi đến PayOS API để tạo đơn và lấy checkoutUrl
     */
    public static void createOrder(
            int orderCode,
            long amount,
            String description,
            String cancelUrl,
            String returnUrl,
            CreateOrderLinkCallback cb
    ) {
        try {
            // Tạo payload JSON
            JSONObject body = new JSONObject();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);

            // Optional: expiredAt (30 phút sau)
            long now = System.currentTimeMillis() / 1000;
            body.put("expiredAt", now + 1800); // 30 phút tính bằng giây

            // Tạo danh sách item
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("name", "Gói dịch vụ #" + orderCode);
            item.put("quantity", 1);
            item.put("price", amount);
            items.put(item);
            body.put("items", items);

            // Tạo request
            OkHttpClient client = new OkHttpClient();
            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(CHECKOUT_URL)
                    .post(reqBody)
                    .addHeader("x-client-id", CLIENT_ID)
                    .addHeader("x-api-key", API_KEY)
                    .build();

            // Gửi request bất đồng bộ
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    cb.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    try {
                        JSONObject json = new JSONObject(resp);
                        JSONObject data = json.optJSONObject("data");

                        if (data != null) {
                            String paymentUrl = data.optString("checkoutUrl");
                            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                cb.onSuccess(paymentUrl);
                                return;
                            }
                        }

                        cb.onError(new Exception("Không có checkoutUrl trong phản hồi"));

                    } catch (Exception e) {
                        cb.onError(e);
                    }
                }
            });

        } catch (Exception e) {
            cb.onError(e);
        }
    }
}
