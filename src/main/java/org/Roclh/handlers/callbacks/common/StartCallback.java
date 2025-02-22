package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@Deprecated
@RequiredArgsConstructor
public class StartCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {
    private final CommandHandler commandHandler;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        String[] command = callbackData.getCallbackData().split(" ");
        SendMessage commandResult = (SendMessage) commandHandler.handleCommands(CommandData.from(callbackData, false));
        if (command.length == 1) {
            return MessageUtils.editMessage(callbackData.getMessageData())
                    .text(commandResult.getText())
                    .replyMarkup((InlineKeyboardMarkup) commandResult.getReplyMarkup())
                    .build();
        } else {
            return commandResult;
        }
    }

    @Override
    public String getName() {
        return "start";
    }
}
