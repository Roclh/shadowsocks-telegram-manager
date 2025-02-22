package org.Roclh.handlers.callbacks.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.commands.common.GuideCommand;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.I18N;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Deprecated
@RequiredArgsConstructor
public class GuideCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {

    private final CommandHandler commandHandler;
    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        int len = callbackData.getCallbackData().split(" ").length;
        return switch (len) {
            case 1 -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.common.guide.select.paltform"))
                    .replyMarkup(getSelectGuideTypeMarkup(callbackData))
                    .build();
            case 2 -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(((SendMessage)commandHandler.handleCommands(CommandData.from(callbackData))).getText())
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.default.navigation.data.error"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    private InlineKeyboardMarkup getSelectGuideTypeMarkup(CallbackData callbackData) {
        return InlineUtils.getListNavigationMarkup(Arrays.stream(GuideCommand.Type.values())
                        .collect(Collectors.toMap(c -> c.localize(I18N.from(callbackData.getMessageData())), GuideCommand.Type::name)),
                data -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale());
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return true;
    }

    @Override
    public String getName() {
        return "guide";
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(i18N.get("callback.common.guide.inline.button"))
                .callbackData(getName())
                .build());
    }
}
