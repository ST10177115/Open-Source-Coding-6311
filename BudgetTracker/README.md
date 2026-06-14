# BudgetTracker - Personal Finance App

## Student Information
- **Student Number:** ST10177115
- **Module:** OPSC7312
- **Part:** 3 - Final App Development

## App Overview
BudgetTracker is an Android application that helps users track their daily expenses, manage budgets, and achieve their financial goals through gamification and visual insights.

## Features

### Core Features
- **User Authentication** - Secure login and registration
- **Expense Tracking** - Add, view, and manage expenses with categories
- **Category Management** - Create and manage expense categories
- **Goal Setting** - Set minimum and maximum monthly spending goals

### Part 3 New Features
- **Spending Graph** - Visual bar chart showing amount spent per category with minimum and maximum goal lines over a user-selectable period
- **Budget Progress** - Visual progress bar showing how well the user is staying within their spending goals
- **Gamification (Badges)** - Earn badges and rewards for meeting budget goals and consistent expense logging

### Own Features
1. **Budget Tips** - Personalised financial tips based on the user's current spending patterns and goals
2. **Monthly Summary** - A breakdown of total spending over the last 6 months to track progress over time

## Screenshots
(Add screenshots of your app here)

## Video Demonstration
[Click here to watch the demo video](#)
(Replace # with your YouTube link)

## GitHub Actions
This project uses GitHub Actions for automated building and testing. The workflow:
- Triggers on every push to main branch
- Sets up JDK 17
- Builds the debug APK automatically
- Uploads the APK as a build artifact

## Technical Details
- **Language:** Kotlin
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Database:** Room (SQLite)
- **Architecture:** MVVM (ViewModel + LiveData)
- **Libraries Used:**
  - MPAndroidChart - for bar charts and graphs
  - Lottie - for animations
  - Room - for local database
  - Glide - for image loading
  - Material Design Components

## How to Run
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an Android device or emulator (API 24+)

## Design Considerations
- Clean and intuitive user interface
- Material Design principles
- Color-coded feedback for budget status
- Responsive layout for different screen sizes

## References
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Android Room Documentation: https://developer.android.com/training/data-storage/room
- GitHub Actions for Android: https://github.com/marketplace/actions/automated-build-android-app-with-github-action