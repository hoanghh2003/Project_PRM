package com.example.project.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.example.project.payment.NetworkUtils;
import com.example.project.payment.VnpayUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class BillingActivity extends AppCompatActivity {

    private TextView txtBillingTotal;
    private Button   btnConfirmPayment;
    private double   totalAmount;

    /* ---- CẤU HÌNH VNPAY SANDBOX ---- */
    private static final String VNP_TMNCODE    = "7OENGDF3";
    private static final String VNP_HASH       = "CR9DZLQ3HHLBEUGOMKKGU2FZ59CSAMZ4";
    private static final String VNP_BASE_URL   = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL = "https://yourapp.com/return";
    /* --------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        txtBillingTotal   = findViewById(R.id.txtBillingTotal);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        totalAmount = getIntent().getDoubleExtra("total", 0.0);
        txtBillingTotal.setText("Tổng: " + String.format("%,.0f", totalAmount) + " VND");

        btnConfirmPayment.setOnClickListener(v -> handleVnpayPayment());
    }

    private void handleVnpayPayment() {
        try {
            long amount = (long) (totalAmount * 100);               // ×100
            String txnRef = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 18);

            /* Thời gian GMT+7 */
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            fmt.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String createDate = fmt.format(new Date());
            String expireDate = fmt.format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));

            String ipAddr = NetworkUtils.getIPAddress();

            /* Build URL */
            VnpayUtil vnp = new VnpayUtil();
            vnp.addRequestData("vnp_Version",   "2.1.0");
            vnp.addRequestData("vnp_Command",   "pay");
            vnp.addRequestData("vnp_TmnCode",   VNP_TMNCODE);
            vnp.addRequestData("vnp_Amount",    String.valueOf(amount));
            vnp.addRequestData("vnp_CreateDate", createDate);
            vnp.addRequestData("vnp_ExpireDate", expireDate);
            vnp.addRequestData("vnp_CurrCode",  "VND");
            vnp.addRequestData("vnp_IpAddr",    ipAddr);
            vnp.addRequestData("vnp_Locale",    "vn");
            vnp.addRequestData("vnp_OrderInfo", "Thanh toán đơn hàng #" + txnRef);
            vnp.addRequestData("vnp_OrderType", "other");
            vnp.addRequestData("vnp_ReturnUrl", VNP_RETURN_URL);
            vnp.addRequestData("vnp_TxnRef",    txnRef);

            String payUrl = vnp.createRequestUrl(VNP_BASE_URL, VNP_HASH);

            /* Log để đối chiếu */
            Log.d("VNPAY", "CreateDate: " + createDate);
            Log.d("VNPAY", "ExpireDate: " + expireDate);
            Log.d("VNPAY", "TxnRef    : " + txnRef);
            Log.d("VNPAY", "IpAddr    : " + ipAddr);
            Log.d("VNPAY", "PayURL    : " + payUrl);

            /* Mở VNPAY */
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)));

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this,
                    "Lỗi tạo link thanh toán: " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
