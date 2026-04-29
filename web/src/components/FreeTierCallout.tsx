import Link from "next/link";

export function FreeTierCallout() {
  return (
    <section className="relative px-6 py-16 md:py-24">
      <div className="mx-auto max-w-4xl">
        <div className="relative overflow-hidden rounded-3xl border border-purple/30 bg-gradient-to-br from-purple/15 via-pink/10 to-amber/5 p-10 text-center md:p-16">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            Free, forever
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl lg:text-6xl">
            Five replies a day.{" "}
            <span className="aurora-text font-serif italic">Forever.</span>
          </h2>
          <p className="mx-auto mt-6 max-w-xl text-base leading-relaxed text-moonlight/70 md:text-lg">
            No ads. No dark patterns. No &ldquo;we sold the company&rdquo;
            notification three years from now. The free tier is part of the
            product, not a trial.
          </p>
          <Link
            href="#waitlist"
            className="mt-8 inline-flex rounded-full bg-aurora-gradient px-8 py-3 text-sm font-semibold text-white transition hover:scale-105"
          >
            Get on the waitlist →
          </Link>
        </div>
      </div>
    </section>
  );
}
