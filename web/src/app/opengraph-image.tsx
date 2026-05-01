import { ImageResponse } from "next/og";

export const runtime = "edge";
export const alt = "kehdo — Just Say It.";
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
        {/* Brand lockup top-left */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "20px",
          }}
        >
          {/* Logo mark */}
          <svg
            width="60"
            height="66"
            viewBox="0 0 40 44"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <defs>
              <linearGradient id="ogMarkGrad" x1="0" y1="0" x2="1" y2="1">
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
              stroke="url(#ogMarkGrad)"
              strokeWidth="3.3"
              strokeLinecap="round"
            />
            <line
              x1="8"
              y1="20"
              x2="30"
              y2="6"
              stroke="url(#ogMarkGrad)"
              strokeWidth="3.3"
              strokeLinecap="round"
            />
            <line
              x1="8"
              y1="20"
              x2="30"
              y2="34"
              stroke="url(#ogMarkGrad)"
              strokeWidth="3.3"
              strokeLinecap="round"
            />
            <rect
              x="10"
              y="40"
              width="20"
              height="3"
              rx="1.5"
              fill="url(#ogMarkGrad)"
              opacity="0.4"
            />
          </svg>
          <span
            style={{
              fontSize: "44px",
              fontWeight: 600,
              letterSpacing: "-0.01em",
              color: "#F5F3FF",
            }}
          >
            kehdo
          </span>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: "32px" }}>
          <div
            style={{
              display: "flex",
              fontSize: "120px",
              fontWeight: 700,
              lineHeight: 1.0,
              letterSpacing: "-0.03em",
              maxWidth: "1000px",
            }}
          >
            <span style={{ display: "flex", flexWrap: "wrap" }}>
              Just{" "}
              <span
                style={{
                  fontStyle: "italic",
                  fontWeight: 400,
                  fontFamily: "serif",
                  marginLeft: "12px",
                  marginRight: "12px",
                  backgroundImage:
                    "linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)",
                  backgroundClip: "text",
                  color: "transparent",
                }}
              >
                Say
              </span>
              It.
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
    { ...size },
  );
}
