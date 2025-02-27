package org.Roclh.handlers.commands.access;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.ShScriptsMocks;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.mock.UserMocks;
import org.Roclh.testutil.UserTestBase;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Import(ShScriptsMocks.class)
public class SetPluginCommandTest extends UserTestBase {

    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private UserMocks uMocks;
    @Autowired
    @InjectMocks
    private SetPluginCommand setPluginCommand;
    @Autowired
    @InjectMocks
    private UserService userService;

    private MessageData rootMessageData;
    private MessageData u1MessageData;
    private MessageData u2MessageData;

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
        u2MessageData = MessageData.builder()
                .telegramId(tgMocks.tgu2().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu2().getTelegramId()))
                .messageId(123335)
                .telegramName(Objects.requireNonNull(tgMocks.tgu2().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
    }

    @Test
    public void whenSetPluginToV2Ray_thenPluginIsChanged() {
        String command = "splug " + uMocks.u1().getUserModel().getTelegramId() + " " + UserModel.Plugin.V2RAY;
        setPluginCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData.getLocale()).get(
                        "command.access.setplugin.success",
                        UserModel.Plugin.V2RAY,
                        uMocks.u1().getUserModel().getTelegramId()
                        ),
                result.getText()
        );
        Assertions.assertAll(
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId())
                        .map(user -> user.getPlugin().equals(UserModel.Plugin.V2RAY)).orElse(false))
        );
    }

    @Test
    public void whenSetPluginIncorrectTgId_thenPluginNotChanged() {
        String incorrectTgId = "1234l4";
        String command = "splug " + incorrectTgId + " " + UserModel.Plugin.V2RAY;
        setPluginCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData.getLocale()).get(
                        "command.access.setplugin.validation.telegram.id",
                        incorrectTgId
                ),
                result.getText()
        );
    }

    @Test
    public void whenSetPluginNonExistingTgId_thenPluginNotChanged() {
        Long nonExistingTgId = 1231293L;
        String command = "splug " + nonExistingTgId + " " + UserModel.Plugin.V2RAY;
        setPluginCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData.getLocale()).get(
                        "command.access.setplugin.validation.telegram.id.not.exists",
                        nonExistingTgId
                ),
                result.getText()
        );
        Assertions.assertTrue(userService.getUser(nonExistingTgId).isEmpty());
    }

    @Test
    public void whenSetPluginNonExistingPlugin_thenPluginNotChanged() {
        String notSupportedPlugin = "ADMIN";
        String command = "splug " + uMocks.u2().getUserModel().getTelegramId() + " " + notSupportedPlugin;
        setPluginCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build()
        );
        Assertions.assertEquals(
                I18N.from(rootMessageData.getLocale()).get(
                        "command.access.setplugin.validation.plugin",
                        notSupportedPlugin
                ),
                result.getText()
        );
        Assertions.assertAll(
                () -> Assertions.assertTrue(userService.getUser(uMocks.u2().getUserModel().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(userService.getUser(uMocks.u2().getUserModel().getTelegramId())
                        .map(user -> user.getPlugin().equals(uMocks.u2().getPlugin())).orElse(false))
        );
    }

    @Test
    public void whenSetPluginUserIncorrectUser_thenNotEnoughRights() {
        String command = "splug " + uMocks.u1().getUserModel().getTelegramId() + " " + UserModel.Plugin.V2RAY;
        setPluginCommand.setI18N(u2MessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(u2MessageData)
                .build()
        );
        Assertions.assertEquals(
                I18N.from(u2MessageData.getLocale()).get(
                        "command.access.setplugin.validation.rights",
                        uMocks.u1().getUserModel().getTelegramId()
                ),
                result.getText()
        );
        Assertions.assertAll(
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId())
                        .map((user) -> user.getPlugin().equals(uMocks.u1().getPlugin())).orElse(false))
        );
    }

    @Test
    public void whenSetPluginNonRootCorrectUserId_thenRoleChanged() {
        String command = "splug " + uMocks.u1().getUserModel().getTelegramId() + " " + UserModel.Plugin.V2RAY;
        setPluginCommand.setI18N(u1MessageData.getLocale());
        SendMessage result = setPluginCommand.handle(CommandData.builder()
                .command(command)
                .messageData(u1MessageData)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData.getLocale()).get(
                        "command.access.setplugin.success",
                        UserModel.Plugin.V2RAY,
                        uMocks.u1().getUserModel().getTelegramId()),
                result.getText()
        );
        Assertions.assertAll(
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(userService.getUser(uMocks.u1().getUserModel().getTelegramId())
                        .map(user -> user.getPlugin().equals(UserModel.Plugin.V2RAY)).orElse(false))
        );
    }

    @Test
    public void whenSetPluginCallbackByUser_thenSelectUserItself() {
        I18N i18N = I18N.from(u1MessageData.getLocale());
        setPluginCommand.setI18N(u1MessageData.getLocale());
        CallbackStack callbackStack = setPluginCommand.getCallbackStack();
        EditMessageText selectCallbackResult = (EditMessageText) callbackStack.handle(CallbackData.builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(callbackStack.getCallbackKey())
                .messageData(u1MessageData)
                .build()
        );
        log.info("Current user 1: {}", userService.getUser(tgMocks.tgu1().getTelegramId()));
        log.info("Select callback result: {}", selectCallbackResult);
        String selectCommandPressButtonCallback = selectCallbackResult.getReplyMarkup()
                .getKeyboard()
                .stream()
                .flatMap(Collection::stream)
                .filter((button) -> button.getText().equals(i18N.get("command.access.setplugin.inline.button")))
                .findFirst()
                .map(InlineKeyboardButton::getCallbackData)
                .orElse(null);
        Assertions.assertNotNull(selectCommandPressButtonCallback);
        Assertions.assertEquals(i18N.get("command.access.select.command"), selectCallbackResult.getText());
        EditMessageText selectCommandResult = (EditMessageText) callbackStack.handle(CallbackData.builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(selectCommandPressButtonCallback)
                .messageData(u1MessageData)
                .build()
        );
        String selectPluginButtonCallback = selectCommandResult.getReplyMarkup()
                .getKeyboard()
                .stream()
                .flatMap(Collection::stream)
                .filter((button) -> button.getText().equals(UserModel.Plugin.V2RAY.name()))
                .findFirst()
                .map(InlineKeyboardButton::getCallbackData)
                .orElse(null);
        Assertions.assertNotNull(selectPluginButtonCallback);
        Assertions.assertEquals(i18N.get("util.callback.select.plugin"), selectCommandResult.getText());
        EditMessageText selectPluginResult = (EditMessageText) callbackStack.handle(CallbackData.builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(selectPluginButtonCallback)
                .messageData(u1MessageData)
                .build());
        Assertions.assertEquals(
                i18N.get("command.access.setplugin.success",
                        UserModel.Plugin.V2RAY,
                        uMocks.u1().getUserModel().getTelegramId()
                ),
                selectPluginResult.getText()
        );
    }
}
