package org.Roclh.handlers.commands.telegramUser;

import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class DeleteTelegramUserCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {

    private final TelegramBotProperties telegramBotProperties;

    public DeleteTelegramUserCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, TelegramBotProperties telegramBotProperties) {
        super(telegramUserService, commandRegistry);
        this.telegramBotProperties = telegramBotProperties;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return SendMessage.builder().chatId(commandData.getMessageData().getChatId()).text("Failed to execute command - not enough arguments").build();
        }
        Long id = Long.valueOf(words[1]);
        if (telegramBotProperties.getDefaultManagerId().equals(id)) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text("Can't delete default manager")
                    .build();
        }
        if (telegramUserService.deleteUser(id)) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Telegram user with identifier " + id + " was deleted successfully!").build();
        } else {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to delete telegram user with identifier " + id).build();
        }
    }

    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + " {id}\n -- delete telegram user\n -- {id}: user telegram id";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("deltg", "deletetg", "removetg", "remtg");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("tguser")
                .forCommand("deltg", i18N.get("callback.user.telegramuser.inline.button.delete.telegram.user"))
                .with(1, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectTelegramUserIdMessage(callbackData,
                                i18N.get("callback.user.telegramuser.select.telegram.user.delete"),
                                telegramUserService,
                                (user) -> !user.getTelegramId().equals(telegramBotProperties.getDefaultManagerId())))
                .with(2, (callbackData) ->
                    MessageUtils.editMessage(callbackData.getMessageData())
                            .text(handle(CommandData.from(callbackData)).getText())
                            .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                            .build()
                )
                .build();
    }
}
