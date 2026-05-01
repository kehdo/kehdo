import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Terms of Service — kehdo",
  description:
    "The agreement between you and kehdo when you use the app or join the waitlist.",
};

const LAST_UPDATED = "April 30, 2026";

export default function TermsPage() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-32 md:py-40">
      <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
        Legal
      </p>
      <h1 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
        Terms of Service
      </h1>
      <p className="mt-3 text-sm text-moonlight/45">
        Last updated: {LAST_UPDATED}
      </p>

      <div className="mt-12 space-y-8 text-base leading-relaxed text-moonlight/85">
        <Section title="Acceptance of terms">
          <p>
            By joining the kehdo waitlist or using kehdo&rsquo;s applications
            (Android, iOS, web) and APIs, you agree to these terms. If you
            don&rsquo;t agree, don&rsquo;t use the service.
          </p>
        </Section>

        <Section title="What kehdo is">
          <p>
            kehdo is an AI-powered chat reply generator. You upload a chat
            screenshot; we use OCR and large language models to suggest reply
            options in a tone you choose. Pricing tiers, response times, and
            feature availability are described on{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="/#pricing"
            >
              the pricing page
            </a>
            .
          </p>
        </Section>

        <Section title="Account responsibilities">
          <ul className="list-disc space-y-2 pl-6">
            <li>You&rsquo;re responsible for your account credentials.</li>
            <li>
              You must be at least 13 years old (16 in the EU) to create an
              account.
            </li>
            <li>One person, one free-tier account.</li>
            <li>
              You&rsquo;re responsible for the screenshots you upload. Don&rsquo;t
              upload conversations you don&rsquo;t have the right to share.
            </li>
          </ul>
        </Section>

        <Section title="Acceptable use">
          <p>You may not use kehdo to:</p>
          <ul className="list-disc space-y-2 pl-6">
            <li>
              Generate replies that harass, threaten, defame, or impersonate
              others.
            </li>
            <li>
              Generate content that is illegal where you live or where the
              recipient lives.
            </li>
            <li>
              Generate content depicting child sexual abuse, terrorism, or
              promotion of self-harm.
            </li>
            <li>
              Reverse-engineer, scrape, or extract our prompts, models, or
              training data.
            </li>
            <li>
              Resell kehdo replies as a competing service or as part of an
              automated bot system without written permission.
            </li>
            <li>
              Use the free tier&rsquo;s 5-replies-a-day limit programmatically
              or via multiple accounts to bypass paid plans.
            </li>
          </ul>
          <p className="mt-4">
            We may suspend or terminate accounts that violate these rules. For
            severe violations (illegal content, abuse), we&rsquo;ll cooperate
            with law enforcement.
          </p>
        </Section>

        <Section title="Subscription and billing">
          <p>
            Paid plans are billed in advance. You can cancel any time from the
            app — cancellation takes effect at the end of your current billing
            period, and you keep paid features until then. We don&rsquo;t
            issue prorated refunds for partial months unless required by law in
            your jurisdiction.
          </p>
        </Section>

        <Section title="Pricing and the grandfathering promise">
          <p>
            <strong className="text-moonlight">
              Your sign-up rate is locked.
            </strong>{" "}
            For as long as your paid subscription stays active and uninterrupted,
            we honor the price you paid at sign-up — even if our standard pricing
            increases later. New customers may pay more; existing customers
            don&rsquo;t.
          </p>
          <p className="mt-4">
            <strong className="text-moonlight">Founding-member rates.</strong>{" "}
            Anyone subscribing during the closed beta or in the first wave after
            public launch qualifies as a founding member. This is a real
            commitment, not a marketing label — see the rate displayed on the{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="/#pricing"
            >
              pricing page
            </a>{" "}
            at sign-up; that&rsquo;s your locked rate.
          </p>
          <p className="mt-4">
            <strong className="text-moonlight">If you cancel and resubscribe</strong>,
            you&rsquo;re billed at the standard rate at the time of resubscription,
            not your old grandfathered rate. The grandfathering applies only to
            continuous subscriptions.
          </p>
          <p className="mt-4">
            <strong className="text-moonlight">If we change pricing</strong>,
            we&rsquo;ll give existing subscribers at least 30 days&rsquo; notice
            via email. Existing subscribers continue at their grandfathered rate
            unless they actively switch plans.
          </p>
          <p className="mt-4">
            <strong className="text-moonlight">No false reference prices.</strong>{" "}
            We don&rsquo;t display crossed-out &ldquo;was&rdquo; prices that
            nobody ever paid. The price you see is the real price.
          </p>
        </Section>

        <Section title="Privacy">
          <p>
            Our handling of your data is described in the{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="/privacy"
            >
              Privacy Policy
            </a>
            , which is incorporated into these terms by reference.
          </p>
        </Section>

        <Section title="Service availability">
          <p>
            kehdo is provided &ldquo;as is&rdquo; and &ldquo;as available&rdquo;.
            We strive for high availability but don&rsquo;t guarantee
            uninterrupted service. We may take the service offline for
            maintenance or in response to security threats.
          </p>
        </Section>

        <Section title="Intellectual property">
          <p>
            The kehdo brand, code, design, and prompt engineering are our
            intellectual property. You may not copy, modify, or distribute them
            without permission. The replies generated by kehdo for your
            screenshots are yours to use however you wish — we claim no
            ownership over generated outputs.
          </p>
        </Section>

        <Section title="Limitation of liability">
          <p>
            To the maximum extent permitted by law, our liability for any claim
            arising from your use of kehdo is limited to the amount you paid us
            in the 12 months before the claim. We&rsquo;re not liable for
            indirect, consequential, or incidental damages — including but not
            limited to lost profits, lost data, or relationship damage caused
            by replies you sent.
          </p>
          <p className="mt-4">
            <strong className="text-moonlight">
              You&rsquo;re responsible for the replies you send.
            </strong>{" "}
            kehdo suggests replies; you choose to send them. We&rsquo;re not
            responsible for outcomes of human conversations.
          </p>
        </Section>

        <Section title="Governing law and disputes">
          <p>
            These terms are governed by the laws of India, without regard to
            conflict of law principles. Disputes will be resolved in the courts
            of Bengaluru, Karnataka, unless your local consumer protection law
            grants you the right to sue in your own jurisdiction.
          </p>
        </Section>

        <Section title="Changes to these terms">
          <p>
            We may update these terms. If the changes are material, we&rsquo;ll
            notify users via email at least 30 days before they take effect.
            Your continued use of the service after the effective date is
            acceptance of the updated terms.
          </p>
        </Section>

        <Section title="Contact">
          <p>
            For questions about these terms, email{" "}
            <a
              className="text-purple-bright underline-offset-4 hover:underline"
              href="mailto:legal@kehdo.app"
            >
              legal@kehdo.app
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
