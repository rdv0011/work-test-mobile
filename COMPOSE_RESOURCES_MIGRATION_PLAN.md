# Compose Multiplatform Resources Migration Plan

## Executive Summary

This document outlines a strategic migration path from the current hybrid localization system (Kotlin sealed class + platform-specific resource files) to **Compose Multiplatform Resources** (`org.jetbrains.compose.resources`).

**Current State**: Manual synchronization required across 5 files. Platform-specific resource handling.

**Target State**: Single shared resource directory with type-safe, automatically-generated resource accessors. Compile-time validation.

**Timeline**: Phase 1-2 weeks for planning and infrastructure, Phase 2-3 weeks for implementation and testing.

**Complexity**: Medium-High (requires Gradle configuration changes, build script updates, test coverage).

---

## Part 1: Why Migrate to Compose Multiplatform Resources?

### Problems with Current System

| Issue | Impact | Severity |
|-------|--------|----------|
| **Manual Synchronization** | 5 files must stay in sync; easy to miss strings | High |
| **Error-Prone** | Typos in keys cause runtime failures, not compile-time errors | High |
| **Platform Duplication** | Android and iOS have separate resource directories | Medium |
| **No Type Safety** | String keys are unvalidated strings, not compile-time symbols | Medium |
| **Add New String = 5 Edits** | Every new string requires changes to TextId, Helper, Android, iOS, Swift | High |
| **IDE Integration** | Limited autocomplete and refactoring support | Low |

### Benefits of Compose Multiplatform Resources

| Benefit | Mechanism | Value |
|---------|-----------|-------|
| **Single Source** | Shared `composeResources/` directory used by both platforms | Eliminates duplication |
| **Type-Safe Access** | `Res.string.app_title` generated at compile-time | Compile-time validation |
| **No Manual Keys** | Filenames generate accessor names automatically | No key typos possible |
| **IDE Support** | Refactoring and autocomplete work natively | Better DX |
| **Build Time Checks** | Missing resources detected during build | Fail-fast errors |
| **Locale Switching** | Automatic resource reloading on locale change | Better UX |
| **Asset Management** | Images, fonts, colors all managed same way | Consistent system |

### Trade-offs to Consider

| Aspect | Impact | Mitigation |
|--------|--------|-----------|
| **Migration Effort** | 2-3 weeks of work | Phased approach, thorough testing |
| **Build Complexity** | Gradle plugin configuration | Use standard patterns from examples |
| **Backward Compatibility** | Breaking change if published as library | Not applicable (end-user app) |
| **Swift Bridge** | May need adaptation for iOS | Compose Resources provides Swift integration |

---

## Part 2: Technical Architecture

### Current Stack

```
┌─────────────────────────────────────────┐
│        Shared Module (Core)              │
├─────────────────────────────────────────┤
│  TextId.kt (sealed class, 19 objects)    │
│  TextIdHelper.kt (mapping function)      │
│  TranslationHelper (global access)       │
│  PlatformTranslationService (interface)  │
└─────────────────────────────────────────┘
         ↓                            ↓
┌──────────────────┐        ┌──────────────────┐
│   Android App    │        │    iOS App       │
│ strings.xml      │        │ Localizable.     │
│ 19 strings       │        │ strings          │
│                  │        │ 19 strings       │
│ R.string.*       │        │                  │
│ Context.         │        │ NSBundle         │
│ getString()      │        │ localizedString  │
└──────────────────┘        └──────────────────┘
```

### Target Stack (Compose Resources)

