package org.Roclh.handlers.commands.access;

import org.Roclh.data.services.ServerSharingService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.mock.UserMocks;
import org.Roclh.testutil.UserTestBase;
import org.Roclh.testutil.callback.CallbackTestUtil;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import static org.Roclh.utils.i18n.EmojiConstants.KEY;

public class GetLinkCommandTest extends UserTestBase {
    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private UserMocks uMocks;
    @Autowired
    @InjectMocks
    private GetLinkCommand getLinkCommand;
    @Autowired
    @InjectMocks
    private UserService userService;
    @Autowired
    private ServerSharingService serverSharingService;

    private MessageData rootMessageData;
    private MessageData u1MessageData;

    @BeforeEach
    public void init() {
        super.init();
        rootMessageData = MessageData.builder()
                .telegramId(tgMocks.tguroot().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tguroot().getChatId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tguroot().getTelegramName()))
                .locale(Locale.forLanguageTag("en"))
                .build();
        u1MessageData = MessageData.builder()
                .telegramId(tgMocks.tgu1().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu1().getTelegramId()))
                .messageId(123331)
                .telegramName(Objects.requireNonNull(tgMocks.tgu1().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
    }

    @Test
    public void whenCorrectCommand_thenLinkWithQRSent() {
        String command = "qr";
        getLinkCommand.setI18N(u1MessageData.getLocale());
        PartialBotApiMethod<? extends Serializable> result = getLinkCommand.handle(CommandData
                .builder()
                .messageData(u1MessageData)
                .command(command)
                .build()
        );
        Assertions.assertInstanceOf(SendPhoto.class, result);
        SendPhoto resultPhoto = (SendPhoto) result;
        Assertions.assertEquals("<code>" + serverSharingService.generateServerUrl(uMocks.u1()) + "</code>",
                resultPhoto.getCaption());
        BufferedImage qrCode = serverSharingService.generateServerUrlQrCode(uMocks.u1());
        Assertions.assertNotNull(qrCode);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrCode, "jpeg", os);
        } catch (IOException e) {
            Assertions.fail("Exception creating image");
        }
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        try {
            Assertions.assertTrue(
                    IOUtils.contentEquals(inputStream, resultPhoto.getPhoto().getNewMediaStream()));
        } catch (IOException e) {
            Assertions.fail("Exception comparing file input streams");
        }
    }

    @Test
    public void whenCorrectCallbackPressed_thenLinkWithQRSent() {
        I18N i18N = I18N.from(u1MessageData.getLocale());
        getLinkCommand.setI18N(u1MessageData.getLocale());
        CallbackStack callbackStack = getLinkCommand.getCallbackStack();
        EditMessageText callbackPressResult = (EditMessageText) callbackStack.handle(CallbackData
                .builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(callbackStack.getCallbackKey())
                .messageData(u1MessageData)
                .build());
        String getQRCommandButtonCallbackData = CallbackTestUtil.extractCallbackData(
                callbackPressResult.getReplyMarkup(),
                (button) -> button.getText().equals(KEY + " " + i18N.get("callback.common.getqr.inline.button"))
        );
        Assertions.assertNotNull(getQRCommandButtonCallbackData);
        Assertions.assertEquals(callbackStack.getCallbackKey() + " qr", getQRCommandButtonCallbackData);

        PartialBotApiMethod<? extends Serializable> result = callbackStack.handle(CallbackData.builder()
                .messageData(u1MessageData)
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(getQRCommandButtonCallbackData)
                .build());
        Assertions.assertInstanceOf(SendPhoto.class, result);
        SendPhoto resultPhoto = (SendPhoto) result;
        Assertions.assertEquals("<code>" + serverSharingService.generateServerUrl(uMocks.u1()) + "</code>",
                resultPhoto.getCaption());
        BufferedImage qrCode = serverSharingService.generateServerUrlQrCode(uMocks.u1());
        Assertions.assertNotNull(qrCode);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrCode, "jpeg", os);
        } catch (IOException e) {
            Assertions.fail("Exception creating image");
        }
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        try {
            Assertions.assertTrue(
                    IOUtils.contentEquals(inputStream, resultPhoto.getPhoto().getNewMediaStream()));
        } catch (IOException e) {
            Assertions.fail("Exception comparing file input streams");
        }


    }

    //TODO: Add tests for each validation
}
