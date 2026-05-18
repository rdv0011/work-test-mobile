# Figma → CSS Normalization → KMP Pipeline

---

# 1️⃣ Purpose

This document defines:

* The **CSS Normalization Prompt**
* The full **Figma → KMP (Kotlin Multiplatform) automated pipeline**
* Layout inference rules
* Component extraction rules
* Token mapping rules
* Scroll detection logic
* Platform-specific implementation strategies (Android & iOS)

This is designed for agentic coding across Android (Jetpack Compose) and iOS (SwiftUI).

---

# 2️⃣ High-Level Pipeline

```text
Figma Export (CSS / JSON)
        ↓
Preprocessor
        ↓
CSS Normalization (LLM)
        ↓
Layout Inference Engine
        ↓
Component Extractor
        ↓
Design Token Mapper
        ↓
DIR (Design Intermediate Representation)
        ↓
     Split
    ↙    ↖
Android       iOS
Code Gen      Code Gen
   ↓            ↓
Compose     SwiftUI
   ↓            ↓
Android      iOS
Preview      Preview
```

**Both platforms follow the same normalization pipeline, then diverge at code generation.**

---

# 3️⃣ Stage Breakdown

---

## Stage 1 — Input

Input can be:

* Raw Figma CSS export
* Figma REST API JSON
* Copy‑pasted inspector CSS

This input is **absolute-position heavy** and visually oriented.

---

## Stage 2 — Preprocessor

Goals:

* Remove comments
* Remove duplicated style blocks
* Group layers by bounding box overlap
* Sort elements by top/left
* Detect identical layout trees

Output:

```json
{
  "nodes": [...],
  "orderedByTop": true,
  "duplicatesDetected": true
}
```

---

# 4️⃣ CSS Normalization Prompt (Core Prompt)

This is the most critical part.

---

## 🔹 SYSTEM PROMPT

You are a senior UI layout normalization engine.

Your task:

Convert raw Figma-exported CSS into a normalized, production-ready layout structure that is **platform-agnostic**.

You must:

1. Remove all absolute positioning
2. Convert pixel stacking into flex/column layout
3. Remove fixed screen heights
4. Replace manual spacing with gap or margin
5. Normalize typography (line-height ≈ 1.3–1.5× font-size)
6. Detect repeated blocks and mark them as components
7. Detect scrollable areas
8. Remove system UI (battery, wifi, status bar)
9. Preserve visual intent
10. Output structure must be convertible to both Jetpack Compose (Android) and SwiftUI (iOS)

Do NOT:

* Keep `position: absolute` unless overlay is intentional
* Keep fixed screen heights
* Keep duplicated layout blocks
* Preserve visual hacks
* Include platform-specific code (Compose or SwiftUI)

Output format must be:

```json
{
  "layoutType": "Column | Row | Box | List | Grid",
  "structure": {...},
  "components": [...],
  "scrollBehavior": "none | vertical | horizontal",
  "tokens": {
    "colors": [...],
    "typography": [...],
    "spacing": [...]
  }
}
```

---

## 🔹 USER PROMPT TEMPLATE

Normalize the following Figma CSS:

```
{PASTE_RAW_FIGMA_CSS_HERE}
```

Rules:

* Infer layout intent
* Convert vertical stacking via top offsets into column layout
* Convert horizontal alignment into row layout
* Replace repeated blocks with reusable components
* Mark scroll containers
* Normalize font metrics
* Keep semantic meaning

Return structured JSON only.

---

# 5️⃣ Layout Inference Rules

---

## Rule 1 — Vertical Stack Detection

If:

* Same `left`
* Increasing `top`
* Same width

Then:

```json
layoutType = "Column"
```

---

## Rule 2 — Horizontal Group

If:

* Same `top`
* Increasing `left`

Then:

```json
layoutType = "Row"
```

---

## Rule 3 — Repeated Blocks

If 3+ identical layout trees:

```json
{
  "component": "Extract",
  "repeat": true,
  "render": "LazyColumn"
}
```

---

## Rule 4 — Scroll Detection

If:

* Content height > screen height
* Manually stacked repeating cards

Then:

```json
scrollBehavior = "vertical"
```

If horizontal overflow:

```json
scrollBehavior = "horizontal"
```

---

## Rule 5 — Hero + Floating Card Pattern

If:

* Large top image
* Card overlaps image

Then:

```json
layoutPattern = "HeroImageFloatingCard"
```

---

# 6️⃣ Component Extraction Stage

Convert repeated blocks into:

```json
{
  "name": "RestaurantCard",
  "props": [
    "title",
    "subtitle",
    "image",
    "rating",
    "time"
  ]
}
```

Store in component registry.

---

# 7️⃣ Design Token Mapping

---

## Color Mapping

Collect all unique colors into semantic tokens:

```json
{
  "#1F2B2E": "onSurface",
  "#999999": "onSurfaceVariant",
  "#F8F8F8": "background",
  "#FFFFFF": "surface",
  "#2ECC71": "success",
  "#FF5252": "error"
}
```

**Android mapping**: Material3 ColorScheme  
**iOS mapping**: Custom Color definitions (no direct Material3 equivalent)

Both platforms implement the same semantic tokens using their native design system libraries.

---

## Typography Mapping

Normalize into roles:

```json
{
  "24px": "headlineSmall",
  "18px": "titleMedium",
  "16px": "bodyLarge",
  "12px": "labelMedium",
  "10px": "labelSmall"
}
```

---

## Spacing Scale

Convert arbitrary pixel spacing into scale:

```json
{
  "4": "xs",
  "8": "sm",
  "16": "md",
  "24": "lg"
}
```

---

# 8️⃣ DIR (Design Intermediate Representation)

Example:

```json
{
  "screen": "RestaurantList",
  "root": "Scaffold",
  "children": [
    {
      "type": "TopAppBar"
    },
    {
      "type": "LazyRow",
      "component": "FilterChip"
    },
    {
      "type": "LazyColumn",
      "component": "RestaurantCard"
    }
  ]
}
```

This becomes the single source of truth.

---

# 9️⃣ Code Generators

## Android Code Generator (Jetpack Compose)

Transforms DIR into:

* Scaffold
* LazyColumn / LazyRow
* Material3 components
* Themed colors (Material3 ColorScheme)
* Typography roles (MaterialTheme.typography)

No absolute positioning allowed.

Output: `.kt` files in `androidApp/`

---

## iOS Code Generator (SwiftUI)

Transforms DIR into:

* NavigationStack / ZStack for layout
* List / ScrollView
* SwiftUI native components
* Custom Color definitions
* Custom Font definitions

No absolute positioning allowed.

Output: `.swift` files in `iosApp/`

---

## Platform Equivalence Map

| Concept | Android (Compose) | iOS (SwiftUI) |
|---------|-------------------|---------------|
| Screen Container | Scaffold | NavigationStack |
| Vertical List (scrollable) | LazyColumn | List |
| Horizontal List (scrollable) | LazyRow | ScrollView + HStack |
| Card | Card | RoundedRectangle + padding |
| Button | Button | Button |
| Text | Text | Text |
| Image | Image | Image |
| Color tokens | MaterialTheme.colorScheme | Custom ColorScheme |
| Typography | MaterialTheme.typography | Custom Font definitions |

---

# 🔟 Preview Integration

## Android Studio Preview (Jetpack Compose)

Generate preview wrappers:

```kotlin
@Preview(showBackground = true)
@Composable
fun RestaurantListPreview() {
    AppTheme {
        RestaurantListScreen(sampleData)
    }
}
```

This allows design verification without running emulator.

---

## Xcode Preview (SwiftUI)

Generate preview wrappers:

```swift
#Preview {
    AppTheme {
        RestaurantListScreen(sampleData: sampleData)
    }
}
```

This allows design verification without running iOS simulator.

---

## Platform Preview Parity

Both Android and iOS generate identical preview wrappers using their native preview systems. This ensures visual parity between platforms and rapid design validation during development.

---

# 1️⃣1️⃣ Full Automation Summary

| Stage               | Responsibility                     | Output                     |
| ------------------- | ---------------------------------- | -------------------------- |
| Preprocess          | Clean raw CSS                      | Cleaned JSON               |
| LLM Normalize       | Convert geometry → semantic layout | Normalized structure       |
| Inference Engine    | Detect rows, columns, scroll       | Layout intelligence        |
| Component Extractor | Create reusable components         | Component definitions      |
| Token Mapper        | Create design system tokens        | Semantic tokens            |
| DIR Builder         | Build layout tree                  | Platform-agnostic DIR      |
| Android Generator   | Emit Jetpack Compose code          | `.kt` files                |
| iOS Generator       | Emit SwiftUI code                  | `.swift` files             |
| Android Preview     | Validate Android visually          | Compose previews           |
| iOS Preview         | Validate iOS visually              | SwiftUI previews           |

