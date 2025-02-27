package org.Roclh.handlers.commands.access;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.ServerSharingService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.ss.ShadowsocksProperties;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
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

@Slf4j
@Component
public class GetTestLinkCommand extends AbstractCommand<PartialBotApiMethod<? extends Serializable>> implements WithCallbackStack {
    private final ServerSharingService serverSharingService;
    private final ShadowsocksProperties shadowsocksProperties;
    private final UserService userService;

    public GetTestLinkCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, ServerSharingService serverSharingService, ShadowsocksProperties shadowsocksProperties, UserService userService) {
        super(telegramUserService, commandRegistry);
        this.serverSharingService = serverSharingService;
        this.shadowsocksProperties = shadowsocksProperties;
        this.userService = userService;
    }

    @Override
    public PartialBotApiMethod<? extends Serializable> handle(CommandData commandData) {
        SendPhoto.SendPhotoBuilder sendPhoto = MessageUtils.sendPhoto(commandData.getMessageData());
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(commandData.getMessageData());
        UserModel userModel = UserModel.builder()
                .userModel(TelegramUserModel.builder()
                        .telegramName("TestUser")
                        .role(Role.USER)
                        .telegramId(0L)
                        .build())
                .plugin(UserModel.Plugin.DEFAULT)
                .usedPort(shadowsocksProperties.getPortRange().getLeftRangeLimit() - 1)
                .isEnabled(true)
                .password("qwertyui")
                .build();
        String uri = serverSharingService.generateServerUrl(userModel);
        BufferedImage qrCode = serverSharingService.generateServerUrlQrCode(userModel);
        sendPhoto.caption("<code>" + uri + "</code>");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Assert.notNull(qrCode, "Can't be null");
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
        return telegramUserService.isAllowed(userId, Role.GUEST);
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("testqr");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("access")
                .forCommand("testqr", EmojiConstants.BULB + " " + i18N.get("command.access.gettestlink.inline.button"))
                .withLocalizedCallbackKey(KEY + " " + i18N.get("callback.access.inline.button.access"))
                .withCommandDisplayCondition(id -> !userService.isEnabledUser(id))
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
