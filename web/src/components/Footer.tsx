import Link from "next/link";
import { Github } from "lucide-react";

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
            <Link
              href="/"
              className="text-2xl font-bold tracking-tight"
              aria-label="kehdo home"
            >
              <span className="aurora-text">kehdo</span>
            </Link>
            <p className="mt-3 max-w-xs text-sm text-moonlight/45">
              Reply with quiet confidence. AI-powered chat reply generator for
              screenshots.
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

          <div className="col-span-1 md:col-span-2">
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

          <div className="col-span-2 md:col-span-2">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-moonlight/45">
              Connect
            </h3>
            <ul className="mt-4 space-y-3 text-sm">
              <li>
                <a
                  href="https://github.com/kehdo"
                  className="inline-flex items-center gap-2 text-moonlight/65 transition hover:text-moonlight"
                  rel="noopener noreferrer"
                  target="_blank"
                >
                  <Github className="h-4 w-4" />
                  <span>GitHub</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-16 flex flex-col items-start justify-between gap-3 border-t border-moonlight/10 pt-8 md:flex-row md:items-center">
          <p className="text-xs text-moonlight/45">
            © {new Date().getFullYear()} kehdo. All rights reserved.
          </p>
          <p className="text-xs text-moonlight/45 [font-feature-settings:'liga']">
            कह दो — say it.
          </p>
        </div>
      </div>
    </footer>
  );
}
