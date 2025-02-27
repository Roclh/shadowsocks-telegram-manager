package org.Roclh.handlers.commands.support;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;

@Slf4j
@Component
public class RemoveReplyKeyboardCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final TelegramBotStorage telegramBotStorage;

    public RemoveReplyKeyboardCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, TelegramBotStorage telegramBotStorage) {
        super(telegramUserService, commandRegistry);
        this.telegramBotStorage = telegramBotStorage;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        telegramBotStorage.getTelegramBot().sendMessage(MessageUtils.sendMessage(commandData.getMessageData())
                .text(i18N.get("command.support.removekeyboard.was.removed"))
                .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                .build());
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text(i18N.get("command.support.removekeyboard.back.to.bot"))
                .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                .build();
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("removekb");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("support")
                .forCommand("removekb", i18N.get("command.support.removekeyboard.inline.button"))
                .with(1, (callbackData) -> handle(CommandData.from(callbackData)))
                .build();
    }
}
