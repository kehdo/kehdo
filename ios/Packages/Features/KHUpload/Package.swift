// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "KHUpload",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "KHUpload", targets: ["KHUpload"])
    ],
    dependencies: [
        .package(path: "../../KHCore"),
        .package(path: "../../KHDesignSystem"),
        .package(path: "../../KHDomain")
    ],
    targets: [
        .target(
            name: "KHUpload",
            dependencies: [
                .product(name: "KHCore", package: "KHCore"),
                .product(name: "KHDesignSystem", package: "KHDesignSystem"),
                .product(name: "KHDomain", package: "KHDomain")
            ],
            path: "Sources/KHUpload"
        ),
        .testTarget(
            name: "KHUploadTests",
            dependencies: ["KHUpload"],
            path: "Tests/KHUploadTests"
        )
    ]
)
