package app.kehdo.backend.api.tone;

import app.kehdo.backend.api.tone.dto.ToneDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public catalog endpoint per {@code contracts/openapi/kehdo.v1.yaml}'s
 * {@code GET /tones}. No auth required — clients use the response to
 * render the tone picker on every screen, including pre-login marketing
 * surfaces.
 *
 * <p>Public access is granted in {@code SecurityConfig} via a
 * {@code permitAll()} matcher on {@code GET /tones}.</p>
 */
@RestController
@RequestMapping("/tones")
public class ToneController {

    private final ToneCatalog catalog;

    public ToneController(ToneCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<ToneDto> listTones() {
        return catalog.all();
    }
}