---

# 🧠 Core Principle

Figma exports describe **geometry**.

Production apps require **semantic layout structure**.

The normalization prompt is responsible for bridging this gap.

---

---

# 1️⃣2️⃣ CLI-Ready Specification

This section defines a production-ready CLI architecture.

---

## CLI Name

```
figma-kmp
```

---

## Command Structure

### 1️⃣ Normalize CSS

```
figma-kmp normalize \
  --input ./input/raw.css \
  --output ./build/normalized.json
```

Output:

* Normalized layout JSON
* Extracted tokens
* Detected components

**Platform-agnostic**, no Compose or SwiftUI code emitted.

---

### 2️⃣ Build DIR

```
figma-kmp build-dir \
  --input ./build/normalized.json \
  --output ./build/dir.json
```

Output:

* Fully structured Design Intermediate Representation
* Platform-neutral layout definitions

---

### 3️⃣ Generate Android (Jetpack Compose)

```
figma-kmp generate \
  --target android \
  --input ./build/dir.json \
  --output ./androidApp/src/main/java
```

Output:

* Screen composables
* Component composables
* Theme.kt (Material3)
* Type.kt (Material3 typography)
* Color.kt (Material3 colors)

---

### 4️⃣ Generate iOS (SwiftUI)

```
figma-kmp generate \
  --target ios \
  --input ./build/dir.json \
  --output ./iosApp
```

Output:

* Screen SwiftUI Views
* Component SwiftUI Views
* Theme.swift (custom theme)
* Font.swift (custom typography)
* Color.swift (custom colors)

---

### 5️⃣ One-Shot Full Pipeline

```
figma-kmp run \
  --input ./input/raw.css \
  --targets android,ios
```

Pipeline executed (shared phase):

1. Preprocess
2. LLM Normalize
3. Layout Inference
4. Component Extraction
5. Token Mapping
6. DIR Build

Then for each target:

7. Platform-specific code generation (Compose for Android, SwiftUI for iOS)

---

### 6️⃣ Generate Both Platforms

```
figma-kmp run \
  --input ./input/raw.css \
  --targets android,ios \
  --project ./
```

Generates:

```
androidApp/ui/
  ├── screens/
  ├── components/
  └── theme/

iosApp/
  ├── Screens/
  ├── Components/
  └── Theme/
```

---

# 📂 Recommended Project Structure

```
root/
 ├── androidApp/
 │    └── src/main/java/com/app/ui/
 │         ├── screens/
 │         ├── components/
 │         └── theme/
 │
 ├── iosApp/
 │    ├── Screens/
 │    ├── Components/
 │    └── Theme/
 │
 ├── shared/
 │    ├── core/
 │    │    ├── tokens/         # Semantic token definitions
 │    │    └── contracts/      # UI contracts (interfaces)
 │    └── features/            # Business logic
 │
 └── figma-kmp/                # Code generator tool
      ├── cli/
      ├── core/
      │    ├── preprocess/
      │    ├── normalization/
      │    ├── inference/
      │    ├── tokens/
      │    ├── dir/
      │    └── generators/
      │         ├── android/
      │         └── ios/
      ├── prompts/
      │    └── css_normalization.prompt
      ├── schemas/
      │    └── dir.schema.json
      └── templates/
           ├── android/
           │    ├── Screen.kt.tpl
           │    ├── Component.kt.tpl
           │    ├── Theme.kt.tpl
           │    ├── Color.kt.tpl
           │    └── Type.kt.tpl
           └── ios/
                ├── Screen.swift.tpl
                ├── Component.swift.tpl
                ├── Theme.swift.tpl
                ├── Color.swift.tpl
                └── Font.swift.tpl
```

**Note**: Platform-specific generated code is placed in `androidApp/` and `iosApp/` directories, not in shared.

---

# 🔧 Internal Module Responsibilities

## preprocess/

* Strip comments
* Parse CSS blocks
* Build bounding boxes
* Detect duplicates

---

## normalization/

* Inject system + user prompt
* Call LLM
* Validate JSON against schema

---

## inference/

* Detect vertical stacks
* Detect horizontal groups
* Detect scroll behavior
* Detect hero + floating patterns

---

## tokens/

* Extract unique colors
* Generate semantic color tokens
* Normalize typography scale
* Generate spacing scale

---

## dir/

* Convert normalized JSON → DIR
* Validate structural integrity

---

## generators/android/

* Emit Jetpack Compose files
* Inject Material3 theme
* Map semantic tokens to Material3 ColorScheme
* Create @Preview wrappers
* Ensure no absolute positioning used

---

## generators/ios/

* Emit SwiftUI files
* Inject custom theme
* Map semantic tokens to SwiftUI Color
* Create #Preview wrappers
* Ensure no absolute positioning used

---

# 🧪 Validation Layer

Every stage must validate output using JSON schema.

If validation fails:

* Retry LLM with repair prompt
* Or exit with structured error

---

# 📦 Output Example

After `run --targets android,ios`:

```
androidApp/src/main/java/com/app/ui/
 ├── screens/RestaurantListScreen.kt
 ├── screens/RestaurantDetailScreen.kt
 ├── components/RestaurantCard.kt
 ├── components/FilterChip.kt
 └── theme/
      ├── Color.kt
      ├── Type.kt
      └── Theme.kt

iosApp/
 ├── Screens/
 │    ├── RestaurantListScreen.swift
 │    └── RestaurantDetailScreen.swift
 ├── Components/
 │    ├── RestaurantCard.swift
 │    └── FilterChip.swift
 └── Theme/
      ├── Color.swift
      ├── Font.swift
      └── Theme.swift
```

Both platforms implement the exact same UI, mapped to their native frameworks.

---

# 🚀 Optional Flags

```
--dry-run          (no files written)
--debug            (prints intermediate JSON)
--no-theme         (skip token mapping)
--preview-only     (generate preview wrappers only)
--strict           (fail on any ambiguity)
```

---

# 🔐 Determinism Strategy

To ensure consistent outputs:

* Temperature ≤ 0.2
* Strict JSON schema validation
* Deterministic spacing scale
* Token deduplication rules

---

# 🧠 Execution Flow Diagram

```
Figma CSS
   ↓
Preprocess
   ↓
LLM Normalize
   ↓
Validate JSON
   ↓
Inference Engine
   ↓
Token Mapper
   ↓
DIR Builder
   ↓
   Split
  ↙    ↖
Android    iOS
 Gen       Gen
  ↓         ↓
 .kt       .swift
  ↓         ↓
Android    iOS
Preview    Preview
```

---

---

# 1️⃣3️⃣ KMP (Kotlin Multiplatform) Implementation Strategy

This section defines how to implement platform-specific UI for both Android (Jetpack Compose) and iOS (SwiftUI) from a shared, platform-agnostic design specification.

**Key Principle**: Normalize design once, implement twice (once per platform).

---

# 📦 Design Tokens Source of Truth

The design-tokens module is the **single source of truth** for all design values across Android and iOS:

```
           Figma Design System
                    ↓
        design-tokens/resources/tokens.json
        (Comprehensive token definitions)
                    ↓
        Generated Design Token Files:
        ↙              ↓              ↖
    Android      Shared (KMP)       iOS
  (Compose)    (DesignTokens.kt)  (Swift)
```

**Key characteristics**:
- **tokens.json**: Single source of truth, defined in design-tokens module
- **Generated, not hand-coded**: Token values are generated from tokens.json
- **Platform-agnostic**: Token names and semantic meanings are shared
- **Platform-specific**: Each platform implements tokens using native APIs
- **Kept in sync**: Changes to Figma ripple automatically through tokens.json → platform implementations

**Token Categories**:
- **Colors**: Text, background, accent colors
- **Typography**: Font families, sizes, weights, line heights, text styles
- **Spacing**: Padding, margin, gap values (none, xxs, xs, sm, md, lg, xl)
- **Border Radius**: Corner radius values
- **Elevation**: Shadow/elevation definitions
- **Sizes**: Icon sizes, card dimensions, component sizes

---

# 🎯 Architecture Overview

```
           tokens.json (Figma)
                    ↓
        Normalized Design (DIR)
        (Platform-Agnostic)
                    ↓
    Semantic Token Mapping
    (Names → Platform Tokens)
               ↙             ↖
  Android/Compose      iOS/SwiftUI
   Implementation      Implementation
```

---

# 🔄 Platform Equivalence Rules

Both platforms must implement the same visual and functional specification. Use these mappings to ensure parity:

