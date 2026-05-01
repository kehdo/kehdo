import Link from "next/link";
import { Logo } from "./Logo";

const PRODUCT_LINKS = [
  { href: "#how-it-works", label: "How it works" },
  { href: "#features", label: "Features" },
  { href: "#pricing", label: "Pricing" },
  { href: "#faq", label: "FAQ" },
];

const COMPANY_LINKS = [
  { href: "/privacy", label: "Privacy" },
  { href: "/terms", label: "Terms" },
  { href: "mailto:hi@kehdo.app", label: "Contact" },
];

export function Footer() {
  return (
    <footer className="border-t border-moonlight/10 bg-surface/40 backdrop-blur-sm">
      <div className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid grid-cols-2 gap-10 md:grid-cols-12">
          <div className="col-span-2 md:col-span-5">
            <Link href="/" aria-label="kehdo home">
              <Logo variant="wordmark" size="lg" />
            </Link>
            <p className="mt-4 font-jost text-xs uppercase tracking-[0.32em] text-moonlight/55">
              Just<span className="mx-1.5 opacity-70">·</span>Say
              <span className="mx-1.5 opacity-70">·</span>It
            </p>
            <p className="mt-5 max-w-xs text-sm text-moonlight/45">
              AI-powered chat reply generator for screenshots. Drop, parse,
              reply — in any tone.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <span
                aria-disabled="true"
                className="cursor-not-allowed rounded-lg border border-moonlight/15 px-4 py-2 text-xs text-moonlight/45"
              >
                App Store · coming soon
              </span>
              <span
                aria-disabled="true"
                className="cursor-not-allowed rounded-lg border border-moonlight/15 px-4 py-2 text-xs text-moonlight/45"
              >
                Google Play · coming soon
              </span>
            </div>
          </div>

          <div className="col-span-1 md:col-span-3">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-moonlight/45">
              Product
            </h3>
            <ul className="mt-4 space-y-3 text-sm">
              {PRODUCT_LINKS.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-moonlight/65 transition hover:text-moonlight"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div className="col-span-1 md:col-span-4">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-moonlight/45">
              Company
            </h3>
            <ul className="mt-4 space-y-3 text-sm">
              {COMPANY_LINKS.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-moonlight/65 transition hover:text-moonlight"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-16 flex flex-col items-start justify-between gap-3 border-t border-moonlight/10 pt-8 md:flex-row md:items-center">
          <p className="text-xs text-moonlight/45">
            © {new Date().getFullYear()} kehdo. All rights reserved.
          </p>
          <p className="text-xs text-moonlight/45 [font-feature-settings:'liga']">
            कह दो — Just Say It.
          </p>
        </div>
      </div>
    </footer>
  );
}
