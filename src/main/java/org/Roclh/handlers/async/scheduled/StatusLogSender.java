package org.Roclh.handlers.async.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBot;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.data.services.TelegramUserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatusLogSender {
    private final TelegramBot telegramBot;
    private final TelegramUserService telegramUserService;
    private final LocalizationService localizationService;

    @Scheduled(cron = "0 0 10-22 * * *")
    public void sendStatus(){
        log.info("Sending scheduled status to managers");
    }
}
