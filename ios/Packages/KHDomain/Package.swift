// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHDomain",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHDomain", targets: ["KHDomain"])
    ],
    dependencies: [
        .package(path: "../KHCore"),



    ],
    targets: [
        .target(
            name: "KHDomain",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),

            ],
            path: "Sources/KHDomain"
        ),
        .testTarget(
            name: "KHDomainTests",
            dependencies: ["KHDomain"],
            path: "Tests/KHDomainTests"
        )
    ]
)
