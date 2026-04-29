// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHDesignSystem",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHDesignSystem", targets: ["KHDesignSystem"])
    ],
    dependencies: [
        .package(path: "../KHCore"),



    ],
    targets: [
        .target(
            name: "KHDesignSystem",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),

            ],
            path: "Sources/KHDesignSystem"
        ),
        .testTarget(
            name: "KHDesignSystemTests",
            dependencies: ["KHDesignSystem"],
            path: "Tests/KHDesignSystemTests"
        )
    ]
)
