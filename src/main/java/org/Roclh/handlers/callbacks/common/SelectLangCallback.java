package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Deprecated
@RequiredArgsConstructor
public class SelectLangCallback extends AbstractCallback<EditMessageText> {
    private final TelegramBotProperties botProperties;
    private final CommandHandler commandHandler;

    @Override
    public EditMessageText apply(CallbackData callbackData) {
        int commandLength = callbackData.getCallbackData().split(" ").length;
        return switch (commandLength) {
            case 1 -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.common.selectlang.select.lang.message"))
                    .replyMarkup(getSelectLangMarkup(callbackData, botProperties.getSupportedLocales()))
                    .build();
            case 2 -> handleOneArgumentCommand(callbackData);
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.default.navigation.data.error"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    @Override
    public String getName() {
        return "lang";
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(EmojiConstants.GLOBE + i18N.get("callback.common.selectlang.select.button.inline"))
                .callbackData(getName())
                .build()
        );
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return true;
    }

    private EditMessageText handleOneArgumentCommand(CallbackData callbackData) {
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(((SendMessage) commandHandler.handleCommands(CommandData.from(callbackData))).getText())
                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                .build();
    }

}
