import { NextRequest, NextResponse } from "next/server";
import { Resend } from "resend";
import { z } from "zod";

const Body = z.object({
  email: z.string().email(),
  source: z.string().optional(),
});

/**
 * POST /api/waitlist
 *
 * Adds an email to the kehdo beta waitlist via Resend Audiences.
 * Falls back to a dev-friendly console.log when env vars are missing
 * (e.g. local pnpm dev without a .env.local).
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

    const apiKey = process.env.RESEND_API_KEY;
    const audienceId = process.env.RESEND_AUDIENCE_ID;

    if (!apiKey || !audienceId) {
      // Dev fallback — env not configured. Log and accept so local UX works.
      console.log("[waitlist] (dev — Resend env not set):", parsed.data.email);
      return NextResponse.json({ ok: true }, { status: 201 });
    }

    const resend = new Resend(apiKey);
    const result = await resend.contacts.create({
      email: parsed.data.email,
      audienceId,
      unsubscribed: false,
    });

    if (result.error) {
      // Resend treats already-subscribed as an error; treat it as success
      // for the user (idempotent) but distinguish in logs.
      const message = result.error.message ?? "";
      const alreadyExists = /already exists|already subscribed/i.test(message);

      if (alreadyExists) {
        console.log("[waitlist] already on list:", parsed.data.email);
        return NextResponse.json({ ok: true, alreadyOnList: true }, { status: 200 });
      }

      console.error("[waitlist] Resend error:", result.error);
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
