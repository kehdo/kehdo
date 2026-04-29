// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHPaywall",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHPaywall", targets: ["KHPaywall"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHPaywall",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHPaywall"
        ),
        .testTarget(
            name: "KHPaywallTests",
            dependencies: ["KHPaywall"],
            path: "Tests/KHPaywallTests"
        )
    ]
)
