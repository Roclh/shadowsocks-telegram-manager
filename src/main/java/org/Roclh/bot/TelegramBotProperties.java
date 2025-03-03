package org.Roclh.bot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.Delimiter;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramBotProperties {
    @NotBlank
    private String name;
    @NotBlank
    private String token;
    @NotBlank
    private String paymentToken;
    @NotBlank
    private Long defaultManagerId;
    @NotBlank
    @Delimiter(";")
    private List<String> supportedLocales;
    @NotBlank
    private String defaultLocale;
    private int maxUpdateBuffer = 30;
    private int maxThreads = 1;
}
