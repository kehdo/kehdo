import { Sparkles, Zap, Target, Lock, Globe } from "lucide-react";

export function FeaturesBento() {
  return (
    <section id="features" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            Features
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Everything you need.{" "}
            <span className="aurora-text font-serif italic">
              Nothing you don&apos;t.
            </span>
          </h2>
        </div>

        <div className="mt-16 grid gap-4 sm:grid-cols-2 lg:grid-cols-3 lg:grid-rows-2">
          {/* 18 tones — large hero card */}
          <div className="relative overflow-hidden rounded-2xl border border-moonlight/10 bg-gradient-to-br from-purple/15 via-surface/60 to-pink/10 p-8 backdrop-blur-sm sm:col-span-2 sm:row-span-2 lg:col-span-2 lg:row-span-2">
            <Sparkles
              aria-hidden="true"
              className="h-8 w-8 text-purple-bright"
            />
            <h3 className="mt-6 text-3xl font-bold leading-tight tracking-tight md:text-4xl">
              18 reply tones —{" "}
              <span className="aurora-text font-serif italic">
                from flirty to formal
              </span>
              .
            </h3>
            <p className="mt-4 max-w-md text-sm leading-relaxed text-moonlight/65">
              Casual. Witty. Polite. Flirty. Professional. Sarcastic. Heartfelt.
              Apologetic. Playful. Each tone is a separately-tuned prompt
              pipeline — not just a temperature knob.
            </p>
            <div className="mt-6 flex flex-wrap gap-2">
              {[
                "Casual",
                "Witty",
                "Flirty",
                "Polite",
                "Sarcastic",
                "Heartfelt",
                "+12 more",
              ].map((tone, i) => (
                <span
                  key={tone}
                  className={`rounded-full px-3 py-1 text-xs ${
                    i === 0
                      ? "bg-aurora-gradient text-white"
                      : "border border-moonlight/15 text-moonlight/65"
                  }`}
                >
                  {tone}
                </span>
              ))}
            </div>
          </div>

          {/* <6s */}
          <div className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm">
            <Zap aria-hidden="true" className="h-7 w-7 text-amber" />
            <p className="mt-6 text-4xl font-bold tracking-tight text-moonlight">
              &lt; 6s
            </p>
            <p className="mt-2 text-sm text-moonlight/65">
              From screenshot upload to four ranked replies. Cached for repeat
              conversations.
            </p>
          </div>

          {/* Privacy */}
          <div className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm">
            <Lock aria-hidden="true" className="h-7 w-7 text-blue" />
            <p className="mt-6 text-xl font-semibold text-moonlight">
              Privacy-first
            </p>
            <p className="mt-2 text-sm text-moonlight/65">
              On-device OCR (Pro). 30-day server delete. Zero training on your
              data — period.
            </p>
          </div>

          {/* 97% accuracy */}
          <div className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm">
            <Target aria-hidden="true" className="h-7 w-7 text-pink" />
            <p className="mt-6 text-4xl font-bold tracking-tight text-moonlight">
              97%
            </p>
            <p className="mt-2 text-sm text-moonlight/65">
              Speaker attribution accuracy on common chat layouts (WhatsApp,
              iMessage, Slack, Telegram, IG).
            </p>
          </div>

          {/* 30+ languages */}
          <div className="rounded-2xl border border-moonlight/10 bg-surface/40 p-8 backdrop-blur-sm">
            <Globe aria-hidden="true" className="h-7 w-7 text-purple-bright" />
            <p className="mt-6 text-4xl font-bold tracking-tight text-moonlight">
              30+
            </p>
            <p className="mt-2 text-sm text-moonlight/65">
              Languages supported. Reply in the language of the conversation,
              or pivot to another mid-thread.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
