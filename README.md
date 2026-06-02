# FitCore – Android Fitness & Nutrition Tracker

## Overview

FitCore is a native Android fitness and nutrition tracking application built with Kotlin and Jetpack Compose. It combines personalised calorie tracking, food logging with barcode scanning, workout scheduling with progressive overload, and data visualisation insights into a single free application.

The app follows the MVVM architecture pattern and uses Firebase Authentication and Cloud Firestore for secure, user-specific data storage.

## Features

- User authentication using Firebase Authentication
- Profile management for age, weight, height, gender, activity level and fitness goal
- Personalised calorie goal using the Mifflin-St Jeor equation
- Food tracking through manual entry, OpenFoodFacts search and barcode scanning
- Barcode scanning using CameraX and ML Kit
- Daily nutrition diary for calories, protein, carbohydrates and fat
- Workout scheduling for weekly workout planning
- Progressive overload tracking for exercise weight progress
- Exercise library with predefined and custom exercises
- Body weight tracking with goal progress
- Insights and charts using MPAndroidChart
- Macro pie chart for daily nutrition breakdown

## Tech Stack

- Kotlin
- Jetpack Compose
- Material3
- MVVM Architecture
- Firebase Authentication
- Cloud Firestore
- Retrofit
- Gson
- CameraX
- ML Kit Barcode Scanning
- MPAndroidChart
- Navigation Compose
- LiveData and StateFlow
- Kotlin Coroutines
- Gradle Kotlin DSL

## Installation Instructions

### Prerequisites

- Android Studio
- JDK 21
- Android SDK API 36
- Firebase project with Authentication and Firestore enabled
- `google-services.json` placed inside `product/app/`

### Clone the Repository

```bash
git clone https://github.com/AlbertGurung/FitnessTracker.git
