package app.kehdo.backend.auth.validation;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Rejects signups from known disposable / throwaway email services.
 *
 * <p>Loaded once at startup from
 * {@code classpath:disposable-email-domains.txt} into an in-memory
 * {@link Set} for O(1) lookup. The list is ~250 entries today; refresh
 * quarterly from {@code github.com/disposable-email-domains/disposable-email-domains}.</p>
 *
 * <p>Detection is by domain only — no aliasing checks
 * ({@code foo+bar@gmail.com} is not de-aliased). Domain comparison is
 * case-insensitive.</p>
 */
@Component
public class DisposableEmailValidator {

    private static final Logger log = LoggerFactory.getLogger(DisposableEmailValidator.class);
    private static final String RESOURCE_PATH = "disposable-email-domains.txt";

    private Set<String> blockedDomains = Set.of();

    @PostConstruct
    public void load() {
        Set<String> domains = new HashSet<>();
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);

        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                domains.add(trimmed.toLowerCase());
            }
        } catch (IOException e) {
            // Fail-fast: a missing/unreadable blocklist would silently allow
            // disposable signups, which is exactly what we don't want.
            throw new IllegalStateException(
                    "Failed to load disposable-email blocklist from " + RESOURCE_PATH, e);
        }

        if (domains.isEmpty()) {
            throw new IllegalStateException(
                    "Disposable-email blocklist is empty — refusing to start. "
                            + "Either restore the resource file or remove this validator.");
        }

        this.blockedDomains = Set.copyOf(domains);
        log.info("Loaded {} disposable email domains from {}", domains.size(), RESOURCE_PATH);
    }

    /**
     * Returns the email's domain part if it's on the blocklist, else {@code null}.
     * The email is assumed to have already passed Bean Validation's {@code @Email}
     * check, so a malformed string won't reach here, but we still defensively
     * handle the no-{@code @} case.
     */
    public String findBlockedDomain(String email) {
        if (email == null) return null;
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) return null;
        String domain = email.substring(at + 1).toLowerCase();
        return blockedDomains.contains(domain) ? domain : null;
    }

    /** Number of blocked domains currently loaded — exposed for tests + ops. */
    public int size() {
        return blockedDomains.size();
    }
}
