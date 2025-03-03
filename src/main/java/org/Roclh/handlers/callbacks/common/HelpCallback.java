package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@Deprecated
@RequiredArgsConstructor
public class HelpCallback extends AbstractCallback<EditMessageText> {
    private final TelegramUserService telegramUserService;
    private final CommandHandler commandHandler;

    @Override
    public EditMessageText apply(CallbackData callbackData) {
        SendMessage sendMessage = (SendMessage) commandHandler.handleCommands(CommandData.from(callbackData));
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(sendMessage.getText())
                .replyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup())
                .build();
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(EmojiConstants.QUESTIONMARK + i18N.get("callback.common.help.inline.button"))
                .callbackData(getName())
                .build());
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return telegramUserService.isAllowed(telegramId, Role.USER);
    }

    @Override
    public String getName() {
        return "help";
    }
}
