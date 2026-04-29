import { ImageDown, Sparkles, MessagesSquare } from "lucide-react";

const STEPS = [
  {
    number: "01",
    Icon: ImageDown,
    title: "Drop your screenshot",
    description:
      "Drag, paste, or pick a screenshot from any chat app — WhatsApp, iMessage, Slack, Instagram, anywhere.",
  },
  {
    number: "02",
    Icon: Sparkles,
    title: "AI reads the conversation",
    description:
      "We identify speakers, parse tone, and understand context. Privacy-first: pro plan runs OCR on-device.",
  },
  {
    number: "03",
    Icon: MessagesSquare,
    title: "Pick the perfect reply",
    description:
      "Get four ranked replies in your chosen tone. Tap to copy, or refine with a custom prompt for a sharper edit.",
  },
];

export function HowItWorks() {
  return (
    <section id="how-it-works" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="max-w-2xl">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            How it works
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            From screenshot to{" "}
            <span className="aurora-text font-serif italic">perfect reply</span>{" "}
            in three steps.
          </h2>
        </div>

        <ol className="mt-16 grid gap-8 md:grid-cols-3 md:gap-6">
          {STEPS.map(({ number, Icon, title, description }) => (
            <li
              key={number}
              className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm transition hover:border-purple/30"
            >
              <div className="flex items-center gap-4">
                <span className="font-serif text-3xl italic text-moonlight/35">
                  {number}
                </span>
                <span
                  aria-hidden="true"
                  className="flex h-10 w-10 items-center justify-center rounded-full border border-purple/30 bg-purple/10 text-purple-bright"
                >
                  <Icon className="h-5 w-5" />
                </span>
              </div>
              <h3 className="mt-6 text-xl font-semibold text-moonlight">
                {title}
              </h3>
              <p className="mt-3 text-sm leading-relaxed text-moonlight/65">
                {description}
              </p>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
