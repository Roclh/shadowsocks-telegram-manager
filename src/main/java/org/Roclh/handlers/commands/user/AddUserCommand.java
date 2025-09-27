package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.enums.Plugin;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.ss.ShadowsocksProperties;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.PasswordUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.Roclh.utils.i18n.I18N;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class AddUserCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final UserService userService;
    private final TelegramUserService telegramUserService;
    private final TelegramBotStorage telegramBotStorage;
    private final LocalizationService localizationService;
    private final ShadowsocksProperties shadowsocksProperties;
    private final EnableDefaultShadowsocksServerScript enableScript;

    public AddUserCommand(TelegramUserService telegramUserService,
                          CommandRegistry commandRegistry,
                          UserService userService,
                          TelegramBotStorage telegramBotStorage, LocalizationService localizationService,
                          ShadowsocksProperties shadowsocksProperties,
                          EnableDefaultShadowsocksServerScript enableScript) {
        super(telegramUserService, commandRegistry);
        this.telegramUserService = telegramUserService;
        this.userService = userService;
        this.telegramBotStorage = telegramBotStorage;
        this.localizationService = localizationService;
        this.shadowsocksProperties = shadowsocksProperties;
        this.enableScript = enableScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 4) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text(i18N.get("common.validation.not.enough.argument", 4)).build();
        }
        if (words.length > 4) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text(i18N.get("command.user.add.validation.password")).build();
        }
        long chatId = messageData.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        Long telegramId;
        try {
            telegramId = Long.valueOf(words[1]);
        } catch (NumberFormatException e) {
            log.error("Failed to add user - failed to parse telegram id {}", words[1]);
            sendMessage.setText(i18N.get("command.user.add.validation.parse.telegram.id", words[1]));
            return sendMessage;
        }
        Long port;
        try {
            port = Long.valueOf(words[2]);
        } catch (NumberFormatException e) {
            log.error("Failed to add user - failed to parse port {}", words[2]);
            sendMessage.setText(i18N.get("command.user.add.validation.parse.port", words[2]));
            return sendMessage;
        }
        String password = words[3];
        if (!PasswordUtils.validate(password)) {
            log.error("Failed to add user - failed to validate password {}", password);
            sendMessage.setText(i18N.get("command.user.add.validation.validate.password", password));
            return sendMessage;
        }
        if (!shadowsocksProperties.getPortRange().range().stream().filter(availablePort -> !userService.isPortInUse(availablePort)).toList().contains(port)) {
            log.error("Failed to add user - port {} already in use!", port);
            sendMessage.setText(i18N.get("command.user.add.validation.port.already.in.use", port));
            return sendMessage;
        }

        TelegramUserModel telegramUserModel = telegramUserService.getUser(telegramId)
                .orElse(null);
        if (telegramUserModel == null) {
            log.error("Failed to add user - Telegram user with id {} does not exists!", telegramId);
            sendMessage.setText(i18N.get("command.user.add.validation.user.dont.exist", telegramId));
            return sendMessage;
        }
        UserModel userModel = UserModel.builder()
                .userModel(telegramUserModel)
                .usedPort(port)
                .password(password)
                .isEnabled(true)
                .plugin(Plugin.DEFAULT)
                .build();
        if (!enableScript.execute(userModel)) {
            log.error("Failed to add user - failed to execute sh script for user with id {}", telegramId);
            sendMessage.setText(i18N.get("command.user.add.validation.failed.execute.script", telegramId));
            return sendMessage;
        }
        if (!userService.saveUser(userModel)) {
            log.error("Failed to add user - failed to save user model with id {}", telegramId);
            sendMessage.setText(i18N.get("command.user.add.validation.failed.to.save.user", telegramId));
            return sendMessage;
        }
        if (userModel.getUserModel().getChatId() != null) {
            MessageData userMessageData = MessageData.builder()
                    .locale(localizationService.getOrCreate(userModel.getUserModel().getTelegramId()))
                    .chatId(userModel.getUserModel().getChatId())
                    .telegramName(userModel.getUserModel().getTelegramName())
                    .telegramId(userModel.getUserModel().getTelegramId())
                    .build();
            telegramBotStorage.getTelegramBot().sendMessage(
                    MessageUtils.sendMessage(userMessageData)
                            .text(I18N.from(userMessageData).get("command.common.adduserwithoutpassword.granted.access"))
                            .replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.common.getqr.inline.button"), "access qr"))
                            .build());
        }
        sendMessage.setText(i18N.get("command.user.add.success", telegramId));
        return sendMessage;
    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + " {telegramId} {port} {password}\n -- add user with defined password";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("adduser", "add", "ad", "addpwd");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("user")
                .forCommand("add", i18N.get("callback.user.user.inline.button.add.with.defined.password"))
                .withCommandDisplayCondition((telegramId) -> !telegramUserService.getUsers(user -> !userService.isAddedUser(user))
                        .toList()
                        .isEmpty())
                .with(1, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectTelegramUserIdMessage(
                                callbackData,
                                i18N.get("callback.user.user.select.user.add"),
                                telegramUserService,
                                (telegramUserModel) -> !userService.isAddedUser(telegramUserModel)
                        ))
                .with(2, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectPortMessage(callbackData, i18N.get("callback.user.user.select.port"), userService))
                .with(3, (callbackData) ->
                        CallbackStackUtils.getDefaultWaitForPasswordInput(callbackData,
                                getCallbackStack())
                )
                .with(4, (callbackData) -> handle(CommandData.from(callbackData)))
                .build();
    }
}
