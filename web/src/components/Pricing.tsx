import Link from "next/link";
import { Check } from "lucide-react";

type Tier = {
  name: string;
  price: string;
  cadence: string;
  description: string;
  features: string[];
  cta: string;
  highlighted?: boolean;
};

const TIERS: Tier[] = [
  {
    name: "Starter",
    price: "$0",
    cadence: "always",
    description: "Perfect for casual users. Forever free.",
    features: [
      "5 replies a day",
      "8 of 18 reply tones",
      "English-only",
      "Standard cloud OCR",
      "30-day screenshot retention",
    ],
    cta: "Join the waitlist",
  },
  {
    name: "Pro",
    price: "$4",
    cadence: "/month, billed yearly",
    description: "For people who reply all day, every day.",
    features: [
      "100 replies a day",
      "All 18 reply tones",
      "30+ languages",
      "On-device OCR option",
      "Refine with custom prompts",
      "Priority support",
    ],
    cta: "Go Pro",
    highlighted: true,
  },
  {
    name: "Unlimited",
    price: "$12",
    cadence: "/month",
    description: "For teams, creators, and the perpetually messaging.",
    features: [
      "Unmetered replies",
      "Everything in Pro",
      "Early access to new features",
      "kehdo for Teams (early access)",
      "Direct line to the founders",
    ],
    cta: "Go Unlimited",
  },
];

export function Pricing() {
  return (
    <section id="pricing" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            Pricing
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Simple.{" "}
            <span className="aurora-text font-serif italic">Honest.</span>{" "}
            Forever.
          </h2>
          <p className="mt-6 text-lg leading-relaxed text-moonlight/65">
            Start free. Upgrade when you outgrow it. Cancel any time — your
            replies are yours.
          </p>
        </div>

        <div className="mt-16 grid gap-6 lg:grid-cols-3">
          {TIERS.map((tier) => (
            <div
              key={tier.name}
              className={`relative flex flex-col rounded-2xl border p-8 backdrop-blur-sm transition ${
                tier.highlighted
                  ? "border-purple/50 bg-gradient-to-br from-purple/15 via-surface/60 to-pink/10 shadow-[0_0_60px_-20px_rgba(156,91,255,0.5)]"
                  : "border-moonlight/10 bg-surface/40"
              }`}
            >
              {tier.highlighted && (
                <span className="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-aurora-gradient px-4 py-1 text-xs font-semibold uppercase tracking-wider text-white">
                  Most popular
                </span>
              )}

              <div>
                <h3 className="text-xl font-semibold text-moonlight">
                  {tier.name}
                </h3>
                <p className="mt-2 text-sm text-moonlight/65">
                  {tier.description}
                </p>
              </div>

              <div className="mt-6 flex items-baseline gap-2">
                <span className="text-5xl font-bold tracking-tight text-moonlight">
                  {tier.price}
                </span>
                <span className="text-sm text-moonlight/45">
                  {tier.cadence}
                </span>
              </div>

              <ul className="mt-8 flex-1 space-y-3 text-sm">
                {tier.features.map((feature) => (
                  <li
                    key={feature}
                    className="flex items-start gap-3 text-moonlight/80"
                  >
                    <Check
                      aria-hidden="true"
                      className="mt-0.5 h-4 w-4 flex-shrink-0 text-purple-bright"
                    />
                    <span>{feature}</span>
                  </li>
                ))}
              </ul>

              <Link
                href="#waitlist"
                className={`mt-10 inline-flex items-center justify-center rounded-full px-6 py-3 text-sm font-semibold transition ${
                  tier.highlighted
                    ? "bg-aurora-gradient text-white hover:scale-105"
                    : "border border-moonlight/15 text-moonlight hover:bg-surface"
                }`}
              >
                {tier.cta}
              </Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
