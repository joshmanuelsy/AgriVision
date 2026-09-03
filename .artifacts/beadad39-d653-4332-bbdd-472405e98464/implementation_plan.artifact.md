# Implementation Plan - Gemini 3.6 Flash Restoration

Restore the correct 2026-era model name and apply the required resource prefix to resolve the 404 error.

## Problem Analysis
The "404 Not Found" errors were introduced when the AI model was "downgraded" to the retired Gemini 1.5/2.5 series based on outdated assumptions. In the current context (September 2026), **Gemini 3.6 Flash** is the stable production model.

Additionally, the standalone Google AI SDK requires the **`models/`** prefix to correctly resolve model resources on the backend.

## Proposed Changes

### AI Analysis Layer

#### [MODIFY] [GeminiAnalyzer.kt](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/src/main/java/com/example/pechayimnida/ai/GeminiAnalyzer.kt)
- Update the `modelName` to **`models/gemini-3.6-flash`**.
- This restores the state-of-the-art 2026 reasoning engine while ensuring the SDK can find the resource.

## Verification Plan

### Manual Verification
1. **Trigger Analysis**:
   - Capture a plant image.
   - Reconnect to the internet and tap **ANALYZE NOW**.
2. **Success Criteria**:
   - The AI request should complete successfully using the Gemini 3.6 engine.
   - The agronomist verdict and explanation should appear in the UI.
