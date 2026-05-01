package app.kehdo.backend.user;

/**
 * Subscription plan tiers, mirroring {@code contracts/openapi/kehdo.v1.yaml}'s
 * {@code User.plan} enum.
 */
public enum UserPlan {
    STARTER,
    PRO,
    UNLIMITED
}
