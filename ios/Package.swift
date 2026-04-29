// swift-tools-version:5.9
//
// kehdo iOS Workspace Package
//
// This file makes the workspace navigable as a Swift package for SPM-aware
// tooling. The actual app target is in Kehdo.xcodeproj.
//
// Open Kehdo.xcworkspace in Xcode 15.2+.

import PackageDescription

let package = Package(
    name: "kehdo-ios-workspace",
    platforms: [.iOS(.v17)],
    products: [],
    targets: []
)