## Layout Primitives

| Design Concept | Jetpack Compose | SwiftUI |
|---|---|---|
| Vertical Stack | Column | VStack |
| Horizontal Stack | Row | HStack |
| Scroll Container (Vertical) | LazyColumn | List / ScrollView + VStack |
| Scroll Container (Horizontal) | LazyRow | ScrollView + HStack |
| Overlay/Absolute | Box + offset | ZStack |
| Screen Wrapper | Scaffold | NavigationStack |
| Conditional Content | if/else in Composable | if/else in View |

---

## Interactive Components

| Component | Jetpack Compose | SwiftUI |
|---|---|---|
| Button | Button | Button |
| Text Input | TextField | TextField |
| Image | Image | Image |
| Card | Card | RoundedRectangle + padding |
| Divider | Divider | Divider |
| Checkbox | Checkbox | Toggle |
| Radio Button | RadioButton | Picker |
| Drop-down Menu | DropdownMenu | Menu |
| Navigation Link | NavigationLink | NavigationLink |

---

## Styling & Design Tokens

| Element | Jetpack Compose | SwiftUI |
|---|---|---|
| Color tokens | `DesignTokens.Colors.*` | `Color.text.*`, `Color.background.*`, `Color.accent.*` or `DesignTokens.iOS.colorUI.*` |
| Primary color | `Color(DesignTokens.Colors.Accent.positive)` | `Color.accent.positive` |
| Background color | `Color(DesignTokens.Colors.Background.primary)` | `Color.background.primary` |
| Text styles | `TextStyle(...)` from DesignTokens | `.font(.headline1)`, `.font(.title1)`, etc. |
| Font family | `fontFamily = DesignTokens.Typography.FontFamilies.helvetica` | `.custom(dt.typography.fontFamily.helvetica, size: ...)` |
| Font size | Via `TextStyle` or direct from DesignTokens | `DesignTokens.iOS.typography.fontSizeUI.headline1` |
| Padding/Margin | `Modifier.padding(dp)` | `.padding(CGFloat.spacingUI.lg)` |
| Spacing (tokens) | `DesignTokens.Spacing.sm` (8), `.md` (13), `.lg` (16) | `CGFloat.spacingUI.sm`, `.md`, `.lg` |
| Border radius | Via `shape` parameter or `DesignTokens.BorderRadius.*` | `CGFloat.borderRadiusUI.md` |
| Shadow/Elevation | `Modifier.shadow()` | `.cardElevation()`, `.filterElevation()` |

---

## Spacing Scale (Design Tokens)

All spacing values are defined in `tokens.json` and available uniformly across platforms:

| Token | Value (dp) | Usage |
|---|---|---|
| `spacing.none` | 0 | No spacing |
| `spacing.xxs` | 2 | Minimal gaps |
| `spacing.xs` | 3 | Extra small gaps |
| `spacing.sm` | 8 | Small padding/gaps |
| `spacing.md` | 13 | Medium padding |
| `spacing.lg` | 16 | Large padding, default spacing |
| `spacing.xl` | 18 | Extra large padding |

---

## Elevation & Shadow Tokens

Shadows and elevation effects are defined in Elevation tokens:

| Token | Color | Opacity | Radius | Usage |
|---|---|---|---|---|
| `elevation.card` | Black | 10% | 2dp | Card shadows (elevated surfaces) |
| `elevation.filter` | Black | 5% | 1dp | Filter chip shadows (subtle elevation) |

**Android**: Applied via `Modifier.shadow()`  
**iOS**: Applied via `.cardElevation()` or `.filterElevation()` extension methods

---

# 📐 Shared Layer (commonMain) — DesignTokens.kt

The shared layer contains **semantic token definitions** generated from `tokens.json`:

## Token Definition (DesignTokens.kt)

Located in: `design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/DesignTokens.kt`

```kotlin
/**
 * Design Tokens - Single source of truth for all UI values
 * Generated from design-tokens/resources/tokens.json
 * 
 * DO NOT use hardcoded values outside this file.
 * Platform-specific units (dp, pt) applied at render time.
 */

object DesignTokens {
    
    // Color Tokens
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
            const val positive = "#2ECC71"      // Green
            const val negative = "#C0392B"      // Red
            const val star = "#F9CA24"          // Yellow
            const val brightRed = "#FF5252"     // Bright Red
        }
    }
    
    // Typography Tokens
    object Typography {
        object FontFamilies {
            const val helvetica = "Helvetica"
            const val poppins = "Poppins"
            const val inter = "Inter"
        }
        
        object FontWeights {
            const val regular = 400
            const val medium = 500
            const val bold = 700
        }
        
        object FontSizes {
            const val headline1 = 24
            const val title1 = 18
            const val headline2 = 16
            const val title2 = 14
            const val subtitle1 = 12
            const val footer1 = 10
        }
        
        object LineHeights {
            const val headline1 = 16
            const val title1 = 16
            const val headline2 = 16
            const val title2 = 20
            const val subtitle1 = 16
            const val footer1 = 12
        }
        
        data class TextStyle(
            val fontFamily: String,
            val fontWeight: Int,
            val fontSize: Int,
            val lineHeight: Int
        )
        
        object TextStyles {
            val headline1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.headline1,
                lineHeight = LineHeights.headline1
            )
            val title1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.title1,
                lineHeight = LineHeights.title1
            )
            val title2 = TextStyle(
                fontFamily = FontFamilies.poppins,
                fontWeight = FontWeights.medium,
                fontSize = FontSizes.title2,
                lineHeight = LineHeights.title2
            )
            // ... more text styles
        }
    }
    
    // Spacing Tokens
    object Spacing {
        const val none = 0
        const val xxs = 2
        const val xs = 3
        const val sm = 8
        const val md = 13
        const val lg = 16
        const val xl = 18
    }
    
    // Border Radius Tokens
    object BorderRadius {
        const val none = 0
        const val sm = 0.8f
        const val md = 12
        const val full = 24
    }
    
    // Elevation Tokens (Shadow definitions)
    object Elevation {
        const val card = "#0000001A"      // 10% black opacity
        const val filter = "#0000000A"    // 5% black opacity
    }
    
    // Size Tokens (Component dimensions)
    object Sizes {
        object Icon {
            const val small = 10
            const val medium = 12
            const val large = 48
        }
        object Card {
            object Restaurant {
                const val width = 343
                const val height = 196
                const val imageHeight = 132
            }
            object Detail {
                const val width = 343
                const val height = 144
            }
        }
        object Filter {
            const val width = 144
            const val height = 48
            const val iconSize = 48
        }
    }
}
```

**Key Points**:
- All token values are **generated from tokens.json** (not hand-coded)
- No platform-specific code (no Compose, no SwiftUI)
- Token names and values are **semantic** and **platform-agnostic**
- Platform-specific unit conversion (dp, pt, CGFloat) happens at render time
- **Single source of truth**: Changes to tokens.json automatically ripple to both platforms

---

# 🤖 Android Implementation (Jetpack Compose)

Located in: `androidApp/src/main/kotlin/io/umain/munchies/android/ui/`

**Direct Token Access Pattern**: Android uses `DesignTokens` from the shared KMP module directly:

## Basic Token Usage Examples

### Colors (Using Helper Extension)

```kotlin
import io.umain.munchies.designtokens.DesignTokens
import androidx.compose.ui.graphics.Color

// Helper extension converts hex strings to Compose Color
fun String.toComposeColor(): Color = Color(android.graphics.Color.parseColor(this))

// Access color tokens
val primaryAccent = DesignTokens.Colors.Accent.positive.toComposeColor()
val cardBg = DesignTokens.Colors.Background.card.toComposeColor()
val textDark = DesignTokens.Colors.Text.dark.toComposeColor()

// Usage in Compose
Text(
    text = "Hello",
    color = DesignTokens.Colors.Text.dark.toComposeColor()
)
```

### Typography (Using Helper Extension)

```kotlin
// Using TextStyle from DesignTokens with helper
@Composable
fun DesignTokens.Typography.TextStyle.toComposeTextStyle(): ComposeTextStyle =
    ComposeTextStyle(
        fontFamily = resolveFontFamily(fontFamily),
        fontWeight = FontWeight(fontWeight),
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp
    )

// Create Compose TextStyle from DesignTokens TextStyle
Text(
    text = "Restaurant Name",
    style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
    color = DesignTokens.Colors.Text.dark.toComposeColor()
)
```

### Spacing

```kotlin
// Padding using spacing tokens
Box(
    modifier = Modifier
        .padding(DesignTokens.Spacing.lg.dp)  // 16.dp
        .fillMaxWidth()
)

// Gap in Column/Row
Column(
    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md.dp)  // 13.dp
) {
    // children
}

// Margin
Box(modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm.dp))  // 8.dp
```

