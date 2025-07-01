package com.example.project.model;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    public Product() {}

    public Product(String id, String name, String description, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getter bắt buộc cho Firebase
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }

    public void setId(String id) {
        this.id = id;
    }

}
