package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.BandwidthService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.DeleteBandwidthRuleScript;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class DeleteFlowLimitCommand extends AbstractCommand<SendMessage> {
    private final BandwidthService bandwidthService;
    private final UserService userService;
    private final DeleteBandwidthRuleScript deleteBandwidthRuleScript;
    public DeleteFlowLimitCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, BandwidthService bandwidthService, UserService userService, DeleteBandwidthRuleScript deleteBandwidthRuleScript) {
        super(telegramUserService, commandRegistry);
        this.bandwidthService = bandwidthService;
        this.userService = userService;
        this.deleteBandwidthRuleScript = deleteBandwidthRuleScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - not enough arguments").build();
        }
        Long telegramId = Long.parseLong(words[1]);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(messageData.getChatId());

        UserModel userModel = userService.getUser(telegramId).orElse(null);
        if(userModel == null){
            log.error("Failed to delete flow limit - user with id {} does not exists", telegramId);
            sendMessage.setText("Failed to delete flow limit - user with id " + telegramId + " does not exists");
            return sendMessage;
        }
        BandwidthModel bandwidthModel = bandwidthService.getRule(telegramId).orElse(null);
        if(bandwidthModel == null){
            log.error("Failed to delete flow limit - bandwidth for user with id {} does not exists", telegramId);
            sendMessage.setText("Failed to delete flow limit - bandwidth for user with id " + telegramId + " does not exists");
            return sendMessage;
        }
        if(!deleteBandwidthRuleScript.execute(bandwidthModel)){
            log.error("Failed to delete flow limit - failed to execute sh script for user with id {}", telegramId);
            sendMessage.setText("Failed to delete flow limit - failed to execute sh script for user with id " + telegramId);
            return sendMessage;
        }
        if(!bandwidthService.deleteRule(bandwidthModel)){
            log.error("Failed to delete flow limit - either it not exists or failed to delete with id {}", telegramId);
            sendMessage.setText("Failed to delete flow limit - either it not exists or failed to delete with id " + telegramId);
            return sendMessage;
        }
        log.info("Successfully deleted flow limit for user with id {}", telegramId);
        sendMessage.setText("Successfully deleted flow limit for user with id " + telegramId);
        return sendMessage;
    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 1))+ " {telegramId} \n -- delete flow for user with id {telegramId}";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("delflow", "flowdel", "flowd", "dflow");
    }
}
