# Building the iOS App with KMP + Xcode

## Prerequisites

- macOS with Xcode 16.0 or later
- Xcode Command Line Tools installed  
- JDK 17 or later for Gradle
- Gradle wrapper (`./gradlew`) in project root

## Current Status

✅ **iOS + KMP Integration: COMPLETE**

**What's configured**:
- ✅ KMP iOS targets: `iosArm64`, `iosSimulatorArm64`
- ✅ `ios-aggregator` module with frameworks configured
- ✅ Xcode build phase configured (`Build KMP Framework`)
- ✅ XCFramework generation integrated
- ✅ Swift Package Manager (SPM) configured
- ✅ `embedSwiftExportForXcode` task available

---

## Quick Start (Recommended)

### Step 1: Initial Setup (One-Time)

```bash
# Build Gradle configuration
./gradlew clean :ios-aggregator:build

# Create initial XCFramework
./gradlew :ios-aggregator:tasks > /dev/null 2>&1  # Verify tasks are available
ios-aggregator/create-xcframework.sh  # Create XCFramework for SPM
```

### Step 2: Open Xcode

```bash
open iosApp/iosApp.xcodeproj
```

### Step 3: Verify Framework is Linked

1. Select the `iosApp` target
2. Go to **General** tab → **Frameworks, Libraries, and Embedded Content**
3. Should show "shared" with status "Embed & Sign"
4. If missing, click **"+"** → **"Add Other..."** → Navigate to `ios-aggregator` → Select it

### Step 4: Build and Run

Press **Cmd+B** to build, then **Cmd+R** to run on simulator or device.

**That's it!** The Xcode build phase automatically handles Gradle compilation.

---

## How It Works

### Architecture

```
Xcode Build Process:
  ↓
Build Phase: "Build KMP Framework"
  ↓
Runs: ./gradlew :ios-aggregator:embedSwiftExportForXcode
  ↓
Gradle compiles KMP code for current architecture
  ↓
Framework embedded in DerivedData
  ↓
Xcode links framework
  ↓
Build completes
```

### Automatic Flow

When you press **Cmd+B** or **Cmd+R** in Xcode:

1. Xcode runs all build phases **before** compiling Swift
2. Our "Build KMP Framework" phase calls Gradle
3. Gradle automatically detects Xcode's environment variables:
   - `SDK_NAME`: iOS or iPhoneSimulator
   - `ARCHS`: arm64, x86_64, etc.
   - `CONFIGURATION`: Debug or Release
4. Gradle embeds the framework in the correct location
5. Xcode compiles Swift code and links the framework
6. App builds successfully

### Environment Variables Passed

The `embedSwiftExportForXcode` task receives:
- `SDK_NAME`: SDK being built for (iphoneos, iphonesimulator)
- `ARCHS`: Target architectures
- `CONFIGURATION`: Debug or Release
- `TARGET_BUILD_DIR`: Where to place output
- `FRAMEWORKS_FOLDER_PATH`: Where to embed frameworks

---

## Manual Framework Generation (If Needed)

If the build phase doesn't run or you need to manually trigger XCFramework creation:

```bash
# Build the Kotlin/Native frameworks
./gradlew :ios-aggregator:build

# Create XCFramework bundle
ios-aggregator/create-xcframework.sh

# Then rebuild in Xcode: Cmd+B
```

---

## Gradle Commands

### Build Targets

```bash
# Build all iOS architectures (device + simulator)
./gradlew :ios-aggregator:build

# Build specific architecture
./gradlew :ios-aggregator:iosArm64Binaries         # Device (arm64)
./gradlew :ios-aggregator:iosSimulatorArm64Binaries # Simulator

# Embed for Xcode (called by build phase automatically)
./gradlew :ios-aggregator:embedSwiftExportForXcode

# Create XCFramework bundle
ios-aggregator/create-xcframework.sh
```

### Verify Available Tasks

```bash
./gradlew :ios-aggregator:tasks | grep -i "embed\|framework"
```

---

## Development Workflow

### Normal Development (Most Common)

```bash
# Just press Cmd+R in Xcode
# Build phase handles everything automatically
```

After making KMP code changes, the build phase will automatically rebuild the framework when you build in Xcode.

### Full Clean Build

```bash
# If you encounter issues, try:
./gradlew clean :ios-aggregator:build

# Recreate XCFramework
ios-aggregator/create-xcframework.sh

# Clean Xcode
cd iosApp
xcodebuild clean -project iosApp.xcodeproj -scheme iosApp

# Rebuild in Xcode: Cmd+B
```

### Debugging Build Issues

