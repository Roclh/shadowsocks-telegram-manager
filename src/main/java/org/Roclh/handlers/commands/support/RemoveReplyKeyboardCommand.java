package org.Roclh.handlers.commands.support;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;

@Slf4j
@Component
public class RemoveReplyKeyboardCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    public RemoveReplyKeyboardCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text("Клавиатура убрана!")
                .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                .build();
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("removekb");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("support")
                .forCommand("removekb", "Убрать клавиатуру")
                .with(1, (callbackData) -> handle(CommandData.from(callbackData)))
                .build();
    }
}
