
package com.harshi.billbook;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout layout;
    EditText name, price, quantity, discount, tax;
    TextView total;
    ArrayList<String> products = new ArrayList<>();
    ArrayList<Double> prices = new ArrayList<>();
    SharedPreferences storage;
    double subtotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = getSharedPreferences("BillBook", MODE_PRIVATE);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.WHITE);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("BILL BOOK");
        title.setTextSize(28);
        layout.addView(title);

        name = new EditText(this);
        name.setHint("Product name");
        layout.addView(name);

        price = new EditText(this);
        price.setHint("Product price");
        price.setInputType(8194);
        layout.addView(price);

        Button add = new Button(this);
        add.setText("Add Product");
        layout.addView(add);

        add.setOnClickListener(v -> addProduct());

        quantity = new EditText(this);
        quantity.setHint("Quantity");
        quantity.setInputType(2);
        layout.addView(quantity);

        discount = new EditText(this);
        discount.setHint("Discount percentage");
        discount.setInputType(8194);
        layout.addView(discount);

        tax = new EditText(this);
        tax.setHint("Tax percentage");
        tax.setInputType(8194);
        layout.addView(tax);

        total = new TextView(this);
        total.setTextSize(22);
        layout.addView(total);

        Button calculate = new Button(this);
        calculate.setText("Calculate Bill");
        layout.addView(calculate);

        calculate.setOnClickListener(v -> calculateBill());

        loadProducts();
        showProducts();
    }

    void addProduct() {
        String product = name.getText().toString().trim();

        double amount = number(price);

        if (product.isEmpty() || amount <= 0) {
            message("Enter valid product details");
            return;
        }

        products.add(product);
        prices.add(amount);

        saveProducts();
        showProducts();

        name.setText("");
        price.setText("");

        message("Product added");
    }

    void showProducts() {
        for (int i = 0; i < products.size(); i++) {
            TextView item = new TextView(this);

            item.setText(
                products.get(i) + " - ₹" +
                String.format(Locale.US, "%.2f", prices.get(i))
            );

            item.setTextSize(18);
            layout.addView(item);
        }
    }

    void calculateBill() {

        int q = (int) number(quantity);

        if (q <= 0) q = 1;

        if (prices.isEmpty()) {
            message("Add a product first");
            return;
        }

        subtotal = prices.get(0) * q;

        double d = number(discount);
        double t = number(tax);

        double amount =
            subtotal * (1 - d / 100) * (1 + t / 100);

        total.setText(
            "Total: ₹" +
            String.format(Locale.US, "%.2f", amount)
        );
    }

    double number(EditText field) {
        try {
            return Double.parseDouble(
                field.getText().toString()
            );
        } catch (Exception e) {
            return 0;
        }
    }

    void saveProducts() {
        JSONArray array = new JSONArray();

        for (int i = 0; i < products.size(); i++) {
            JSONObject item = new JSONObject();

            try {
                item.put("name", products.get(i));
                item.put("price", prices.get(i));
                array.put(item);
            } catch (Exception ignored) {}
        }

        storage.edit()
            .putString("products", array.toString())
            .apply();
    }

    void loadProducts() {
        try {
            JSONArray array = new JSONArray(
                storage.getString("products", "[]")
            );

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                products.add(item.getString("name"));
                prices.add(item.getDouble("price"));
            }
        } catch (Exception ignored) {}
    }

    void message(String text) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show();
    }
}
