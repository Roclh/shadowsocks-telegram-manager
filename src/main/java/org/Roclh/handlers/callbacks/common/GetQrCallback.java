package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetQrCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {
    private final TelegramUserService telegramUserService;
    private final UserService userService;
    private final CommandHandler commandHandler;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        PartialBotApiMethod<? extends Serializable> result = commandHandler.handleCommands(CommandData.from(callbackData));
        if(result instanceof SendMessage){
            return MessageUtils.editMessage(callbackData.getMessageData())
                    .text(((SendMessage) result).getText())
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        }
        if(result instanceof SendPhoto){
            callbackData.setCallbackData("start nl");
            ((SendPhoto) result).setReplyMarkup(InlineUtils.getDefaultNavigationMarkup(callbackData));
            return result;
        }
        throw new RuntimeException("Impossible state");
    }

    @Override
    public String getName() {
        return "qr";
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(i18N.get("callback.common.getqr.inline.button"))
                .callbackData(getName())
                .build());
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return telegramUserService.isAllowed(telegramId, Role.USER) && userService.getUser(telegramId).map(UserModel::isAdded).orElse(false);
    }
}
