package org.Roclh.handlers.messaging;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Locale;

@Data
@Builder
public class CommandData {
    @NonNull
    private String command;
    @NonNull
    private MessageData messageData;

    @NonNull
    public static CommandData from(@NonNull Message message, Locale locale) {
        return builder()
                .command(message.getText())
                .messageData(MessageData.builder()
                        .telegramName(message.getFrom().getUserName())
                        .telegramId(message.getFrom().getId())
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId())
                        .locale(locale)
                        .build())
                .build();
    }

    /**
     * Converts callback data to command data, populating command data "command" field with "callbackData"
     * @param callbackData callback data to convert from
     * @return command data with populated message data and callback data as a command
     */
    @NonNull
    public static CommandData from(@NonNull CallbackData callbackData){
        return from(callbackData, true);
    }

    @NonNull
    public static CommandData from(@NonNull CallbackData callbackData, boolean trim){
        return CommandData.builder()
                .command(trim ?
                        callbackData.getCallbackData().substring(callbackData.getCallbackData().indexOf(" ") + 1) :
                        callbackData.getCallbackData()
                )
                .messageData(callbackData.getMessageData())
                .build();
    }
}
