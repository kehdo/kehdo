package app.kehdo.backend.auth.error;

import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;

public class RefreshTokenInvalidException extends ApiException {
    public RefreshTokenInvalidException() {
        super(
                ErrorCode.REFRESH_TOKEN_INVALID,
                401,
                "Session expired — please sign in again.");
    }
}
