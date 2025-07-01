package com.example.project.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.example.project.payment.VnpayUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BillingActivity extends AppCompatActivity {

    private TextView txtBillingTotal;
    private Button btnConfirmPayment;
    private double totalAmount = 0.0;

    // ⚠️ Thay thế bằng return URL của bạn (bắt buộc có trên cổng VNPAY sandbox)
    private static final String VNP_RETURN_URL = "https://yourapp.com/return";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        txtBillingTotal = findViewById(R.id.txtBillingTotal);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // ✅ Lấy tổng tiền từ Intent
        totalAmount = getIntent().getDoubleExtra("total", 0.0);
        txtBillingTotal.setText("Tổng: " + String.format("%,.0f", totalAmount) + " VND");

        // ✅ Bắt sự kiện nút thanh toán
        btnConfirmPayment.setOnClickListener(v -> handleVnpayPayment());
    }

    private void handleVnpayPayment() {
        try {
            // ✅ Chuyển tiền sang định dạng chuỗi, không có phần thập phân
            String amount = String.valueOf((long) totalAmount); // VD: 15000

            // ✅ Tạo mã giao dịch (random theo thời gian)
            String orderId = String.valueOf(System.currentTimeMillis());

            // ✅ Thông tin hiển thị trên VNPAY
            String orderInfo = "Thanh toán đơn hàng #" + orderId;

            // ✅ Thông tin VNPAY sandbox
            String vnp_TmnCode = "7OENGDF3";
            String vnp_HashSecret = "CR9DZLQ3HHLBEUGOMKKGU2FZ59CSAMZ4";
            String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

            // ✅ Gọi hàm tạo URL thanh toán
            String paymentUrl = VnpayUtil.buildCheckoutUrl(
                    amount,
                    orderInfo,
                    orderId,
                    vnp_TmnCode,
                    vnp_HashSecret,
                    vnp_Url,
                    VNP_RETURN_URL
            );

            if (paymentUrl != null) {
                // ✅ Mở trình duyệt để thanh toán
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Không thể tạo liên kết thanh toán.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ✅ OPTIONAL: Xóa giỏ hàng sau khi thanh toán (chỉ gọi sau khi xác minh callback)
     */
    private void clearCartAfterPayment() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userCartRef = FirebaseDatabase
                .getInstance("https://productsaleapp-65d39-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId)
                .child("cart");

        userCartRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(BillingActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(BillingActivity.this, "Lỗi khi xử lý thanh toán.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
