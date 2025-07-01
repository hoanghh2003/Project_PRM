package com.example.project.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();

    public LiveData<List<Product>> getProductList() {
        return productList;
    }

    public void fetchProducts() {
        DatabaseReference ref = FirebaseDatabase.getInstance(
                "https://productsaleapp-65d39-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("products");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                productList.setValue(products);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Optional: Xử lý lỗi nếu có
            }
        });
    }
}
