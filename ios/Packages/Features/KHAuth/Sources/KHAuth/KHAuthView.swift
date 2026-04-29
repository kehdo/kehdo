// KHAuth — kehdo iOS feature module
//
// Canonical example of the MVVM + @Observable pattern used across all
// feature modules. Mirrors Android's MVI-lite pattern in feature-auth.

import SwiftUI
import Observation
import KHCore
import KHDomain
import KHDesignSystem

// MARK: - State

public struct SignInState: Equatable {
    public var email: String = ""
    public var password: String = ""
    public var isLoading: Bool = false
    public var isSignedIn: Bool = false
    public var errorCode: String? = nil
}

// MARK: - ViewModel

@Observable
@MainActor
public final class SignInViewModel {
    public private(set) var state = SignInState()
    private let signIn: SignInUseCase

    public init(signIn: SignInUseCase) {
        self.signIn = signIn
    }

    public func updateEmail(_ value: String) { state.email = value }
    public func updatePassword(_ value: String) { state.password = value }

    public func submit() async {
        state.isLoading = true
        state.errorCode = nil
        let result = await signIn(email: state.email, password: state.password)
        state.isLoading = false
        switch result {
        case .success: state.isSignedIn = true
        case .failure(let error): state.errorCode = error.code
        }
    }
}

// MARK: - View

public struct KHAuthView: View {
    @State private var viewModel: SignInViewModel

    public init(viewModel: SignInViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            Text("Welcome back.")
                .font(.system(size: 32, weight: .bold))

            VStack(alignment: .leading, spacing: 12) {
                TextField("Email", text: Binding(
                    get: { viewModel.state.email },
                    set: { viewModel.updateEmail($0) }
                ))
                .textInputAutocapitalization(.never)
                .keyboardType(.emailAddress)
                .padding()
                .background(AuroraColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                SecureField("Password", text: Binding(
                    get: { viewModel.state.password },
                    set: { viewModel.updatePassword($0) }
                ))
                .padding()
                .background(AuroraColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            if let errorCode = viewModel.state.errorCode {
                Text(errorCode)
                    .font(.caption)
                    .foregroundStyle(AuroraColors.pink)
            }

            AuroraButton("Sign in") {
                Task { await viewModel.submit() }
            }
            .disabled(viewModel.state.isLoading)

            Spacer()
        }
        .padding(24)
    }
}
