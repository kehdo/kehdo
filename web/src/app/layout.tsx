import type { Metadata } from "next";
import { Inter, Instrument_Serif } from "next/font/google";
import Script from "next/script";
import { Nav } from "@/components/Nav";
import { Footer } from "@/components/Footer";
import "@/styles/globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const instrumentSerif = Instrument_Serif({
  weight: "400",
  style: "italic",
  subsets: ["latin"],
  variable: "--font-instrument-serif",
  display: "swap",
});

export const metadata: Metadata = {
  metadataBase: new URL("https://kehdo.app"),
  title: "kehdo — Reply with quiet confidence.",
  description:
    "AI-powered reply generator for chat screenshots. Drop a screenshot from WhatsApp, iMessage, Slack, or Instagram. Get the perfect reply in seconds.",
  openGraph: {
    title: "kehdo — Reply with quiet confidence.",
    description:
      "Drop a chat screenshot, get the perfect reply in seconds. 5 free replies/day, forever.",
    url: "https://kehdo.app",
    siteName: "kehdo",
    locale: "en_US",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "kehdo — Reply with quiet confidence.",
    description: "Drop a chat screenshot, get the perfect reply in seconds.",
  },
};

const PLAUSIBLE_DOMAIN = process.env.NEXT_PUBLIC_PLAUSIBLE_DOMAIN;

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={`${inter.variable} ${instrumentSerif.variable}`}>
      <body>
        <Nav />
        {children}
        <Footer />
        {PLAUSIBLE_DOMAIN && (
          <Script
            defer
            data-domain={PLAUSIBLE_DOMAIN}
            src="https://plausible.io/js/script.js"
            strategy="afterInteractive"
          />
        )}
      </body>
    </html>
  );
}
