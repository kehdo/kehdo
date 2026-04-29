// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHCore",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHCore", targets: ["KHCore"])
    ],
    dependencies: [




    ],
    targets: [
        .target(
            name: "KHCore",
            dependencies: [


            ],
            path: "Sources/KHCore"
        ),
        .testTarget(
            name: "KHCoreTests",
            dependencies: ["KHCore"],
            path: "Tests/KHCoreTests"
        )
    ]
)
