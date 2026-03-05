# Munchies Localization System - Quick Start Guide

**Status**: ✅ Complete - All 19 strings synchronized across platforms

---

## What Was Done

### 1. Fixed Missing Strings ✅
- Added 6 missing strings to Android and iOS resource files
- All 19 strings now properly localized

### 2. Created Infrastructure ✅
- **strings-catalog.json**: Single source of truth for all strings
- **validate-localization.py**: Automated validation script
- **LLM_REGENERATION_PROMPTS.md**: 5 prompts for automated file generation
- **COMPOSE_RESOURCES_MIGRATION_PLAN.md**: Strategic modernization roadmap
- **IMPLEMENTATION_SUMMARY.md**: Complete overview

---

## Using the Validation Script

```bash
# Quick validation
python3 validate-localization.py

# Detailed output
python3 validate-localization.py --verbose

# In CI/CD (fail if out of sync)
python3 validate-localization.py || exit 1
```

Expected output (all should pass):
```
✓ TextId.kt completeness
✓ TextIdHelper.kt mappings
✓ Android strings.xml completeness
✓ iOS Localizable.strings completeness
✓ TextIdHelper.swift static properties
✓ Key consistency

SUMMARY: 6 passed, 0 failed
```

---

## Adding a New String

### Option A: Manual (5 minutes)

1. Add entry to `strings-catalog.json`
2. Add to `TextId.kt` as new object
3. Add mapping to `TextIdHelper.kt`
4. Add to `androidApp/src/main/res/values/strings.xml`
5. Add to `iosApp/iosApp/Resources/en.lproj/Localizable.strings`
6. Run `python3 validate-localization.py` to verify

### Option B: LLM-Assisted (< 5 minutes)

1. Add entry to `strings-catalog.json`
2. Open `LLM_REGENERATION_PROMPTS.md`
3. Choose which file(s) to regenerate
4. Copy the relevant prompt(s)
5. Send to Claude with updated `strings-catalog.json`
6. Replace generated files
7. Run validation script

---

## File Reference

| File | Purpose |
|------|---------|
| `strings-catalog.json` | **Single source of truth** - JSON definition of all 19 strings |
| `validate-localization.py` | **Automated validation** - Python script to check all files stay in sync |
| `LLM_REGENERATION_PROMPTS.md` | **5 LLM prompts** - Copy-paste into Claude to regenerate files |
| `LOCALIZATION_ANALYSIS.md` | **Detailed analysis** - Deep dive into current implementation |
| `IMPLEMENTATION_SUMMARY.md` | **Complete overview** - What was done and why |
| `COMPOSE_RESOURCES_MIGRATION_PLAN.md` | **Future roadmap** - How to modernize to Compose Resources |

---

## Current String Inventory (19 total)

**App Core** (2)
- app.title → "Munchies"

**Restaurant** (4)
- restaurant.list.title → "Restaurants"
- restaurant.detail.title → "Restaurant Details"
- filter.all → "All Restaurants"
- restaurant.status.open → "Open"
- restaurant.status.closed → "Closed"

**Accessibility** (4)
- accessibility.restaurant.card → "Restaurant card for %s"
- accessibility.filter.chip → "Filter by %s"
- accessibility.filter.selected → "Filter %s selected"
- accessibility.back.button → "Back"

**Errors & Loading** (3)
- error.loading → "Failed to load data"
- error.network → "Network error"
- loading → "Loading…"

**Navigation Tabs** (2)
- tab.restaurants → "Restaurants"
- tab.settings → "Settings"

**Settings** (4)
- settings.title → "Settings"
- settings.dark.mode → "Dark Mode"
- settings.notifications → "Notifications"
- settings.about → "About"

---

## Platform File Locations

| Platform | File | Format |
|----------|------|--------|
| Android | `androidApp/src/main/res/values/strings.xml` | XML (`name="key"`) |
| iOS | `iosApp/iosApp/Resources/en.lproj/Localizable.strings` | Strings (`"key" = "value"`) |
| Shared | `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt` | Kotlin (sealed class) |
| Shared | `core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt` | Kotlin (mapping function) |
| iOS Swift | `iosApp/iosApp/Core/Localization/TextIdHelper.swift` | Swift (extensions) |

---

## Next Steps (Recommended)

### This Week
- [ ] Review this document with team
- [ ] Test `validate-localization.py` script
- [ ] Add validation to pre-commit hooks or CI

### Next Sprint
- [ ] Try LLM-assisted string generation with a test string
- [ ] Document team workflow for adding strings
- [ ] Set up CI to run validation on every PR

### Future (2-4 Sprints)
- [ ] Plan Compose Multiplatform Resources migration (read COMPOSE_RESOURCES_MIGRATION_PLAN.md)
- [ ] Execute migration (2-3 weeks)
- [ ] Add support for multiple languages

---

## Troubleshooting

### Validation fails - "Mismatched" error

Check the specific file mentioned:
```bash
python3 validate-localization.py --verbose

# Look for "Mismatched:" lines
# Fix the value in the file to match strings-catalog.json
# Run validation again
```

### String not appearing in app

1. Check it's in `strings-catalog.json`
2. Verify it's in all 5 platform files
3. Run validation to see what's out of sync
4. Update missing files

### How to regenerate a single file

1. Open `LLM_REGENERATION_PROMPTS.md`
2. Find the relevant prompt (1-5)
3. Copy prompt + include `strings-catalog.json`
4. Send to Claude
5. Replace file with generated output
6. Run validation

---

## Key Concepts

### strings-catalog.json
The **single source of truth** for all localization strings. Contains:
- `id`: Kotlin object name (PascalCase)
- `key`: iOS/shared key (dot.notation)
- `value`: Actual string content
- `android`: Android resource name (snake_case)
- `category`: Logical grouping
- `description`: Purpose of string

### Validation Script
Checks that all 5 platform-specific files stay in sync:
- TextId.kt (Kotlin)
- TextIdHelper.kt (Kotlin)
- strings.xml (Android XML)
- Localizable.strings (iOS)
- TextIdHelper.swift (iOS Swift)

### LLM Prompts
5 detailed prompts that enable automatic regeneration of any of the 5 files from the catalog.

---

## Important Notes

⚠️ **Before You Commit**
- Always run `python3 validate-localization.py`
- All 6 checks must pass
- No "Mismatched" or "Missing" errors

⚠️ **iOS Placeholder Format**
- Catalog uses `%s` (generic)
- iOS uses `%@` (Objective-C format)
- Android uses `%s` (Java format)
- Validation script handles this automatically

⚠️ **Key Naming Convention**
- iOS/shared: `dot.notation` (lowercase with dots)
- Android: `snake_case` (lowercase with underscores)
- Script automatically converts between them

---

## Questions?

1. **How do I add a new string?** → See "Adding a New String" section above
2. **What's the validation script do?** → See "Using the Validation Script" section
3. **What if something breaks?** → Run with `--verbose`, check detailed errors
4. **When do we modernize?** → Read COMPOSE_RESOURCES_MIGRATION_PLAN.md

---

**Last Updated**: March 5, 2025  
**System Status**: ✅ Fully operational, all 19 strings synchronized
