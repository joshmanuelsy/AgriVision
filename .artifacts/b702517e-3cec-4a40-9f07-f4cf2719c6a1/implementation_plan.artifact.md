# Implementation Plan - Fix Project "Scrambled" State and Integrate Plant Classifier

The project is currently in a broken state because `SplashScreen.kt` is a duplicate of `ScanScreen.kt`, `PlantClassifier.kt` references a non-existent model file, and there are leftover YOLO-related files with compilation errors.

## Proposed Changes

### [Core App Logic]

#### [MODIFY] [PlantClassifier.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ai/PlantClassifier.kt)
- Update `MODEL_NAME` to `"plant_model.tflite"` to match the asset file.
- **Note:** The hardcoded labels in this file might need verification if the new model has different output classes.

#### [DELETE] [BoundingBoxOverlay.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/components/BoundingBoxOverlay.kt)
- Remove this file as it is broken (`Unresolved reference 'etection'`) and is used for object detection (YOLO), whereas the new model is for classification.

### [UI / Screens]

#### [MODIFY] [SplashScreen.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/screens/SplashScreen.kt)
- Replace the duplicate `ScanScreen` code with a proper `SplashScreen` composable that shows the AgriVision branding.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/MainActivity.kt)
- Fix the ambiguous destructuring in `onSaveResult` by explicitly typing the lambda parameter.
- Ensure the `SplashScreen` import is resolved correctly once the file is fixed.

#### [MODIFY] [ScanScreen.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/screens/ScanScreen.kt)
- Verify `ImageProxy.toBitmap()` and ensure it correctly handles the `RGBA_8888` format for the classifier.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that all compilation errors are resolved.

### Manual Verification
- Deploy the app to a device/emulator.
- Verify the Splash -> Welcome -> Home flow.
- Test the Scan feature with the new `plant_model.tflite`.
