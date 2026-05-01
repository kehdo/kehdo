import { cn } from "@/lib/cn";

/**
 * The kehdo brand mark + wordmark + tagline lockup.
 *
 * <p>Three composition variants, two size scales. The mark is the only
 * element that carries the Aurora gradient; the wordmark uses moonlight
 * (default) so it stays legible. Per
 * <a href="../../../design/assets/logo/README.md">/design/assets/logo</a>,
 * never apply the gradient to the wordmark.</p>
 *
 * @example
 *   <Logo variant="lockup" size="lg" />            // hero / OG image
 *   <Logo variant="wordmark" size="md" />          // Nav, Footer
 *   <Logo variant="mark" size="sm" />              // tight UI corners
 */

type LogoVariant = "mark" | "wordmark" | "lockup";
type LogoSize = "sm" | "md" | "lg";

interface LogoProps {
  variant?: LogoVariant;
  size?: LogoSize;
  /** When true, the wordmark uses bg color instead of moonlight (for light backgrounds). */
  inverted?: boolean;
  /** Optional accessible label for the whole lockup; the mark itself stays decorative. */
  ariaLabel?: string;
  className?: string;
}

const MARK_DIMENSIONS: Record<LogoSize, { width: number; height: number }> = {
  sm: { width: 22, height: 24 },
  md: { width: 32, height: 35 },
  lg: { width: 56, height: 62 },
};

const WORDMARK_TEXT_CLASS: Record<LogoSize, string> = {
  sm: "text-base",
  md: "text-xl",
  lg: "text-4xl",
};

const TAGLINE_TEXT_CLASS: Record<LogoSize, string> = {
  sm: "text-[10px]",
  md: "text-xs",
  lg: "text-sm",
};

export function Logo({
  variant = "wordmark",
  size = "md",
  inverted = false,
  ariaLabel = "kehdo",
  className,
}: LogoProps) {
  const wordmarkColor = inverted ? "text-bg" : "text-moonlight";
  const taglineColor = inverted ? "text-bg/55" : "text-moonlight/55";

  if (variant === "mark") {
    return (
      <span
        role="img"
        aria-label={ariaLabel}
        className={cn("inline-block", className)}
        style={{
          width: MARK_DIMENSIONS[size].width,
          height: MARK_DIMENSIONS[size].height,
        }}
      >
        <Mark size={size} />
      </span>
    );
  }

  if (variant === "wordmark") {
    return (
      <span
        role="img"
        aria-label={ariaLabel}
        className={cn("inline-flex items-center gap-2.5", className)}
      >
        <Mark size={size} />
        <span
          className={cn(
            "font-jost font-semibold tracking-tight leading-none",
            WORDMARK_TEXT_CLASS[size],
            wordmarkColor,
          )}
        >
          kehdo
        </span>
      </span>
    );
  }

  // lockup
  return (
    <span
      role="img"
      aria-label={`${ariaLabel} — Just Say It`}
      className={cn("inline-flex flex-col items-center gap-2", className)}
    >
      <span className="inline-flex items-center gap-2.5">
        <Mark size={size} />
        <span
          className={cn(
            "font-jost font-semibold tracking-tight leading-none",
            WORDMARK_TEXT_CLASS[size],
            wordmarkColor,
          )}
        >
          kehdo
        </span>
      </span>
      <span
        className={cn(
          "font-jost font-medium uppercase tracking-[0.32em] leading-none",
          TAGLINE_TEXT_CLASS[size],
          taglineColor,
        )}
      >
        Just<span className="mx-1.5 opacity-70">·</span>Say
        <span className="mx-1.5 opacity-70">·</span>It
      </span>
    </span>
  );
}

function Mark({ size }: { size: LogoSize }) {
  const { width, height } = MARK_DIMENSIONS[size];
  // Per-instance gradient id so multiple Logo instances on a page don't
  // collide (SVG <defs> with the same id would otherwise share a single
  // definition across the document).
  const gradientId = `kehdoMarkGradient-${size}`;
  return (
    <svg
      width={width}
      height={height}
      viewBox="0 0 40 44"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      className="flex-shrink-0"
    >
      <defs>
        <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#9C5BFF" />
          <stop offset="50%" stopColor="#EC4899" />
          <stop offset="100%" stopColor="#F59E0B" />
        </linearGradient>
      </defs>
      <line
        x1="8"
        y1="6"
        x2="8"
        y2="34"
        stroke={`url(#${gradientId})`}
        strokeWidth="3.3"
        strokeLinecap="round"
      />
      <line
        x1="8"
        y1="20"
        x2="30"
        y2="6"
        stroke={`url(#${gradientId})`}
        strokeWidth="3.3"
        strokeLinecap="round"
      />
      <line
        x1="8"
        y1="20"
        x2="30"
        y2="34"
        stroke={`url(#${gradientId})`}
        strokeWidth="3.3"
        strokeLinecap="round"
      />
      <rect
        x="10"
        y="40"
        width="20"
        height="3"
        rx="1.5"
        fill={`url(#${gradientId})`}
        opacity="0.4"
      />
    </svg>
  );
}
