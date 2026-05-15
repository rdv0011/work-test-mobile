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
- ✅ Automatic framework generation via build phase
- ✅ Swift Package Manager (SPM) configured
- ✅ **Circular dependency RESOLVED** ✓
- ✅ **Xcode package resolution working** ✓

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

**That's it!** Subsequent builds automatically update KMP frameworks.

---

## How It Works

### Architecture Decision: Separating XCFramework Creation from Build

**The Problem We Solved:**
```
Original approach (CIRCULAR):
1. Build phase tries to create XCFramework
2. Package.swift references build/XCFrameworks/release/shared.xcframework
3. Xcode resolves packages BEFORE running build phases
4. XCFramework doesn't exist yet
5. Xcode can't resolve package → tries to run build phase → circular dependency
```

**The Solution:**
```
Corrected approach (LINEAR):
1. XCFramework created ONCE during initial setup
2. Xcode can resolve packages immediately (framework exists)
3. Build phase ONLY updates framework binaries (./gradlew build)
4. Framework files updated in-place
5. No circular dependency
```

### Build Flow (After Setup)

```
Cmd+B in Xcode
       ↓
Xcode starts build process
       ↓
Build Phase 1: "Build KMP Framework" executes
  ├─ cd "${SRCROOT}/.."
  ├─ ./gradlew :ios-aggregator:build
  │  └─ Recompiles Kotlin for iosArm64 + iosSimulatorArm64
  │  └─ Updates binaries in build/bin/ios{Arm64,SimulatorArm64}/...
  │
  └─ XCFramework automatically includes updated binaries
     (Framework bundles point to build/bin/ directories)
       ↓
Build Phase 2+: Compile Swift, Link Frameworks, etc.
       ↓
Build completes ✓
```

### Key Insight: XCFramework Structure

```
XCFramework is just a wrapper around per-architecture frameworks:

ios-aggregator/build/XCFrameworks/release/shared.xcframework/
├── Info.plist (metadata, lists architectures)
├── ios-arm64/shared.framework/
│   └── shared (binary for device)
└── ios-arm64-simulator/shared.framework/
    └── shared (binary for simulator)

When you update build/bin/.../shared.framework/shared binaries,
the XCFramework automatically sees the changes.
No need to recreate it every build.
```

---

## Development Workflow

### Normal Development (Most Common)

```bash
# Edit Kotlin code
# iosApp source changes...

# Build in Xcode
open iosApp/iosApp.xcodeproj
# Press Cmd+B

# What happens automatically:
# 1. Build phase runs: ./gradlew :ios-aggregator:build
# 2. Kotlin compiled for simulator (arm64)
# 3. Binary updated in build/bin/iosSimulatorArm64/...
# 4. Xcode links the updated binary
# 5. Build completes

# Run on simulator
# Press Cmd+R
```

### After Code Changes

Just press **Cmd+B** in Xcode - everything else is automatic.

### Full Clean Build

If you encounter weird issues:

```bash
# Option 1: Clean Gradle and Xcode separately
./gradlew clean :ios-aggregator:build
ios-aggregator/create-xcframework.sh

cd iosApp
xcodebuild clean -project iosApp.xcodeproj -scheme iosApp

# Then rebuild in Xcode: Cmd+B

# Option 2: Complete reset
rm -rf ios-aggregator/build iosApp/build
./gradlew clean :ios-aggregator:build
ios-aggregator/create-xcframework.sh
open iosApp/iosApp.xcodeproj
# Cmd+B in Xcode
```

---

## Gradle Commands

### Build Targets

```bash
# Build all iOS architectures (creates binaries)
./gradlew :ios-aggregator:build

# Build specific architecture
./gradlew :ios-aggregator:iosArm64Binaries         # Device
./gradlew :ios-aggregator:iosSimulatorArm64Binaries # Simulator

# Create/update XCFramework bundle (only needed once or after framework changes)
ios-aggregator/create-xcframework.sh

# Verify available tasks
./gradlew :ios-aggregator:tasks | grep -i framework
```

---

## Troubleshooting

### "Cycle inside iosApp" Build Error

**This was the main issue we solved!**

If you see this error:
```
error: Cycle inside iosApp; building could produce unreliable results.
Cycle details: ProcessXCFramework depends on script phase "Build KMP Framework"
```

**Root cause:** XCFramework creation was in the build phase.

**Solution:** Ensure:
1. XCFramework exists BEFORE opening Xcode:
   ```bash
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   ```

2. Build phase only runs gradle (not create-xcframework.sh):
   ```bash
   grep "shellScript" iosApp/iosApp.xcodeproj/project.pbxproj
   # Should show: ./gradlew :ios-aggregator:build (no create-xcframework.sh)
   ```

3. Clean and rebuild:
   ```bash
   rm -rf ios-aggregator/build iosApp/build
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   ```

### "No such module 'shared'" in Xcode

1. **Verify XCFramework exists:**
   ```bash
   ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/Info.plist
   ```

2. **If missing, create it:**
   ```bash
   ./gradlew :ios-aggregator:build
   ios-aggregator/create-xcframework.sh
   ```

3. **Verify Package.swift is correct:**
   ```bash
   grep "xcframework" ios-aggregator/Package.swift
   # Should show: path: "./build/XCFrameworks/release/shared.xcframework"
   ```

