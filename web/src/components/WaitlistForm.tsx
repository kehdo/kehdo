"use client";

import { useState } from "react";

type Status = "idle" | "submitting" | "success" | "error";

export function WaitlistForm() {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [message, setMessage] = useState("");

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setStatus("submitting");
    setMessage("");

    try {
      const res = await fetch("/api/waitlist", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ email, source: "landing" }),
      });
      const data = await res.json();

      if (!res.ok) {
        setStatus("error");
        setMessage(data?.error?.message ?? "Something went wrong.");
        return;
      }

      setStatus("success");
      setMessage("You're on the list — we'll be in touch.");
      setEmail("");
    } catch {
      setStatus("error");
      setMessage("Network error. Please try again.");
    }
  }

  return (
    <form
      onSubmit={onSubmit}
      aria-label="Waitlist signup"
      className="mt-10 flex w-full max-w-md flex-col gap-3 sm:flex-row"
    >
      <input
        type="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="you@example.com"
        className="flex-1 rounded-full border border-moonlight/15 bg-surface px-5 py-3 text-sm text-moonlight placeholder:text-moonlight/40 focus:border-purple-bright focus:outline-none"
        disabled={status === "submitting"}
      />
      <button
        type="submit"
        disabled={status === "submitting" || !email}
        className="rounded-full bg-aurora-gradient px-6 py-3 text-sm font-semibold text-white transition hover:scale-105 disabled:opacity-50"
      >
        {status === "submitting" ? "..." : "Get early access"}
      </button>
      {message && (
        <p
          className={`mt-2 text-xs ${
            status === "success" ? "text-success" : "text-pink"
          }`}
          aria-live="polite"
        >
          {message}
        </p>
      )}
    </form>
  );
}
