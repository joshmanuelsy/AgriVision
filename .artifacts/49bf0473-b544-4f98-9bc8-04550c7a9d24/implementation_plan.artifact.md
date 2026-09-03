# Implementation Plan - Fix Missing TensorFlow Lite Metadata Dependency

The project is failing to compile because the generated class `Yolo11n.java` (from ML Model Binding) uses `MetadataExtractor`, which is located in the `org.tensorflow:tensorflow-lite-metadata` artifact. This dependency is currently missing from the `app` module's build configuration.

## Proposed Changes

### [app](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/aray mo/AndroidStudioProjects/pechayimnida/app/build.gradle.kts)
- Add `implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")` to the dependencies block to provide the missing `MetadataExtractor` class.
- Optionally, I will also add it to `libs.versions.toml` if you prefer, but I will start by adding it directly to `build.gradle.kts` to match the existing TensorFlow Lite dependency declarations.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugJavaWithJavac` to verify that the compilation error is resolved.
- Perform a Gradle Sync to ensure the IDE recognizes the new dependency.

### Manual Verification
- Verify that the import `import org.tensorflow.lite.support.metadata.MetadataExtractor;` in `Yolo11n.java` no longer shows an error.
