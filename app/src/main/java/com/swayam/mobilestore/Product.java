package com.swayam.mobilestore;

public class Product {
    private int id;
    private String category;
    private double price;
    private String model;
    private String summary;
    private String imagePath;
    private int quantity;

    public Product(int id, String category, double price, String model, String summary,String imagePath) {
        this.id = id;
        this.category = category;
        this.price = price;
        this.model = model;
        this.summary = summary;
        this.imagePath = imagePath;
    }

    public void addQuantity(int quantity){
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getModel() {
        return model;
    }

    public String getSummary() {
        return summary;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getQuantity() {
        return quantity;
    }
}
