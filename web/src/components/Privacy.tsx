import { Trash2, BookX, Smartphone, ShieldCheck } from "lucide-react";

const PROMISES = [
  {
    Icon: Trash2,
    title: "Auto-delete after 30 days",
    description:
      "Every screenshot is removed from our servers 30 days after upload. No exceptions, no archives.",
  },
  {
    Icon: BookX,
    title: "Zero training on your data",
    description:
      "Your conversations never train an LLM. We pay the API providers explicitly to opt out, and we audit it.",
  },
  {
    Icon: Smartphone,
    title: "On-device OCR option",
    description:
      "On Pro and Unlimited, screenshot text extraction can run entirely on your phone. Nothing leaves your device.",
  },
  {
    Icon: ShieldCheck,
    title: "SOC 2 Type I in progress",
    description:
      "We're working with a Big-4 auditor on SOC 2 Type I certification. Type II follows once we hit the observation period.",
  },
];

export function Privacy() {
  return (
    <section id="privacy" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="max-w-2xl">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            Privacy
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Built for{" "}
            <span className="aurora-text font-serif italic">trust</span>, not
            engagement.
          </h2>
          <p className="mt-6 text-lg leading-relaxed text-moonlight/65">
            We get paid by you. So our incentives align with yours — fast,
            accurate replies on your terms.
          </p>
        </div>

        <div className="mt-16 grid gap-6 sm:grid-cols-2">
          {PROMISES.map(({ Icon, title, description }) => (
            <div
              key={title}
              className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm"
            >
              <span
                aria-hidden="true"
                className="flex h-10 w-10 items-center justify-center rounded-full border border-purple/30 bg-purple/10 text-purple-bright"
              >
                <Icon className="h-5 w-5" />
              </span>
              <h3 className="mt-6 text-lg font-semibold text-moonlight">
                {title}
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-moonlight/65">
                {description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
