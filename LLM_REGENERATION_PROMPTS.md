# LLM Prompts for Automated Localization File Generation

This document contains 5 LLM prompts that can be used to automatically regenerate all localization-related files from the `strings-catalog.json` single source of truth.

## Overview

The localization system consists of these key files that need to stay in sync:

1. `TextId.kt` - Kotlin sealed class with all string identifiers
2. `TextIdHelper.kt` - Mapping function from TextId → dot-notation keys
3. `strings.xml` - Android resource strings
4. `Localizable.strings` - iOS resource strings
5. `TextIdHelper.swift` - iOS Swift bridge with extensions

All these files should be regenerated from `strings-catalog.json` to maintain consistency.

---

## Prompt 1: Generate TextId.kt (Kotlin Sealed Class)

**File to generate:** `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt`

### LLM Prompt:

```
You are a Kotlin code generator. Your task is to generate a Kotlin sealed class file that defines all translatable string identifiers for the Munchies mobile app.

Use the attached strings-catalog.json as the source of truth. For each string entry in the catalog, create an object declaration in the sealed class.

Requirements:
- Package: io.umain.munchies.core.ui
- Class name: TextId (sealed class)
- For each string in the catalog, create: object {id} : TextId()
- Group the objects by category with comments (e.g., /* App */, /* Restaurant */, /* Navigation Tabs */, etc.)
- Keep the order from strings-catalog.json
- Total objects should match the number of "strings" array items in the catalog

Input: strings-catalog.json (attached)
Output: Complete TextId.kt file ready to use

Important: Do NOT include any implementation beyond the sealed class definition. This is just type definitions.
```

---

## Prompt 2: Generate TextIdHelper.kt (Mapping Function)

**File to generate:** `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt`

### LLM Prompt:

```
You are a Kotlin code generator. Your task is to generate a mapping function that converts TextId objects to their corresponding translation keys.

Use the attached strings-catalog.json as the source of truth. For each string entry, create a when branch that maps the TextId object to its "key" field.

Requirements:
- Package: io.umain.munchies.core.ui
- Function: fun mapTextIdToKey(textId: TextId): String
- Use a when expression to match each TextId object
- Map each object to its "key" value from strings-catalog.json
- Order the when branches by category, matching TextId.kt structure
- Include the header comment explaining the function's purpose
- Add category comments above related groups (e.g., // App, // Restaurant, etc.)

Input: strings-catalog.json (attached)
Output: Complete TextIdHelper.kt file ready to use

Important: The mapping keys MUST exactly match the "key" field in strings-catalog.json (dot-notation).
```

---

## Prompt 3: Generate strings.xml (Android Resources)

**File to generate:** `androidApp/src/main/res/values/strings.xml`

### LLM Prompt:

```
You are an Android resource generator. Your task is to generate an Android strings.xml file with all translatable strings for the Munchies app.

Use the attached strings-catalog.json as the source of truth. For each string entry, create a <string> resource element using the Android-specific naming convention.

Requirements:
- XML version: 1.0, encoding: utf-8
- Root element: <resources>
- For each string in catalog, create: <string name="{android}"><value></string>
- Use the "android" field from catalog for resource name (replaces dots with underscores if needed)
- Use the "value" field for the string content
- Include XML comments for each category (e.g., <!-- App -->, <!-- Restaurant List -->, etc.)
- Keep the order from strings-catalog.json
- For strings with placeholders, use %s for formatting (as shown in catalog)
- Properly escape XML special characters if needed

Input: strings-catalog.json (attached)
Output: Complete strings.xml file ready to use in Android project

Important: Resource names must follow Android naming conventions (lowercase_with_underscores). The "android" field already has the correct name.
```

---

## Prompt 4: Generate Localizable.strings (iOS Resources)

**File to generate:** `iosApp/iosApp/Resources/en.lproj/Localizable.strings`

### LLM Prompt:

```
You are an iOS localization string generator. Your task is to generate an iOS Localizable.strings file with all translatable strings for the Munchies app.

Use the attached strings-catalog.json as the source of truth. For each string entry, create a localization key-value pair in the standard format.

Requirements:
- Format: "key" = "value";
- Use the "key" field from catalog (dot-notation keys like "app.title")
- Use the "value" field for the string content
- Include comments for each category (e.g., /* App */, /* Restaurant */, etc.)
- Keep the order from strings-catalog.json
- For strings with placeholders, use %@ for iOS string formatting (not %s)
- Properly escape special characters as needed for .strings format
- Ensure each key-value pair ends with a semicolon

Input: strings-catalog.json (attached)
Output: Complete Localizable.strings file ready to use in iOS project

Important: iOS .strings format uses %@ for placeholders, not %s. Strings with placeholders in catalog have "placeholders" field set - convert those placeholder markers appropriately.
```

---

## Prompt 5: Generate TextIdHelper.swift (iOS Swift Bridge)

**File to generate:** `iosApp/iosApp/Core/Localization/TextIdHelper.swift`

### LLM Prompt:

```
You are a Swift code generator. Your task is to generate a Swift extension file that provides convenient access to TextId objects from the shared Kotlin code.

Use the attached strings-catalog.json as the source of truth. Create two extensions: one for a computed property "localized", and one with static properties for each TextId.

Requirements:
- Import Foundation and shared
- First extension: TextId with computed property var localized: String { return tr(self) }
- Second extension: TextId with static properties for each string
- For each string in catalog, create: static var {camelCase}: TextId { TextId.{PascalCase}() }
- Use the "id" field from catalog to generate property names (convert to camelCase)
- Order static properties by category, matching TextId structure
- Add comment sections for each category (e.g., // App, // Restaurant, etc.)
- Include the tr() helper function that bridges to Kotlin (provided below)

Helper function to include at the top:
```swift
func tr(_ textId: TextId, _ args: Any...) -> String {
    return TranslationKt.tr(textId: textId, args: KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    })
}
```

Input: strings-catalog.json (attached)
Output: Complete TextIdHelper.swift file ready to use

Important: Swift static property names must be in camelCase, not PascalCase. The TextId class names are PascalCase (like AppTitle) but the static properties should be camelCase (like appTitle).
```

---

## Usage Instructions

To regenerate any of the 5 files:

1. **Prepare**: Ensure strings-catalog.json is up-to-date with all current strings
2. **Select**: Choose which file(s) need regeneration
3. **Attach**: Include strings-catalog.json with the relevant prompt
4. **Run**: Send the prompt to Claude or similar LLM
5. **Verify**: Check that:
   - All 19 strings are present
   - Categories match
   - Names/keys are correctly transformed for each platform
   - No syntax errors
6. **Replace**: Copy the generated content to the target file
7. **Validate**: Run the validation script (see validation-script.sh) to ensure all files sync

## Batch Regeneration

To regenerate all 5 files at once:

1. Create a new prompt that concatenates all 5 prompts above
2. Add a final instruction: "Generate all 5 files in sequence. For each file, clearly label with '=== FILE: [filename] ===' before the content."
3. The LLM will output all files separated by clear markers for easy extraction

## Notes

- These prompts are designed to be platform-agnostic - they work with any LLM capable of code generation (GPT, Claude, etc.)
- The prompts reference "attached" for strings-catalog.json - in practice, you can paste the JSON content or reference the file path
- Always verify generated code for syntax errors and logical correctness before committing
- Consider version control - commit changes to strings-catalog.json FIRST, then regenerate dependent files
