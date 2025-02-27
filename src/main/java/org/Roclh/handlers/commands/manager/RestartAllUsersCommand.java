package org.Roclh.handlers.commands.manager;


import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
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
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestartAllUsersCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {

    private final UserService userService;
    private final RestartShadowsocksServerScript restartShadowsocksServerScript;

    public RestartAllUsersCommand(TelegramUserService telegramUserService,
                                  CommandRegistry commandRegistry,
                                  UserService userService,
                                  RestartShadowsocksServerScript restartShadowsocksServerScript) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.restartShadowsocksServerScript = restartShadowsocksServerScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        log.info("restart all users command was requested");
        MessageData messageData = commandData.getMessageData();
        return MessageUtils.sendMessage(messageData)
                .text(restartAllUsers().toString()).build();
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("restartall");
    }

    public List<String> restartAllUsers() {
        return userService.getActiveUsers()
                .stream()
                .map((user) -> user.getUserModel().getTelegramName() +
                        (restartShadowsocksServerScript.execute(user, false) ? " successful restarted" : " failed restart"))
                .collect(Collectors.toList());
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("manager")
                .forCommand("restartall", i18N.get("callback.manager.inline.button.restart.all.users"))
                .with(1, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                .build();
    }
}