import Foundation

/// Domain-level error type. Mirrors the API ErrorEnvelope from /contracts/.
///
/// UI layers map these to user-facing strings via KHDesignSystem's error
/// presenter, which reads localized messages from /design/copy/<lang>.json.
public enum KehdoError: Error, Equatable {
    /// Network is unreachable or timed out.
    case network(underlying: String? = nil)

    /// Authentication required or token expired.
    case unauthorized

    /// User has hit their daily quota.
    case rateLimit(limit: Int, resetAt: Date)

    /// OCR or speaker attribution failed.
    case parsingFailed(reason: String)

    /// LLM provider returned an error.
    case generationFailed(reason: String)

    /// Server returned a code we don't specifically handle.
    case server(code: String, message: String)

    /// Unknown / unexpected error.
    case unknown(underlying: String? = nil)

    /// UPPER_SNAKE_CASE stable identifier matching the API contract.
    public var code: String {
        switch self {
        case .network: return "NETWORK_ERROR"
        case .unauthorized: return "UNAUTHORIZED"
        case .rateLimit: return "RATE_LIMIT_EXCEEDED"
        case .parsingFailed: return "PARSING_FAILED"
        case .generationFailed: return "GENERATION_FAILED"
        case .server(let code, _): return code
        case .unknown: return "UNKNOWN_ERROR"
        }
    }
}
