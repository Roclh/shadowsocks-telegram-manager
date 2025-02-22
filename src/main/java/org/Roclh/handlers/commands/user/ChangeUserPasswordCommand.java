package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBot;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.PasswordUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class ChangeUserPasswordCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final UserService userService;
    private final RestartShadowsocksServerScript restartScript;

    public ChangeUserPasswordCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, UserService userService, RestartShadowsocksServerScript restartScript) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.restartScript = restartScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 3) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - not enough arguments").build();
        }
        long chatId = messageData.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        Long telegramId = Long.valueOf(words[1]);
        String password = words[2];

        if (userService.getUser(telegramId).map(restartScript::execute).orElse(false)) {
            log.error("Failed to change password - failed to execute sh script for user with id {}", telegramId);
            sendMessage.setText("Failed to change password - failed to execute sh script for user with id " + telegramId);
            return sendMessage;
        }
        if (!userService.changePassword(telegramId, password)) {
            log.error("Failed to change password - failed to change password for user with id {}", telegramId);
            sendMessage.setText("Failed to change password - failed to change password for user with id " + telegramId);
            return sendMessage;
        }
        sendMessage.setText("Successfully changed password for user with id " + telegramId);
        return sendMessage;
    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + " {telegramId} {password}\n -- change user password";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("changepassword", "chgpwd", "cpwd", "chpwd", "pwd");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("user")
                .forCommand("chpwd", i18N.get("callback.user.chpwd.inline.button.change.password"))
                .with(1, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectTelegramUserIdMessage(
                                callbackData,
                                i18N.get("callback.user.chpwd.select.user.to.change.password"),
                                telegramUserService,
                                (telegramUserModel) -> true
                        ))
                .with(2, (callbackData) ->
                        CallbackStackUtils.getDefaultWaitForPasswordInput(callbackData,
                                this::handle)
                )
                .build();
    }
}
