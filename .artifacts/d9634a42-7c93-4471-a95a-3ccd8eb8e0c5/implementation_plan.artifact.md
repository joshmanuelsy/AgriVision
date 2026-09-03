# Implementation Plan - Gemini API Key Location & ScanScreen Improvements

This plan addresses the user's request to find the Gemini API key and fixes minor issues in `ScanScreen.kt`, specifically the unused flash toggle.

## Gemini API Key Location

Based on the implementation in `GeminiAnalyzer.kt`, the Gemini API key is managed via the **Secrets Gradle Plugin**.

The key is expected to be in your project's `local.properties` file (located in the root directory).

### How to add/update the key:
1. Open [local.properties](file:///C:/Users/aray%20mo/AndroidStudioProjects/pechayimnida/local.properties) in the root of your project.
2. Add or update the following line:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Sync your project with Gradle.

> [!IMPORTANT]
> Never commit `local.properties` to version control (it is already in `.gitignore` by default).

## Proposed Changes to ScanScreen.kt

### [MODIFY] [ScanScreen.kt](file:///C:/Users/aray%20mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/screens/ScanScreen.kt)

1. **Implement Flash Control**: Currently, the `flashEnabled` state is toggled but doesn't actually control the camera's torch. I will update the `bindToLifecycle` logic to enable/disable the torch based on this state.
2. **Optimize Image Analysis**: Ensure the analyzer stops processing when the result sheet is shown to save resources. (Partially implemented, but can be made more robust).

## Verification Plan

### Manual Verification
- Verify that toggling the flash button in `ScanScreen` actually turns the device's torch on and off.
- Verify that the API key error in `GeminiAnalyzer` disappears once the key is added to `local.properties`.
