// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHAuth",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHAuth", targets: ["KHAuth"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHAuth",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHAuth"
        ),
        .testTarget(
            name: "KHAuthTests",
            dependencies: ["KHAuth"],
            path: "Tests/KHAuthTests"
        )
    ]
)
