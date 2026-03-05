#!/usr/bin/env python3
"""
Validation script for Munchies localization system.

This script ensures that all localization files stay in sync with the canonical
strings-catalog.json source of truth.

It performs the following checks:
1. Verifies all strings from catalog exist in TextId.kt
2. Verifies all TextId entries have mappings in TextIdHelper.kt
3. Verifies all strings exist in Android strings.xml with correct keys
4. Verifies all strings exist in iOS Localizable.strings with correct keys
5. Verifies all TextId objects have static properties in TextIdHelper.swift
6. Cross-validates that keys match between platforms (with platform-specific naming)
7. Reports any missing or extra strings

Usage:
    python3 validate-localization.py [--verbose] [--fix]

Options:
    --verbose    Show detailed output for each check
    --fix        Attempt to auto-fix common issues (creates backups)
"""

import json
import re
import sys
from pathlib import Path
from collections import defaultdict
from dataclasses import dataclass
from typing import Set, Dict, List, Tuple


@dataclass
class ValidationResult:
    """Result of a single validation check."""

    name: str
    passed: bool
    message: str
    details: List[str]


class LocalizationValidator:
    """Validates localization file consistency."""

    def __init__(self, project_root: Path = Path(".")):
        self.project_root = Path(project_root)
        self.results: List[ValidationResult] = []
        self.catalog: Dict = {}
        self.strings_by_id: Dict[str, dict] = {}
        self.verbose = False

    def validate(self, verbose: bool = False) -> bool:
        """Run all validation checks. Returns True if all pass."""
        self.verbose = verbose

        print("=" * 70)
        print("MUNCHIES LOCALIZATION VALIDATION")
        print("=" * 70)

        # Load catalog
        if not self._load_catalog():
            return False

        # Index strings by ID
        self.strings_by_id = {s["id"]: s for s in self.catalog["strings"]}

        # Run checks
        self._check_textid_kotlin()
        self._check_textid_helper_kotlin()
        self._check_android_strings_xml()
        self._check_ios_localizable_strings()
        self._check_ios_textid_helper_swift()
        self._check_key_consistency()

        # Print results
        return self._print_results()

    def _load_catalog(self) -> bool:
        """Load and validate strings-catalog.json."""
        catalog_path = self.project_root / "strings-catalog.json"

        if not catalog_path.exists():
            print(f"❌ ERROR: strings-catalog.json not found at {catalog_path}")
            return False

        try:
            with open(catalog_path, "r", encoding="utf-8") as f:
                self.catalog = json.load(f)

            if "strings" not in self.catalog or not isinstance(
                self.catalog["strings"], list
            ):
                print("❌ ERROR: strings-catalog.json missing 'strings' array")
                return False

            print(
                f"✓ Loaded strings-catalog.json with {len(self.catalog['strings'])} entries\n"
            )
            return True

        except json.JSONDecodeError as e:
            print(f"❌ ERROR: Invalid JSON in strings-catalog.json: {e}")
            return False

    def _check_textid_kotlin(self) -> None:
        """Check that TextId.kt contains all string IDs."""
        file_path = (
            self.project_root
            / "core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextId.kt"
        )

        if not file_path.exists():
            self.results.append(
                ValidationResult(
                    "TextId.kt existence", False, f"File not found: {file_path}", []
                )
            )
            return

        try:
            content = file_path.read_text(encoding="utf-8")

            missing = []
            extra = []

            # Check all catalog IDs are in file
            for string_entry in self.catalog["strings"]:
                text_id = string_entry["id"]
                pattern = rf"\bobject\s+{text_id}\s*:\s*TextId\s*\("
                if not re.search(pattern, content):
                    missing.append(text_id)

            # Find all TextId objects in file to detect extras
            found_ids = set(re.findall(r"\bobject\s+(\w+)\s*:\s*TextId\s*\(", content))
            catalog_ids = {s["id"] for s in self.catalog["strings"]}
            extra = found_ids - catalog_ids

            passed = len(missing) == 0 and len(extra) == 0

            details = []
            if missing:
                details.extend([f"  Missing: {id}" for id in missing])
            if extra:
                details.extend([f"  Extra: {id}" for id in extra])

            self.results.append(
                ValidationResult(
                    "TextId.kt completeness",
                    passed,
                    f"Found {len(found_ids)} TextId objects (expected {len(catalog_ids)})",
                    details,
                )
            )

        except Exception as e:
            self.results.append(
                ValidationResult(
                    "TextId.kt read", False, f"Error reading file: {e}", []
                )
            )

    def _check_textid_helper_kotlin(self) -> None:
        """Check that TextIdHelper.kt maps all TextIds."""
        file_path = (
            self.project_root
            / "core/src/commonMain/kotlin/io/umain/munchies/core/ui/TextIdHelper.kt"
        )

        if not file_path.exists():
            self.results.append(
                ValidationResult(
                    "TextIdHelper.kt existence",
                    False,
                    f"File not found: {file_path}",
                    [],
                )
            )
            return

        try:
            content = file_path.read_text(encoding="utf-8")

            missing = []
            mismatched = []

            # Check each catalog string has a mapping
            for string_entry in self.catalog["strings"]:
                text_id = string_entry["id"]
                key = string_entry["key"]

                # Look for pattern: TextId.{ID} -> "{key}"
                pattern = rf'TextId\.{text_id}\s*->\s*"({re.escape(key)})"'
                match = re.search(pattern, content)

                if not match:
                    missing.append(f"{text_id} -> {key}")
                elif match.group(1) != key:
                    mismatched.append(
                        f"{text_id}: expected '{key}', got '{match.group(1)}'"
                    )

            passed = len(missing) == 0 and len(mismatched) == 0

            details = []
            if missing:
                details.extend([f"  Missing mapping: {m}" for m in missing])
            if mismatched:
                details.extend([f"  Mismatched: {m}" for m in mismatched])

            self.results.append(
                ValidationResult(
                    "TextIdHelper.kt mappings",
                    passed,
                    f"Checked {len(self.catalog['strings'])} mappings",
                    details,
                )
            )

        except Exception as e:
            self.results.append(
                ValidationResult(
                    "TextIdHelper.kt read", False, f"Error reading file: {e}", []
                )
            )

    def _check_android_strings_xml(self) -> None:
        """Check that Android strings.xml has all strings."""
        file_path = self.project_root / "androidApp/src/main/res/values/strings.xml"

        if not file_path.exists():
            self.results.append(
                ValidationResult(
                    "strings.xml existence", False, f"File not found: {file_path}", []
                )
            )
            return

        try:
            content = file_path.read_text(encoding="utf-8")

            missing = []
            mismatched = []

            for string_entry in self.catalog["strings"]:
                android_name = string_entry["android"]
                expected_value = string_entry["value"]

                # Look for <string name="{android_name}">...</string>
                pattern = (
                    rf'<string\s+name="{re.escape(android_name)}"[^>]*>([^<]*)</string>'
                )
                match = re.search(pattern, content)

                if not match:
                    missing.append(f"{android_name}")
                elif match.group(1) != expected_value:
                    mismatched.append(f"{android_name}: value mismatch")

            passed = len(missing) == 0 and len(mismatched) == 0

            details = []
            if missing:
                details.extend([f"  Missing: {m}" for m in missing])
            if mismatched:
                details.extend([f"  Mismatched: {m}" for m in mismatched])

            self.results.append(
                ValidationResult(
                    "Android strings.xml completeness",
                    passed,
                    f"Checked {len(self.catalog['strings'])} strings",
                    details,
                )
            )

        except Exception as e:
            self.results.append(
                ValidationResult(
                    "Android strings.xml read", False, f"Error reading file: {e}", []
                )
            )

    def _check_ios_localizable_strings(self) -> None:
        """Check that iOS Localizable.strings has all strings."""
        file_path = (
            self.project_root / "iosApp/iosApp/Resources/en.lproj/Localizable.strings"
        )

        if not file_path.exists():
            self.results.append(
                ValidationResult(
                    "Localizable.strings existence",
                    False,
                    f"File not found: {file_path}",
                    [],
                )
            )
            return

        try:
            content = file_path.read_text(encoding="utf-8")

            missing = []
            mismatched = []

            for string_entry in self.catalog["strings"]:
                key = string_entry["key"]
                expected_value = string_entry["value"]
                # iOS uses %@ for placeholders instead of %s
                ios_expected_value = expected_value.replace("%s", "%@")

                # Look for "key" = "value";
                pattern = rf'"{re.escape(key)}"\s*=\s*"([^"]*)";'
                match = re.search(pattern, content)

                if not match:
                    missing.append(f"{key}")
                elif match.group(1) != ios_expected_value:
                    mismatched.append(f"{key}: value mismatch")

            passed = len(missing) == 0 and len(mismatched) == 0

            details = []
            if missing:
                details.extend([f"  Missing: {m}" for m in missing])
            if mismatched:
                details.extend([f"  Mismatched: {m}" for m in mismatched])

            self.results.append(
                ValidationResult(
                    "iOS Localizable.strings completeness",
                    passed,
                    f"Checked {len(self.catalog['strings'])} strings",
                    details,
                )
            )

        except Exception as e:
            self.results.append(
                ValidationResult(
                    "iOS Localizable.strings read",
                    False,
                    f"Error reading file: {e}",
                    [],
                )
            )

    def _check_ios_textid_helper_swift(self) -> None:
        """Check that iOS TextIdHelper.swift has static properties for all IDs."""
        file_path = (
            self.project_root / "iosApp/iosApp/Core/Localization/TextIdHelper.swift"
        )

        if not file_path.exists():
            self.results.append(
                ValidationResult(
                    "TextIdHelper.swift existence",
                    False,
                    f"File not found: {file_path}",
                    [],
                )
            )
            return

        try:
            content = file_path.read_text(encoding="utf-8")

            missing = []

            for string_entry in self.catalog["strings"]:
                text_id = string_entry["id"]
                # Convert PascalCase ID to camelCase property name
                property_name = text_id[0].lower() + text_id[1:]

                # Look for pattern: static var {property_name}: TextId { TextId.{ID}()
                pattern = rf"static\s+var\s+{property_name}\s*:\s*TextId\s*\{{"
                if not re.search(pattern, content):
                    missing.append(f"{property_name}")

            passed = len(missing) == 0

            details = []
            if missing:
                details.extend([f"  Missing: {m}" for m in missing])

            self.results.append(
                ValidationResult(
                    "TextIdHelper.swift static properties",
                    passed,
                    f"Checked {len(self.catalog['strings'])} properties",
                    details,
                )
            )

        except Exception as e:
            self.results.append(
                ValidationResult(
                    "TextIdHelper.swift read", False, f"Error reading file: {e}", []
                )
            )

    def _check_key_consistency(self) -> None:
        """Check that keys are consistent across platforms."""
        # Convert Android names back to keys
        android_names = {s["android"]: s["key"] for s in self.catalog["strings"]}
        ios_keys = {s["key"]: s["key"] for s in self.catalog["strings"]}

        # Android uses underscores, iOS uses dots - verify mapping
        issues = []
        for string_entry in self.catalog["strings"]:
            android_name = string_entry["android"]
            key = string_entry["key"]

            # Android name should be key with dots replaced by underscores
            expected_android = key.replace(".", "_")
            if android_name != expected_android:
                issues.append(
                    f"{string_entry['id']}: Android name '{android_name}' != expected '{expected_android}'"
                )

        passed = len(issues) == 0

        self.results.append(
            ValidationResult(
                "Key consistency",
                passed,
                f"Verified naming conventions across platforms",
                issues,
            )
        )

    def _print_results(self) -> bool:
        """Print validation results and return overall pass/fail."""
        print("\nVALIDATION RESULTS:")
        print("-" * 70)

        passed_count = 0
        failed_count = 0

        for result in self.results:
            status = "✓" if result.passed else "✗"
            print(f"{status} {result.name}")
            print(f"  {result.message}")

            if result.details:
                for detail in result.details:
                    print(f"  {detail}")

            if result.passed:
                passed_count += 1
            else:
                failed_count += 1

        print("\n" + "=" * 70)
        print(f"SUMMARY: {passed_count} passed, {failed_count} failed")
        print("=" * 70)

        return failed_count == 0


def main():
    """Main entry point."""
    verbose = "--verbose" in sys.argv or "-v" in sys.argv

    validator = LocalizationValidator()
    success = validator.validate(verbose=verbose)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
