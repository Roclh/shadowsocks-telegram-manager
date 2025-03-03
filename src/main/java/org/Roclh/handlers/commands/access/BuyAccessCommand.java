package org.Roclh.handlers.commands.access;

import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class BuyAccessCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {

    public BuyAccessCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    // buy bandwidth datetime 
    @Override
    public SendMessage handle(CommandData commandData) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("buy");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return null;
    }
}
