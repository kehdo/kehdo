// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHProfile",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHProfile", targets: ["KHProfile"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHProfile",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHProfile"
        ),
        .testTarget(
            name: "KHProfileTests",
            dependencies: ["KHProfile"],
            path: "Tests/KHProfileTests"
        )
    ]
)
