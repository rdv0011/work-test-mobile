# Localization & Resources Analysis

## Executive Summary

This document analyzes the localization implementation in the Munchies mobile app (KMP project supporting Android and iOS). The codebase uses a **hybrid approach**:

1. **Common Localization Layer**: Kotlin Multiplatform `TextId` sealed class for type-safe string identifiers
2. **Platform-Specific Resolution**: Android uses `strings.xml`, iOS uses `Localizable.strings`
3. **Design System**: Dedicated `design-tokens` module for colors, typography, and spacing
4. **Missing Pattern**: Compose Multiplatform Resources library is NOT currently used

---

## Part 1: Current Localization Implementation

### 1.1 Architecture Overview

```
┌─────────────────────────────────────────┐
│       UI Layer (Android Compose / iOS SwiftUI)
│  tr(TextId.AppTitle) or tr(.appTitle)
└────────────────────┬────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
   ┌────▼──────────┐      ┌──────▼──────────┐
   │  Android      │      │  iOS            │
   │  Context.     │      │  NSBundle.      │
   │  getString()  │      │  localized      │
   │  (R.string.*)│      │  StringForKey() │
   └───────────────┘      └─────────────────┘
        │                         │
   ┌───▼──────────────────────────▼─────┐
   │ PlatformTranslationService          │
   │ (actual class for each platform)    │
   └───┬──────────────────────────────┬──┘
       │                              │
   ┌───▼──────────┐         ┌────────▼────────┐
   │ Android:     │         │ iOS:            │
   │ mapTextId... │         │ mapTextId...    │
   │ ToStringRsc()│         │ ToLocalizableKey│
   └───┬──────────┘         └────────┬────────┘
       │                              │
   ┌───▼──────────────────────────────▼──────────┐
   │ TextIdHelper.kt (Common)                    │
   │ mapTextIdToKey(): TextId -> String          │
   │ e.g., TextId.AppTitle -> "app.title"        │
   └────────────────────────────────────────────┘
       │
   ┌───▼──────────────────────────────────────┐
   │ TextId.kt (Common)                         │
   │ sealed class TextId {                      │
   │   object AppTitle : TextId()               │
   │   object RestaurantListTitle : TextId()    │
   │   ... (all 26 string identifiers)          │
   │ }                                          │
   └────────────────────────────────────────────┘
```

### 1.2 TextId Sealed Class

**File**: `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt`

```kotlin
sealed class TextId {
    // App
    object AppTitle : TextId()
    
    // Restaurants
    object RestaurantListTitle : TextId()
    object RestaurantDetailTitle : TextId()
    object FilterAll : TextId()
    object RestaurantStatusOpen : TextId()
    object RestaurantStatusClosed : TextId()
    
    // Accessibility
    object AccessibilityRestaurantCard : TextId()
    object AccessibilityFilterChip : TextId()
    object AccessibilityFilterSelected : TextId()
    object AccessibilityBackButton : TextId()
    
    // Errors & Loading
    object ErrorLoading : TextId()
    object ErrorNetwork : TextId()
    object Loading : TextId()
    
    // Navigation
    object Restaurants : TextId()
    object Settings : TextId()
    
    // Settings
    object SettingsTitle : TextId()
    object DarkMode : TextId()
    object Notifications : TextId()
    object About : TextId()
}
```

**Total Items**: 26 string identifiers

### 1.3 Key Mapping Layer

**File**: `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt`

Maps `TextId` objects to dot-notation keys:

```kotlin
fun mapTextIdToKey(textId: TextId): String = when (textId) {
    TextId.AppTitle -> "app.title"
    TextId.RestaurantListTitle -> "restaurant.list.title"
    // ... (all 26 mappings)
    TextId.About -> "settings.about"
}
```

**Key Format**: Dot notation (e.g., `restaurant.detail.title`, `accessibility.filter.chip`)

### 1.4 Platform-Specific Resolution

#### Android

**File**: `core/src/androidMain/kotlin/io/umain/munchies/localization/PlatformTranslationService.android.kt`

```kotlin
actual class PlatformTranslationService actual constructor() : TranslationService, KoinComponent {
    private val context: Context by inject()
    
    override fun translate(textId: TextId, vararg args: Any): String {
        val resourceId = mapTextIdToStringResource(textId)
        return if (resourceId != 0) {
            if (args.isEmpty()) {
                context.getString(resourceId)
            } else {
                context.getString(resourceId, *args)
            }
        } else {
            textId::class.simpleName ?: "unknown"
        }
    }
}
```

