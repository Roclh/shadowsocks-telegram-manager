package org.Roclh.data.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.data.entities.LocalizationModel;
import org.Roclh.data.repositories.LocalizationRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalizationService {
    private final LocalizationRepository localizationRepository;
    private final TelegramBotProperties telegramBotProperties;

    @NonNull
    public Locale getOrCreate(@NonNull Long telegramId) {
        if (localizationRepository.existsById(telegramId)) {
            return localizationRepository.findById(telegramId).map(LocalizationModel::getLocale).map(Locale::forLanguageTag).orElse(Locale.forLanguageTag(telegramBotProperties.getDefaultLocale()));
        } else {
            LocalizationModel localizationModel = LocalizationModel.builder()
                    .telegramId(telegramId)
                    .locale(telegramBotProperties.getDefaultLocale())
                    .build();
            return Locale.forLanguageTag(localizationRepository.saveAndFlush(localizationModel).getLocale());
        }
    }

    public boolean exists(@NonNull Long telegramId){
        return localizationRepository.existsById(telegramId);
    }

    public boolean setLocale(@NonNull Long telegramId, String locale) {
        if (localizationRepository.existsById(telegramId)) {
            return localizationRepository.updateLocaleByTelegramId(locale, telegramId) > 0;
        }
        return false;
    }

    public boolean matchesLang(@NonNull Long telegramId, String locale) {
        return localizationRepository.findById(telegramId)
                .map(localizationModel -> localizationModel.getLocale().equals(locale)).orElse(false);
    }
}
