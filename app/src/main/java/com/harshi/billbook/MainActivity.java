
package com.harshi.billbook;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import com.google.zxing.*;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.*;
import java.text.*;
import java.io.*;

public class MainActivity extends Activity {

    LinearLayout root, itemsLayout;
    EditText productName, productPrice, productQty;
    EditText discountInput, taxInput, upiInput;
    TextView totalText, subtotalText;
    SharedPreferences prefs;

    ArrayList<JSONObject> billItems = new ArrayList<>();
    double finalTotal = 0;
    String billText = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("BillBook", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,20,20,20);
        scroll.addView(root);
        setContentView(scroll);

        title("BILL BOOK");

        productName = input("Product name");
        productPrice = input("Price (₹)");
        productQty = input("Quantity");

        productQty.setText("1");

        button("ADD ITEM TO BILL", v -> addItem());

        itemsLayout = new LinearLayout(this);
        itemsLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsLayout);

        discountInput = input("Discount (%)");
        discountInput.setText("0");

        taxInput = input("Tax (%)");
        taxInput.setText("0");

        subtotalText = text("Subtotal: ₹0.00", 20);
        totalText = text("Total: ₹0.00", 25);

        button("CALCULATE BILL", v -> calculate());

        upiInput = input("Your UPI ID (example@upi)");

        button("GENERATE UPI QR", v -> generateQR());

        button("SAVE BILL", v -> saveBill());

        button("SHARE BILL", v -> shareBill());

        button("BILL HISTORY", v -> showHistory());

        button("TODAY'S SALES", v -> showSales());
    }

    void title(String s) {
        TextView t = text(s,30);
        root.addView(t);
    }

    TextView text(String s, int size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setPadding(0,12,0,12);
        return t;
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        root.addView(e);
        return e;
    }

    void button(String name, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(name);
        root.addView(b);
        b.setOnClickListener(listener);
    }

    double number(EditText e) {
        try {
            return Double.parseDouble(e.getText().toString());
        } catch(Exception ex) {
            return 0;
        }
    }

    void addItem() {
        String name = productName.getText().toString().trim();
        double price = number(productPrice);
        int qty = (int) number(productQty);

        if(name.isEmpty() || price <= 0 || qty <= 0) {
            toast("Enter valid product, price and quantity");
            return;
        }

        JSONObject item = new JSONObject();

        try {
            item.put("name",name);
            item.put("price",price);
            item.put("qty",qty);
        } catch(Exception ignored) {}

        billItems.add(item);

        TextView row = text(
            name + " × " + qty + " = ₹" +
            String.format(Locale.US,"%.2f",price*qty),18
        );

        itemsLayout.addView(row);

        productName.setText("");
        productPrice.setText("");
        productQty.setText("1");

        calculate();
    }

    double subtotal() {
        double sum = 0;

        for(JSONObject item : billItems) {
            sum += item.optDouble("price") *
                   item.optInt("qty");
        }

        return sum;
    }

    void calculate() {
        double sub = subtotal();
        double discount = Math.max(0,Math.min(100,number(discountInput)));
        double tax = Math.max(0,number(taxInput));

        double afterDiscount = sub * (1-discount/100);
        finalTotal = afterDiscount * (1+tax/100);

        subtotalText.setText(
            "Subtotal: ₹" +
            String.format(Locale.US,"%.2f",sub)
        );

        totalText.setText(
            "TOTAL: ₹" +
            String.format(Locale.US,"%.2f",finalTotal)
        );

        billText = buildBill();
    }

    String buildBill() {
        StringBuilder s = new StringBuilder();

        s.append("BILL BOOK\n");
        s.append("Date: ").append(date()).append("\n\n");

        for(JSONObject item : billItems) {
            double p = item.optDouble("price");
            int q = item.optInt("qty");

            s.append(item.optString("name"))
             .append(" × ").append(q)
             .append(" = ₹")
             .append(String.format(Locale.US,"%.2f",p*q))
             .append("\n");
        }

        s.append("\nSubtotal: ₹")
         .append(String.format(Locale.US,"%.2f",subtotal()));

        s.append("\nDiscount: ")
         .append(number(discountInput)).append("%");

        s.append("\nTax: ")
         .append(number(taxInput)).append("%");

        s.append("\nTOTAL: ₹")
         .append(String.format(Locale.US,"%.2f",finalTotal));

        return s.toString();
    }

    String date() {
        return new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",Locale.US
        ).format(new Date());
    }

    void saveBill() {
        if(billItems.isEmpty()) {
            toast("Add items first");
            return;
        }

        calculate();

        try {
            JSONArray history = new JSONArray(
                prefs.getString("history","[]")
            );

            JSONObject bill = new JSONObject();
            bill.put("date",date());
            bill.put("total",finalTotal);
            bill.put("items",new JSONArray(billItems.toString()));
            bill.put("text",billText);

            history.put(bill);

            prefs.edit()
                .putString("history",history.toString())
                .apply();

            toast("Bill saved successfully");

        } catch(Exception e) {
            toast("Could not save bill");
        }
    }

    void showHistory() {
        try {
            JSONArray history = new JSONArray(
                prefs.getString("history","[]")
            );

            StringBuilder s = new StringBuilder();

            for(int i=history.length()-1;i>=0;i--) {
                JSONObject bill = history.getJSONObject(i);

                s.append(bill.optString("date"))
                 .append("\n₹")
                 .append(String.format(Locale.US,
                     "%.2f",bill.optDouble("total")))
                 .append("\n\n");
            }

            new AlertDialog.Builder(this)
                .setTitle("Bill History")
                .setMessage(s.length()==0?"No bills saved":s.toString())
                .setPositiveButton("OK",null)
                .show();

        } catch(Exception e) {
            toast("History unavailable");
        }
    }

    void showSales() {
        try {
            JSONArray history = new JSONArray(
                prefs.getString("history","[]")
            );

            String today = new SimpleDateFormat(
                "yyyy-MM-dd",Locale.US
            ).format(new Date());

            double sales = 0;
            int count = 0;

            for(int i=0;i<history.length();i++) {
                JSONObject bill = history.getJSONObject(i);

                if(bill.optString("date").startsWith(today)) {
                    sales += bill.optDouble("total");
                    count++;
                }
            }

            new AlertDialog.Builder(this)
                .setTitle("Today's Sales")
                .setMessage(
                    "Bills: "+count+
                    "\nTotal sales: ₹"+
                    String.format(Locale.US,"%.2f",sales)
                )
                .setPositiveButton("OK",null)
                .show();

        } catch(Exception e) {
            toast("Sales unavailable");
        }
    }

    void generateQR() {
        if(finalTotal <= 0) {
            calculate();
        }

        String upi = upiInput.getText().toString().trim();

        if(upi.isEmpty()) {
            toast("Enter your UPI ID first");
            return;
        }

        try {
            String link =
                "upi://pay?pa="+Uri.encode(upi)+
                "&pn="+Uri.encode("Bill Book")+
                "&am="+String.format(Locale.US,"%.2f",finalTotal)+
                "&cu=INR";

            
Bitmap bitmap = new QRCodeWriter().encode(
    link, BarcodeFormat.QR_CODE,500,500
).let();


        } catch(Exception e) {
            toast("QR generation failed");
        }
    }

    void shareBill() {
        if(billItems.isEmpty()) {
            toast("Add items first");
            return;
        }

        calculate();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,billText);

        startActivity(Intent.createChooser(intent,"Share Bill"));
    }

    void toast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}
