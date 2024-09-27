package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.BandwidthService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LimitFlowCommand extends AbstractCommand<SendMessage> {
    private final UserService userService;
    private final BandwidthService bandwidthService;

    public LimitFlowCommand(TelegramUserService telegramUserService, UserService userService, BandwidthService bandwidthService) {
        super(telegramUserService);
        this.userService = userService;
        this.bandwidthService = bandwidthService;
    }

    @Override
    public SendMessage handle(Update update) {
        String[] words = update.getMessage().getText().split(" ");
        if (words.length < 3) {
            return SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to execute command - not enough arguments").build();
        }
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        Long telegramId = Long.valueOf(words[1]);
        BandwidthModel.Bandwidth bandwidth;
        try {
            bandwidth = BandwidthModel.Bandwidth.valueOf(words[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Failed to set bandwidth rule - bandwidth type does not exists {}", words[2], e);
            sendMessage.setText("Failed to set bandwidth rule - bandwidth type does not exists " + words[2]);
            return sendMessage;
        }
        Optional<UserModel> userModel = userService.getUser(telegramId);
        if (userModel.isEmpty()) {
            log.error("Failed to set bandwidth rule - user with id {} does not exists", telegramId);
            sendMessage.setText("Failed to set bandwidth rule - user with id " + telegramId + " does not exists");
            return sendMessage;
        }
        BandwidthModel bandwidthModel = BandwidthModel.builder()
                .userModel(userModel.get())
                .bandwidth(bandwidth)
                .build();
        if (!bandwidthService.executeShScriptSetBandwidthRule(bandwidthModel)){
            log.error("Failed to set a bandwidth rule - failed to execute sh script for id {}", telegramId);
            sendMessage.setText("Failed to set a bandwidth rule - failed to add or create a rule for id " + telegramId);
            return sendMessage;
        }
        if (!bandwidthService.setRule(bandwidthModel)){
            log.error("Failed to set a bandwidth rule - failed to add or create a rule for id {}", telegramId);
            sendMessage.setText("Failed to set a bandwidth rule - failed to add or create a rule for id " + telegramId);
            return sendMessage;
        }
        log.info("Successfully added a bandwidth rule for id {}", telegramId);
        sendMessage.setText("Successfully added a bandwidth rule for id " + telegramId);
        return sendMessage;
    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + " {id} {bandwidth}\n -- delete user\n -- {id}: user telegram id" +
                "\n -- {bandwidth}: bandwidth, one of " + Arrays.toString(BandwidthModel.Bandwidth.values());
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("limitflow", "lflow", "flow", "fl");
    }
}
