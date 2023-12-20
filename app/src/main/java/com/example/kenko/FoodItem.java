package com.example.kenko;

public class FoodItem {
    private int quantity;
    private double cal;
    private double protein;
    private double fat;
    private double carb;
    private double fiber;
    public FoodItem(int qty, double calories, double pro, double fatAmt, double carbAmt, double fbr) {
        quantity = qty;
        cal = calories;
        protein = pro;
        fat = fatAmt;
        carb = carbAmt;
        fiber = fbr;
    }

    public String getData() {
        return "# of servings: "+quantity+"\nPer Serving:\n\nCalories: "+cal+"kcal\nProtein: "+protein+"g\nFat: "+fat+"g\nCarbohydrates: "+carb+"g\nFiber: "+fiber+"g";
    }

    public String getOnlyData() {
        return quantity+" "+cal+" "+protein+" "+fat+" "+carb+" "+fiber;
    }

}