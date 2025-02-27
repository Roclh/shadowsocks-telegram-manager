package org.Roclh.handlers.commands.support;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.OSType;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.I18N;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GuideCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    public GuideCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if(words.length < 2){
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("common.validation.not.enough.argument", 2))
                    .build();
        }
        OSType guideType;
        try {
            guideType = OSType.valueOf(words[1]);
        }catch (IllegalArgumentException e){
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text("Failed to execute command - unkown guide type " + words[1])
                    .build();
        }
        return switch (guideType){
            case PC -> MessageUtils.sendMessage(commandData.getMessageData())
                        .text(i18N.get("command.common.guide.pc"))
                        .build();
            case IOS -> MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.common.guide.ios"))
                    .build();
            case ANDROID -> MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.common.guide.android"))
                    .build();
        };
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("guide");
    }

    @Override
    public boolean isAllowed(Long userId) {
        return true;
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("support")
                .forCommand("guide", i18N.get("callback.common.guide.inline.button"))
                .with(1, (callbackData) ->
                    MessageUtils.editMessage(callbackData.getMessageData())
                            .text(i18N.get("callback.common.guide.select.paltform"))
                            .replyMarkup(InlineUtils.getListNavigationMarkup(Arrays.stream(OSType.values())
                                            .collect(Collectors.toMap(c -> c.localize(I18N.from(callbackData.getMessageData())), OSType::name)),
                                    data -> callbackData.getCallbackData() + " " + data,
                                    callbackData.getMessageData().getLocale()))
                            .build()
                )
                .with(2, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                .build();
    }

}
