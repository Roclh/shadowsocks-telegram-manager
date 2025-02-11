package org.Roclh.handlers.commands.manager;


import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.callbacks.user.UserCallback;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestartAllUsersCommand extends AbstractCommand<PartialBotApiMethod<? extends Serializable>> {

    private final UserService userService;
    private final RestartShadowsocksServerScript restartShadowsocksServerScript;

    public RestartAllUsersCommand(TelegramUserService telegramUserService,
                                  UserService userService,
                                  RestartShadowsocksServerScript restartShadowsocksServerScript) {
        super(telegramUserService);
        this.userService = userService;
        this.restartShadowsocksServerScript = restartShadowsocksServerScript;
    }

    @Override
    public PartialBotApiMethod<? extends Serializable> handle(CommandData commandData) {
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
        return userService.getAllUsers().stream()
                .filter(UserModel::isAdded)
                .map((user) -> user.getUserModel().getTelegramName() +
                        (restartShadowsocksServerScript.execute(user) ? " successful restarted" : " failed restart"))
                .collect(Collectors.toList());
    }
}