package org.Roclh.handlers.commands.access;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.enums.Role;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.ServerSharingService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.Roclh.utils.i18n.EmojiConstants.KEY;

@Component
@Slf4j
public class GetLinkCommand extends AbstractCommand<PartialBotApiMethod<? extends Serializable>> implements WithCallbackStack {
    private final ServerSharingService serverSharingService;
    private final UserService userService;

    public GetLinkCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, ServerSharingService serverSharingService, UserService userService) {
        super(telegramUserService, commandRegistry);
        this.serverSharingService = serverSharingService;
        this.userService = userService;
    }

    @Override
    public PartialBotApiMethod<? extends Serializable> handle(CommandData commandData) {
        SendPhoto.SendPhotoBuilder sendPhoto = MessageUtils.sendPhoto(commandData.getMessageData());
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(commandData.getMessageData());
        Long telegramId = commandData.getMessageData().getTelegramId();


        UserModel userModel = userService.getUser(telegramId).orElse(null);
        if (userModel == null || !userModel.isEnabled()) {
            log.error("Failed to generate link - user with id {} not exists or is not added", telegramId);
            sendMessage.text(i18N.get("command.common.getlink.validation.failed.not.exists", telegramId));
            return sendMessage.build();
        }
        String uri = serverSharingService.generateServerUrl(userModel);
        if (uri == null) {
            log.error("Failed to generate link - failed to generate server url");
            sendMessage.text(i18N.get("command.common.getlink.validation.failed.generate.url"));
            return sendMessage.build();
        }
        BufferedImage qrCode = serverSharingService.generateServerUrlQrCode(userModel);
        if (qrCode == null) {
            log.error("Failed to generate link - failed to generate server QR code");
            sendMessage.text(i18N.get("command.common.getlink.validation.failed.generate.qr"));
            return sendMessage.build();
        }
        sendPhoto.caption("<code>" + uri+ "</code>");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrCode, "jpeg", os);
        } catch (IOException e) {
            log.error("Failed to generate link - failed to parse qr code to output stream", e);
            sendMessage.text(i18N.get("command.common.getlink.validation.failed.parse.qr"));
            return sendMessage.build();
        }
        sendPhoto.photo(new InputFile().setMedia(new ByteArrayInputStream(os.toByteArray()), "QR.jpeg"));
        return sendPhoto.build();
    }


    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.USER) && userService.getUser(userId).map(UserModel::isEnabled).orElse(false);
    }

    @Override
    public String getHelp() {
        return getCommandNames().get(0) + "\n" + i18N.get("command.common.getlink.help");
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("qr", "link");
    }


    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("access")
                .forCommand("qr", KEY + " " + i18N.get("callback.common.getqr.inline.button"))
                .withSelectCommandText(i18N.get("command.access.select.command"))
                .withCommandDisplayCondition(userService::isEnabledUser)
                .with(1, (callbackData) -> {
                    PartialBotApiMethod<?> result = handle(CommandData.from(callbackData));
                    if(result instanceof SendMessage){
                        return MessageUtils.editMessage(callbackData.getMessageData())
                                .text(((SendMessage) result).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build();
                    }
                    if(result instanceof SendPhoto){
                        callbackData.setCallbackData("start nl");
                        ((SendPhoto) result).setReplyMarkup(InlineUtils.getDefaultNavigationMarkup(callbackData));
                        return result;
                    }
                    throw new RuntimeException("Impossible state");
                })
                .build();
    }
}
