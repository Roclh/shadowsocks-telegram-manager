package org.Roclh.handlers.commands.access;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
public class SetPluginCommand extends AbstractCommand<PartialBotApiMethod<? extends Serializable>> {
    public SetPluginCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public PartialBotApiMethod<? extends Serializable> handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("common.validation.not.enough.argument", 2))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                    .build();
        }
        return null;
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("setplugin", "splug");
    }
}