```
┌────────────────────────────────────────────────┐
│          Shared Module (Core)                   │
├────────────────────────────────────────────────┤
│  composeResources/                              │
│  ├── strings.xml (unified, 19 strings)          │
│  ├── en/strings.xml (fallback)                  │
│  └── [other locales]/strings.xml                │
│                                                 │
│  Generated at build-time:                       │
│  ├── Res.string.app_title (type-safe)          │
│  ├── Res.string.restaurant_list_title          │
│  └── ... (all 19 accessors)                    │
│                                                 │
│  TranslationHelper (simplified)                 │
│  └── Uses Res.string.* directly                │
└────────────────────────────────────────────────┘
         ↓                                    ↓
┌──────────────────┐              ┌──────────────────┐
│   Android App    │              │    iOS App       │
│                  │              │                  │
│ Uses Res.string  │              │ Uses Res.string  │
│ from compose     │              │ from compose     │
│ resources        │              │ resources        │
└──────────────────┘              └──────────────────┘
```

### New Project Structure

```
project-root/
├── core/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/
│   │   │   │   └── io/umain/munchies/
│   │   │   │       ├── core/ui/TextId.kt        [REMOVE]
│   │   │   │       ├── core/ui/TextIdHelper.kt  [REMOVE]
│   │   │   │       └── localization/
│   │   │   │           └── TranslationHelper.kt [SIMPLIFY]
│   │   │   └── composeResources/   [NEW]
│   │   │       └── strings/
│   │   │           ├── strings.xml (base)
│   │   │           ├── en/strings.xml
│   │   │           ├── es/strings.xml
│   │   │           └── fr/strings.xml
│   │   ├── androidMain/
│   │   │   └── kotlin/.../localization/
│   │   │       └── PlatformTranslationService.android.kt [SIMPLIFY]
│   │   └── iosMain/
│   │       └── kotlin/.../localization/
│   │           └── PlatformTranslationService.ios.kt [SIMPLIFY]
│   ├── build.gradle.kts [UPDATE - add compose.resources]
│   └── ...
│
├── iosApp/
│   ├── iosApp/
│   │   ├── Core/Localization/
│   │   │   └── TextIdHelper.swift [REMOVE]
│   │   └── ...
│   └── ...
│
└── strings-catalog.json [ARCHIVE - no longer needed for runtime]
```

### Key Changes

1. **Remove TextId sealed class** - No longer needed
2. **Remove TextIdHelper mapping** - Generated automatically
3. **Add composeResources directory** - Single source of truth for strings
4. **Simplify PlatformTranslationService** - Delegates to Compose Resources
5. **Update TranslationHelper** - Uses Res.string.* directly
6. **Remove iOS TextIdHelper.swift** - Uses Res.string.* directly

---

## Part 3: Implementation Roadmap

### Phase 1: Planning & Setup (Week 1)

#### 1.1 Gradle Configuration (1 day)
- [ ] Add Compose Resources Gradle plugin to `core/build.gradle.kts`
  ```kotlin
  plugins {
      id("org.jetbrains.compose") version "1.6.0"
  }
  ```
- [ ] Configure multiplatform resources in `core/build.gradle.kts`
  ```kotlin
  kotlin {
      targets.configureEach {
          compilations.configureEach {
              compilerOptions.configure {
                  freeCompilerArgs.add("-opt-in=org.jetbrains.compose.resources.InternalResourceApi")
              }
          }
      }
  }
  ```
- [ ] Add compose.resources dependency to shared module
- [ ] Create `core/src/commonMain/composeResources/` directory structure
- [ ] Verify plugin loads correctly with `./gradlew build` (dry run)

#### 1.2 Resource File Migration (1 day)
- [ ] Create `core/src/commonMain/composeResources/strings/strings.xml` (base strings)
- [ ] Populate with all 19 strings from current catalog
- [ ] Create `strings.xml` for default/en locale
- [ ] Verify XML structure is valid (schema validation)
- [ ] Test that Compose Resources generates accessor classes

#### 1.3 Testing Infrastructure (1 day)
- [ ] Create unit tests for new resource accessors
  - Verify `Res.string.app_title` exists
  - Verify all 19 strings are accessible
  - Verify types are correct
- [ ] Set up integration tests for Kotlin/Native compilation
- [ ] Create test suite to verify both Android and iOS can access strings
- [ ] Establish baseline: all tests pass before implementation

### Phase 2: Implementation (Week 2-3)

