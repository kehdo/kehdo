import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Privacy Policy — kehdo",
  description:
    "How kehdo handles your data: what we collect, what we don't, and your rights.",
};

const LAST_UPDATED = "April 30, 2026";

export default function PrivacyPage() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-32 md:py-40">
      <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
        Legal
      </p>
      <h1 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
        Privacy Policy
      </h1>
      <p className="mt-3 text-sm text-moonlight/45">
        Last updated: {LAST_UPDATED}
      </p>

      <div className="mt-12 space-y-8 text-base leading-relaxed text-moonlight/85">
        <Section title="The short version">
          <p>
            We collect the minimum data needed to run kehdo. Your email if you
            join the waitlist. Your screenshots if you use the app — auto-deleted
            after 30 days. We don&rsquo;t train AI on your data, we don&rsquo;t
            sell your data, and we don&rsquo;t share it with advertisers. You can
            ask for deletion at any time and we&rsquo;ll comply within 30 days.
          </p>
        </Section>

        <Section title="What we collect">
          <ul className="list-disc space-y-2 pl-6">
            <li>
              <strong className="text-moonlight">Waitlist email</strong> —
              stored in a Google Sheet that only the founders can access. We
              email you from this list when the app is ready to install.
            </li>
            <li>
              <strong className="text-moonlight">Screenshots (post-launch)</strong>{" "}
              — uploaded so we can extract text and generate replies. Stored on
              AWS S3, encrypted at rest, auto-deleted 30 days after upload.
            </li>
            <li>
              <strong className="text-moonlight">Account data (post-launch)</strong>{" "}
              — email, password hash (BCrypt cost 12), and your subscription
              tier. Required to bill and to enforce free-tier daily limits.
            </li>
            <li>
              <strong className="text-moonlight">Usage analytics</strong> —
              anonymous page views via Vercel Web Analytics (no cookies,
              no fingerprinting, IP addresses are hashed and never logged).
              Used to understand which features matter.
            </li>
          </ul>
        </Section>

        <Section title="What we don't do">
          <ul className="list-disc space-y-2 pl-6">
            <li>
              We don&rsquo;t train any AI model on your screenshots, your
              conversations, or your generated replies. Our LLM providers
              (Anthropic, OpenAI) are explicitly opted-out of training on our
              data.
            </li>
            <li>
              We don&rsquo;t sell your data to advertisers, data brokers, or
              anyone else.
            </li>
            <li>
              We don&rsquo;t use cookies for tracking. The site is fully
              functional with cookies disabled.
            </li>
            <li>
              We don&rsquo;t fingerprint your device or log your IP address.
            </li>
          </ul>
        </Section>

        <Section title="Third-party services we use">
          <ul className="list-disc space-y-2 pl-6">
            <li>
              <strong className="text-moonlight">Google Workspace</strong> —
              waitlist emails are stored in a Google Sheet accessible only to
              the founders. Subject to Google&rsquo;s privacy and security
              policies.
            </li>
            <li>
              <strong className="text-moonlight">Vercel Web Analytics</strong>{" "}
              — anonymous page-level analytics. No cookies, no third-party
              tracking, IP addresses are hashed.
            </li>
            <li>
              <strong className="text-moonlight">Vercel</strong> — hosting for
              kehdo.app and staging.kehdo.app.
            </li>
            <li>
              <strong className="text-moonlight">AWS</strong> (post-launch) —
              backend infrastructure and S3 for screenshots.
            </li>
            <li>
              <strong className="text-moonlight">Anthropic, OpenAI</strong>{" "}
              (post-launch) — LLM providers for reply generation. Both
              contractually opt out of training on our data.
            </li>
            <li>
              <strong className="text-moonlight">Google Cloud Vision</strong>{" "}
              (post-launch) — OCR for cloud-mode screenshot text extraction.
              Not used in on-device mode.
            </li>
          </ul>
        </Section>

        <Section title="Your rights">
          <ul className="list-disc space-y-2 pl-6">
            <li>
              <strong className="text-moonlight">Access</strong> — request a
              copy of all data we hold about you.
            </li>
            <li>
              <strong className="text-moonlight">Deletion</strong> — request
              that we delete your account and all associated data.
            </li>
            <li>
              <strong className="text-moonlight">Correction</strong> — request
              that we correct inaccurate data.
            </li>
            <li>
              <strong className="text-moonlight">Portability</strong> — request
              your data in a structured, machine-readable format.
            </li>
            <li>
              <strong className="text-moonlight">Withdrawal of consent</strong>{" "}
              — unsubscribe from the waitlist or delete your account at any
              time.
            </li>
          </ul>
          <p className="mt-4">
            To exercise any of these, email{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="mailto:privacy@kehdo.app"
            >
              privacy@kehdo.app
            </a>
            . We&rsquo;ll respond within 30 days.
          </p>
        </Section>

        <Section title="Children's data">
          <p>
            kehdo is not directed to children under 13 (or under 16 in the EU).
            We don&rsquo;t knowingly collect data from children. If you believe
            a child has signed up, email us at{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="mailto:privacy@kehdo.app"
            >
              privacy@kehdo.app
            </a>{" "}
            and we&rsquo;ll delete the account.
          </p>
        </Section>

        <Section title="Changes to this policy">
          <p>
            If we change this policy materially, we&rsquo;ll notify users via
            email at least 30 days before the change takes effect. Older
            versions are available on request.
          </p>
        </Section>

        <Section title="Contact">
          <p>
            kehdo is operated by an Indian sole proprietorship pre-incorporation;
            company entity TBD before public launch. For privacy questions:{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="mailto:privacy@kehdo.app"
            >
              privacy@kehdo.app
            </a>
            .
          </p>
        </Section>
      </div>
    </main>
  );
}

function Section({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section>
      <h2 className="text-2xl font-semibold tracking-tight text-moonlight">
        {title}
      </h2>
      <div className="mt-4 text-moonlight/85">{children}</div>
    </section>
  );
}
