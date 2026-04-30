import { ImageResponse } from "next/og";

export const runtime = "edge";
export const alt = "kehdo — Reply with quiet confidence.";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default async function Image() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          padding: "80px",
          background: "#0A0612",
          backgroundImage:
            "radial-gradient(circle at 20% 20%, rgba(156, 91, 255, 0.4) 0%, transparent 45%), radial-gradient(circle at 80% 80%, rgba(236, 72, 153, 0.3) 0%, transparent 45%), radial-gradient(circle at 70% 30%, rgba(245, 158, 11, 0.2) 0%, transparent 35%)",
          color: "#F5F3FF",
          fontFamily: "Inter",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "12px",
            fontSize: "36px",
            fontWeight: 800,
            letterSpacing: "-0.02em",
          }}
        >
          <span
            style={{
              backgroundImage:
                "linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)",
              backgroundClip: "text",
              color: "transparent",
            }}
          >
            kehdo
          </span>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: "32px" }}>
          <div
            style={{
              display: "flex",
              fontSize: "92px",
              fontWeight: 800,
              lineHeight: 1.05,
              letterSpacing: "-0.03em",
              maxWidth: "900px",
            }}
          >
            <span style={{ display: "flex", flexWrap: "wrap" }}>
              Reply with{" "}
              <span
                style={{
                  fontStyle: "italic",
                  fontWeight: 400,
                  fontFamily: "serif",
                  marginLeft: "12px",
                  backgroundImage:
                    "linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)",
                  backgroundClip: "text",
                  color: "transparent",
                }}
              >
                quiet confidence.
              </span>
            </span>
          </div>

          <div
            style={{
              display: "flex",
              fontSize: "30px",
              color: "rgba(245, 243, 255, 0.65)",
              lineHeight: 1.4,
              maxWidth: "850px",
            }}
          >
            AI-powered reply generator for chat screenshots. Five free replies
            a day, forever.
          </div>
        </div>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            fontSize: "24px",
            color: "rgba(245, 243, 255, 0.45)",
          }}
        >
          <span>kehdo.app</span>
          <span style={{ display: "flex", gap: "16px" }}>
            <span>WhatsApp</span>
            <span>·</span>
            <span>iMessage</span>
            <span>·</span>
            <span>Slack</span>
            <span>·</span>
            <span>Instagram</span>
          </span>
        </div>
      </div>
    ),
    { ...size }
  );
}
