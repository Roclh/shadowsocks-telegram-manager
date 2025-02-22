package org.Roclh.handlers;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.handlers.commands.Command;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class CommandHandler {

    private final CommandRegistry commandRegistry;
    private final LocalizationService localizationService;

    public CommandHandler(
            CommandRegistry commandRegistry,
            LocalizationService localizationService) {
        this.localizationService = localizationService;
        this.commandRegistry = commandRegistry;
    }

    public PartialBotApiMethod<? extends Serializable> handleCommands(Update update) {
        return handleCommands(CommandData.from(update.getMessage(), localizationService.getOrCreate(update.getMessage().getFrom().getId())));
    }

    public PartialBotApiMethod<? extends Serializable> handleCommands(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String messageText = commandData.getCommand();
        String command = messageText.split(" ")[0];
        long chatId = messageData.getChatId();
        log.info("Received a message from user {} from a chat with id:\"{}\", containing message \"{}\"", messageData.getTelegramName(), chatId, messageText);

        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String finalCommand = command;
        Command<? extends PartialBotApiMethod<?>> commandHandler = getCommand(finalCommand, commandData);
        if (commandHandler != null) {
            log.info("Recognized command {}, starting handling", command);
            return commandHandler.handle(commandData);
        } else {
            return new SendMessage(String.valueOf(chatId), "Unknown command");
        }
    }

    @Nullable
    private Command<? extends PartialBotApiMethod<?>> getCommand(String key, CommandData commandData) {
        return commandRegistry.getRegisteredCommands(commandData.getMessageData().getTelegramId(), commandData.getMessageData().getLocale())
                .stream()
                .filter(command -> command.getCommandNames().contains(key))
                .findFirst()
                .orElse(null);
    }
}