**Helper**: `core/src/androidMain/kotlin/io/umain/munchies/core/TextIdMapper.android.helper.kt`

```kotlin
fun mapTextIdToStringResource(textId: TextId): Int {
    val context: Context = GlobalContext.get().get()
    val key = mapTextIdToKey(textId)  // "app.title"
    val resourceName = key.replace(".", "_")  // "app_title"
    return context.resources.getIdentifier(resourceName, "string", context.packageName)
}
```

**String Resources**: `androidApp/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App -->
    <string name="app_title">Munchies</string>
    
    <!-- Restaurant List -->
    <string name="restaurant_list_title">Restaurants</string>
    <string name="filter_all">All Restaurants</string>
    
    <!-- Restaurant Detail -->
    <string name="restaurant_detail_title">Restaurant Details</string>
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
</resources>
```

**Missing Strings in XML**:
- `tab.restaurants`
- `tab.settings`
- `settings.title`
- `settings.dark.mode`
- `settings.notifications`
- `settings.about`

#### iOS

**File**: `core/src/iosMain/kotlin/io/umain/munchies/localization/PlatformTranslationService.ios.kt`

```kotlin
actual class PlatformTranslationService actual constructor() : TranslationService {
    override fun translate(textId: TextId, vararg args: Any): String {
        val localizableKey = mapTextIdToLocalizableKey(textId)
        val localizedString = NSBundle.mainBundle.localizedStringForKey(
            key = localizableKey,
            value = localizableKey,
            table = null
        )
        return if (args.isEmpty()) {
            localizedString
        } else {
            formatString(localizedString, args)
        }
    }
    
    private fun formatString(template: String, args: Array<out Any>): String {
        var result = template
        args.forEach { arg ->
            result = result.replaceFirst("%s", arg.toString())
                .replaceFirst("%d", arg.toString())
        }
        return result
    }
}
```

**Helper**: `core/src/iosMain/kotlin/io/umain/munchies/core/TextIdMapper.ios.helper.kt`

```kotlin
fun mapTextIdToLocalizableKey(textId: TextId): String {
    return mapTextIdToKey(textId)  // Returns dot-notation key
}
```

**String Resources**: `iosApp/iosApp/Resources/en.lproj/Localizable.strings`

```strings
/* App */
"app.title" = "Munchies";

/* Restaurant List */
"restaurant.list.title" = "Restaurants";
"filter.all" = "All Restaurants";

/* Restaurant Detail */
"restaurant.detail.title" = "Restaurant Details";
"restaurant.status.open" = "Open";
"restaurant.status.closed" = "Closed";

/* Accessibility */
"accessibility.restaurant.card" = "Restaurant card for %@";
"accessibility.filter.chip" = "Filter by %@";
"accessibility.filter.selected" = "Filter %@ selected";
"accessibility.back.button" = "Back";

/* Errors & Loading */
"error.loading" = "Failed to load data";
"error.network" = "Network error";
"loading" = "Loading…";
```

**Missing Strings in Localizable.strings**:
- `tab.restaurants`
- `tab.settings`
- `settings.title`
- `settings.dark.mode`
- `settings.notifications`
- `settings.about`

### 1.5 Global Helper Functions

**File**: `core/src/commonMain/kotlin/io/umain/munchies/localization/Translation.kt`

```kotlin
interface TranslationService {
    fun translate(textId: TextId, vararg args: Any): String
    fun getCurrentLocale(): String
}

expect class PlatformTranslationService() : TranslationService

object TranslationHelper : KoinComponent {
    private val translationService: TranslationService by inject()
    
    fun translate(textId: TextId, vararg args: Any): String {
        return translationService.translate(textId, *args)
    }
    
    fun getCurrentLocale(): String {
        return translationService.getCurrentLocale()
    }
}

// Global helper function
fun tr(textId: TextId, vararg args: Any): String {
    return TranslationHelper.translate(textId, *args)
}
```

### 1.6 iOS Swift Extensions

**File**: `iosApp/iosApp/Core/Localization/TextIdHelper.swift`

