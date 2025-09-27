package org.Roclh.handlers.messaging;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.Roclh.data.entities.TelegramUserModel;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Locale;

@Data
@Builder
public class MessageData {
    @Nullable
    private String telegramName;
    @NonNull
    private Long telegramId;
    @NonNull
    private Long chatId;
    @Nullable
    private Integer messageId;
    @NonNull
    private Locale locale;

    @NonNull
    public static MessageData fromUser(@NonNull TelegramUserModel telegramUserModel, @NonNull Locale locale){
        Assert.notNull(telegramUserModel.getChatId(), "Chat id can't be null!");
        return builder()
                .telegramName(telegramUserModel.getTelegramName())
                .telegramId(telegramUserModel.getTelegramId())
                .chatId(telegramUserModel.getChatId())
                .locale(locale)
                .build();

    }
}
