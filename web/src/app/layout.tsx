import type { Metadata } from "next";
import { Inter, Instrument_Serif, Jost } from "next/font/google";
import { Analytics } from "@vercel/analytics/next";
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

const jost = Jost({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-jost",
  display: "swap",
});

export const metadata: Metadata = {
  metadataBase: new URL("https://kehdo.app"),
  title: "kehdo — Just Say It.",
  description:
    "AI-powered reply generator for chat screenshots. Drop a screenshot from WhatsApp, iMessage, Slack, or Instagram. Get the perfect reply in seconds.",
  openGraph: {
    title: "kehdo — Just Say It.",
    description:
      "Drop a chat screenshot, get the perfect reply in seconds. 5 free replies/day, forever.",
    url: "https://kehdo.app",
    siteName: "kehdo",
    locale: "en_US",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "kehdo — Just Say It.",
    description: "Drop a chat screenshot, get the perfect reply in seconds.",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${instrumentSerif.variable} ${jost.variable}`}
    >
      <body>
        <Nav />
        {children}
        <Footer />
        <Analytics />
      </body>
    </html>
  );
}
