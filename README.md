# Acuma 💰

**Acuma** is a modern, lightweight personal finance manager for Android. It helps users track their expenses and incomes by organizing them into categories, managing balances, and keeping a detailed transaction history.

## ✨ Features

* **Category Management:** Create and manage custom financial categories.
* **Financial Operations:**
    * ➕ **Deposit:** Add funds to specific categories.
    * ➖ **Withdraw:** Record expenses from your categories.
    * 🔄 **Transfer:** Move money between categories seamlessly.
* **Transaction History:** A dedicated tab to review all past financial activities.
* **Reactive UI:** Built with Kotlin Flow for real-time data updates.
* **Smooth UX:** Features Material Design 3 components, including Bottom Sheets for inputs and smart-hiding buttons on scroll.

## 🛠 Tech Stack

* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern.
* **Local Database:** [Room](https://developer.android.com/training/data-storage/room) for persistent data storage with KSP.
* **Asynchronous Programming:** Kotlin Coroutines & Flow.
* **Jetpack Components:**
    * Navigation Component (Bottom Navigation)
    * View Binding
    * ViewModel & Lifecycle
* **UI/UX:** Material Design 3, RecyclerView with ListAdapter, BottomSheetFragments.

## 🚀 Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/chsrdev/AcumaView.git](https://github.com/chsrdev/AcumaView.git)
    ```
    
2.  **Open in Android Studio:**
    Import the project as a Gradle project.
3.  **Build & Run:**
    Make sure you have **JDK 17** configured. Select your emulator or physical device and click **Run**.

*Minimum SDK: 26 (Android 8.0)*

## 📸 Preview
![Screenshot_20260308-215732](https://github.com/user-attachments/assets/c934ee83-4753-4bfc-9341-8caad05855ce)
![Screenshot_20260308-215736](https://github.com/user-attachments/assets/bfd78bbb-ef84-40b3-bf97-6907f42a11b7)

## 🛠 Project Structure

* `ui.categories`: Logic for displaying balances and managing money operations.
* `ui.history`: Detailed list of all transactions.
* `database`: Room setup, DAOs, and entities.
* `repository`: Data layer abstraction.

## 🤝 Contributing
Contributions, issues, and feature requests are welcome!

## 📄 License
This project is licensed under the MIT License.
