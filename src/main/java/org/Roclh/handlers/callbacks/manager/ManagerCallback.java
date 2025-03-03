package org.Roclh.handlers.callbacks.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@Deprecated
@RequiredArgsConstructor
public class ManagerCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {
    private final TelegramUserService telegramUserService;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        return
                MessageUtils.editMessage(callbackData.getMessageData()).text(i18N.get("callback.default.navigation.data.error"))
                        .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                        .build();

    }

    @Override
    public String getName() {
        return "manager";
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(EmojiConstants.WRENCH + i18N.get("callback.manager.inline.button"))
                .callbackData(getName())
                .build());
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return telegramUserService.isAllowed(telegramId, Role.MANAGER);
    }
}
