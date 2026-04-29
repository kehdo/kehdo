export function TrustSignals() {
  return (
    <section className="relative px-6 py-16">
      <div className="mx-auto max-w-6xl">
        <div className="rounded-2xl border border-moonlight/10 bg-surface/40 px-8 py-10 backdrop-blur-sm">
          <p className="text-center text-xs font-semibold uppercase tracking-wider text-moonlight/45">
            Powered by
          </p>
          <div className="mt-6 flex flex-wrap items-center justify-center gap-x-12 gap-y-6 text-moonlight/65">
            <span className="text-base font-semibold tracking-tight">
              Claude Opus 4.7
            </span>
            <span className="text-base font-semibold tracking-tight">
              Google Cloud Vision
            </span>
            <span className="text-base font-semibold tracking-tight">
              Spring Boot 3.2
            </span>
            <span className="text-base font-semibold tracking-tight">
              Native Kotlin · Swift
            </span>
          </div>
          <p className="mt-6 text-center text-xs text-moonlight/45">
            Best-in-class infrastructure. Built in India · 🇮🇳
          </p>
        </div>
      </div>
    </section>
  );
}