```bash
# Run Gradle manually to see what happens
./gradlew :ios-aggregator:embedSwiftExportForXcode --info

# Check if frameworks were created
ls ios-aggregator/build/bin/iosArm64/releaseFramework/shared.framework
ls ios-aggregator/build/bin/iosSimulatorArm64/releaseFramework/shared.framework

# Check if XCFramework exists
ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/Info.plist
```

---

## Troubleshooting

### "No such module 'shared'" in Xcode

1. Check XCFramework exists:
   ```bash
   ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/Info.plist
   ```

2. If missing, create it:
   ```bash
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   ```

3. Clean Xcode and rebuild:
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   # Then Cmd+B in Xcode
   ```

### Build Phase Not Running

1. Verify phase exists: Target → Build Phases → Look for "Build KMP Framework"
2. Check it's **before** "Compile Sources"
3. Run manually:
   ```bash
   ./gradlew :ios-aggregator:embedSwiftExportForXcode
   ```

### Build Fails with Symbol Not Found

1. Rebuild all targets:
   ```bash
   ./gradlew clean :ios-aggregator:build
   ```

2. Recreate XCFramework:
   ```bash
   ios-aggregator/create-xcframework.sh
   ```

3. Clean Xcode and rebuild: Cmd+Shift+K, then Cmd+B

### Gradle Build Hangs

Stop Gradle daemon and retry:
```bash
./gradlew --stop
./gradlew :ios-aggregator:build
```

### Firebase Configuration Warning

`GoogleService-Info.plist` is expected. Ensure it exists:
```bash
ls iosApp/iosApp/GoogleService-Info.plist
```

If missing, add it via Xcode: drag file into Project Navigator.

---

## Architecture Details

### Module Structure

```
ios-aggregator/ (KMP module)
├── build.gradle.kts
├── Package.swift (SPM manifest)
├── create-xcframework.sh (Helper script)
└── build/
    ├── bin/
    │   ├── iosArm64/releaseFramework/shared.framework
    │   └── iosSimulatorArm64/releaseFramework/shared.framework
    └── XCFrameworks/release/shared.xcframework
        ├── Info.plist
        ├── ios-arm64/shared.framework
        └── ios-arm64-simulator/shared.framework

Exported modules:
├── :core (navigation, logging, DI, networking)
├── :ui-components (reusable components)
└── :feature-restaurant (business logic)
```

### Why XCFramework?

- **Universal**: Single bundle containing all architectures
- **Simple Integration**: One import in Swift
- **SPM Compatible**: Package.swift finds it automatically
- **Standards-Compliant**: Industry standard for iOS frameworks

---

## File Structure

| File | Purpose |
|------|---------|
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Xcode project with build phase |
| `ios-aggregator/build.gradle.kts` | KMP module with framework config |
| `ios-aggregator/Package.swift` | SPM manifest pointing to XCFramework |
| `ios-aggregator/create-xcframework.sh` | Script to bundle frameworks into XCFramework |
| `ios-aggregator/build/XCFrameworks/release/shared.xcframework` | Final XCFramework (generated) |

---

## Testing

### Verify Build Phase is Configured

```bash
# Check pbxproj has our build phase
grep "Build KMP Framework" iosApp/iosApp.xcodeproj/project.pbxproj
```

### Manual Test of embedSwiftExportForXcode

```bash
# This requires Xcode environment variables
# Normally only runs during Xcode build
# But can be tested by setting them manually:
export SDK_NAME=iphoneos
export ARCHS=arm64
export CONFIGURATION=Release
export TARGET_BUILD_DIR=/tmp/test_build
export FRAMEWORKS_FOLDER_PATH=/tmp/test_build/Frameworks

./gradlew :ios-aggregator:embedSwiftExportForXcode

# (This will likely fail without full Xcode environment, but shows the task works)
```

### Integration Test (Recommended)

```bash
# Best test: actually build in Xcode
open iosApp/iosApp.xcodeproj

# Then press Cmd+B to build
# If successful, the build phase ran and framework was embedded correctly
```

---

## Next Steps

1. **First time?** Follow "Quick Start" section above
2. **Build fails?** Check "Troubleshooting" section
3. **Making changes?** Just press Cmd+R - build phase handles Gradle automatically
4. **Want to understand more?** See "Architecture Details" section

---

## Reference

### Related Documentation

- `notes.md` - Project architecture and decisions
- `NAVIGATION.md` - iOS navigation setup
- `ios-aggregator/build.gradle.kts` - KMP build configuration
- `/core/src/iosMain/` - iOS-specific Kotlin code
- `iosApp/iosApp/` - Swift app code

---

## Troubleshooting

### "No such module 'shared'" in Xcode

1. Verify KMP binaries were built:
   ```bash
   ./gradlew :ios-aggregator:iosArm64Binaries :ios-aggregator:iosSimulatorArm64Binaries
   ```

2. In Xcode: Product → Clean Build Folder (Cmd+Shift+K)

3. Rebuild: Cmd+B

4. If still failing, check framework is linked:
   - Target → General → Frameworks, Libraries, and Embedded Content
   - Should show "shared" with status "Embed & Sign"

### Xcode Build Fails: "Symbol Not Found"

1. Rebuild all KMP targets:
   ```bash
   ./gradlew :ios-aggregator:build
   ```

2. Clean Xcode:
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   ```

