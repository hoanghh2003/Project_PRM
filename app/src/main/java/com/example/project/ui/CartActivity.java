package com.example.project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapter.CartAdapter;
import com.example.project.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtTotal;
    private Button btnCheckout;

    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;

    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        txtTotal = findViewById(R.id.txtTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems();

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, BillingActivity.class);
            intent.putExtra("total", totalPrice);
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://productsaleapp-65d39-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId)
                .child("cart");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                totalPrice = 0.0;

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    try {
                        String id = itemSnap.child("id").getValue(String.class);
                        String name = itemSnap.child("name").getValue(String.class);
                        String imageUrl = itemSnap.child("imageUrl").getValue(String.class);
                        Double price = itemSnap.child("price").getValue(Double.class);
                        Integer quantity = itemSnap.child("quantity").getValue(Integer.class);

                        if (price == null) price = 0.0;
                        if (quantity == null) quantity = 1;

                        CartItem item = new CartItem(id, name, imageUrl, price, quantity);
                        cartItems.add(item);
                        totalPrice += price * quantity;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                txtTotal.setText("Tổng: $" + String.format("%.2f", totalPrice));
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtTotal.setText("Lỗi tải dữ liệu");
            }
        });
    }
}
