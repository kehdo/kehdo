-- ============================================================
-- V3 — sessions.ip_address: INET -> VARCHAR(45)
-- ============================================================
-- The Postgres INET type was a premature optimisation. Hibernate sends
-- the JPA entity field (a String) as varchar, and Postgres refuses the
-- implicit cast inet <- varchar, blowing up signup with:
--   ERROR: column "ip_address" is of type inet but expression is of
--   type character varying
--
-- We don't query by CIDR range or do any inet-specific operations —
-- we just record the IP for audit. VARCHAR(45) fits every IPv4 (15
-- chars) and IPv6 (39 chars; 45 includes RFC 5952 zone-index suffix).
--
-- USING clause converts any existing inet rows to their canonical
-- string form. Staging currently has no signups so this is a no-op
-- in practice, but kept for safety.
-- ============================================================

ALTER TABLE sessions
    ALTER COLUMN ip_address TYPE VARCHAR(45)
    USING ip_address::text;
