// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHData",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHData", targets: ["KHData"])
    ],
    dependencies: [
        .package(path: "../KHCore"),
        .package(path: "../KHDomain"),
        .package(path: "../KHNetwork"),
        .package(path: "../KHPersistence"),
    ],
    targets: [
        .target(
            name: "KHData",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDomain", package: "KHDomain"),
                .product(name: "KHNetwork", package: "KHNetwork"),
                .product(name: "KHPersistence", package: "KHPersistence"),
            ],
            path: "Sources/KHData"
        ),
        .testTarget(
            name: "KHDataTests",
            dependencies: ["KHData"],
            path: "Tests/KHDataTests"
        )
    ]
)