#### 2.1 Kotlin Shared Code (2 days)
- [ ] **Remove TextId sealed class** from `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt`
- [ ] **Remove TextIdHelper** from `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt`
- [ ] **Refactor TranslationHelper**:
  ```kotlin
  object TranslationHelper {
      fun tr(stringId: StringResource): String {
          return stringId.readableResourceString()  // Compose API
      }
  }
  ```
- [ ] **Update PlatformTranslationService** (Android):
  - Remove TextId → resource ID mapping
  - Simplify to just return string from Compose Resources
- [ ] **Update PlatformTranslationService** (iOS):
  - Remove TextId mapping
  - Use Compose Resources directly
- [ ] Update all imports in composables to use `Res.string.*` instead of `TextId`

#### 2.2 Android Integration (2 days)
- [ ] Verify Android app compiles with Compose Resources dependency
- [ ] Update `androidApp/build.gradle.kts` to use shared resources
- [ ] Remove `androidApp/src/main/res/values/strings.xml` (now unused)
- [ ] Update any direct `Context.getString()` calls to use Compose Resources
- [ ] Test on Android emulator:
  - All strings display correctly
  - No missing resource errors
  - Locale switching works (if implemented)
- [ ] Verify Android Studio provides autocomplete for `Res.string.*`

#### 2.3 iOS Integration (2 days)
- [ ] Update iOS app Xcode configuration to include shared resources
- [ ] Remove `iosApp/iosApp/Resources/en.lproj/Localizable.strings`
- [ ] Update `iosApp/iosApp/Core/Localization/TextIdHelper.swift` to use Compose Resources
  - Or replace entirely with direct `Res.string.*` usage in SwiftUI views
- [ ] Test on iOS simulator:
  - All strings display correctly
  - SwiftUI previews show strings
  - No missing resource errors
- [ ] Verify Xcode provides autocomplete for resources

#### 2.4 Testing & Validation (2 days)
- [ ] Run full unit test suite
  - Verify all string resources are accessible
  - Verify no broken references remain
- [ ] Run integration tests
  - Android app tests pass
  - iOS app tests pass
  - Both platforms load all 19 strings
- [ ] Manual QA:
  - Test app on Android device
  - Test app on iOS device
  - Verify all UI text displays correctly
- [ ] Build verification
  - Clean build on main branch works
  - No compilation warnings related to resources
  - Build time acceptable

### Phase 3: Multi-Language Support (Optional, Future)

Once Compose Resources foundation is solid:
- [ ] Add Spanish (`es/strings.xml`)
- [ ] Add French (`fr/strings.xml`)
- [ ] Implement locale switching in UI
- [ ] Test RTL support (if needed)

---

## Part 4: Step-by-Step Implementation Guide

### Step 1: Add Gradle Plugin

**File**: `core/build.gradle.kts`

```kotlin
// Add to plugins block
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose") version "1.6.0"
}

// Add to dependencies
dependencies {
    commonMainImplementation(compose.resources)
}

// Add resource configuration
kotlin {
    // ... existing configuration ...
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.resources)
        }
    }
}
```

### Step 2: Create Resource Directory Structure

```bash
mkdir -p core/src/commonMain/composeResources/strings/en
```

### Step 3: Add Base strings.xml

**File**: `core/src/commonMain/composeResources/strings/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App -->
    <string name="app_title">Munchies</string>
    
    <!-- Restaurant List -->
    <string name="restaurant_list_title">Restaurants</string>
    <string name="restaurant_detail_title">Restaurant Details</string>
    <string name="filter_all">All Restaurants</string>
    
    <!-- Status -->
    <string name="restaurant_status_open">Open</string>
    <string name="restaurant_status_closed">Closed</string>
    
    <!-- Accessibility -->
    <string name="accessibility_restaurant_card">Restaurant card for %s</string>
    <string name="accessibility_filter_chip">Filter by %s</string>
    <string name="accessibility_filter_selected">Filter %s selected</string>
    <string name="accessibility_back_button">Back</string>
    
    <!-- Errors & Loading -->
    <string name="error_loading">Failed to load data</string>
    <string name="error_network">Network error</string>
    <string name="loading">Loading…</string>
    
    <!-- Navigation -->
    <string name="tab_restaurants">Restaurants</string>
    <string name="tab_settings">Settings</string>
    
    <!-- Settings -->
    <string name="settings_title">Settings</string>
    <string name="settings_dark_mode">Dark Mode</string>
    <string name="settings_notifications">Notifications</string>
    <string name="settings_about">About</string>
</resources>
```

