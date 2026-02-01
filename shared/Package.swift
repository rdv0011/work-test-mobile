// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "shared",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "shared",
            targets: ["shared"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "shared",
            path: "./build/XCFrameworks/release/shared.xcframework"
        )
    ]
)