```swift
func tr(_ textId: TextId, _ args: Any...) -> String {
    return TranslationKt.tr(textId: textId, args: KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    })
}

extension TextId {
    var localized: String {
        return tr(self)
    }
}

extension TextId {
    static var appTitle: TextId { TextId.AppTitle() }
    static var restaurantListTitle: TextId { TextId.RestaurantListTitle() }
    static var restaurantDetailTitle: TextId { TextId.RestaurantDetailTitle() }
    static var filterAll: TextId { TextId.FilterAll() }
    static var restaurantStatusOpen: TextId { TextId.RestaurantStatusOpen() }
    static var restaurantStatusClosed: TextId { TextId.RestaurantStatusClosed() }
    static var accessibilityRestaurantCard: TextId { TextId.AccessibilityRestaurantCard() }
    static var accessibilityFilterChip: TextId { TextId.AccessibilityFilterChip() }
    static var accessibilityFilterSelected: TextId { TextId.AccessibilityFilterSelected() }
    static var accessibilityBackButton: TextId { TextId.AccessibilityBackButton() }
    static var errorLoading: TextId { TextId.ErrorLoading() }
    static var errorNetwork: TextId { TextId.ErrorNetwork() }
    static var loading: TextId { TextId.Loading() }
    static var settingsTitle: TextId { TextId.SettingsTitle() }
    static var darkMode: TextId { TextId.DarkMode() }
    static var notifications: TextId { TextId.Notifications() }
    static var about: TextId { TextId.About() }
    static var restaurants: TextId { TextId.Restaurants() }
    static var settings: TextId { TextId.Settings() }
}
```

---

## Part 2: Design Tokens (Colors, Typography, Spacing)

### 2.1 Structure

**File**: `design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/DesignTokens.kt`

```kotlin
object DesignTokens {
    object Colors {
        object Text {
            const val dark = "#1F2B2E"
            const val light = "#FFFFFF"
            const val subtitle = "#999999"
            const val footer = "#50555C"
            const val picto = "#000000"
        }
        
        object Background {
            const val primary = "#F8F8F8"
            const val card = "#FFFFFF"
            const val filterDefault = "#FFFFFF66"
        }
        
        object Accent {
            const val selected = "#E2A364"
            const val positive = "#2ECC71"
            const val negative = "#C0392B"
            const val star = "#F9CA24"
            const val brightRed = "#FF5252"
        }
    }
    
    object Typography {
        object FontFamilies { ... }
        object FontWeights { ... }
        object FontSizes { ... }
        object LineHeights { ... }
        object TextStyles { ... }
    }
    
    object Spacing { ... }
    object BorderRadius { ... }
}
```

**Source**: Generated from `design-tokens/resources/tokens.json`

---

## Part 3: Compose Multiplatform Resources Analysis

### 3.1 Current Status

**NOT IMPLEMENTED** - The project does not use `org.jetbrains.compose.resources` or `androidx.compose.resources`.

**Current Approach**:
- ✅ Android: Standard `res/` directory with `strings.xml` and drawables
- ✅ iOS: Standard `Localizable.strings` and asset bundles
- ✅ Design Tokens: Kotlin code-based design system in `design-tokens` module
- ❌ Compose Multiplatform Resources: Not integrated

### 3.2 What Compose Multiplatform Resources Provides

The `org.jetbrains.compose.resources` plugin enables:

1. **Shared Resources Directory**: Single source for strings, drawables, fonts across platforms
   ```
   commonMain/composeResources/
   ├── drawable/
   │   ├── ic_logo.xml
   │   └── ic_star.xml
   ├── values/
   │   ├── colors.xml
   │   └── strings.xml
   └── font/
   ```

2. **Type-Safe Resource Access**:
   ```kotlin
   Res.string.app_title  // Instead of tr(TextId.AppTitle)
   Res.drawable.ic_logo
   Res.color.text_dark
   ```

3. **Automatic Platform Adaptation**:
   - Resources are compiled to platform-specific formats automatically
   - No manual mapping or conversion needed

4. **Locale Support**:
   - Automatic handling of multiple language variants
   - Platform-native locale switching

### 3.3 Benefits of Migration

| Aspect | Current Approach | Compose Resources |
|--------|------------------|-------------------|
| **Type Safety** | Partial (TextId sealing) | Full (auto-generated Res class) |
| **Maintenance** | Manual mapping required | Automatic compilation |
| **Code Duplication** | High (strings.xml + Localizable.strings) | None (single source) |
| **IDE Support** | Limited | Full IDE integration |
| **Scalability** | Manual for each new string | Automatic on file save |
| **Shared Drawing** | Custom module (design-tokens) | Built-in colors/fonts |

