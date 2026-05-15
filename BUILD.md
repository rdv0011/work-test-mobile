# Building the iOS App with KMP + Xcode

## Prerequisites

- macOS with Xcode 16.0 or later
- Xcode Command Line Tools installed  
- JDK 17 or later for Gradle
- Gradle wrapper (`./gradlew`) in project root

## Current Status

✅ **iOS + KMP Integration: COMPLETE & TESTED**

**What's configured**:
- ✅ KMP iOS targets: `iosArm64`, `iosSimulatorArm64`
- ✅ `ios-aggregator` module with frameworks configured
- ✅ Xcode build phase configured (`Build KMP Framework`)
- ✅ Automatic framework + XCFramework generation
- ✅ Swift Package Manager (SPM) configured
- ✅ **Build phase tested and working** ✓

---

## Quick Start (Recommended)

### Step 1: Initial Setup (One-Time)

```bash
# Build frameworks
./gradlew clean :ios-aggregator:build

# Create XCFramework
ios-aggregator/create-xcframework.sh
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

**That's it!** The Xcode build phase automatically:
1. Builds KMP frameworks for current architecture
2. Creates XCFramework bundle
3. Xcode links and embeds in your app

---

## How It Works

### Build Phase Flow

```
You press Cmd+B in Xcode
         ↓
Xcode runs build phases (in order)
         ↓
Build Phase: "Build KMP Framework" executes
         ├─ cd "${SRCROOT}/.."
         ├─ ./gradlew :ios-aggregator:build
         │  └─ Compiles KMP for arm64 + simulator
         │  └─ Generates: build/bin/iosArm64/releaseFramework/shared.framework
         │  └─ Generates: build/bin/iosSimulatorArm64/releaseFramework/shared.framework
         ├─ ios-aggregator/create-xcframework.sh
         │  └─ Bundles frameworks into single XCFramework
         │  └─ Generates: build/XCFrameworks/release/shared.xcframework
         ↓
Xcode compiles Swift code
         ↓
Xcode links frameworks (via SPM)
         ↓
Build completes ✓
```

### Why This Works

1. **Gradle builds frameworks**: Each architecture gets its own `.framework` bundle with binary
2. **XCFramework bundles them**: Single file containing all architectures (arm64 + simulator)
3. **SPM integrates**: `Package.swift` points to XCFramework, Xcode links automatically
4. **Xcode build phase**: Automatically runs before compilation, ensures fresh KMP binaries
5. **Environment variables**: Xcode provides SDK_NAME, ARCHS, etc. (automatically used by Gradle)

### Architecture Diagram

```
ios-aggregator/ (KMP module)
├── build.gradle.kts (Kotlin/Native config)
├── Package.swift (SPM manifest)
├── create-xcframework.sh (bundling script)
└── build/
    ├── bin/ (per-architecture frameworks)
    │   ├── iosArm64/releaseFramework/shared.framework
    │   │   └── shared (arm64 binary)
    │   └── iosSimulatorArm64/releaseFramework/shared.framework
    │       └── shared (arm64 simulator binary)
    │
    └── XCFrameworks/release/shared.xcframework (final bundle)
        ├── Info.plist (metadata)
        ├── ios-arm64/shared.framework/
        │   └── shared (arm64 binary)
        └── ios-arm64-simulator/shared.framework/
            └── shared (simulator arm64 binary)

iosApp/ (Swift app)
├── iosApp.xcodeproj/project.pbxproj
│   └── Build Phases: "Build KMP Framework"
│       └── Runs the gradle build + create-xcframework.sh
└── iosApp/ (Swift source files)
```

---

## Development Workflow

### Normal Development (Most Common)

```bash
# Make changes to Kotlin code in core, feature-restaurant, etc.
# Then in Xcode:
# Press Cmd+B to build
# Build phase automatically rebuilds KMP frameworks
# Press Cmd+R to run on simulator
```

### Make Changes → Test Cycle

```bash
# Edit: /core/src/commonMain/.../*.kt
# Edit: /feature-restaurant/src/commonMain/.../*.kt
# Edit: iosApp/iosApp/*.swift

# Build:
open iosApp/iosApp.xcodeproj
# Press Cmd+B (builds everything)
# Press Cmd+R (runs on simulator)

# Changes automatically included ✓
```

### Full Clean Build

If you encounter weird issues:

```bash
# Clean everything
./gradlew clean

# Remove build directories
rm -rf ios-aggregator/build iosApp/build

# Full rebuild
./gradlew :ios-aggregator:build

# Recreate XCFramework
ios-aggregator/create-xcframework.sh

# Clean Xcode
cd iosApp
xcodebuild clean -project iosApp.xcodeproj -scheme iosApp

# Rebuild in Xcode: Cmd+B
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

# Create XCFramework bundle
ios-aggregator/create-xcframework.sh

