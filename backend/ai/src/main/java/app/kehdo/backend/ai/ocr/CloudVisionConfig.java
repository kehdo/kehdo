package app.kehdo.backend.ai.ocr;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Provisions the Cloud Vision client only when
 * {@code kehdo.ai.ocr.provider=gcp}. Defaults to {@code stub}, so local
 * dev / tests / CI never accidentally try to instantiate the real client
 * (which would attempt to contact ADC at startup).
 *
 * <p>Auth: relies on Application Default Credentials. Locally that's
 * the file gcloud writes to {@code %APPDATA%\gcloud\application_default_credentials.json};
 * in production the platform's attached service account /
 * Workload Identity is picked up automatically.</p>
 */
@Configuration
@ConditionalOnProperty(name = "kehdo.ai.ocr.provider", havingValue = "gcp")
public class CloudVisionConfig {

    @Bean(destroyMethod = "close")
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        return ImageAnnotatorClient.create();
    }
}
