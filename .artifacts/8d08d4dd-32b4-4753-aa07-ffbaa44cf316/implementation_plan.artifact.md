# Implementation Plan - Gemini UI Stability & UX Refinement

This plan addresses the "disappearing message" issue and improves the overall user experience during AI analysis by providing more robust state management and error handling, while keeping the focus entirely on the Gemini analysis.

## Proposed Changes

### 1. UI Stability Component

#### [MODIFY] [ScanResultSheet.kt](file:///C:/Users/aray%20mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/components/ScanResultSheet.kt)
- **Persistent State Management**: Ensure the "Analyzing..." state or the final report remains visible without flickering or disappearing.
- **Retry Mechanism**: Add a "RETRY AI ANALYSIS" button that appears if the Gemini report fails to load.
- **Scrollable Report**: Adjust the report container to handle longer AI responses without cutting off text.

### 2. AI Stability Component

#### [MODIFY] [GeminiAnalyzer.kt](file:///C:/Users/aray%20mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ai/GeminiAnalyzer.kt)
- **Guaranteed String Return**: Refactor `analyzePlantHealth` to never return `null`. It will return specific, actionable error messages (e.g., "Network error", "API quota exceeded", "Model error").
- **Detailed Error Logging**: Improve error categorization to help the user understand *why* the analysis might have failed.

### 3. Image Capture Component

#### [MODIFY] [ScanScreen.kt](file:///C:/Users/aray%20mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ui/screens/ScanScreen.kt)
- **Robust Bitmap Handling**: Ensure the captured bitmap is correctly managed during the transition to the result sheet.

## Verification Plan

### Manual Verification
- **Slow Connection Test**: Verify that the "Analyzing..." state remains visible until a result or error is returned.
- **Error Test**: Temporarily disconnect internet and verify the "Retry" button appears and allows re-attempting the analysis.