4. **Clean and rebuild:**
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   # Then Cmd+B
   ```

### Xcode Build Fails: "Symbol Not Found"

1. **Rebuild KMP frameworks:**
   ```bash
   ./gradlew clean :ios-aggregator:build
   ```

2. **Verify binaries were created:**
   ```bash
   ls -la ios-aggregator/build/bin/*/releaseFramework/shared.framework/shared
   ```

3. **Recreate XCFramework:**
   ```bash
   ios-aggregator/create-xcframework.sh
   ```

4. **Clean Xcode cache:**
   ```bash
   rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
   ```

5. **Rebuild:**
   ```bash
   cd iosApp
   xcodebuild clean -project iosApp.xcodeproj -scheme iosApp
   # Then Cmd+B
   ```

### Build Phase Not Running

1. **Verify phase exists:**
   - Target → Build Phases → Look for "Build KMP Framework"

2. **Check it's positioned correctly:**
   - Should be **before** "Compile Sources"

3. **Verify script:**
   ```bash
   grep -A5 "Build KMP Framework" iosApp/iosApp.xcodeproj/project.pbxproj | grep "shellScript"
   # Should contain: ./gradlew :ios-aggregator:build
   ```

4. **Run manually to test:**
   ```bash
   cd iosApp/..
   ./gradlew :ios-aggregator:build
   ```

### Gradle Build Hangs

```bash
# Stop Gradle daemon
./gradlew --stop

# Try again
./gradlew :ios-aggregator:build
```

### "Package Resolution Failed" in Xcode

If Xcode can't resolve the "shared" package:

```bash
# Verify XCFramework structure
ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/
# Should contain: Info.plist, ios-arm64/, ios-arm64-simulator/

# Verify binaries exist
ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/*/shared.framework/shared

# If files missing, recreate:
./gradlew clean :ios-aggregator:build
ios-aggregator/create-xcframework.sh
```

---

## File Reference

| File | Purpose |
|------|---------|
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Xcode project config (build phases) |
| `ios-aggregator/build.gradle.kts` | KMP module build configuration |
| `build-logic/src/main/kotlin/plugins/kotlin/KotlinIosConventionPlugin.kt` | iOS framework config |
| `ios-aggregator/Package.swift` | SPM manifest (references XCFramework) |
| `ios-aggregator/create-xcframework.sh` | Helper script to bundle frameworks |
| `ios-aggregator/build/bin/{arch}/releaseFramework/` | Per-architecture frameworks (build output) |
| `ios-aggregator/build/XCFrameworks/release/shared.xcframework` | Universal framework (user-facing) |

---

## Key Technical Details

### Why XCFramework?

- **Universal**: Single bundle containing all architectures
- **Simple Integration**: One import in Swift
- **SPM Compatible**: Package.swift finds it automatically
- **Industry Standard**: Standard way to distribute iOS frameworks

### Why Separate XCFramework Creation from Build?

- **Avoids Circular Dependency**: Package resolution happens before build phase execution
- **Simpler Build**: Build phase only needs to rebuild binaries (fast)
- **Smaller Scope**: Build phase has clear, single responsibility
- **CI/CD Friendly**: Setup can be one-time, builds are incremental

### Framework Binary Updates

```
Initial Setup:
  ./gradlew build  →  Creates binaries in build/bin/
  create-xcframework.sh  →  Wraps them in XCFramework

Subsequent Builds (from Xcode):
  Build phase runs: ./gradlew build
  ↓
  Binaries updated in build/bin/
  ↓
  XCFramework automatically sees new binaries
  ↓
  SPM picks up updated framework
  ↓
  Xcode links updated binary
```

No need to recreate XCFramework - the framework bundle references the binary paths, not copies them.

---

## Testing

### Verify Setup is Correct

```bash
# 1. Check XCFramework structure
ls ios-aggregator/build/XCFrameworks/release/shared.xcframework/
# Should show: Info.plist, ios-arm64/, ios-arm64-simulator/

# 2. Check binaries exist
file ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64/shared.framework/shared
file ios-aggregator/build/XCFrameworks/release/shared.xcframework/ios-arm64-simulator/shared.framework/shared
# Should show: Mach-O 64-bit dynamically linked shared library

# 3. Check Xcode project validation (no circular dependency)
xcodebuild -list -project iosApp/iosApp.xcodeproj 2>&1 | head -20
# Should show package resolution success, not errors
```

### Integration Test

```bash
# Best test: actually build in Xcode
open iosApp/iosApp.xcodeproj
# Press Cmd+B
# If successful, everything is working ✓
```

---

## Next Steps

1. **First time?** Follow "Quick Start" above
2. **Setup complete?** Just press Cmd+R in Xcode for each build
3. **Build fails?** Check "Troubleshooting" section
4. **Want to understand?** Read "How It Works" section

---

## Related Documentation

- `notes.md` - Architecture and design decisions
- `NAVIGATION.md` - iOS navigation setup details
- `/core/src/iosMain/` - iOS-specific Kotlin code
- `iosApp/iosApp/` - Swift app source code
- `ios-aggregator/build.gradle.kts` - Complete KMP build configuration
