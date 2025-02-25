package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.DisableShadowsocksServerScript;
import org.Roclh.sh.scripts.ScreenListScript;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@Slf4j
public class DeleteUserCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final UserService userService;
    private final DisableShadowsocksServerScript disableScript;
    private final ScreenListScript screenListScript;

    public DeleteUserCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, UserService userService, DisableShadowsocksServerScript disableScript, ScreenListScript screenListScript) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.disableScript = disableScript;
        this.screenListScript = screenListScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - not enough arguments").build();
        }
        Long id = Long.valueOf(words[1]);

        long chatId = messageData.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (screenListScript.execute().stream()
                .map(line -> Long.valueOf(line.split(":")[1]))
                .anyMatch(id::equals) && !userService.getUser(id).map(disableScript::execute).orElse(false)) {
            log.error("Failed to delete user with id {}, failed to stop screen", id);
            sendMessage.setText("Failed to delete user with id " + id + ", failed to stop screen");
            return sendMessage;
        }

        if (!userService.deleteUser(id)) {
            log.error("Failed to delete user with identifier {}", id);
            sendMessage.setText("Failed to delete user with identifier " + id);
            return sendMessage;
        }

        log.info("User with identifier {} was deleted successfully!", id);
        sendMessage.setText("User with identifier " + id + " was deleted successfully!");
        return sendMessage;

    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + " {id}\n -- delete user\n -- {id}: user telegram id";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("del", "delete", "rem", "remove");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("user")
                .forCommand("del", i18N.get("callback.user.user.inline.button.delete.user"))
                .withCommandDisplayCondition((telegramId) -> !userService.getAllUsers().isEmpty())
                .with(1, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectUserIdMessage(callbackData,
                                i18N.get("callback.user.user.select.user.delete"),
                                userService,
                                (user) -> true)
                )
                .with(2, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                .build();
    }
}
