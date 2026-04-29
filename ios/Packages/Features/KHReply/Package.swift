// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHReply",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHReply", targets: ["KHReply"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHReply",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHReply"
        ),
        .testTarget(
            name: "KHReplyTests",
            dependencies: ["KHReply"],
            path: "Tests/KHReplyTests"
        )
    ]
)
