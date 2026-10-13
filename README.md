# 🚗 App Auto

An Android app to manage your vehicles and never miss a deadline again.  
Track insurance, road tax, MOT and maintenance for each car — with smart color-coded alerts based on days remaining or kilometres driven.

---

## Screenshots

<p align="center">
  <img src="foto/Screenshot_20261013_090022.png" width="22%" />
  <img src="foto/Screenshot_20261013_090120.png" width="22%" />
  <img src="foto/Screenshot_20261013_090125.png" width="22%" />
  <img src="foto/Screenshot_20261012_160717.png" width="22%" />
</p>

---

## Features

- **Multi-vehicle support** — add and manage multiple cars, each with name, brand, model, fuel type and licence plate
- **Deadline tracking** — assicurazione (insurance), bollo (road tax), revisione (MOT) and manutenzione (maintenance)
- **Smart alerts** — cards change colour from green → yellow → orange → red as deadlines approach, and turn red when expired
- **Km-based maintenance reminders** — set a service interval (e.g. every 10,000 km) and the app calculates how many km are left based on your current mileage
- **Mileage history** — log odometer readings over time and see the full history at a glance
- **Quick renew** — one-tap "+1 year" renewal for insurance, road tax and MOT

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** |
| UI | **Jetpack Compose** + Material 3 |
| Architecture | **MVVM** (ViewModel + StateFlow) |
| Local database | **Room** |
| Background tasks | **WorkManager** |
| Navigation | **Navigation Compose** |
| Min SDK | Android 8.0 (API 26) |

---

## Build

Clone the repo and open in Android Studio, or build from the command line:

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