---

## Part 4: Automated TextId Regeneration

### 4.1 Problem Statement

Currently, adding a new translatable string requires **MANUAL** updates to:
1. `TextId.kt` - Add object
2. `TextIdHelper.kt` - Add mapping
3. `strings.xml` - Add string
4. `Localizable.strings` - Add string
5. `TextIdHelper.swift` - Add extension property
6. Update any feature modules using the string

**This is error-prone and violates DRY principle.**

### 4.2 Proposed Solution: LLM-Powered Regeneration

Create a script that uses Claude API to regenerate these files based on a **single source of truth** (e.g., `strings.json` or a Google Sheet).

**Source Format** (`strings-catalog.json`):
```json
{
  "strings": [
    {
      "id": "app_title",
      "textIdCase": "AppTitle",
      "keyPath": "app.title",
      "android": "Munchies",
      "ios": "Munchies",
      "description": "Main app title",
      "category": "app"
    },
    {
      "id": "restaurant_list_title",
      "textIdCase": "RestaurantListTitle",
      "keyPath": "restaurant.list.title",
      "android": "Restaurants",
      "ios": "Restaurants",
      "description": "Title for restaurant list screen",
      "category": "restaurant"
    }
  ]
}
```

---

## Part 5: LLM Prompts for Automated Regeneration

### 5.1 Prompt 1: Generate TextId.kt

**Input**: `strings-catalog.json`
**Output**: `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt`

```
You are a Kotlin code generator. Given a JSON catalog of string identifiers, 
generate a Kotlin sealed class called TextId with one object for each string.

Requirements:
1. Create `sealed class TextId`
2. For each entry in the catalog, create: `object {TextIdCase} : TextId()`
3. Group by category as comments
4. Preserve ordering from catalog

Input JSON schema:
{
  "strings": [
    {
      "id": "string_id",
      "textIdCase": "TextIdCaseName",
      "category": "category_name",
      ...
    }
  ]
}

Output must be valid Kotlin code, ready to copy/paste into the file.
```

### 5.2 Prompt 2: Generate TextIdHelper.kt

**Input**: `strings-catalog.json`
**Output**: `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt`

```
You are a Kotlin code generator. Given a JSON catalog of string identifiers,
generate a function that maps TextId cases to dot-notation keys.

Requirements:
1. Create function: `fun mapTextIdToKey(textId: TextId): String`
2. Use when() expression with all TextId cases
3. Format: `TextId.{Case} -> "{keyPath}"`
4. Group by category as comments in the when block
5. Preserve catalog ordering

Input JSON schema:
{
  "strings": [
    {
      "textIdCase": "TextIdCaseName",
      "keyPath": "dot.notation.path",
      "category": "category_name",
      ...
    }
  ]
}

Output must be valid Kotlin code.
```

### 5.3 Prompt 3: Generate strings.xml

**Input**: `strings-catalog.json`
**Output**: `androidApp/src/main/res/values/strings.xml`

```
You are an Android resource file generator. Given a JSON catalog of string identifiers,
generate an Android strings.xml file.

Requirements:
1. Create valid XML with <resources> root
2. For each entry, create: `<string name="{id_with_underscores}">{android_text}</string>`
3. Use category comments above groups: `<!-- Category Name -->`
4. Replace dots in names with underscores
5. Escape XML special characters (&, <, >, ", ')
6. Format with proper indentation

Input JSON schema:
{
  "strings": [
    {
      "id": "string_id",
      "android": "String text with %s for formatting",
      "category": "category_name",
      ...
    }
  ]
}

Output must be valid XML, ready to copy/paste.
```

### 5.4 Prompt 4: Generate Localizable.strings

**Input**: `strings-catalog.json`
**Output**: `iosApp/iosApp/Resources/en.lproj/Localizable.strings`

```
You are an iOS localization file generator. Given a JSON catalog of string identifiers,
generate an iOS Localizable.strings file.

Requirements:
1. Format: key = "value";
2. Use keyPath from catalog (not the id)
3. Use iOS format codes (%@ for objects, %d for numbers)
4. Add category comments: `/* Category Name */`
5. Escape special characters (quotes, backslashes)
6. Group by category with comments above

Input JSON schema:
{
  "strings": [
    {
      "keyPath": "dot.notation.path",
      "ios": "String text with %@ for formatting",
      "category": "category_name",
      ...
    }
  ]
}

Output must be valid .strings file format.
```

