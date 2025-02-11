package org.Roclh.handlers;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.handlers.commands.Command;
import org.Roclh.handlers.commands.common.GetLinkCommand;
import org.Roclh.handlers.commands.common.GuideCommand;
import org.Roclh.handlers.commands.common.HelpCommand;
import org.Roclh.handlers.commands.common.RegisterCommand;
import org.Roclh.handlers.commands.common.SelectLangCommand;
import org.Roclh.handlers.commands.common.StartCommand;
import org.Roclh.handlers.commands.manager.RestartAllUsersCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.commands.manager.ExportCsvCommand;
import org.Roclh.handlers.commands.manager.SendNotificationCommand;
import org.Roclh.handlers.commands.sh.ScreenListCommand;
import org.Roclh.handlers.commands.telegramUser.DeleteTelegramUserCommand;
import org.Roclh.handlers.commands.telegramUser.ListTelegramUserCommand;
import org.Roclh.handlers.commands.telegramUser.SetRoleCommand;
import org.Roclh.handlers.commands.user.AddContractCommand;
import org.Roclh.handlers.commands.user.AddUserCommand;
import org.Roclh.handlers.commands.user.AddUserWithoutPasswordCommand;
import org.Roclh.handlers.commands.user.ChangeUserEnabledCommand;
import org.Roclh.handlers.commands.user.ChangeUserPasswordCommand;
import org.Roclh.handlers.commands.user.DeleteUserCommand;
import org.Roclh.handlers.commands.user.LimitFlowCommand;
import org.Roclh.handlers.commands.user.ListCommand;
import org.Roclh.handlers.messaging.MessageData;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class CommandHandler {

    private static final Map<List<String>, Command<? extends PartialBotApiMethod<?>>> commands = new LinkedHashMap<>();
    private final LocalizationService localizationService;

    public CommandHandler(StartCommand startCommand,
                          HelpCommand helpCommand,
                          RegisterCommand registerCommand,
                          ListTelegramUserCommand listTelegramUserCommand,
                          ListCommand listCommand,
                          AddUserWithoutPasswordCommand addUserWithoutPasswordCommand,
                          AddUserCommand addUserCommand,
                          ChangeUserPasswordCommand changeUserPasswordCommand,
                          ChangeUserEnabledCommand changeUserEnabledCommand,
                          DeleteTelegramUserCommand deleteTelegramUserCommand,
                          ScreenListCommand screenListCommand,
                          DeleteUserCommand deleteUserCommand,
                          LimitFlowCommand limitFlowCommand,
                          AddContractCommand addContractCommand,
                          SetRoleCommand setRoleCommand,
                          SelectLangCommand selectLangCommand,
                          LocalizationService localizationService,
                          GetLinkCommand getLinkCommand,
                          ExportCsvCommand exportCsvCommand,
                          SendNotificationCommand sendNotificationCommand,
                          GuideCommand guideCommand,
                          RestartAllUsersCommand restartAllUsersCommand) {
        this.localizationService = localizationService;
        commands.put(startCommand.getCommandNames(), startCommand);
        commands.put(helpCommand.getCommandNames(), helpCommand);
        commands.put(registerCommand.getCommandNames(), registerCommand);
        commands.put(addUserWithoutPasswordCommand.getCommandNames(), addUserWithoutPasswordCommand);
        commands.put(listTelegramUserCommand.getCommandNames(), listTelegramUserCommand);
        commands.put(addUserCommand.getCommandNames(), addUserCommand);
        commands.put(changeUserPasswordCommand.getCommandNames(), changeUserPasswordCommand);
        commands.put(changeUserEnabledCommand.getCommandNames(), changeUserEnabledCommand);
        commands.put(deleteTelegramUserCommand.getCommandNames(), deleteTelegramUserCommand);
        commands.put(screenListCommand.getCommandNames(), screenListCommand);
        commands.put(deleteUserCommand.getCommandNames(), deleteUserCommand);
        commands.put(limitFlowCommand.getCommandNames(), limitFlowCommand);
        commands.put(listCommand.getCommandNames(), listCommand);
        commands.put(exportCsvCommand.getCommandNames(), exportCsvCommand);
        commands.put(addContractCommand.getCommandNames(), addContractCommand);
        commands.put(getLinkCommand.getCommandNames(), getLinkCommand);
        commands.put(setRoleCommand.getCommandNames(), setRoleCommand);
        commands.put(selectLangCommand.getCommandNames(), selectLangCommand);
        commands.put(sendNotificationCommand.getCommandNames(), sendNotificationCommand);
        commands.put(guideCommand.getCommandNames(), guideCommand);
        commands.put(restartAllUsersCommand.getCommandNames(), restartAllUsersCommand);
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
        Command<? extends PartialBotApiMethod<?>> commandHandler = getCommand(finalCommand);
        if (commandHandler != null && commandHandler.isAllowed(messageData.getTelegramId())) {
            log.info("Recognized command {}, starting handling", command);
            commandHandler.setI18N(messageData.getLocale());
            return commandHandler.handle(commandData);
        } else {
            return new SendMessage(String.valueOf(chatId), "Unknown command");
        }
    }

    public static String getCommandNames(Long telegramId, Locale locale) {
        return commands.values().stream()
                .filter(command -> command.isAllowed(telegramId))
                .map(command -> command.setI18N(locale))
                .map(Command::getHelp)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));
    }

    public static List<Command> getCommands(Update update) {
        return commands.values().stream()
                .filter(command -> command.isAllowed(update.getMessage().getFrom().getId()))
                .collect(Collectors.toList());
    }

    @Nullable
    private Command<? extends PartialBotApiMethod<?>> getCommand(String key) {
        return commands.keySet().stream()
                .filter(keys -> keys.contains(key.toLowerCase()) || keys.contains(key.toLowerCase().replace(" ", "_")))
                .findFirst()
                .map(commands::get)
                .orElse(null);
    }
}
