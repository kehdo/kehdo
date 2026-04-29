// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHOnboarding",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHOnboarding", targets: ["KHOnboarding"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHOnboarding",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHOnboarding"
        ),
        .testTarget(
            name: "KHOnboardingTests",
            dependencies: ["KHOnboarding"],
            path: "Tests/KHOnboardingTests"
        )
    ]
)
