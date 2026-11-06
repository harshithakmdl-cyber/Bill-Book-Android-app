package com.harshi.billbook;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.*;

import java.text.*;
import java.util.*;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends Activity {

    LinearLayout root, itemsBox;

    EditText customer, phone, product, price, qty;
    EditText discount, tax, upi;

    TextView totalView, summary;

    ArrayList<Item> items = new ArrayList<>();

    SharedPreferences prefs;

    double lastTotal = 0;

    class Item {

        String name;
        double price;
        int qty;

        Item(String n, double p, int q) {
            name = n;
            price = p;
            qty = q;
        }

        double total() {
            return price * qty;
        }

        JSONObject json() throws JSONException {

            JSONObject o = new JSONObject();

            o.put("name", name);
            o.put("price", price);
            o.put("qty", qty);

            return o;
        }
    }

    @Override
    public void onCreate(Bundle b) {

        super.onCreate(b);

        prefs = getSharedPreferences(
                "billbook",
                MODE_PRIVATE
        );

        buildUI();
    }

    int dp(float v) {

        return (int) (
                v * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    TextView text(String s, int size) {

        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(30, 40, 50));

        t.setPadding(
                0,
                dp(6),
                0,
                dp(6)
        );

        return t;
    }

    EditText field(String hint) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(16);

        return e;
    }

    Button button(String label, Runnable action) {

        Button b = new Button(this);

        b.setText(label);

        b.setOnClickListener(v -> action.run());

        return b;
    }

    void buildUI() {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);

        root.setOrientation(1);

        root.setPadding(
                dp(18),
                dp(14),
                dp(18),
                dp(24)
        );

        root.setBackgroundColor(
                Color.rgb(247, 249, 252)
        );

        scroll.addView(root);

        setContentView(scroll);

        root.addView(text("BILL BOOK", 26));

        root.addView(
                text("Create bills • Save sales • Share", 14)
        );

        customer = field("Customer name");

        phone = field("Customer phone (optional)");

        root.addView(customer);
        root.addView(phone);

        root.addView(text("Add product", 18));

        product = field("Product name");

        price = field("Price (₹)");

        qty = field("Quantity");

        qty.setText("1");

        root.addView(product);
        root.addView(price);
        root.addView(qty);

        root.addView(
                button("＋ Add product", () -> addItem())
        );

        itemsBox = new LinearLayout(this);

        itemsBox.setOrientation(1);

        root.addView(itemsBox);

        discount = field("Discount (%)");

        discount.setText("0");

        tax = field("Tax (%)");

        tax.setText("0");

        upi = field("UPI ID (for payment QR)");

        root.addView(discount);
        root.addView(tax);
        root.addView(upi);

        totalView = text("Total: ₹0.00", 22);

        root.addView(totalView);

        summary = text("No products added", 14);

        root.addView(summary);

        root.addView(
                button("Save bill", () -> saveBill())
        );

        root.addView(
                button("Share bill", () -> shareBill())
        );

        root.addView(
                button("Show UPI QR", () -> showQR())
        );

        root.addView(
                button("Bill history", () -> history())
        );

        root.addView(
                button("Today's sales", () -> dailySales())
        );

        root.addView(
                button("Clear current bill", () -> clearBill())
        );
    }

    void addItem() {

        String n = product.getText()
                .toString()
                .trim();

        if (n.isEmpty()) {

            toast("Enter product name");

            return;
        }

        try {

            double p = Double.parseDouble(
                    price.getText().toString()
            );

            int q = Integer.parseInt(
                    qty.getText().toString()
            );

            if (p < 0 || q < 1) {

                throw new Exception();
            }

            items.add(
                    new Item(n, p, q)
            );

            product.setText("");

            price.setText("");

            qty.setText("1");

            renderItems();

        } catch (Exception e) {

            toast("Enter a valid price and quantity");
        }
    }

    void renderItems() {

        itemsBox.removeAllViews();

        for (int i = 0; i < items.size(); i++) {

            final int index = i;

            Item it = items.get(i);

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            TextView t = text(
                    it.name
                            + "  ×"
                            + it.qty
                            + "   ₹"
                            + money(it.total()),
                    15
            );

            t.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
                            1
                    )
            );

            row.addView(t);

            Button x = new Button(this);

            x.setText("Remove");

            x.setOnClickListener(v -> {

                items.remove(index);

                renderItems();
            });

            row.addView(x);

            itemsBox.addView(row);
        }

        updateTotal();
    }

    double number(EditText e) {

        try {

            return Double.parseDouble(
                    e.getText().toString()
            );

        } catch (Exception ex) {

            return 0;
        }
    }

    double subtotal() {

        double s = 0;

        for (Item i : items) {

            s += i.total();
        }

        return s;
    }

    double total() {

        double s = subtotal();

        s -= s * Math.max(
                0,
                Math.min(100, number(discount))
        ) / 100;

        s += s * Math.max(
                0,
                number(tax)
        ) / 100;

        return Math.max(0, s);
    }

    void updateTotal() {

        lastTotal = total();

        totalView.setText(
                "Total: ₹" + money(lastTotal)
        );

        summary.setText(
                "Items: "
                        + items.size()
                        + "   Subtotal: ₹"
                        + money(subtotal())
        );
    }

    String money(double d) {

        return String.format(
                Locale.US,
                "%.2f",
                d
        );
    }

    String billText() {

        StringBuilder s = new StringBuilder("BILL BOOK\n");

        s.append("Date: ")
                .append(
                        new SimpleDateFormat(
                                "dd-MM-yyyy HH:mm",
                                Locale.getDefault()
                        ).format(new Date())
                )
                .append("\n");

        s.append("Customer: ")
                .append(customer.getText())
                .append("\n");

        if (phone.getText().length() > 0) {

            s.append("Phone: ")
                    .append(phone.getText())
                    .append("\n");
        }

        s.append("--------------------\n");

        for (Item i : items) {

            s.append(i.name)
                    .append("  ")
                    .append(i.qty)
                    .append(" × ₹")
                    .append(money(i.price))
                    .append(" = ₹")
                    .append(money(i.total()))
                    .append("\n");
        }

        s.append("Subtotal: ₹")
                .append(money(subtotal()))
                .append("\n");

        s.append("Discount: ")
                .append(money(number(discount)))
                .append("%\n");

        s.append("Tax: ")
                .append(money(number(tax)))
                .append("%\n");

        s.append("TOTAL: ₹")
                .append(money(total()));

        return s.toString();
    }

    void saveBill() {

        if (items.isEmpty()) {

            toast("Add at least one product");

            return;
        }

        try {

            JSONArray all = new JSONArray(
                    prefs.getString("bills", "[]")
            );

            JSONObject bill = new JSONObject();

            bill.put(
                    "date",
                    System.currentTimeMillis()
            );

            bill.put(
                    "customer",
                    customer.getText().toString()
            );

            bill.put(
                    "total",
                    total()
            );

            bill.put(
                    "text",
                    billText()
            );

            JSONArray arr = new JSONArray();

            for (Item i : items) {

                arr.put(i.json());
            }

            bill.put("items", arr);

            all.put(bill);

            prefs.edit()
                    .putString("bills", all.toString())
                    .apply();

            toast("Bill saved offline");

        } catch (Exception e) {

            toast("Could not save bill");
        }
    }

    void shareBill() {

        if (items.isEmpty()) {

            toast("Add products first");

            return;
        }

        Intent i = new Intent(
                Intent.ACTION_SEND
        );

        i.setType("text/plain");

        i.putExtra(
                Intent.EXTRA_SUBJECT,
                "Bill Book receipt"
        );

        i.putExtra(
                Intent.EXTRA_TEXT,
                billText()
        );

        startActivity(
                Intent.createChooser(
                        i,
                        "Share bill"
                )
        );
    }

    void showQR() {

        String id = upi.getText()
                .toString()
                .trim();

        if (id.isEmpty()) {

            toast("Enter your UPI ID first");

            return;
        }

        try {

            String link =
                    "upi://pay?pa="
                            + java.net.URLEncoder.encode(
                                    id,
                                    "UTF-8"
                            )
                            + "&pn=BillBook"
                            + "&am="
                            + money(total())
                            + "&cu=INR";

            BitMatrix matrix =
                    new QRCodeWriter().encode(
                            link,
                            BarcodeFormat.QR_CODE,
                            600,
                            600
                    );

            Bitmap bmp = Bitmap.createBitmap(
                    600,
                    600,
                    Bitmap.Config.RGB_565
            );

            for (int x = 0; x < 600; x++) {

                for (int y = 0; y < 600; y++) {

                    bmp.setPixel(
                            x,
                            y,
                            matrix.get(x, y)
                                    ? Color.BLACK
                                    : Color.WHITE
                    );
                }
            }

            ImageView image = new ImageView(this);

            image.setImageBitmap(bmp);

            new AlertDialog.Builder(this)
                    .setTitle("UPI payment QR")
                    .setView(image)
                    .setPositiveButton(
                            "Close",
                            null
                    )
                    .show();

        } catch (Exception e) {

            toast(
                    "QR generation failed. Check ZXing dependency."
            );
        }
    }

    void history() {

        try {

            JSONArray a = new JSONArray(
                    prefs.getString("bills", "[]")
            );

            StringBuilder s = new StringBuilder();

            for (int i = a.length() - 1; i >= 0; i--) {

                JSONObject b = a.getJSONObject(i);

                s.append(
                        new SimpleDateFormat(
                                "dd-MM-yyyy HH:mm",
                                Locale.getDefault()
                        ).format(
                                new Date(
                                        b.optLong("date")
                                )
                        )
                );

                s.append("\nCustomer: ")
                        .append(
                                b.optString(
                                        "customer",
                                        "Walk-in"
                                )
                        );

                s.append("\n₹")
                        .append(
                                money(
                                        b.optDouble("total")
                                )
                        );

                s.append("\n\n");
            }

            if (s.length() == 0) {

                s.append("No saved bills yet.");
            }

            new AlertDialog.Builder(this)
                    .setTitle("Bill history")
                    .setMessage(s.toString())
                    .setPositiveButton(
                            "Close",
                            null
                    )
                    .show();

        } catch (Exception e) {

            toast("Could not load history");
        }
    }

    void dailySales() {

        try {

            JSONArray a = new JSONArray(
                    prefs.getString("bills", "[]")
            );

            Calendar c = Calendar.getInstance();

            int count = 0;

            double sum = 0;

            for (int i = 0; i < a.length(); i++) {

                JSONObject b = a.getJSONObject(i);

                Calendar d = Calendar.getInstance();

                d.setTimeInMillis(
                        b.optLong("date")
                );

                if (
                        c.get(Calendar.YEAR)
                                == d.get(Calendar.YEAR)
                                &&
                        c.get(Calendar.DAY_OF_YEAR)
                                == d.get(Calendar.DAY_OF_YEAR)
                ) {

                    count++;

                    sum += b.optDouble("total");
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle("Today's sales")
                    .setMessage(
                            "Bills: "
                                    + count
                                    + "\nTotal sales: ₹"
                                    + money(sum)
                    )
                    .setPositiveButton(
                            "Close",
                            null
                    )
                    .show();

        } catch (Exception e) {

            toast("Could not calculate sales");
        }
    }

    void clearBill() {

        items.clear();

        customer.setText("");

        phone.setText("");

        product.setText("");

        price.setText("");

        qty.setText("1");

        discount.setText("0");

        tax.setText("0");

        renderItems();
    }

    void toast(String s) {

        Toast.makeText(
                this,
                s,
                Toast.LENGTH_SHORT
        ).show();
    }
}
