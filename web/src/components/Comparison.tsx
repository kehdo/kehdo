import { Check, Minus, X } from "lucide-react";

type Cell = "yes" | "no" | "partial";

type Row = {
  feature: string;
  typingYourself: Cell;
  genericAi: Cell;
  kehdo: Cell;
};

const ROWS: Row[] = [
  {
    feature: "Time to a great reply",
    typingYourself: "no",
    genericAi: "partial",
    kehdo: "yes",
  },
  {
    feature: "Reads tone of the original conversation",
    typingYourself: "yes",
    genericAi: "no",
    kehdo: "yes",
  },
  {
    feature: "Knows who said what",
    typingYourself: "yes",
    genericAi: "no",
    kehdo: "yes",
  },
  {
    feature: "Multiple replies to choose from",
    typingYourself: "no",
    genericAi: "partial",
    kehdo: "yes",
  },
  {
    feature: "Privacy-first by default",
    typingYourself: "yes",
    genericAi: "no",
    kehdo: "yes",
  },
  {
    feature: "On-device option",
    typingYourself: "yes",
    genericAi: "no",
    kehdo: "yes",
  },
];

function Mark({ value }: { value: Cell }) {
  if (value === "yes") {
    return (
      <span
        aria-label="Yes"
        className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-success/15 text-success"
      >
        <Check className="h-4 w-4" />
      </span>
    );
  }
  if (value === "partial") {
    return (
      <span
        aria-label="Partial"
        className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-amber/15 text-amber"
      >
        <Minus className="h-4 w-4" />
      </span>
    );
  }
  return (
    <span
      aria-label="No"
      className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-pink/15 text-pink"
    >
      <X className="h-4 w-4" />
    </span>
  );
}

export function Comparison() {
  return (
    <section className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-5xl">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-purple-bright">
            How we compare
          </p>
          <h2 className="mt-4 text-4xl font-bold leading-tight tracking-tight md:text-5xl">
            Built for{" "}
            <span className="aurora-text font-serif italic">replies</span>, not
            for everything.
          </h2>
        </div>

        <div className="mt-16 overflow-x-auto rounded-2xl border border-moonlight/10 bg-surface/40 backdrop-blur-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-moonlight/10">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-moonlight/45"
                >
                  Feature
                </th>
                <th
                  scope="col"
                  className="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-moonlight/45"
                >
                  Typing yourself
                </th>
                <th
                  scope="col"
                  className="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-moonlight/45"
                >
                  Generic AI chat
                </th>
                <th
                  scope="col"
                  className="border-l border-moonlight/10 bg-purple/5 px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-purple-bright"
                >
                  kehdo
                </th>
              </tr>
            </thead>
            <tbody>
              {ROWS.map((row) => (
                <tr
                  key={row.feature}
                  className="border-b border-moonlight/10 last:border-0"
                >
                  <td className="px-6 py-4 text-moonlight/85">{row.feature}</td>
                  <td className="px-6 py-4 text-center">
                    <Mark value={row.typingYourself} />
                  </td>
                  <td className="px-6 py-4 text-center">
                    <Mark value={row.genericAi} />
                  </td>
                  <td className="border-l border-moonlight/10 bg-purple/5 px-6 py-4 text-center">
                    <Mark value={row.kehdo} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
