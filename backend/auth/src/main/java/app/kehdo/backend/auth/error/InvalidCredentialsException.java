package app.kehdo.backend.auth.error;

import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super(
                ErrorCode.INVALID_CREDENTIALS,
                401,
                "Email or password is incorrect.");
    }
}
