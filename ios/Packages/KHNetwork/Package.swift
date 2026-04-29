// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHNetwork",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHNetwork", targets: ["KHNetwork"])
    ],
    dependencies: [
        .package(path: "../KHCore"),



    ],
    targets: [
        .target(
            name: "KHNetwork",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),

            ],
            path: "Sources/KHNetwork"
        ),
        .testTarget(
            name: "KHNetworkTests",
            dependencies: ["KHNetwork"],
            path: "Tests/KHNetworkTests"
        )
    ]
)
