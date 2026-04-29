import Foundation

/// Domain model for an authenticated user.
/// Pure Swift — no UIKit, networking, or persistence concerns.
public struct User: Equatable, Sendable {
    public let id: String
    public let email: String
    public let displayName: String?
    public let plan: Plan
    public let quotaRemaining: Int
    public let quotaResetAt: Date

    public enum Plan: String, Sendable, Equatable {
        case free = "FREE"
        case pro = "PRO"
        case unlimited = "UNLIMITED"
    }

    public init(
        id: String,
        email: String,
        displayName: String? = nil,
        plan: Plan,
        quotaRemaining: Int,
        quotaResetAt: Date
    ) {
        self.id = id
        self.email = email
        self.displayName = displayName
        self.plan = plan
        self.quotaRemaining = quotaRemaining
        self.quotaResetAt = quotaResetAt
    }
}
