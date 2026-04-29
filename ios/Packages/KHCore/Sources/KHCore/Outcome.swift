import Foundation

/// Outcome of an operation that can succeed or fail.
/// Mirrors the Android `Outcome<T>` sealed class for cross-platform parity.
public enum Outcome<T> {
    case success(T)
    case failure(KehdoError)

    public func map<R>(_ transform: (T) -> R) -> Outcome<R> {
        switch self {
        case .success(let value): return .success(transform(value))
        case .failure(let error): return .failure(error)
        }
    }

    @discardableResult
    public func onSuccess(_ block: (T) -> Void) -> Outcome<T> {
        if case .success(let value) = self { block(value) }
        return self
    }

    @discardableResult
    public func onFailure(_ block: (KehdoError) -> Void) -> Outcome<T> {
        if case .failure(let error) = self { block(error) }
        return self
    }
}