# Verify available tasks
./gradlew :ios-aggregator:tasks | grep -i "framework"
```

---

## Troubleshooting

### "No such module 'shared'" in Xcode

1. **Ensure XCFramework exists:**
   ```bash
   ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/Info.plist
   ```

2. **If missing, create it:**
   ```bash
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   ```

3. **Clean and rebuild in Xcode:**
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   # Then Cmd+B in Xcode
   ```

4. **Verify framework is linked:**
   - Target → General → Frameworks, Libraries, and Embedded Content
   - Should show "shared" with "Embed & Sign" status

### Build Phase Not Running

1. **Check phase exists:**
   - Target → Build Phases → Look for "Build KMP Framework"

2. **Check phase order:**
   - Should be **before** "Compile Sources"

3. **Verify it's executable:**
   ```bash
   grep -A10 "Build KMP Framework" iosApp/iosApp.xcodeproj/project.pbxproj | grep shellScript
   ```

4. **Run manually to test:**
   ```bash
   cd iosApp/..
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   ```

### Xcode Build Fails: "Symbol Not Found"

1. **Rebuild KMP targets:**
   ```bash
   ./gradlew clean :ios-aggregator:build
   ```

2. **Recreate XCFramework:**
   ```bash
   ios-aggregator/create-xcframework.sh
   ```

3. **Clean Xcode and rebuild:**
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   # Then Cmd+B
   ```

### Gradle Build Hangs

```bash
# Stop Gradle daemon
./gradlew --stop

# Try again
./gradlew :ios-aggregator:build
```

### Build Fails with "Binary doesn't exist"

Make sure the XCFramework has valid binaries:

```bash
# Check binaries are present
file ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64/shared.framework/shared
file ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64-simulator/shared.framework/shared

# Both should output something like:
# "Mach-O 64-bit dynamically linked shared library arm64"

# If missing or empty, rebuild:
./gradlew clean :ios-aggregator:build
ios-aggregator/create-xcframework.sh
```

### Firebase Configuration Warning

If Firebase plist is missing:

```bash
# Check for existing file
ls iosApp/iosApp/GoogleService-Info.plist

# If missing, add via Xcode:
# 1. Drag file from Finder into Xcode project navigator
# 2. Ensure it's copied to target
```

---

## Advanced: Manual Framework Generation

### Generate Frameworks Without Build Phase

```bash
# Navigate to project root
cd /Users/rybakdmy/Development/private/work-test-mobile

# Build for all architectures
./gradlew :ios-aggregator:build

# Frameworks now at:
# - ios-aggregator/build/bin/iosArm64/releaseFramework/shared.framework
# - ios-aggregator/build/bin/iosSimulatorArm64/releaseFramework/shared.framework

# Create XCFramework bundle
ios-aggregator/create-xcframework.sh

# XCFramework now at:
# - ios-aggregator/build/XCFrameworks/release/shared.xcframework
```

### Generate for Specific Architecture

```bash
# Device only (arm64)
./gradlew :ios-aggregator:iosArm64Binaries

# Simulator only (arm64)
./gradlew :ios-aggregator:iosSimulatorArm64Binaries

# Then create XCFramework (bundles whatever is available)
ios-aggregator/create-xcframework.sh
```

---

## File Reference

| File | Purpose |
|------|---------|
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Xcode project with build phase configuration |
| `ios-aggregator/build.gradle.kts` | KMP module build configuration |
| `build-logic/src/main/kotlin/plugins/kotlin/KotlinIosConventionPlugin.kt` | iOS framework configuration |
| `ios-aggregator/Package.swift` | SPM manifest (points to XCFramework) |
| `ios-aggregator/create-xcframework.sh` | Helper script to bundle frameworks |
| `ios-aggregator/build/XCFrameworks/release/shared.xcframework` | Final XCFramework (generated) |

---

## Testing the Build Phase

### Automated Verification

```bash
# Clean everything
rm -rf ios-aggregator/build iosApp/build

# Run build phase commands manually
cd iosApp/..
./gradlew :ios-aggregator:build
ios-aggregator/create-xcframework.sh

# Verify output
[ -f "ios-aggregator/build/XCFrameworks/release/shared.xcframework/Info.plist" ] && echo "✓ XCFramework created"
[ -f "ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64/shared.framework/shared" ] && echo "✓ ARM64 binary present"
[ -f "ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64-simulator/shared.framework/shared" ] && echo "✓ Simulator binary present"
```

### Integration Test (Recommended)

```bash
# The best test: actually build in Xcode
open iosApp/iosApp.xcodeproj

# Press Cmd+B
# If build succeeds, the build phase ran correctly ✓
```

---

## Next Steps

1. **First time?** Follow "Quick Start" section above
2. **Build fails?** Check "Troubleshooting" section
3. **Making changes?** Just press Cmd+R - build phase handles everything
4. **Want to understand more?** See "How It Works" section

---

## Related Documentation

- `notes.md` - Architecture and design decisions
- `NAVIGATION.md` - iOS navigation setup details
- `/core/src/iosMain/` - iOS-specific Kotlin code
- `iosApp/iosApp/` - Swift app source code
- `ios-aggregator/build.gradle.kts` - Complete KMP build configuration