### Step 4: Update TranslationHelper

**File**: `core/src/commonMain/kotlin/io/umain/munchies/localization/TranslationHelper.kt`

```kotlin
package io.umain.munchies.localization

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object TranslationHelper {
    // For Composable functions
    @Composable
    fun tr(stringResource: StringResource, vararg args: Any): String {
        return if (args.isEmpty()) {
            stringResource(stringResource)
        } else {
            stringResource(stringResource).format(*args)
        }
    }
    
    // For non-Composable contexts (if needed)
    suspend fun trAsync(stringResource: StringResource, vararg args: Any): String {
        return if (args.isEmpty()) {
            stringResource(stringResource)
        } else {
            stringResource(stringResource).format(*args)
        }
    }
}
```

### Step 5: Update Usage in Composables

**Before**:
```kotlin
Text(text = tr(TextId.AppTitle))
```

**After**:
```kotlin
import org.jetbrains.compose.resources.stringResource
import io.umain.munchies.resources.Res

Text(text = stringResource(Res.string.app_title))
```

Or with TranslationHelper:
```kotlin
Text(text = TranslationHelper.tr(Res.string.app_title))
```

### Step 6: Update Android String Resources

Remove or archive:
- `androidApp/src/main/res/values/strings.xml` (no longer needed)

The app now uses shared resources from `core/composeResources/`.

### Step 7: Update iOS String Resources

Remove or archive:
- `iosApp/iosApp/Resources/en.lproj/Localizable.strings` (no longer needed)

Update SwiftUI views to use Compose Resources directly:
```swift
import shared

Text(SharedRes.strings().app_title)
```

Or update TextIdHelper.swift to use Compose Resources:
```swift
import shared

extension Res.String {
    static var appTitle: StringResource { Res.string.app_title }
    static var restaurantListTitle: StringResource { Res.string.restaurant_list_title }
    // ... etc
}
```

---

## Part 5: Risk Mitigation

### Risks and Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| **Gradle plugin conflicts** | Medium | High | Use latest stable version, test isolated build first |
| **Build time increases** | Medium | Medium | Profile gradle build, optimize if needed |
| **iOS integration issues** | Medium | High | Start with Android, use well-documented Swift patterns |
| **String lookup failures** | Low | High | Comprehensive testing, CI/CD validation |
| **Locale/encoding issues** | Medium | Low | Test with multiple locales before shipping |

### Testing Strategy

```
Phase 1: Unit Tests
└─ All string resources accessible
   └─ All 19 strings defined
   └─ No typos in accessor names

Phase 2: Integration Tests  
└─ Android app compiles
   └─ iOS app compiles
   └─ Both platforms load all strings
   └─ Platform translation services work

Phase 3: Manual QA
└─ Android device testing
   └─ iOS device testing
   └─ Locale switching (if implemented)
   └─ UI layout (strings not too long)

Phase 4: Regression Testing
└─ All existing tests pass
   └─ No new warnings
   └─ No performance degradation
```

---

## Part 6: Migration Checklist

### Pre-Migration
- [ ] Read [Compose Multiplatform Resources Documentation](https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/resources/README.md)
- [ ] Create feature branch: `feature/compose-resources-migration`
- [ ] Back up current strings across all platforms
- [ ] Ensure all tests pass on main branch
- [ ] Get team review of this plan

