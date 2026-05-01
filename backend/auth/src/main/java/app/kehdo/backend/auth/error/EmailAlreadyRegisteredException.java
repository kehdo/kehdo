package app.kehdo.backend.auth.error;

import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;

public class EmailAlreadyRegisteredException extends ApiException {
    public EmailAlreadyRegisteredException() {
        super(
                ErrorCode.EMAIL_ALREADY_REGISTERED,
                409,
                "An account with this email already exists. Sign in instead.");
    }
}
