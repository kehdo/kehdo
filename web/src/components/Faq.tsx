import { Plus } from "lucide-react";

const QUESTIONS = [
  {
    q: "How does kehdo actually work?",
    a: "Drop a screenshot — kehdo's OCR pulls the text, identifies who said what, and infers tone. A large language model generates four reply candidates in your chosen tone, ranked by likely fit. Pick one, copy, send. The whole flow takes under six seconds.",
  },
  {
    q: "Is my chat data private?",
    a: "Yes. Screenshots are auto-deleted from our servers 30 days after upload. We pay our LLM providers explicitly to opt out of training on user data. On Pro and Unlimited plans, OCR can run entirely on-device — your screenshots never leave your phone.",
  },
  {
    q: "What languages does kehdo support?",
    a: "English at launch. Hindi, Spanish, and Portuguese arrive within the first month after public launch. The model itself supports 30+ languages — we&apos;re prioritizing UI translations based on signups.",
  },
  {
    q: "Can I cancel anytime?",
    a: "Yes. One-click cancel from the app, no email-the-founders nonsense. Cancellation takes effect at the end of your current billing period — you keep Pro features until then.",
  },
  {
    q: "Do you train AI on my conversations?",
    a: "No. Never. We&apos;ve audited this with our model providers and we&apos;ll commit to it in writing in the privacy policy. If we ever change this, you&apos;ll get a 90-day notice and an export of your data.",
  },
  {
    q: "When does kehdo launch?",
    a: "Android first, in closed beta within weeks. iOS follows ~6 weeks after Android. Join the waitlist and we&apos;ll email you when it&apos;s your turn — no marketing emails in the meantime.",
  },
];

export function Faq() {
  return (
    <section id="faq" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-4xl">
        <div className="text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            FAQ
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Questions, <span className="aurora-text font-serif italic">answered</span>.
          </h2>
        </div>

        <ul className="mt-16 space-y-4">
          {QUESTIONS.map(({ q, a }) => (
            <li
              key={q}
              className="rounded-2xl border border-moonlight/10 bg-surface/40 backdrop-blur-sm"
            >
              <details className="group">
                <summary className="flex cursor-pointer list-none items-center justify-between gap-6 p-6 text-base font-semibold text-moonlight">
                  <span>{q}</span>
                  <Plus
                    aria-hidden="true"
                    className="h-5 w-5 flex-shrink-0 text-purple-bright transition-transform group-open:rotate-45"
                  />
                </summary>
                <p className="px-6 pb-6 text-sm leading-relaxed text-moonlight/65">
                  {a}
                </p>
              </details>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
