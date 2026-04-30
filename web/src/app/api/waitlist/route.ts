import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";

const Body = z.object({
  email: z.string().email(),
  source: z.string().optional(),
});

/**
 * POST /api/waitlist
 *
 * Forwards the email to a Google Apps Script web app endpoint that
 * appends a row to a Google Sheet. The Apps Script is responsible for
 * deduplication; this route just relays.
 *
 * Falls back to a dev-friendly console.log when GOOGLE_SHEET_WEBHOOK_URL
 * is missing (e.g. local pnpm dev without a .env.local).
 */
export async function POST(req: NextRequest) {
  try {
    const json = await req.json();
    const parsed = Body.safeParse(json);

    if (!parsed.success) {
      return NextResponse.json(
        {
          error: {
            code: "INVALID_INPUT",
            message: "Email is required and must be valid.",
          },
        },
        { status: 400 }
      );
    }

    const webhookUrl = process.env.GOOGLE_SHEET_WEBHOOK_URL;

    if (!webhookUrl) {
      // Dev fallback — webhook not configured. Log and accept so local UX works.
      console.log(
        "[waitlist] (dev — GOOGLE_SHEET_WEBHOOK_URL not set):",
        parsed.data.email
      );
      return NextResponse.json({ ok: true }, { status: 201 });
    }

    const upstream = await fetch(webhookUrl, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email: parsed.data.email,
        source: parsed.data.source ?? "landing",
        timestamp: new Date().toISOString(),
      }),
      // Apps Script web apps can be slow on cold start; cap our wait.
      signal: AbortSignal.timeout(8000),
    });

    if (!upstream.ok) {
      console.error("[waitlist] webhook returned non-2xx:", upstream.status);
      return NextResponse.json(
        {
          error: {
            code: "WAITLIST_FAILED",
            message: "Couldn't add you right now. Please try again in a moment.",
          },
        },
        { status: 502 }
      );
    }

    // Apps Script may answer with { ok, alreadyOnList } JSON or a plain "OK".
    const text = await upstream.text();
    let result: { ok?: boolean; alreadyOnList?: boolean } = {};
    try {
      result = JSON.parse(text);
    } catch {
      // Plain-text response — treat as success
    }

    if (result.alreadyOnList) {
      return NextResponse.json(
        { ok: true, alreadyOnList: true },
        { status: 200 }
      );
    }

    return NextResponse.json({ ok: true }, { status: 201 });
  } catch (err) {
    console.error("[waitlist] error:", err);
    return NextResponse.json(
      {
        error: {
          code: "SERVER_ERROR",
          message: "Something went wrong. Please try again.",
        },
      },
      { status: 500 }
    );
  }
}
