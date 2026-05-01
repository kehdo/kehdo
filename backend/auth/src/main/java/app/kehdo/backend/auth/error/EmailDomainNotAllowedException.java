package app.kehdo.backend.auth.error;

import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;

import java.util.Map;

/**
 * Raised when a signup email's domain is on the disposable-email blocklist.
 *
 * <p>Maps to HTTP 422 (Unprocessable Entity) — the request was syntactically
 * valid but couldn't be processed because the email domain isn't allowed.
 * 422 (vs 400) signals to the client that the input format itself was fine
 * and just the policy rule rejected it.</p>
 */
public class EmailDomainNotAllowedException extends ApiException {

    public EmailDomainNotAllowedException(String domain) {
        super(
                ErrorCode.EMAIL_DOMAIN_NOT_ALLOWED,
                422,
                "Use a permanent email address — disposable email services aren't supported.",
                Map.of("domain", domain));
    }
}
