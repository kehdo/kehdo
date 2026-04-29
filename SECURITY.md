# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in kehdo, please report it responsibly:

- **Email:** security@kehdo.app
- **Do NOT** open a public GitHub issue for security vulnerabilities
- **Do NOT** disclose publicly until we've had a chance to respond and patch

We aim to respond within 48 hours and will keep you updated as we work on a fix.

## Supported versions

| Version | Supported |
|---------|-----------|
| Latest stable | ✅ |
| Pre-release / beta | ⚠️ on a best-effort basis |
| End-of-life | ❌ |

## Security practices

- All API traffic uses TLS 1.3
- Mobile clients enforce certificate pinning for `api.kehdo.app`
- Secrets are stored in AWS Secrets Manager / GitHub Actions secrets — never committed
- Passwords are hashed with BCrypt (cost 12)
- JWT access tokens are RS256 with 5-minute TTL
- Refresh tokens are hashed in the database and rotate on every use
- Dependencies are scanned with Trivy on every backend build
- Renovate bot proposes dependency updates weekly

## Privacy commitments

- User screenshots are auto-deleted from S3 after 30 days
- No user content (conversation text, reply text) is logged
- No user data is used to train LLMs
- Sentry / Datadog have PII scrubbing rules applied
- See [Privacy Policy](https://kehdo.app/privacy) for full details
