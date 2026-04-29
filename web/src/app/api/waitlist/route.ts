import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";

const Body = z.object({
  email: z.string().email(),
  source: z.string().optional(),
});

/**
 * POST /api/waitlist
 *
 * Adds an email to the kehdo beta waitlist.
 * Wires to Resend audiences (or a Postgres table once the backend ships).
 *
 * Phase 0: stub. Phase 1: Resend integration. Phase 2: backend endpoint.
 */
export async function POST(req: NextRequest) {
  try {
    const json = await req.json();
    const parsed = Body.safeParse(json);

    if (!parsed.success) {
      return NextResponse.json(
        { error: { code: "INVALID_INPUT", message: "Email is required and must be valid." } },
        { status: 400 }
      );
    }

    // TODO Phase 1: integrate Resend
    //
    //   import { Resend } from "resend";
    //   const resend = new Resend(process.env.RESEND_API_KEY);
    //   await resend.contacts.create({
    //     email: parsed.data.email,
    //     audienceId: process.env.RESEND_AUDIENCE_ID!,
    //   });

    console.log("[waitlist] new signup:", parsed.data.email);

    return NextResponse.json({ ok: true }, { status: 201 });
  } catch (err) {
    console.error("[waitlist] error:", err);
    return NextResponse.json(
      { error: { code: "SERVER_ERROR", message: "Something went wrong. Please try again." } },
      { status: 500 }
    );
  }
}
