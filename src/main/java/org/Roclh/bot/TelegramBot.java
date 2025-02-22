package org.Roclh.bot;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.handlers.CallbackHandler;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.messaging.CommandData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private static final Map<Long, Function<CommandData, PartialBotApiMethod<? extends Serializable>>> waitingForInput = new HashMap<>();
    private final TelegramBotProperties telegramBotProperties;
    private final LocalizationService localizationService;
    private final CallbackHandler callbackHandler;
    private final CommandHandler commandsHandler;

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        log.info("Received updates amount: {}", updates.size());
        Update update = updates.get(0);
        update.setMessage(mergeMessages(updates.stream().map(Update::getMessage).toList()));
        update.setEditedMessage(mergeMessages(updates.stream().map(Update::getEditedMessage).toList()));
        onUpdateReceived(update);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long telegramId = update.getMessage().getFrom().getId();
            log.info("Received message {}", ("[id:\"" + telegramId + "\"];" +
                    "[username:\"" + update.getMessage().getFrom().getUserName() + "\"];" +
                    "[text:\"" + update.getMessage().getText() + "\"]"));
            if (waitingForInput.containsKey(telegramId)) {
                Function<CommandData, PartialBotApiMethod<?>> asyncFunction = waitingForInput.get(telegramId);
                waitingForInput.remove(telegramId);
                sendMessage(asyncFunction.apply(CommandData.from(update.getMessage(), localizationService.getOrCreate(telegramId))));
                return;
            }
            if (update.getMessage().hasText()) {
                sendMessage(commandsHandler.handleCommands(update));
            }
        } else if (update.hasCallbackQuery()) {
            Long telegramId = update.getCallbackQuery().getFrom().getId();
            if(waitingForInput.containsKey(telegramId)){
                cancelSyncUpdate(telegramId);
            }
            sendMessage(callbackHandler.handleCallbacks(update));
        }
    }

    public static void waitSyncUpdate(@NonNull Long telegramId, @NonNull Function<CommandData, PartialBotApiMethod<? extends Serializable>> onUpdate) {
        waitingForInput.put(telegramId, onUpdate);
    }

    public static void cancelSyncUpdate(@NonNull Long telegramId){
        waitingForInput.remove(telegramId);
    }

    public <T extends Serializable> void sendMessage(PartialBotApiMethod<T> sendMessage) {
        log.info("Trying to send message to telegram client {}", sendMessage);
        if (sendMessage == null) {
            return;
        }
        try {
            if (sendMessage instanceof BotApiMethod<T>) {
                executeAsync((BotApiMethod<T>) sendMessage);
            } else if (sendMessage instanceof SendMediaGroup) {
                executeAsync((SendMediaGroup) sendMessage);
            } else if (sendMessage instanceof SendPhoto) {
                executeAsync((SendPhoto) sendMessage);
            } else if (sendMessage instanceof SendDocument) {
                executeAsync((SendDocument) sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.getName();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getBotToken() {
        return telegramBotProperties.getToken();
    }

    private org.telegram.telegrambots.meta.api.objects.Message mergeMessages(List<org.telegram.telegrambots.meta.api.objects.Message> messages) {
        if (messages.stream().filter(Objects::nonNull).findAny().isEmpty()) {
            return null;
        }
        org.telegram.telegrambots.meta.api.objects.Message newMessage = messages.get(0);
        newMessage.setText(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getText).filter(Objects::nonNull).collect(Collectors.joining("\n")));
        newMessage.setCaption(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getCaption).filter(Objects::nonNull).collect(Collectors.joining("\n")));
        newMessage.setCaptionEntities(messages.stream().flatMap(s -> s.getCaptionEntities() != null ? s.getCaptionEntities().stream() : null)
                .filter(Objects::nonNull).toList());
        newMessage.setPhoto(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getPhoto)
                .filter(Objects::nonNull)
                .map(s -> s.get(s.size() - 1))
                .toList());
        return newMessage;
    }
}