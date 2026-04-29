// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHHome",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHHome", targets: ["KHHome"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHHome",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHHome"
        ),
        .testTarget(
            name: "KHHomeTests",
            dependencies: ["KHHome"],
            path: "Tests/KHHomeTests"
        )
    ]
)
