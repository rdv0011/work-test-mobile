# Building the iOS App with XCFramework + SPM

## Prerequisites

- macOS with Xcode 16.0 or later
- Xcode Command Line Tools installed  
- JDK 17 or later for Gradle

## Quick Start

### Step 1: Build the XCFramework

From the project root, build the aggregated XCFramework from the `ios-aggregator` module:

```bash
./gradlew :ios-aggregator:assembleSharedXCFramework
```

This builds all KMP modules (core, ui-components, feature-restaurant) aggregated into a single XCFramework with support for both iOS device and simulator.

Output: `ios-aggregator/build/XCFrameworks/release/shared.xcframework`

### Step 2: Open in Xcode

```bash
open iosApp/iosApp.xcodeproj
```

### Step 3: Add SPM Dependency (First Time Only)

1. In Xcode, select the project (top level in navigator)
2. Select the `iosApp` target
3. Go to "General" tab → "Frameworks, Libraries, and Embedded Content"
4. Click "+" → "Add Other..." → "Add Package Dependency..."
5. Click "Add Local..." and navigate to `ios-aggregator` folder (not the old `shared` folder)
6. Select the `ios-aggregator` directory → "Open"
7. Xcode will find `Package.swift` → Click "Add Package"
8. Select "shared" library → "Add Package" (this imports the XCFramework)

### Step 4: Build and Run

Press `Cmd+R` to build and run on simulator or device.

## XCFramework Details

- **Location**: `ios-aggregator/build/XCFrameworks/release/shared.xcframework`
- **Architectures**: 
   - Device: arm64
   - Simulator: arm64 (Apple Silicon) + x86_64 (Intel)
- **iOS Target**: 15.0+
- **SPM Manifest**: `ios-aggregator/Package.swift` (points to XCFramework binary)
- **Module Contents**: Exports `:core`, `:ui-components`, `:feature-restaurant`

## Gradle Commands

```bash
./gradlew :ios-aggregator:assembleSharedXCFramework              # Build both debug & release
./gradlew :ios-aggregator:assembleSharedDebugXCFramework        # Debug only
./gradlew :ios-aggregator:assembleSharedReleaseXCFramework      # Release only (recommended)
```

## Troubleshooting

### "No such module 'shared'"

1. Verify XCFramework exists:
   ```bash
   ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/
   ```

2. Rebuild if missing:
   ```bash
   ./gradlew :ios-aggregator:assembleSharedXCFramework
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
./gradlew :ios-aggregator:assembleSharedReleaseXCFramework
```

4. Move it **before** "Compile Sources"

