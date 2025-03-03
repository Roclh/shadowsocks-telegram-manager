package org.Roclh.bot;

import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Component
public class TelegramBotStorage {
    private TelegramBot telegramBot;

    public TelegramBot getTelegramBot(){
        if(telegramBot == null){
            throw  new RuntimeException("Illegal state: Telegram bot is not initialized");
        }
        return telegramBot;
    }
}
