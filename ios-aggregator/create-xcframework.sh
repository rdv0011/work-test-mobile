#!/bin/bash
set -e

# This script creates an XCFramework from the built Kotlin/Native frameworks
# Run this manually or from a build phase when needed

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build/bin"
XCFRAMEWORK_DIR="$PROJECT_DIR/build/XCFrameworks/release"

echo "Creating XCFramework..."

# Clean old XCFramework
rm -rf "$XCFRAMEWORK_DIR/shared.xcframework"
mkdir -p "$XCFRAMEWORK_DIR"

# Create XCFramework from individual frameworks
xcodebuild -create-xcframework \
  -framework "$BUILD_DIR/iosArm64/releaseFramework/shared.framework" \
  -framework "$BUILD_DIR/iosSimulatorArm64/releaseFramework/shared.framework" \
  -output "$XCFRAMEWORK_DIR/shared.xcframework"

echo "✓ XCFramework created at: $XCFRAMEWORK_DIR/shared.xcframework"
