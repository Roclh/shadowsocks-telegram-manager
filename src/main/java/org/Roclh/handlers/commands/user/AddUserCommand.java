package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.ss.ShadowsocksProperties;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class AddUserCommand extends AbstractCommand<SendMessage> {
    private final UserService userManager;
    private final TelegramUserService telegramUserService;
    private final TelegramBotStorage telegramBotStorage;
    private final ShadowsocksProperties shadowsocksProperties;
    private final EnableDefaultShadowsocksServerScript enableScript;

    public AddUserCommand(TelegramUserService telegramUserService, UserService userManager, TelegramUserService telegramUserService1, TelegramBotStorage telegramBotStorage, ShadowsocksProperties shadowsocksProperties, EnableDefaultShadowsocksServerScript enableScript) {
        super(telegramUserService);
        this.userManager = userManager;
        this.telegramUserService = telegramUserService1;
        this.telegramBotStorage = telegramBotStorage;
        this.shadowsocksProperties = shadowsocksProperties;
        this.enableScript = enableScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 4) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - not enough arguments").build();
        }
        if (words.length > 4) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - password should not contain spaces").build();
        }
        long chatId = messageData.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        Long telegramId = Long.valueOf(words[1]);
        Long port = Long.valueOf(words[2]);
        String password = words[3];

        if (shadowsocksProperties.getPortRange().range().stream().filter(userManager::isPortInUse).toList().contains(port)) {
            log.error("Failed to add user - port {} already in use!", port);
            sendMessage.setText("Failed to add user - port " + port + " already in use!");
            return sendMessage;
        }

        TelegramUserModel telegramUserModel = telegramUserService.getUser(telegramId)
                .orElse(null);
        if (telegramUserModel == null) {
            log.error("Failed to add user - Telegram user with id {} does not exists!", telegramId);
            sendMessage.setText("Failed to add user - Telegram user with id " + telegramId + " does not exists!");
            return sendMessage;
        }
        UserModel userModel = UserModel.builder()
                .userModel(telegramUserModel)
                .usedPort(port)
                .password(password)
                .isAdded(true)
                .plugin(UserModel.Plugin.DEFAULT)
                .build();
        if (!enableScript.execute(userModel)) {
            log.error("Failed to add user - failed to execute sh script for user with id {}", telegramId);
            sendMessage.setText("Failed to add user - failed to execute sh script for user with id" + telegramId);
            return sendMessage;
        }
        if (!userManager.saveUser(userModel)) {
            log.error("Failed to add user - failed to save user model with id {}", telegramId);
            sendMessage.setText("Failed to add user - failed to save user model with id " + telegramId);
            return sendMessage;
        }
        if (userModel.getUserModel().getChatId() != null) {
            telegramBotStorage.getTelegramBot().sendMessage(
                    MessageUtils.sendMessage(commandData.getMessageData())
                            .text(i18N.get("command.common.adduserwithoutpassword.granted.access"))
                            .replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.common.getqr.inline.button"), "qr"))
                            .build());
        }
        sendMessage.setText("User with id " + telegramId + " was added successfully!");
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

}
