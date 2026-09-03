# Task: Fix Scrambled Project and Integrate Plant Model

- [x] Clean up redundant/broken files
    - [x] Delete `BoundingBoxOverlay.kt`
- [x] Fix Core Logic
    - [x] Update `PlantClassifier.kt` to use `plant_model.tflite`
- [x] Restore UI Flow
    - [x] Replace duplicate code in `SplashScreen.kt` with actual Splash UI
    - [x] Fix ambiguous code in `MainActivity.kt`
- [x] Verification
    - [x] Run `./gradlew :app:assembleDebug`
    - [x] Provide walkthrough