### Gradle & Setup
- [ ] Add Compose Resources plugin to core/build.gradle.kts
- [ ] Create composeResources directory structure
- [ ] Create base strings.xml with all 19 strings
- [ ] Verify build succeeds with `./gradlew build`
- [ ] Commit: "Add Compose Multiplatform Resources setup"

### Kotlin Code Migration
- [ ] Update TranslationHelper to use Compose Resources
- [ ] Update all @Composable functions to use stringResource()
- [ ] Remove TextId.kt (after all usages updated)
- [ ] Remove TextIdHelper.kt (after all usages updated)
- [ ] Update Android PlatformTranslationService
- [ ] Update iOS PlatformTranslationService
- [ ] Run tests: `./gradlew test`
- [ ] Commit: "Migrate Kotlin code to Compose Resources"

### Platform-Specific
- [ ] Remove Android strings.xml (or keep as backup)
- [ ] Update Android app to use Compose Resources
- [ ] Verify Android app compiles: `./gradlew :androidApp:build`
- [ ] Test on Android emulator
- [ ] Commit: "Migrate Android to Compose Resources"

- [ ] Remove iOS Localizable.strings (or keep as backup)
- [ ] Update iOS TextIdHelper.swift
- [ ] Verify iOS app compiles in Xcode
- [ ] Test on iOS simulator
- [ ] Commit: "Migrate iOS to Compose Resources"

### Testing & Validation
- [ ] Run full test suite: `./gradlew test`
- [ ] Android instrumented tests pass
- [ ] iOS unit tests pass
- [ ] Manual QA on device
- [ ] Code review
- [ ] Commit: "Add Compose Resources tests and validation"

### Final Steps
- [ ] Delete archives of old files (strings-catalog.json if no longer needed for build)
- [ ] Update documentation
- [ ] Create PR for review
- [ ] Merge to main after approval

---

## Part 7: Post-Migration Improvements

Once migration is complete:

### 1. Add More Locales
```
composeResources/strings/
├── strings.xml (base)
├── en/strings.xml (English)
├── es/strings.xml (Spanish)
├── fr/strings.xml (French)
└── de/strings.xml (German)
```

### 2. Implement Runtime Locale Switching
```kotlin
// In shared module
object LocaleManager {
    var currentLocale: Locale = Locale.getDefault()
    
    fun changeLocale(locale: Locale) {
        currentLocale = locale
        // Trigger recomposition
    }
}
```

### 3. Add Other Resource Types
- Images: `composeResources/drawable/`
- Fonts: `composeResources/font/`
- Colors (if moving from DesignTokens)
- Strings become part of unified resource system

### 4. Create Scripts for Locale Management
```bash
# Auto-generate locale files from base
scripts/generate-locales.sh

# Validate all locales have all strings
scripts/validate-locale-completeness.sh

# Extract strings for translation service
scripts/extract-strings-for-translation.sh
```

---

## Part 8: Appendix - Resources & References

### Official Documentation
- [Compose Multiplatform Resources](https://github.com/JetBrains/compose-multiplatform)
- [Compose Resources Gradle Plugin](https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/resources/)
- [JetBrains Compose Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html)

### Example Projects
- [Compose Multiplatform Template](https://github.com/JetBrains/compose-multiplatform-template)
- [MusicBox-KMP (resources example)](https://github.com/SEAbdullah/MusicBox-KMP)

### Related Issues to Watch
- Migration effort estimation: ~2-3 weeks for team of 1-2 developers
- Build time impact: +10-20% (resource compilation adds overhead)
- Runtime performance: Negligible impact (resources cached at app start)

---

## Conclusion

Compose Multiplatform Resources provides a cleaner, type-safe, and more maintainable localization system compared to the current manual approach. The migration is straightforward but requires careful planning and thorough testing.

**Recommendation**: Execute this migration in phases:
1. **Now**: Complete current cleanup (missing strings, validation script)
2. **Next Sprint**: Plan and set up Gradle infrastructure
3. **Following Sprint**: Implement and test migration
4. **Future**: Add more locales and other resource types

This provides immediate value (fixed missing strings, validation) while laying groundwork for the larger migration.
