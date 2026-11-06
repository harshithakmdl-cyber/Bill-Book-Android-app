# 📒 Bill Book Android App

A simple Android billing application that helps small businesses create bills, calculate totals, save sales records, and share receipts.

## 📱 About the Project

Bill Book is an Android application developed using Java. It provides a simple interface for creating customer bills, adding products, calculating prices, and managing daily sales.

The application stores bill records locally on the device, allowing users to access their saved bills without an internet connection.

## ✨ Features

- **Customer Details:** Enter customer name and phone number.
- **Product Management:** Add products with price and quantity.
- **Multiple Products:** Add multiple products to a single bill.
- **Automatic Calculation:** Calculate product subtotal and bill total.
- **Discount:** Apply percentage-based discounts.
- **Tax:** Add tax percentage to the bill.
- **Save Bills:** Store bills locally on the device.
- **Bill History:** View previously saved bills.
- **Daily Sales:** View the number of bills and total sales for the current day.
- **Share Bill:** Share bill details as text using other applications.
- **UPI QR Code:** Generate a UPI payment QR code using the entered UPI ID.
- **Offline Storage:** Save bill records using Android SharedPreferences.

## 🛠️ Technologies Used

- Java
- Android SDK
- Android Activity
- Android UI Components
- SharedPreferences
- JSON
- ZXing QR Code Library
- GitHub
- GitHub Actions

## 📂 Project Structure

```text
Bill-Book-Android-app/
│
├── app/
│   ├── build.gradle
│   │
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           └── java/
│               └── com/
│                   └── harshi/
│                       └── billbook/
│                           └── MainActivity.java
│
├── .github/
│   └── workflows/
│       └── android.yml
│
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

## 🚀 How to Use

1. Open the Bill Book application.
2. Enter the customer name and phone number.
3. Enter the product name.
4. Enter the product price.
5. Enter the quantity.
6. Click **Add Product**.
7. Add additional products if required.
8. Enter discount and tax percentages.
9. Check the calculated bill total.
10. Click **Save Bill** to store the bill.
11. Click **Share Bill** to share the receipt.
12. Open **Bill History** to view saved bills.
13. Open **Today's Sales** to check daily sales.
14. Enter a UPI ID and select **Show UPI QR** to generate a payment QR code.

## 🧮 Bill Calculation

The application calculates the subtotal using the product price and quantity.

**Subtotal**

Subtotal = Sum of (Product Price × Quantity)

**Discount**

Discount Amount = Subtotal × Discount Percentage / 100

**Tax**

Tax Amount = Discounted Amount × Tax Percentage / 100

**Final Total**

Final Total = Discounted Amount + Tax Amount

## 💾 Data Storage

Bill records are stored locally using Android SharedPreferences.

The application uses JSON to store information such as:

- Bill date
- Customer name
- Product details
- Product price
- Quantity
- Bill total

Saved bills can be accessed through the Bill History section.

## 📲 Application Details

| Property | Description |
|---|---|
| Project Name | Bill Book |
| Platform | Android |
| Programming Language | Java |
| Application Type | Billing and Sales Management |
| Storage | SharedPreferences |
| QR Code Library | ZXing |
| Build System | Gradle |

## 🔮 Future Enhancements

- Edit and delete saved bills.
- Share bills as PDF or image.
- QR code scanner.
- Product inventory management.
- Customer management.
- Monthly sales reports.
- Improved user interface.
- Database integration.
- Backup and restore functionality.

## 👩‍💻 Developer

**Harshitha N**

Computer Science and Engineering

## 📄 License

This project is developed for educational and learning purposes.
