package org.Roclh.handlers.commands.access;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class SetPluginCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final UserService userService;
    private final RestartShadowsocksServerScript restartShadowsocksServerScript;

    public SetPluginCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, UserService userService, RestartShadowsocksServerScript restartShadowsocksServerScript) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.restartShadowsocksServerScript = restartShadowsocksServerScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 3) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("common.validation.not.enough.argument", 3))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                    .build();
        }
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(commandData.getMessageData());
        sendMessage.replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.default.navigation.data.back"), "start"));
        long telegramId;
        UserModel.Plugin plugin;
        try {
            telegramId = Long.parseLong(words[1]);
            plugin = UserModel.Plugin.valueOf(words[2]);
        } catch (NumberFormatException e) {
            log.error("Failed to set plugin - failed to parse telegram id {}", words[1]);
            sendMessage.text(i18N.get("command.access.setplugin.validation.telegram.id", words[1]));
            return sendMessage.build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to set plugin - failed to parse plugin {}", words[2]);
            sendMessage.text(i18N.get("command.access.setplugin.validation.plugin", words[2]));
            return sendMessage.build();
        }
        if (!telegramUserService.isAllowed(commandData.getMessageData().getTelegramId(), Role.MANAGER) &&
                telegramId != commandData.getMessageData().getTelegramId()
        ) {
            log.error("Failed to set plugin - not enough rights");
            sendMessage.text(i18N.get("command.access.setplugin.validation.rights", telegramId));
            return sendMessage.build();
        }
        UserModel userModel = userService.getUser(telegramId).orElse(null);
        if (userModel == null) {
            log.error("Failed to set plugin - user does not exists");
            sendMessage.text(i18N.get("command.access.setplugin.validation.telegram.id.not.exists", telegramId));
            return sendMessage.build();
        }
        if (!userModel.isEnabled()) {
            log.error("Failed to set plugin - user is not enabled");
            sendMessage.text(i18N.get("command.access.setplugin.validation.not.enabled", telegramId));
            return sendMessage.build();
        }
        userModel.setPlugin(plugin);
        if (!restartShadowsocksServerScript.execute(userModel, true)) {
            log.error("Failed to set plugin - failed to execute restart script");
            sendMessage.text("Failed to set plugin - failed to execute restart script");
            return sendMessage.build();
        }
        if (!userService.saveUser(userModel)) {
            log.error("Failed to set plugin - user is not saved");
            sendMessage.text("Failed to set plugin - user is not saved");
            return sendMessage.build();
        }
        sendMessage.text(i18N.get("command.access.setplugin.success", plugin, telegramId));
        return sendMessage.build();
    }

    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.USER) &&
                (telegramUserService.isAllowed(userId, Role.MANAGER) || userService.isAddedUser(userId));
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("setplugin", "splug");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("access")
                .forCommand("splug", i18N.get("command.access.setplugin.inline.button"))
                .withSelectCommandText(i18N.get("command.access.select.command"))
                .with(1, (callbackData) ->
                        telegramUserService.isAllowed(callbackData.getMessageData().getTelegramId(), Role.MANAGER) ?
                                CallbackStackUtils.getDefaultSelectUserIdMessage(
                                        callbackData,
                                        i18N.get("command.access.setplugin.select.user.to.set.plugin"),
                                        userService,
                                        UserModel::isEnabled
                                ) :
                                CallbackStackUtils.getDefaultSelectPluginMessage(
                                        CallbackData.builder()
                                                .callbackData(callbackData.getCallbackData() + " " + callbackData.getMessageData().getTelegramId())
                                                .callbackCommand(callbackData.getCallbackCommand())
                                                .messageData(callbackData.getMessageData())
                                                .build(),
                                        () -> InlineUtils.trimLastWord(callbackData.getCallbackData())
                                )
                )
                .with(2, CallbackStackUtils::getDefaultSelectPluginMessage)
                .with(3, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build()
                )
                .build();
    }
}
