import Link from "next/link";
import { HowItWorks } from "@/components/HowItWorks";
import { Privacy } from "@/components/Privacy";
import { FreeTierCallout } from "@/components/FreeTierCallout";
import { Pricing } from "@/components/Pricing";
import { Faq } from "@/components/Faq";
import { FinalCta } from "@/components/FinalCta";

export default function Home() {
  return (
    <main className="min-h-screen overflow-hidden">
      {/* Aurora background orbs */}
      <div className="pointer-events-none fixed inset-0 -z-10">
        <div className="absolute -top-40 -right-20 h-[600px] w-[600px] rounded-full bg-purple/30 blur-[120px]" />
        <div className="absolute -bottom-40 -left-20 h-[500px] w-[500px] rounded-full bg-pink/20 blur-[120px]" />
      </div>

      <section className="mx-auto max-w-6xl px-6 py-32 md:py-48">
        <div className="inline-flex items-center gap-2 rounded-full border border-purple/30 bg-purple/10 px-4 py-2 text-xs font-medium text-purple-bright">
          <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-purple-bright" />
          NOW IN BETA · LANDING SOON
        </div>

        <h1 className="mt-8 text-5xl font-bold leading-[0.95] tracking-tight md:text-7xl lg:text-8xl">
          Reply with{" "}
          <span className="aurora-text font-serif italic">
            quiet confidence.
          </span>
        </h1>

        <p className="mt-8 max-w-2xl text-xl leading-relaxed text-moonlight/65">
          Drop a chat screenshot from WhatsApp, iMessage, Slack, or Instagram.
          Get four ranked replies in seconds — in any tone you choose.
        </p>

        <div className="mt-12 flex flex-wrap gap-4">
          <Link
            href="#waitlist"
            className="rounded-full bg-aurora-gradient px-8 py-4 text-sm font-semibold text-white transition hover:scale-105"
          >
            Join the waitlist →
          </Link>
          <Link
            href="#how-it-works"
            className="rounded-full border border-moonlight/15 px-8 py-4 text-sm font-semibold text-moonlight transition hover:bg-surface"
          >
            See how it works
          </Link>
        </div>

        <div className="mt-16 grid grid-cols-2 gap-8 md:grid-cols-4">
          {[
            { value: "5", label: "Free replies / day" },
            { value: "<6s", label: "Generation time" },
            { value: "97%", label: "Speaker accuracy" },
            { value: "30+", label: "Languages" },
          ].map((stat) => (
            <div key={stat.label}>
              <div className="text-3xl font-bold text-moonlight md:text-4xl">
                {stat.value}
              </div>
              <div className="mt-1 text-xs uppercase tracking-wider text-moonlight/45">
                {stat.label}
              </div>
            </div>
          ))}
        </div>
      </section>

      <HowItWorks />
      <Privacy />
      <FreeTierCallout />
      <Pricing />
      <Faq />
      <FinalCta />

      {/* TODO: ProductPreview, FeaturesBento, Testimonials, Comparison, TrustSignals — slot into spec positions 3, 5, 9, 10, 11 via PR 4 merge per /web/CLAUDE.md */}
    </main>
  );
}
