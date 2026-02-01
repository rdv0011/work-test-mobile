# Building the iOS App with XCFramework + SPM

## Prerequisites

- macOS with Xcode 14.0 or later
- Xcode Command Line Tools installed  
- JDK 17 or later for Gradle

## Quick Start

### Step 1: Build the XCFramework

From the project root:

```bash
./gradlew :shared:assembleXCFramework
```

This builds the KMP shared module as an XCFramework with support for both iOS device and simulator.

Output: `shared/build/XCFrameworks/release/shared.xcframework`

### Step 2: Open in Xcode

```bash
open iosApp/iosApp.xcodeproj
```

### Step 3: Add SPM Dependency (First Time Only)

1. In Xcode, select the project (top level in navigator)
2. Select the `iosApp` target
3. Go to "General" tab → "Frameworks, Libraries, and Embedded Content"
4. Click "+" → "Add Other..." → "Add Package Dependency..."
5. Enter local path: `../shared` (or click "Add Local..." and navigate to shared folder)
6. Click "Add Package" → Select "shared" library → "Add Package"

### Step 4: Build and Run

Press `Cmd+R` to build and run on simulator or device.

## XCFramework Details

- **Location**: `shared/build/XCFrameworks/release/shared.xcframework`
- **Architectures**: 
  - Device: arm64
  - Simulator: arm64 (Apple Silicon) + x86_64 (Intel)
- **iOS Target**: 15.0+
- **SPM Manifest**: `shared/Package.swift` (points to XCFramework binary)

## Gradle Commands

```bash
./gradlew :shared:assembleXCFramework                  # Build both debug & release
./gradlew :shared:assembleSharedDebugXCFramework       # Debug only
./gradlew :shared:assembleSharedReleaseXCFramework     # Release only
```

## Troubleshooting

### "No such module 'shared'"

1. Verify XCFramework exists:
   ```bash
   ls shared/build/XCFrameworks/release/shared.xcframework/
   ```

2. Rebuild if missing:
   ```bash
   ./gradlew :shared:assembleXCFramework
   ```

3. In Xcode: Product → Clean Build Folder (Cmd+Shift+K)

4. Rebuild project (Cmd+B)

### SPM Package Not Recognized

1. File → Packages → Reset Package Caches
2. File → Packages → Resolve Package Versions
3. Restart Xcode

### XCFramework Not Found at Build Time

Make sure you've built the XCFramework at least once before opening Xcode:
```bash
./gradlew :shared:assembleXCFramework
```

## Optional: Auto-Build on Xcode Build

To automatically rebuild the XCFramework when building in Xcode:

1. Select `iosApp` target → "Build Phases" tab
2. Click "+" → "New Run Script Phase"
3. Add this script:

```bash
cd "$SRCROOT/.."
./gradlew :shared:assembleSharedReleaseXCFramework
```

4. Move it **before** "Compile Sources"
