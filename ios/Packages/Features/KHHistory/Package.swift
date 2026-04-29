// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHHistory",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHHistory", targets: ["KHHistory"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHHistory",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHHistory"
        ),
        .testTarget(
            name: "KHHistoryTests",
            dependencies: ["KHHistory"],
            path: "Tests/KHHistoryTests"
        )
    ]
)