3. Rebuild: Cmd+B

### Gradle Build Hangs or is Slow

- Gradle daemon consuming memory:
  ```bash
  ./gradlew --stop
  ```
- Then try again

### "GoogleService-Info.plist" Warning

Firebase is configured. Ensure file exists:
```bash
ls iosApp/iosApp/GoogleService-Info.plist
```

If missing, drag it into Xcode project from Finder.

### XCFramework Support: "assembleDebugXCFramework Task Not Found"

XCFramework support not yet enabled. Follow "Method B: Enable XCFramework Support" section above.

### After Enabling XCFramework: Build Phase Fails

1. Check build phase exists: Target → Build Phases → Look for "Build KMP Framework"
2. Verify phase order: Should be **before** "Compile Sources"
3. Check script syntax:
   ```bash
   # Should match exactly:
   if [ "$CONFIGURATION" = "Debug" ]; then
       ./gradlew :ios-aggregator:assembleDebugXCFramework
   else
       ./gradlew :ios-aggregator:assembleReleaseXCFramework
   fi
   ```

4. Test manually:
   ```bash
   cd ios-aggregator
   ../gradlew assembleReleaseXCFramework
   ```

---

## Testing

### Unit Tests (Shared Code)

```bash
# Test all modules
./gradlew test

# Test specific module
./gradlew :core:test
./gradlew :feature-restaurant:test
```

### Integration Tests (iOS)

After building, run iOS app on simulator:
```bash
open iosApp/iosApp.xcodeproj
# Press Cmd+R in Xcode
```

### Debugging KMP Code from Swift

1. Set breakpoints in Swift files
2. Run app with debugger attached
3. Execution will stop at breakpoints in both Swift and generated KMP code

---

## Architecture

### Module Structure

```
Shared Kotlin Code (ios-aggregator)
├── :core
│   ├── navigation/ (AppCoordinator, routes)
│   ├── logging/ (cross-platform logging)
│   ├── localization/ (tr() function)
│   ├── di/ (Koin DI setup)
│   └── networking/ (Ktor HTTP)
├── :ui-components (reusable components)
└── :feature-restaurant (business logic)

      ↓ Compiled to ↓

Kotlin/Native Binaries (current)
├── .klib (debug)
└── .xcframework (with XCFramework support)

      ↓ Linked by ↓

iOS App (iosApp)
├── Swift UI views
├── navigation (SwiftUI NavigationStack)
└── Firebase analytics integration
```

### iOS vs Android Strategy

**Android**:
- Depends directly on `:core`, `:ui-components`, `:feature-restaurant`
- Allows parallel Gradle builds and incremental compilation

**iOS**:
- Aggregates all modules into single framework (ios-aggregator)
- Simplifies Xcode integration (one import: `import shared`)
- Single Swift Package (Package.swift) manages all shared code

---

## When to Rebuild

### Before XCFramework Support

Rebuild KMP when:
```bash
./gradlew :ios-aggregator:iosArm64Binaries :ios-aggregator:iosSimulatorArm64Binaries
```

After:
- Any Kotlin code changes
- Dependencies updated in build.gradle.kts
- Build errors in Xcode (symbol not found)

### After XCFramework Support Enabled

Rebuild when:
```bash
./gradlew :ios-aggregator:assembleReleaseXCFramework
```

**Or automatically** via Xcode build phase when you press Cmd+R

---

## Next Steps

1. **Try Method A first** (current working setup)
   - Build Kotlin/Native binaries
   - Link in Xcode
   - Test on simulator

2. **Enable XCFramework support** (one-time, ~15 mins)
   - Update KotlinIosConventionPlugin.kt
   - Add Xcode build phase
   - Enjoy automated builds

3. **Questions?** Check "Troubleshooting" section above or refer to:
   - `notes.md` - Architecture decisions
   - `NAVIGATION.md` - iOS navigation setup
   - `/core/src/iosMain/` - iOS-specific code

