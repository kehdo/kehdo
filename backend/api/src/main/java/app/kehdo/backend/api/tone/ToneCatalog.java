package app.kehdo.backend.api.tone;

import app.kehdo.backend.api.tone.dto.ToneDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authoritative catalog of the 18 reply tones surfaced by {@code GET /tones}.
 *
 * <p>Single source of truth on the backend; the OpenAPI {@code ToneCode}
 * enum mirrors the {@link ToneDto#code()} values, and the per-tone copy
 * mirrors {@code contracts/openapi/examples/tones/list-response-200.json}.
 * If you add a tone here, also add it to:
 * <ul>
 *   <li>{@code contracts/openapi/kehdo.v1.yaml#components.schemas.ToneCode}</li>
 *   <li>{@code contracts/openapi/examples/tones/list-response-200.json}</li>
 *   <li>{@code design/copy/<lang>.json} for the localized name/description</li>
 * </ul>
 */
@Component
public class ToneCatalog {

    private static final List<ToneDto> TONES = List.of(
            // ---- Free tier (8) ----------------------------------------------
            new ToneDto("CASUAL",       "Casual",       "💬",         "Relaxed, conversational",         false),
            new ToneDto("WARM",         "Warm",         "🤗",         "Kind, considerate",               false),
            new ToneDto("DIRECT",       "Direct",       "🎯",         "Concise and to the point",        false),
            new ToneDto("THOUGHTFUL",   "Thoughtful",   "🧠",         "Reflective and considered",       false),
            new ToneDto("GRATEFUL",     "Grateful",     "🙏",         "Sincere thanks",                  false),
            new ToneDto("APOLOGETIC",   "Apologetic",   "😔",         "Genuine, no over-explaining",     false),
            new ToneDto("CONFIDENT",    "Confident",    "😎",         "Assured without arrogance",       false),
            new ToneDto("BRIEF",        "Brief",        "✂️",         "Minimal — \"k thx\" energy",      false),
            // ---- Pro tier (10) ----------------------------------------------
            new ToneDto("FLIRTY",       "Flirty",       "😏",         "Playful interest",                true),
            new ToneDto("WITTY",        "Witty",        "🪄",         "Clever and self-aware",           true),
            new ToneDto("PLAYFUL",      "Playful",      "🎈",         "Light-hearted, fun",              true),
            new ToneDto("SARCASTIC",    "Sarcastic",    "🙃",         "Dry, knowing humor",              true),
            new ToneDto("FORMAL",       "Formal",       "🎩",         "Professional, polished",          true),
            new ToneDto("POETIC",       "Poetic",       "✨",               "Expressive, lyrical",             true),
            new ToneDto("EMPATHETIC",   "Empathetic",   "💗",         "Deeply listening",                true),
            new ToneDto("ENTHUSIASTIC", "Enthusiastic", "🔥",         "High-energy, hype",               true),
            new ToneDto("DIPLOMATIC",   "Diplomatic",   "🕊️",   "Tactful, conflict-defusing",      true),
            new ToneDto("CURIOUS",      "Curious",      "🧐",         "Inquisitive, opens conversation", true)
    );

    public List<ToneDto> all() {
        return TONES;
    }
}
