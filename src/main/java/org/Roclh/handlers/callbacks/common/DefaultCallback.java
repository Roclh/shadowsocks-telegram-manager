package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class DefaultCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {

    private final CommandHandler commandHandler;
    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        SendMessage sendMessage = (SendMessage) commandHandler.handleCommands(CommandData.from(callbackData, false));
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(sendMessage.getText())
                .replyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup())
                .build();
    }

    @Override
    public String getName() {
        return "default";
    }
}
