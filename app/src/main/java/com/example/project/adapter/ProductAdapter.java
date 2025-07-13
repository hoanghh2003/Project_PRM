package com.example.project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Product;
import com.example.project.ui.ProductDetailActivity;
import com.example.project.utils.CartManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>implements Filterable {

    private final List<Product> originalList;  // dữ liệu gốc (luôn giữ nguyên)
    private final List<Product> displayList;
    private final Context context;

    public ProductAdapter(List<Product> list,Context context) {
        this.originalList = new ArrayList<>(list);;
        this.displayList  = new ArrayList<>(list);
        this.context = context;
    }
    public void setData(List<Product> newData) {
        originalList.clear();
        originalList.addAll(newData);

        displayList.clear();
        displayList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return displayList != null ? displayList.size() : 0;
    }
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = displayList.get(position);
        if (product != null) {
            holder.bind(product,context);
        }
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence keyword) {
                List<Product> filtered = new ArrayList<>();
                if (keyword == null || keyword.length() == 0) {
                    filtered.addAll(originalList);
                } else {
                    String q = keyword.toString().toLowerCase(Locale.ROOT);
                    for (Product p : originalList) {
                        if (p.getName().toLowerCase(Locale.ROOT).contains(q)) {
                            filtered.add(p);
                        }
                    }
                }
                FilterResults r = new FilterResults();
                r.values = filtered;
                return r;
            }
            @Override
            protected void publishResults(CharSequence s, FilterResults r) {
                displayList.clear();
                //noinspection unchecked
                displayList.addAll((List<Product>) r.values);
                notifyDataSetChanged();
            }
        };
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice;
        ImageView imgProduct, btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        public void bind(Product product, Context context) {
            txtName.setText(product.getName());
            txtPrice.setText("$" + product.getPrice());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Picasso.get().load(product.getImageUrl()).into(imgProduct);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);

                // ✅ BỔ SUNG DÒNG DƯỚI ĐỂ TRÁNH LỖI KHÔNG CÓ ID
                intent.putExtra("id", product.getId());

                intent.putExtra("name", product.getName());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("imageUrl", product.getImageUrl());

                v.getContext().startActivity(intent);
            });
            btnAddToCart.setOnClickListener(v -> {
                CartManager.addToCart(context, product);
                Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
