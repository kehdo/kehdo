import { Check, Copy, RefreshCcw } from "lucide-react";

const TONES = ["Casual", "Flirty", "Polite", "Witty"];

const REPLIES = [
  { text: "Yeah, what time were you thinking?", best: true },
  { text: "Free after 7 — pick a place?", best: false },
  { text: "Maybe — depends on whether the meeting wraps.", best: false },
  { text: "Tonight's tight, but tomorrow's wide open.", best: false },
];

export function ProductPreview() {
  return (
    <section className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            See it in action
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Drop, parse,{" "}
            <span className="aurora-text font-serif italic">reply</span>.
          </h2>
        </div>

        <div className="mt-16">
          <div className="relative mx-auto max-w-3xl overflow-hidden rounded-2xl border border-moonlight/10 bg-surface/60 shadow-[0_0_80px_-20px_rgba(156,91,255,0.45)] backdrop-blur-md">
            {/* Window chrome */}
            <div className="flex items-center gap-2 border-b border-moonlight/10 px-4 py-3">
              <span
                aria-hidden="true"
                className="h-3 w-3 rounded-full bg-pink"
              />
              <span
                aria-hidden="true"
                className="h-3 w-3 rounded-full bg-amber"
              />
              <span
                aria-hidden="true"
                className="h-3 w-3 rounded-full bg-success"
              />
              <span className="ml-3 text-xs text-moonlight/45">
                kehdo · screenshot.png
              </span>
            </div>

            {/* Conversation preview */}
            <div className="space-y-3 border-b border-moonlight/10 p-6">
              <div className="flex justify-start">
                <div className="max-w-[75%] rounded-2xl rounded-bl-sm bg-surface-2 px-4 py-2.5 text-sm text-moonlight">
                  Are you free tonight?
                </div>
              </div>
              <div className="flex justify-start">
                <div className="max-w-[75%] rounded-2xl rounded-bl-sm bg-surface-2 px-4 py-2.5 text-sm text-moonlight">
                  there&apos;s this new ramen place
                </div>
              </div>
              <div className="flex justify-end">
                <div className="max-w-[75%] rounded-2xl rounded-br-sm bg-purple/20 px-4 py-2.5 text-sm text-moonlight/85 italic">
                  …
                </div>
              </div>
            </div>

            {/* Tone selector */}
            <div className="flex flex-wrap items-center gap-2 border-b border-moonlight/10 px-6 py-4">
              <span className="text-xs uppercase tracking-wider text-moonlight/45">
                Tone
              </span>
              {TONES.map((tone, i) => (
                <span
                  key={tone}
                  className={`rounded-full px-3 py-1 text-xs font-medium ${
                    i === 0
                      ? "bg-aurora-gradient text-white"
                      : "border border-moonlight/15 text-moonlight/65"
                  }`}
                >
                  {tone}
                </span>
              ))}
            </div>

            {/* Replies list */}
            <div className="space-y-3 p-6">
              {REPLIES.map(({ text, best }) => (
                <div
                  key={text}
                  className={`group flex items-start gap-3 rounded-xl border p-4 transition ${
                    best
                      ? "border-purple/40 bg-purple/10"
                      : "border-moonlight/10 bg-surface-2/40"
                  }`}
                >
                  {best && (
                    <span className="flex h-6 items-center gap-1.5 rounded-full bg-aurora-gradient px-2 text-[10px] font-semibold uppercase tracking-wider text-white">
                      <Check className="h-3 w-3" />
                      Best
                    </span>
                  )}
                  <p className="flex-1 text-sm text-moonlight">{text}</p>
                  <button
                    type="button"
                    aria-label={`Copy reply: ${text}`}
                    className="rounded-lg p-1 text-moonlight/45 opacity-0 transition group-hover:opacity-100 hover:text-moonlight"
                  >
                    <Copy className="h-4 w-4" />
                  </button>
                </div>
              ))}
              <button
                type="button"
                className="mt-2 inline-flex items-center gap-2 text-xs text-moonlight/45 transition hover:text-moonlight"
              >
                <RefreshCcw className="h-3 w-3" />
                Regenerate
              </button>
            </div>
          </div>

          <p className="mt-6 text-center text-xs text-moonlight/45">
            Static preview — drop a real screenshot in the app to see your own
            replies.
          </p>
        </div>
      </div>
    </section>
  );
}
