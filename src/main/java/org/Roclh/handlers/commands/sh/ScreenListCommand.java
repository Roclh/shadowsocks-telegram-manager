package org.Roclh.handlers.commands.sh;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.ScreenListScript;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScreenListCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final ScreenListScript screenListScript;

    public ScreenListCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, ScreenListScript screenListScript) {
        super(telegramUserService, commandRegistry);
        this.screenListScript = screenListScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String message = screenListScript.execute()
                .stream().map(line -> telegramUserService.getUser(Long.valueOf(line.split(":")[1])).orElse(null))
                .map((userModel) -> {
                    if (userModel == null) {
                        return "Unknown screen\n";
                    }
                    return userModel.getTelegramId() + ":" + userModel.getTelegramName();
                })
                .collect(Collectors.joining("\n"));
        if(message.isEmpty()){
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text("There is no active screens now")
                    .build();
        }
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text(message)
                .build();
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("screen", "screenlist", "screenls", "lsscreen");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("manager")
                .forCommand("screen", EmojiConstants.CLIPBOARD + i18N.get("callback.manager.inline.button.screen.list"))
                .withLocalizedCallbackKey(EmojiConstants.WRENCH + i18N.get("callback.manager.inline.button"))
                .with(1, (callbackData) -> MessageUtils.editMessage(callbackData.getMessageData())
                        .text(handle(CommandData.from(callbackData)).getText())
                        .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                        .build())
                .build();
    }
}
