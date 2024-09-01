package org.Roclh.handlers.commands.user;

import org.Roclh.data.model.user.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.utils.PropertiesContainer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class ChangeUserPasswordCommand extends AbstractCommand {
    private final UserService userManager;

    public ChangeUserPasswordCommand(PropertiesContainer propertiesContainer, UserService userManager) {
        super(propertiesContainer);
        this.userManager = userManager;
    }

    @Override
    public SendMessage handle(Update update) {
        String[] words = update.getMessage().getText().split(" ");
        if (words.length < 3) {
            return SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to execute command - not enough arguments").build();
        }

        String telegramId = words[1];
        String password = words[2];

        boolean isChanged = userManager.changePassword(telegramId, password);
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (isChanged) {
            sendMessage.setText("Password for user with id " + telegramId + " was changed successfully!");
        } else {
            sendMessage.setText("Password for user with id " + telegramId + " was not changed! Either it is the same or failed to change");
        }
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
}
