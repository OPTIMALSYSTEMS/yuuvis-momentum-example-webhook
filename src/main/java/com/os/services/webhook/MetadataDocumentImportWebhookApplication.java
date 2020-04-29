
package com.os.services.webhook;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetadataDocumentImportWebhookApplication
{
    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(MetadataDocumentImportWebhookApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
