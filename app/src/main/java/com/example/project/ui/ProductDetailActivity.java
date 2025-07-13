package com.example.project.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.example.project.model.CartItem;
import com.example.project.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    TextView txtName, txtDescription, txtPrice;
    ImageView imgProduct;
    Button btnAddToCart;

    String name, description, imageUrl, productId;
    double price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        txtName = findViewById(R.id.txtDetailName);
        txtDescription = findViewById(R.id.txtDetailDescription);
        txtPrice = findViewById(R.id.txtDetailPrice);
        imgProduct = findViewById(R.id.imgDetailProduct);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Lấy dữ liệu từ Intent
        productId = getIntent().getStringExtra("id");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish(); // Dừng activity tránh crash
            return;
        }

        name = getIntent().getStringExtra("name");
        description = getIntent().getStringExtra("description");
        price = getIntent().getDoubleExtra("price", 0.0);
        imageUrl = getIntent().getStringExtra("imageUrl");



        // Set dữ liệu lên view
        txtName.setText(name);
        txtDescription.setText(description);
        txtPrice.setText("$" + price);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image) // thêm placeholder
                    .error(R.drawable.placeholder_image)
                    .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.placeholder_image);
        }


        // Tạo object Product
        Product product = new Product(productId, name, description, price, imageUrl);

        // Gán sự kiện thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> addToCart(product));
    }

    private void addToCart(Product product) {
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

                    Toast.makeText(ProductDetailActivity.this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    CartItem newItem = new CartItem(
                            product.getId(),
                            product.getName(),
                            product.getImageUrl(),
                            product.getPrice(),
                            1
                    );
                    cartRef.setValue(newItem);
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
