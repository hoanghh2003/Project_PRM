package com.example.project.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.project.model.CartItem;
import com.example.project.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class CartManager {

    public static void addToCart(Context context, Product product) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference cartRef = FirebaseDatabase
                .getInstance("https://productsaleapp-65d39-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId)
                .child("cart")
                .child(product.getId());

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer currentQty = snapshot.child("quantity").getValue(Integer.class);
                    if (currentQty == null) currentQty = 0;
                    cartRef.child("quantity").setValue(currentQty + 1);
                    Toast.makeText(context, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    CartItem newItem = new CartItem(
                            product.getId(),
                            product.getName(),
                            product.getImageUrl(),
                            product.getPrice(),
                            1
                    );
                    cartRef.setValue(newItem);
                    Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
