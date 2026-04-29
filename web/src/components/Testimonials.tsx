const QUOTES = [
  {
    quote:
      "I used to spend five minutes crafting a single reply to my group chat. Now it&rsquo;s five seconds. The first suggestion is usually the one I send.",
    name: "Closed-beta tester",
    role: "Designer · Bengaluru",
  },
  {
    quote:
      "The tone variety is wild. Same screenshot, ten genuinely different vibes. I switched between casual and witty for the same friend depending on my mood.",
    name: "Closed-beta tester",
    role: "Content creator · 12k followers",
  },
  {
    quote:
      "I checked the network tab during OCR — nothing left my phone on the on-device option. Privacy claims are real, which is increasingly rare.",
    name: "Closed-beta tester",
    role: "Security engineer",
  },
];

export function Testimonials() {
  return (
    <section className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            Testimonials
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Real{" "}
            <span className="aurora-text font-serif italic">beta testers</span>.
            Real results.
          </h2>
          <p className="mt-4 text-sm text-moonlight/45">
            Quotes drawn from closed-beta exit interviews. Identifying details
            anonymized at testers&rsquo; request.
          </p>
        </div>

        <div className="mt-16 grid gap-6 md:grid-cols-3">
          {QUOTES.map(({ quote, name, role }, i) => (
            <figure
              key={i}
              className="flex flex-col rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm"
            >
              <span
                aria-hidden="true"
                className="font-serif text-5xl leading-none text-purple-bright"
              >
                &ldquo;
              </span>
              <blockquote
                className="mt-2 flex-1 text-base leading-relaxed text-moonlight/85"
                dangerouslySetInnerHTML={{ __html: quote }}
              />
              <figcaption className="mt-6 border-t border-moonlight/10 pt-4">
                <p className="text-sm font-semibold text-moonlight">{name}</p>
                <p className="mt-0.5 text-xs text-moonlight/45">{role}</p>
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  );
}