### 5.5 Prompt 5: Generate TextIdHelper.swift

**Input**: `strings-catalog.json`
**Output**: `iosApp/iosApp/Core/Localization/TextIdHelper.swift`

```
You are a Swift code generator. Given a JSON catalog of string identifiers,
generate Swift extensions on TextId.

Requirements:
1. Create extension TextId
2. For each entry, create: `static var {camelCaseName}: TextId { TextId.{TextIdCase}() }`
3. Use camelCase (first letter lowercase) for property names
4. Group by category as comments
5. Include order preservation

Input JSON schema:
{
  "strings": [
    {
      "id": "string_id",
      "textIdCase": "TextIdCaseName",
      "category": "category_name",
      ...
    }
  ]
}

Output must be valid Swift code.

Naming convention: 
- For id "app_title" -> property name "appTitle"
- For id "restaurant_list_title" -> property name "restaurantListTitle"
(Convert underscores to camelCase)
```

---

## Part 6: Missing Strings (Audit)

The following strings are defined in `TextId.kt` but missing from both platform files:

### Tab Navigation (3 missing)
- `TextId.Restaurants` → `tab.restaurants` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings
- `TextId.Settings` → `tab.settings` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings

### Settings (4 missing)
- `TextId.SettingsTitle` → `settings.title` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings
- `TextId.DarkMode` → `settings.dark.mode` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings
- `TextId.Notifications` → `settings.notifications` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings
- `TextId.About` → `settings.about` → Android: NOT in strings.xml | iOS: NOT in Localizable.strings

**Impact**: These strings will fall back to `textId::class.simpleName` on Android and the key itself on iOS when used in UI.

---

## Recommendations

### Immediate (Quick Wins)
1. ✅ **Add missing string entries** to both `strings.xml` and `Localizable.strings`
2. ✅ **Verify TextIdHelper.swift** includes all static properties

### Short-Term (Robustness)
3. ✅ **Create `strings-catalog.json`** as single source of truth
4. ✅ **Implement validation script** to check sync between TextId, mappers, and resource files
5. ✅ **Create LLM-powered regeneration script** for automated updates

### Long-Term (Modernization)
6. ✅ **Migrate to Compose Multiplatform Resources** (org.jetbrains.compose.resources)
   - Eliminates manual TextId/strings.xml synchronization
   - Provides type-safe resource access (`Res.string.app_title`)
   - Supports multiple locales natively
   - Better IDE integration and autocomplete

---

## Files Reference

### Localization Files
| File | Purpose | Status |
|------|---------|--------|
| `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt` | Type-safe identifiers | ✅ Complete |
| `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt` | ID→key mapping | ✅ Complete |
| `core/src/commonMain/kotlin/io/umain/munchies/localization/Translation.kt` | Global helpers | ✅ Complete |
| `core/src/androidMain/kotlin/io/umain/munchies/localization/PlatformTranslationService.android.kt` | Android impl | ✅ Complete |
| `core/src/iosMain/kotlin/io/umain/munchies/localization/PlatformTranslationService.ios.kt` | iOS impl | ✅ Complete |
| `core/src/androidMain/kotlin/io/umain/munchies/core/TextIdMapper.android.helper.kt` | Android mapper | ✅ Complete |
| `core/src/iosMain/kotlin/io/umain/munchies/core/TextIdMapper.ios.helper.kt` | iOS mapper | ✅ Complete |
| `androidApp/src/main/res/values/strings.xml` | Android strings | ⚠️ **6 missing entries** |
| `iosApp/iosApp/Resources/en.lproj/Localizable.strings` | iOS strings | ⚠️ **6 missing entries** |
| `iosApp/iosApp/Core/Localization/TextIdHelper.swift` | Swift bridge | ⚠️ **Verify completeness** |
| `design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/DesignTokens.kt` | Colors/Typography | ✅ Complete |

---

## Next Steps for Implementation

1. **Review this analysis** - Ensure all points align with team understanding
2. **Approve localization approach** - Current hybrid model vs. Compose Resources migration
3. **Decide on LLM automation** - Implement regeneration helpers?
4. **Fix missing strings** - Add 6 missing entries to both platforms
5. **Create validation script** - Ensure future consistency
6. **Plan modernization** - Timeline for Compose Resources migration
