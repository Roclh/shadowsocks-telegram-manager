package org.Roclh.handlers.commands.support;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.enums.Plugin;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class PluginGuideCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    public PluginGuideCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if(words.length < 2){
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Саси пиписю").build();
        }
        Plugin plugin = Plugin.valueOf(words[1]);
        return switch (plugin){
            case DEFAULT -> MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.support.pluginguide.how.to.use.plugins.guide.default"))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                    .build();
            case V2RAY -> MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.support.pluginguide.how.to.use.plugins.guide.v2ray"))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                    .build();
        };
    }

    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.USER);
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("pluginh");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("support")
                .forCommand("pluginh", EmojiConstants.ALIEN + " " + i18N.get("command.support.pluginguide.inline.button"))
                .with(1, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(i18N.get("command.support.pluginguide.how.to.use.plugins.guide"))
                                .replyMarkup(CallbackStackUtils.getDefaultSelectPluginMessage(callbackData, () -> InlineUtils.trimLastWord(callbackData.getCallbackData())).getReplyMarkup())
                                .build()
                )
                .with(2, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToPreviousCommand(callbackData))
                                .build()
                )
                .build();
    }
}
