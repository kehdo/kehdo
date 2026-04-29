// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHPersistence",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHPersistence", targets: ["KHPersistence"])
    ],
    dependencies: [
        .package(path: "../KHCore"),



    ],
    targets: [
        .target(
            name: "KHPersistence",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),

            ],
            path: "Sources/KHPersistence"
        ),
        .testTarget(
            name: "KHPersistenceTests",
            dependencies: ["KHPersistence"],
            path: "Tests/KHPersistenceTests"
        )
    ]
)