### Border Radius

```kotlin
// Using border radius tokens
Card(
    shape = RoundedCornerShape(DesignTokens.BorderRadius.md.dp),  // 12.dp
    modifier = Modifier
        .width(DesignTokens.Sizes.Card.Restaurant.width.dp)
        .height(DesignTokens.Sizes.Card.Restaurant.height.dp)
) {
    // content
}
```

### Elevation/Shadow

```kotlin
// Shadow using elevation tokens (convert hex to opacity)
Card(
    modifier = Modifier
        .shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            ambientColor = Color.Black.copy(alpha = 0.1f)  // From elevation.card (10%)
        )
) {
    // content
}
```

## Theme Wrapper

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/ui/theme/MunchiesTheme.kt
@Composable
fun MunchiesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,  // Use system Material3 theme
        typography = MaterialTheme.typography,
        content = content
    )
}
```

## Screen Implementation Example

```kotlin
@Composable
fun RestaurantListScreen(
    viewModel: RestaurantListViewModel,
) {
    val state by viewModel.state.collectAsState()

    MunchiesTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    DesignTokens.Colors.Background.primary.toComposeColor()
                )
                .padding(DesignTokens.Spacing.lg.dp)
        ) {
            Text(
                text = "Restaurants",
                style = DesignTokens.Typography.TextStyles.headline1.toComposeTextStyle(),
                color = DesignTokens.Colors.Text.dark.toComposeColor()
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg.dp)
            ) {
                items(state.restaurants) { restaurant ->
                    RestaurantCard(restaurant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantListScreenPreview() {
    MunchiesTheme {
        RestaurantListScreen(viewModel = /* mock */)
    }
}
```

---

## Token Access Pattern

**Android uses direct access to `DesignTokens` from the shared KMP module**:

| What | How | Example |
|---|---|---|
| Color values | `DesignTokens.Colors.Text.dark` | `"#1F2B2E"` (hex string) |
| Font metrics | `DesignTokens.Typography.*` | Size: 24, Weight: 700 |
| Spacing | `DesignTokens.Spacing.*` | 8, 13, 16 (pixels) |
| Border Radius | `DesignTokens.BorderRadius.*` | 12, 24 (pixels) |
| Size values | `DesignTokens.Sizes.*` | Width: 343, Height: 196 |
| Shadow | `DesignTokens.Elevation.*` | "#0000001A" (10% black opacity) |

---

## 🔧 Android Helper Extensions

**To match iOS simplicity and reduce boilerplate, use extension functions** for converting design tokens to Compose types:

### ColorMapper (String hex → Compose Color)

Helper extension to convert hex string tokens to Compose `Color`:

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/ui/ColorMapper.kt

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Convert design token hex strings to Compose Color.
 * Usage: Color(DesignTokens.Colors.Text.dark.toComposeColor())
 *        or even simpler: DesignTokens.Colors.Text.dark.toComposeColor()
 */
fun String.toComposeColor(): Color =
    Color(this.toColorInt())
```

**Usage in code**:
```kotlin
// Before (verbose):
Color(DesignTokens.Colors.Text.dark.toColorInt())

// After (cleaner):
DesignTokens.Colors.Text.dark.toComposeColor()
```

---

### TextStyleMapper (DesignTokens TextStyle → Compose TextStyle)

Helper extension to convert design token text styles to Compose `TextStyle`:

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/ui/TextStyleMapper.kt

import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.umain.munchies.designtokens.DesignTokens
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable

/**
 * Convert design token TextStyle to Compose TextStyle.
 * Automatically resolves font families using Material3 typography.
 */
@Composable
fun DesignTokens.Typography.TextStyle.toComposeTextStyle(): ComposeTextStyle =
    ComposeTextStyle(
        fontFamily = resolveFontFamily(fontFamily),
        fontWeight = FontWeight(fontWeight),
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp
    )

@Composable
private fun resolveFontFamily(fontFamily: String): FontFamily =
    with(typography) {
        when (fontFamily) {
            DesignTokens.Typography.FontFamilies.helvetica -> bodyLarge.fontFamily
            DesignTokens.Typography.FontFamilies.poppins -> titleMedium.fontFamily
            DesignTokens.Typography.FontFamilies.inter -> bodyMedium.fontFamily
            else -> null
        }
    } ?: FontFamily.Default
```

**Usage in code**:
```kotlin
// Before (verbose manual mapping):
Text(
    text = "Restaurant Name",
    style = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(DesignTokens.Typography.TextStyles.title1.fontWeight),
        fontSize = DesignTokens.Typography.TextStyles.title1.fontSize.sp,
        lineHeight = DesignTokens.Typography.TextStyles.title1.lineHeight.sp,
    ),
    color = Color(DesignTokens.Colors.Text.dark.toColorInt())
)

// After (using helpers):
Text(
    text = "Restaurant Name",
    style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
    color = DesignTokens.Colors.Text.dark.toComposeColor()
)
```

---

### SpacingMapper (Int → Compose Dp)

Helper extension for cleaner spacing usage:

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/ui/SpacingMapper.kt

import androidx.compose.ui.unit.dp

/**
 * Convert spacing token values to Compose Dp.
 * Usage: DesignTokens.Spacing.lg.asDp()
 */
fun Int.asDp() = this.dp
```

**Usage in code**:
```kotlin
// Before:
Box(modifier = Modifier.padding(DesignTokens.Spacing.lg.dp))

// After (optional, equally readable):
Box(modifier = Modifier.padding(DesignTokens.Spacing.lg.asDp()))
```

---

### Real-World Example: RestaurantCard with Helpers

**Before (verbose)**:
```kotlin
@Composable
fun RestaurantCardCompose(data: RestaurantCardData) {
    val titleStyle = DesignTokens.Typography.TextStyles.title1
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(DesignTokens.Colors.Background.card.toColorInt())
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.md.dp)
    ) {
        Text(
            text = data.restaurantName,
            style = TextStyle(
                fontWeight = FontWeight(titleStyle.fontWeight),
                fontSize = titleStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.dark.toColorInt())
        )
        
        Text(
            text = "★ ${data.rating}",
            color = Color(DesignTokens.Colors.Accent.star.toColorInt()),
            fontSize = 10.sp
        )
    }
}
```

**After (using helpers)**:
```kotlin
@Composable
fun RestaurantCardCompose(data: RestaurantCardData) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.Colors.Background.card.toComposeColor()
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.md.dp)
    ) {
        Text(
            text = data.restaurantName,
            style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
            color = DesignTokens.Colors.Text.dark.toComposeColor()
        )
        
        Text(
            text = "★ ${data.rating}",
            color = DesignTokens.Colors.Accent.star.toComposeColor()
        )
    }
}
```

Much cleaner! The helpers reduce boilerplate and make intent clearer.

---

## Android vs iOS: API Parity

Both platforms now have equally simple APIs:

| Task | Android | iOS |
|---|---|---|
| **Set text color** | `color = DesignTokens.Colors.Text.dark.toComposeColor()` | `.foregroundColor(.text.dark)` |
| **Apply text style** | `style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle()` | `.font(.title2)` |
| **Add padding** | `Modifier.padding(DesignTokens.Spacing.lg.dp)` | `.padding(CGFloat.spacingUI.lg)` |
| **Set background color** | `background = DesignTokens.Colors.Background.primary.toComposeColor()` | `.background(.background.primary)` |
| **Apply shadow** | `Modifier.shadow(elevation = 2.dp, ambientColor = Color.Black.copy(alpha = 0.1f))` | `.cardElevation()` |

Both platforms use **design tokens exclusively** — no hardcoded colors, fonts, or spacing anywhere in UI code.

---

# 🍎 iOS Implementation (SwiftUI)

Located in: `iosApp/iosApp/Core/DesignTokens/`

iOS provides **convenient extension APIs** for simple, readable token access:

## Color API Extension (Color.swift)

Simple color token access using namespaced Color extensions:

```swift
// iosApp/Core/DesignTokens/Extensions/Color.swift

extension Color {
    init(hex: String) { /* hex to Color conversion */ }
    
    // Namespaced color access
    static var text: ColorTextNamespace { ColorTextNamespace() }
    static var background: ColorBackgroundNamespace { ColorBackgroundNamespace() }
    static var accent: ColorAccentNamespace { ColorAccentNamespace() }
}

struct ColorTextNamespace {
    var dark: Color { DesignTokens.iOS.colorUI.text.dark }
    var light: Color { DesignTokens.iOS.colorUI.text.light }
    var subtitle: Color { DesignTokens.iOS.colorUI.text.subtitle }
    var footer: Color { DesignTokens.iOS.colorUI.text.footer }
    var picto: Color { DesignTokens.iOS.colorUI.text.picto }
}

struct ColorBackgroundNamespace {
    var primary: Color { DesignTokens.iOS.colorUI.background.primary }
    var card: Color { DesignTokens.iOS.colorUI.background.card }
    var filterDefault: Color { DesignTokens.iOS.colorUI.background.filterDefault }
}

struct ColorAccentNamespace {
    var selected: Color { DesignTokens.iOS.colorUI.accent.selected }
    var positive: Color { DesignTokens.iOS.colorUI.accent.positive }
    var negative: Color { DesignTokens.iOS.colorUI.accent.negative }
    var star: Color { DesignTokens.iOS.colorUI.accent.star }
    var brightRed: Color { DesignTokens.iOS.colorUI.accent.brightRed }
}

// Usage is simple:
Text("Hello").foregroundColor(.text.dark)
```

---

## Font API Extension (Font+DesignTokens.swift)

**Simple font API** — just use `.font(.headline1)` or `.font(.title2)`:

```swift
// iosApp/Core/DesignTokens/Extensions/Font+DesignTokens.swift

extension Font {
    // Text style fonts
    static var headline1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.headline1)
    }
    
    static var title1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.title1)
    }
    
    static var headline2: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.headline2)
    }
    
    static var title2: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.poppins, size: dt.typography.fontSizeUI.title2)
    }
    
    static var subtitle1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.subtitle1)
            .weight(.bold)
    }
    
    static var footer1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.inter, size: dt.typography.fontSizeUI.footer1)
            .weight(.medium)
    }
}

// Usage is super simple:
Text("Restaurant Name")
    .font(.title2)     // Automatically uses Poppins, 14pt
    .foregroundColor(.text.dark)
```

---

## Spacing API Extension (CGFloat+DesignTokens.swift)

**Simple spacing access** via `CGFloat.spacingUI` namespace:

```swift
// iosApp/Core/DesignTokens/Extensions/CGFloat+DesignTokens.swift

extension CGFloat {
    // Spacing tokens
    static var spacingUI: CGFloatSpacingNamespace { CGFloatSpacingNamespace() }
    
    // Icon sizes
    static var iconSmall: CGFloat { DesignTokens.iOS.size.iconUI.small }
    static var iconMedium: CGFloat { DesignTokens.iOS.size.iconUI.medium }
    static var iconLarge: CGFloat { DesignTokens.iOS.size.iconUI.large }
    
    // Card dimensions
    static var cardRestaurantWidth: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.width }
    static var cardRestaurantHeight: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.height }
    static var cardRestaurantImageHeight: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.imageHeight }
}

struct CGFloatSpacingNamespace {
    var none: CGFloat { DesignTokens.iOS.spacingUI.none }
    var xxs: CGFloat { DesignTokens.iOS.spacingUI.xxs }
    var xs: CGFloat { DesignTokens.iOS.spacingUI.xs }
    var sm: CGFloat { DesignTokens.iOS.spacingUI.sm }      // 8
    var md: CGFloat { DesignTokens.iOS.spacingUI.md }      // 13
    var lg: CGFloat { DesignTokens.iOS.spacingUI.lg }      // 16
    var xl: CGFloat { DesignTokens.iOS.spacingUI.xl }      // 18
}

struct CGFloatBorderRadiusNamespace {
    var none: CGFloat { DesignTokens.iOS.borderRadiusUI.none }
    var sm: CGFloat { DesignTokens.iOS.borderRadiusUI.sm }
    var md: CGFloat { DesignTokens.iOS.borderRadiusUI.md }
    var full: CGFloat { DesignTokens.iOS.borderRadiusUI.full }
}

// Usage:
VStack(spacing: CGFloat.spacingUI.md) {     // 13pt
    // children
}
.padding(CGFloat.spacingUI.lg)              // 16pt padding
.cornerRadius(CGFloat.borderRadiusUI.md)    // 12pt radius
```

---

## Shadow/Elevation API Extension (Shadow+DesignTokens.swift)

**Simple shadow application** via View extensions:

```swift
// iosApp/Core/DesignTokens/Extensions/Shadow+DesignTokens.swift

extension View {
    @ViewBuilder
    func cardElevation() -> some View {
        self.shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 2)
    }
    
    @ViewBuilder
    func filterElevation() -> some View {
        self.shadow(color: Color.black.opacity(0.05), radius: 1, x: 0, y: 1)
    }
}

struct ElevationShadow {
    static let card = ShadowDefinition(
        color: Color.black,
        opacity: 0.1,
        radius: 2,
        x: 0,
        y: 2
    )
    
    static let filter = ShadowDefinition(
        color: Color.black,
        opacity: 0.05,
        radius: 1,
        x: 0,
        y: 1
    )
}

// Usage:
Card {
    // content
}
.cardElevation()    // Applies 10% black shadow (elevated card)
```

---

## Screen Implementation Example

```swift
// iosApp/Features/Restaurants/RestaurantList/RestaurantListView.swift

struct RestaurantListView: View {
    @ObservedObject var viewModel: RestaurantListViewModel
    
    var body: some View {
        NavigationStack {
            VStack(spacing: CGFloat.spacingUI.md) {
                Text("Restaurants")
                    .font(.headline1)
                    .foregroundColor(.text.dark)
                
                List(viewModel.state.restaurants) { restaurant in
                    RestaurantCard(restaurant: restaurant)
                        .listRowInsets(EdgeInsets())
                        .listRowSeparator(.hidden)
                }
                .listStyle(.plain)
            }
            .background(.background.primary)
            .padding(CGFloat.spacingUI.lg)
            .navigationTitle("Munchies")
        }
    }
}

#Preview {
    RestaurantListView(
        viewModel: RestaurantListViewModel()
    )
}

// RestaurantCard using simple token APIs
struct RestaurantCard: View {
    let restaurant: Restaurant
    
    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat.spacingUI.md) {
            Image(uiImage: restaurant.image)
                .resizable()
                .scaledToFill()
                .frame(height: CGFloat.cardRestaurantImageHeight)
                .clipped()
                .cornerRadius(CGFloat.borderRadiusUI.md)
            
            VStack(alignment: .leading, spacing: CGFloat.spacingUI.sm) {
                Text(restaurant.name)
                    .font(.title2)
                    .foregroundColor(.text.dark)
                
                Text(restaurant.cuisine)
                    .font(.subtitle1)
                    .foregroundColor(.text.subtitle)
            }
            .padding(CGFloat.spacingUI.lg)
        }
        .frame(width: CGFloat.cardRestaurantWidth, height: CGFloat.cardRestaurantHeight)
        .background(.background.card)
        .cornerRadius(CGFloat.borderRadiusUI.md)
        .cardElevation()
    }
}
```

---

## Token Access Pattern Summary

**iOS provides convenient extensions for ultra-simple token access**:

| What | How | Example |
|---|---|---|
| **Colors** | `Color.text.dark`, `Color.accent.positive` | `.foregroundColor(.text.dark)` |
| **Fonts** | `.font(.headline1)`, `.font(.title2)` | `.font(.title2)` |
| **Spacing** | `CGFloat.spacingUI.lg`, `.md`, `.sm` | `.padding(CGFloat.spacingUI.lg)` |
| **Border Radius** | `CGFloat.borderRadiusUI.md` | `.cornerRadius(CGFloat.borderRadiusUI.md)` |
| **Shadows** | `.cardElevation()`, `.filterElevation()` | `.cardElevation()` |
| **Sizes** | `CGFloat.iconSmall`, `CGFloat.cardRestaurantWidth` | `.frame(width: CGFloat.cardRestaurantWidth)` |

All values are **automatically synced from DesignTokens** (the shared KMP module).

---

# 🔁 Design Token Generation Workflow

The complete flow from Figma to platform implementations:

```
Figma Design System (UI Components + Tokens)
            ↓
Export/Sync to design-tokens/resources/tokens.json
            ↓
Generate platform-specific implementations:
    ↙              ↓              ↖
Android        Shared        iOS
(Compose)   (DesignTokens.kt)  (SwiftUI)
    ↓              ↓              ↓
Direct token   Token refs    Extension APIs
access via      (values &    (.font(), 
DesignTokens   styles)      Color.*, etc)
    ↓              ↓              ↓
Use in UI       Used by       Use in UI
components     both           components
```

---

## Stage 1 — Normalize (Platform-Agnostic)

```bash
figma-kmp normalize --input raw.css
```

Output:
* Semantic layout structure
* Token names and definitions
* Component roles

**NOT platform-specific** — just normalized design data.

---

## Stage 2 — Generate Shared Tokens (commonMain)

```bash
figma-kmp generate --target common
```

Produces:
```
design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/
└── DesignTokens.kt
    ├── Colors (nested objects)
    ├── Typography (FontFamilies, FontSizes, TextStyles, etc.)
    ├── Spacing (scale tokens)
    ├── BorderRadius
    ├── Elevation
    └── Sizes
```

This file is **generated directly from tokens.json** and used by both platforms.

---

## Stage 3 — Generate Android Implementation (Jetpack Compose)

```bash
figma-kmp generate --target android
```

Produces:
```
androidApp/src/main/kotlin/io/umain/munchies/android/ui/
├── theme/
│   └── MunchiesTheme.kt
├── components/
│   ├── RestaurantCard.kt
│   ├── FilterChip.kt
│   └── /* ... */
└── screens/
    ├── RestaurantListScreen.kt
    └── RestaurantDetailScreen.kt
```

**Android Pattern**: Direct access to `DesignTokens` from shared module:
```kotlin
Color(android.graphics.Color.parseColor(DesignTokens.Colors.Text.dark))
DesignTokens.Spacing.lg.dp
DesignTokens.Typography.TextStyles.title1
```

---

## Stage 4 — Generate iOS Implementation (SwiftUI)

```bash
figma-kmp generate --target ios
```

Produces:
```
iosApp/iosApp/Core/DesignTokens/
├── DesignTokens.swift
│   └── IOSDesignTokens (wrapper with platform-specific types)
└── Extensions/
    ├── Font+DesignTokens.swift         # .font(.headline1)
    ├── Color.swift                      # Color.text.dark
    ├── CGFloat+DesignTokens.swift      # CGFloat.spacingUI.lg
    └── Shadow+DesignTokens.swift       # .cardElevation()

iosApp/iosApp/Features/
├── Restaurants/
│   ├── RestaurantListView.swift
│   ├── RestaurantDetailView.swift
│   └── Components/
│       └── RestaurantCardView.swift
└── /* ... */
```

**iOS Pattern**: Convenient extension APIs for token access:
```swift
.font(.title2)
.foregroundColor(.text.dark)
.padding(CGFloat.spacingUI.lg)
.cornerRadius(CGFloat.borderRadiusUI.md)
.cardElevation()
```

---

## Complete One-Shot Generation

```bash
figma-kmp run \
  --input ./design-tokens/resources/tokens.json \
  --targets android,ios
```

This executes all stages in sequence:
1. Parse tokens.json
2. Generate shared DesignTokens.kt
3. Generate Android Compose implementations
4. Generate iOS SwiftUI implementations
5. Create @Preview (Android) and #Preview (iOS) wrappers

---

# 🛠️ Token Generation Tooling & Implementation

This section provides practical guidance on how design tokens flow from `tokens.json` through generation tools to platform-specific implementations.

## tokens.json Structure

The single source of truth for all design values:

```json
{
  "$schema": "https://tr.designtokens.org/format/#design-token",
  "colors": {
    "text": {
      "dark": { "value": "#1F2B2E" },
      "light": { "value": "#FFFFFF" }
    },
    "background": {
      "primary": { "value": "#F8F8F8" }
    },
    "accent": {
      "positive": { "value": "#2ECC71" }
    }
  },
  "typography": {
    "fontFamilies": {
      "helvetica": { "value": "Helvetica" },
      "poppins": { "value": "Poppins" }
    },
    "fontWeights": {
      "regular": { "value": 400 },
      "bold": { "value": 700 }
    },
    "fontSizes": {
      "headline1": { "value": 24 },
      "title2": { "value": 14 }
    },
    "lineHeights": {
      "headline1": { "value": 16 },
      "title2": { "value": 20 }
    },
    "textStyles": {
      "headline1": {
        "fontFamily": { "$ref": "typography.fontFamilies.helvetica" },
        "fontWeight": { "$ref": "typography.fontWeights.regular" },
        "fontSize": { "$ref": "typography.fontSizes.headline1" },
        "lineHeight": { "$ref": "typography.lineHeights.headline1" }
      }
    }
  },
  "spacing": {
    "none": { "value": 0 },
    "sm": { "value": 8 },
    "md": { "value": 13 },
    "lg": { "value": 16 }
  },
  "borderRadius": {
    "none": { "value": 0 },
    "md": { "value": 12 }
  },
  "elevation": {
    "card": { "value": "#0000001A" },
    "filter": { "value": "#0000000A" }
  },
  "sizes": {
    "icon": {
      "small": { "value": 10 },
      "medium": { "value": 12 }
    }
  }
}
```

**Key characteristics**:
- **Flat structure with nested objects**: Colors, Typography, Spacing, etc.
- **Token references (`$ref`)**: TextStyles reference FontFamilies, FontSizes, etc., ensuring consistency
- **Standard W3C Design Token format**: Compatible with Figma Tokens Studio
- **Single source of truth**: All token values defined once, referenced everywhere

---

## Generating DesignTokens.kt (Shared Module)

The shared KMP module requires `DesignTokens.kt` to be generated from `tokens.json`.

### Manual Generation Process

**Option 1: Using a code generator tool** (recommended for CI/CD):

```bash
# Pseudo-code for token generator
npx design-tokens-generator \
  --input design-tokens/resources/tokens.json \
  --output design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/DesignTokens.kt \
  --language kotlin
```

**Option 2: Using a Gradle task** (if integrated in your build):

```bash
./gradlew generateDesignTokens
```

### Generated DesignTokens.kt Structure

The generator produces a Kotlin object with nested structure matching tokens.json:

```kotlin
object DesignTokens {
    object Colors {
        object Text {
            const val dark = "#1F2B2E"
            const val light = "#FFFFFF"
        }
        object Background {
            const val primary = "#F8F8F8"
            const val card = "#FFFFFF"
        }
        object Accent {
            const val positive = "#2ECC71"
            const val negative = "#C0392B"
        }
    }
    
    object Typography {
        object FontFamilies {
            const val helvetica = "Helvetica"
            const val poppins = "Poppins"
        }
        
        object FontWeights {
            const val regular = 400
            const val bold = 700
        }
        
        object FontSizes {
            const val headline1 = 24
            const val title2 = 14
        }
        
        object LineHeights {
            const val headline1 = 16
            const val title2 = 20
        }
        
        data class TextStyle(
            val fontFamily: String,
            val fontWeight: Int,
            val fontSize: Int,
            val lineHeight: Int
        )
        
        object TextStyles {
            val headline1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.headline1,
                lineHeight = LineHeights.headline1
            )
            val title2 = TextStyle(
                fontFamily = FontFamilies.poppins,
                fontWeight = FontWeights.bold,
                fontSize = FontSizes.title2,
                lineHeight = LineHeights.title2
            )
        }
    }
    
    object Spacing {
        const val none = 0
        const val sm = 8
        const val md = 13
        const val lg = 16
    }
    
    object BorderRadius {
        const val none = 0
        const val md = 12
    }
    
    object Elevation {
        const val card = "#0000001A"
        const val filter = "#0000000A"
    }
    
    object Sizes {
        object Icon {
            const val small = 10
            const val medium = 12
        }
    }
}
```

---

## Generating Platform Implementations

### Android: TextStyleMapper.kt

Auto-generated extension for Jetpack Compose:

```kotlin
// Generated: androidApp/src/main/kotlin/io/umain/munchies/android/ui/TextStyleMapper.kt

import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.umain.munchies.designtokens.DesignTokens
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable

@Composable
fun DesignTokens.Typography.TextStyle.toComposeTextStyle(): ComposeTextStyle =
    ComposeTextStyle(
        fontFamily = resolveFontFamily(fontFamily),
        fontWeight = FontWeight(fontWeight),
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp
    )

@Composable
private fun resolveFontFamily(fontFamily: String): FontFamily =
    with(typography) {
        when (fontFamily) {
            DesignTokens.Typography.FontFamilies.helvetica -> bodyLarge.fontFamily
            DesignTokens.Typography.FontFamilies.poppins -> titleMedium.fontFamily
            else -> FontFamily.Default
        }
    }
```

**Generation trigger**: Runs after `DesignTokens.kt` changes

---

### iOS: DesignTokens.swift Extension

Auto-generated wrapper for SwiftUI:

```swift
// Generated: iosApp/iosApp/Core/DesignTokens/DesignTokens.swift

import SwiftUI
import shared  // KMP shared module

extension DesignTokens {
    static var iOS: IOSDesignTokens {
        IOSDesignTokens.shared
    }
}

class IOSDesignTokens {
    static let shared = IOSDesignTokens()
    
    private init() {}
    
    // Raw color tokens (hex strings)
    struct ColorTokens {
        struct Text {
            let dark: String
            let light: String
            
            init() {
                let tokensColorsText = DesignTokens.ColorsText.shared
                self.dark = tokensColorsText.dark
                self.light = tokensColorsText.light
            }
        }
        let text = Text()
    }
    
    // Converted Color objects for SwiftUI
    struct ColorUITokens {
        struct Text {
            var dark: Color { Color(hex: DesignTokens.ColorsText.shared.dark) }
            var light: Color { Color(hex: DesignTokens.ColorsText.shared.light) }
        }
        let text = Text()
    }
    
    struct TypographyTokens {
        struct FontSizeUI {
            let headline1: CGFloat = CGFloat(DesignTokens.TypographyFontSizes.shared.headline1)
            let title2: CGFloat = CGFloat(DesignTokens.TypographyFontSizes.shared.title2)
        }
        let fontSizeUI = FontSizeUI()
    }
    
    struct SpacingUITokens {
        let none: CGFloat = CGFloat(DesignTokens.Spacing.shared.none)
        let sm: CGFloat = CGFloat(DesignTokens.Spacing.shared.sm)
        let md: CGFloat = CGFloat(DesignTokens.Spacing.shared.md)
        let lg: CGFloat = CGFloat(DesignTokens.Spacing.shared.lg)
    }
    
    let colorUI = ColorUITokens()
    let typography = TypographyTokens()
    let spacingUI = SpacingUITokens()
}
```

**Key points**:
- Wraps shared `DesignTokens` (from KMP module)
- Provides platform-specific types (`Color`, `CGFloat`)
- Generated from tokens.json structure + platform conventions

---

## Workflow: Edit → Generate → Verify

### Step 1: Edit tokens.json

```bash
# Update tokens in the single source of truth
vi design-tokens/resources/tokens.json
```

Example change:
```json
{
  "colors": {
    "accent": {
      "positive": { "value": "#27AE60" }  // Changed from #2ECC71
    }
  }
}
```

### Step 2: Generate Platform Code

```bash
# Regenerate all platform implementations
./gradlew generateDesignTokens

# Or for the one-shot pipeline:
figma-kmp run \
  --input ./design-tokens/resources/tokens.json \
  --targets android,ios
```

This produces:
- ✅ Updated `DesignTokens.kt` (shared)
- ✅ Updated `TextStyleMapper.kt` (Android)
- ✅ Updated `DesignTokens.swift` (iOS)
- ✅ Updated extension files (iOS)

### Step 3: Verify Generation

```bash
# Check git diff to ensure only tokens changed
git diff design-tokens/

# Verify no syntax errors
./gradlew design-tokens:build
./gradlew iosApp:build
```

### Step 4: Commit & Deploy

```bash
# Commit token changes and generated code together
git add design-tokens/
git add androidApp/src/main/kotlin/io/umain/munchies/android/ui/TextStyleMapper.kt
git add iosApp/iosApp/Core/DesignTokens/
git commit -m "chore: update design tokens (positive accent color)"
```

---

## Incremental Updates: Don't Regenerate Manually

**WRONG** ❌: Manually editing generated files:
```kotlin
// ❌ DON'T do this - changes will be lost on next generation
object DesignTokens {
    object Colors {
        object Accent {
            const val positive = "#27AE60"  // Manually edited!
        }
    }
}
```

**RIGHT** ✅: Edit tokens.json, regenerate:
```bash
# Edit tokens.json
# Run generation
./gradlew generateDesignTokens
# All platform files automatically synced
```

---

# 📋 Best Practices: Keeping Tokens Synchronized

Ensuring single source of truth across Android, iOS, and Figma:

## 1. **Always Update tokens.json First**

| ❌ Wrong | ✅ Correct |
|---------|-----------|
| Edit `DesignTokens.kt` directly | Edit `tokens.json` |
| Change colors in `Color.swift` manually | Edit `tokens.json` → regenerate |
| Hardcode spacing in components | Reference `DesignTokens.Spacing.*` |

**Why?** Single edit in `tokens.json` automatically syncs to all platforms.

---

## 2. **Regenerate All Platform Code Together**

When tokens change, regenerate everything:

```bash
# ✅ GOOD: One command regenerates all platforms
./gradlew generateDesignTokens

# ✅ GOOD: One-shot pipeline
figma-kmp run --targets android,ios
```

Never regenerate Android without iOS (or vice versa):
```bash
# ❌ BAD: Platform parity breaks
./gradlew generateAndroidTokens  # Only Android updated
```

---

## 3. **Validate Consistency After Generation**

```bash
# Build all platforms to catch errors early
./gradlew design-tokens:build
./gradlew androidApp:build
./gradlew iosApp:build
```

**Common issues**:
- Token references not resolved (`$ref` broken)
- Font family mismatch between platforms
- Spacing value conflicts

---

## 4. **Commit tokens.json + Generated Files Together**

```bash
# ✅ Good: Atomic commit with all changes
git commit -m "chore: update design tokens"

# ❌ Bad: Separate commits for tokens and generated code
# (creates merge conflicts, inconsistency)
```

---

## 5. **CI/CD: Automate Generation**

Prevent manual regeneration errors:

```yaml
# .github/workflows/tokens.yml (GitHub Actions example)
name: Regenerate Design Tokens

on:
  push:
    paths:
      - 'design-tokens/resources/tokens.json'

jobs:
  generate:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Generate Tokens
        run: ./gradlew generateDesignTokens
      
      - name: Verify Builds
        run: |
          ./gradlew design-tokens:build
          ./gradlew androidApp:build
          ./gradlew iosApp:build
      
      - name: Commit Generated Files
        run: |
          git add design-tokens/ androidApp/ iosApp/
          git commit -m "chore: regenerate design tokens"
          git push
```

**Benefits**:
- No manual generation mistakes
- Automatic consistency checks
- Prevents uncommitted generated files

---

## 6. **Figma → tokens.json Sync Strategy**

When design system updates in Figma:

```
Figma (Design System)
        ↓
Figma Tokens Studio Export
        ↓
design-tokens/resources/tokens.json
        ↓
./gradlew generateDesignTokens
        ↓
Android + iOS Code Updated
        ↓
Build Verification
        ↓
Commit & Deploy
```

**Automated approach** (recommended):
- Figma Tokens Studio webhook → tokens.json update
- GitHub Actions → automatic regeneration
- CI/CD → verification and commit

---

## 7. **Handling Platform-Specific Overrides**

**Problem**: Some tokens need platform tweaks (e.g., iOS uses different font sizes).

**Solution**: 
- Keep base tokens in `tokens.json`
- Platform adapters apply conversions:

```kotlin
// Android: Use pixel values as-is
Modifier.padding(DesignTokens.Spacing.lg.dp)  // 16.dp

// iOS: Convert to CGFloat (same logical value)
.padding(CGFloat.spacingUI.lg)  // 16pt
```

**Never duplicate tokens** in platform-specific files!

---

## 8. **Validation: Token Completeness Check**

After updating tokens.json, verify:

```bash
#!/bin/bash
# Pseudo-script for validation

# 1. Check all color tokens have hex values
jq '.colors | .. | select(has("value")) | .value' tokens.json | grep "^#" || exit 1

# 2. Check all typography tokens referenced
jq '.typography.textStyles | .. | select(has("$ref"))' tokens.json || exit 1

# 3. Verify no orphaned references
jq '.typography.textStyles[] | .. | select(has("$ref")) | .$ref' tokens.json | \
  while read ref; do
    jq "getpath(\"$ref\") | select(. != null)" tokens.json || exit 1
  done

echo "✅ All tokens valid"
```

---

## 9. **Example: Adding a New Token**

To add a new "warning" accent color:

### Step 1: Edit tokens.json

```json
{
  "colors": {
    "accent": {
      "positive": { "value": "#2ECC71" },
      "negative": { "value": "#C0392B" },
      "warning": { "value": "#F39C12" }  // NEW
    }
  }
}
```

### Step 2: Regenerate

```bash
./gradlew generateDesignTokens
```

### Step 3: Use in Code

**Android**:
```kotlin
Text(
    text = "Warning",
    color = DesignTokens.Colors.Accent.warning.toComposeColor()
)
```

**iOS**:
```swift
Text("Warning")
    .foregroundColor(.accent.warning)  // Automatically available!
```

**Figma**: 
- Update design system
- Export new token
- CI/CD regenerates
- Done ✅

---

# 🔗 Integration Points

## Shared Module (design-tokens)

**Location**: `design-tokens/src/commonMain/kotlin/io/umain/munchies/designtokens/DesignTokens.kt`

```kotlin
/**
 * Single source of truth for all design values.
 * Generated from design-tokens/resources/tokens.json
 * 
 * Used by both Android and iOS platforms.
 * Token names and values are platform-agnostic.
 */
object DesignTokens {
    object Colors { /* ... */ }
    object Typography { /* ... */ }
    object Spacing { /* ... */ }
    object BorderRadius { /* ... */ }
    object Elevation { /* ... */ }
    object Sizes { /* ... */ }
}
```

**Usage**:
- **Android**: Direct import and use
- **iOS**: Wrapped and exposed via extension APIs

---

## Android Module (androidApp)

**Direct token access**:
```kotlin
import io.umain.munchies.designtokens.DesignTokens

// In UI code:
Color(android.graphics.Color.parseColor(DesignTokens.Colors.Accent.positive))
Modifier.padding(DesignTokens.Spacing.lg.dp)
```

---

## iOS Module (iosApp)

**Extension-based access** (simple and readable):
```swift
// In UI code:
.foregroundColor(.text.dark)
.font(.title2)
.padding(CGFloat.spacingUI.lg)
.cardElevation()
```

These extensions import from the shared `DesignTokens` but present them in a SwiftUI-friendly API.

---

---

# ✅ Parity Verification Checklist

Ensure both Android and iOS implementations match using design tokens:

| Aspect | Android (Compose) | iOS (SwiftUI) | Token Source |
|---|---|---|---|
| **Colors** | `DesignTokens.Colors.*` | `Color.text.*`, `Color.accent.*` | `DesignTokens.Colors` |
| **Typography** | `DesignTokens.Typography.TextStyles.*` | `.font(.headline1)`, `.font(.title2)` | `DesignTokens.Typography` |
| **Spacing** | `DesignTokens.Spacing.lg.dp` | `CGFloat.spacingUI.lg` | `DesignTokens.Spacing` |
| **Border Radius** | `DesignTokens.BorderRadius.md.dp` | `CGFloat.borderRadiusUI.md` | `DesignTokens.BorderRadius` |
| **Shadows** | `Modifier.shadow()` | `.cardElevation()`, `.filterElevation()` | `DesignTokens.Elevation` |
| **Component Sizes** | `DesignTokens.Sizes.*` | `CGFloat.cardRestaurantWidth` | `DesignTokens.Sizes` |
| **Preview generation** | `@Preview` | `#Preview` | Both auto-generated |
| **Font families** | Via `TextStyle` | Via `.custom()` or Font extensions | `DesignTokens.Typography` |
| **Font sizes** | Via `TextStyle` | Via Font extensions or `fontSizeUI` | `DesignTokens.Typography` |
| **Font weights** | Via `TextStyle` | Via Font extensions with `.weight()` | `DesignTokens.Typography` |

---

# 🎨 Design System Examples with Tokens

### Example 1: Text with Typography Token

**Figma Design**: Headline text in Helvetica, 24pt bold

**Android (Compose)**:
```kotlin
Text(
    text = "Restaurants",
    style = DesignTokens.Typography.TextStyles.headline1.toComposeTextStyle(),
    color = DesignTokens.Colors.Text.dark.toComposeColor()
)
```

**iOS (SwiftUI)**:
```swift
Text("Restaurants")
    .font(.headline1)
    .foregroundColor(.text.dark)
```

Both render identically using the same token values.

---

### Example 2: Padded Container with Spacing Tokens

**Figma Design**: Box with 16pt padding (lg spacing), white background

**Android (Compose)**:
```kotlin
Box(
    modifier = Modifier
        .padding(DesignTokens.Spacing.lg.dp)  // 16.dp
        .background(
            DesignTokens.Colors.Background.card.toComposeColor()
        )
) {
    // content
}
```

**iOS (SwiftUI)**:
```swift
VStack {
    // content
}
.padding(CGFloat.spacingUI.lg)  // 16pt
.background(.background.card)
```

Both use the same spacing value from `DesignTokens.Spacing.lg`.

---

### Example 3: Restaurant Card with Elevation & Spacing

**Figma Design**: Card with 16pt padding, 12pt corner radius, 10% black shadow

**Android (Compose)**:
```kotlin
Card(
    shape = RoundedCornerShape(DesignTokens.BorderRadius.md.dp),  // 12dp
    colors = CardDefaults.cardColors(
        containerColor = DesignTokens.Colors.Background.card.toComposeColor()
    ),
    modifier = Modifier
        .padding(DesignTokens.Spacing.lg.dp)  // 16dp
        .shadow(
            elevation = 2.dp,
            ambientColor = Color.Black.copy(alpha = 0.1f)  // elevation.card (10%)
        )
) {
    Column(
        modifier = Modifier.padding(DesignTokens.Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md.dp)
    ) {
        Image(/* ... */)
        Text(
            text = "Restaurant Name",
            style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
            color = DesignTokens.Colors.Text.dark.toComposeColor()
        )
    }
}
```

**iOS (SwiftUI)**:
```swift
VStack(spacing: CGFloat.spacingUI.md) {  // 13pt gaps
    Image(/* ... */)
    Text("Restaurant Name")
        .font(.title1)
        .foregroundColor(.text.dark)
}
.padding(CGFloat.spacingUI.lg)  // 16pt padding
.background(.background.card)
.cornerRadius(CGFloat.borderRadiusUI.md)  // 12pt radius
.cardElevation()  // 10% black shadow
```

All spacing, radius, and elevation values come from the same `DesignTokens` source.

---

### Example 4: Vertical Stack with Multiple Spacing Values

**Figma Design**: List with 13pt gap between items, 8pt internal spacing

**Android (Compose)**:
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(
        DesignTokens.Spacing.md.dp  // 13dp between items
    )
) {
    items(items) { item ->
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm.dp),  // 8dp
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs.dp)  // 3dp
        ) {
            Text(
                text = item.name,
                 style = DesignTokens.Typography.TextStyles.title2.toComposeTextStyle(),
                 color = DesignTokens.Colors.Text.dark.toComposeColor()
             )
             Text(
                 text = item.value,
                 style = DesignTokens.Typography.TextStyles.subtitle1.toComposeTextStyle(),
                 color = DesignTokens.Colors.Text.subtitle.toComposeColor()
             )
         }
     }
}
```

**iOS (SwiftUI)**:
```swift
List(items) { item in
    HStack(spacing: CGFloat.spacingUI.xs) {  // 3pt
        Text(item.name)
            .font(.title2)
            .foregroundColor(.text.dark)
        
        Text(item.value)
            .font(.subtitle1)
            .foregroundColor(.text.subtitle)
    }
    .padding(.horizontal, CGFloat.spacingUI.sm)  // 8pt
}
.listStyle(.plain)
```

The spacing scale is consistent: `none` (0), `xxs` (2), `xs` (3), `sm` (8), `md` (13), `lg` (16), `xl` (18).

---

---

# 📊 Responsibility Matrix

| Layer | Android | iOS | Shared |
|---|---|---|---|
| **UI Components** | Compose + Material3 | SwiftUI | ✗ |
| **Theme/Design System** | Material3 | Custom SwiftUI | ✗ |
| **Semantic Tokens** | Used from shared | Used from shared | ✓ (Define) |
| **UI Contracts** | Implement actual | Implement actual | ✓ (Define expect) |
| **Business Logic** | Uses from shared | Uses from shared | ✓ (Implement) |
| **Navigation** | Compose navigation | SwiftUI NavigationStack | ✗ |
| **State Management** | ViewModel + Compose | @ObservedObject | ✗ (Shared state) |

---

# 🚀 Final Corrected Execution Flow

```
Figma CSS
   ↓
Normalize → Semantic Layout + Token Names (Platform-Agnostic)
   ↓
Split
 ↙   ↖
Android    iOS
  ↓        ↓
Generate   Generate
Compose    SwiftUI
  ↓        ↓
Compose    SwiftUI
Code       Code
  ↓        ↓
Material3  Custom
Theme      Theme
  ↓        ↓
@Preview   #Preview
Validation Validation
```

---

# 🧠 Core Principles for KMP UI

1. **Design Once**: Normalize and specify design once (platform-agnostic)
2. **Implement Twice**: Implement UI separately for each platform
3. **Token Parity**: Semantic tokens map to platform-native design systems
4. **Component Equivalence**: Use platform-native components that provide equivalent UX
5. **Pure Separation**: No shared UI code; only contracts and tokens are shared
6. **Preview Everything**: Both platforms must have preview/live preview support
7. **Maintainability**: Changes to design tokens ripple automatically to both platforms