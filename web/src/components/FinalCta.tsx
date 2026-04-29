import { WaitlistForm } from "./WaitlistForm";

export function FinalCta() {
  return (
    <section id="waitlist" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-4xl">
        <div className="relative overflow-hidden rounded-3xl border border-moonlight/10 bg-surface/40 p-10 backdrop-blur-sm md:p-16">
          {/* Soft Aurora glow */}
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -top-40 left-1/2 h-80 w-80 -translate-x-1/2 rounded-full bg-purple/20 blur-[100px]"
          />
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -bottom-32 right-0 h-64 w-64 rounded-full bg-pink/15 blur-[100px]"
          />

          <div className="relative">
            <h2 className="text-4xl font-bold leading-tight tracking-tight md:text-5xl lg:text-6xl">
              Your next reply is one{" "}
              <span className="aurora-text font-serif italic">screenshot</span>{" "}
              away.
            </h2>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-moonlight/65">
              Join the waitlist for early access. 5 free replies a day, forever
              — no ads, no dark patterns.
            </p>

            <WaitlistForm />

            <p className="mt-6 text-xs text-moonlight/45">
              We&apos;ll only email you about the launch. Unsubscribe in one
              click.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
