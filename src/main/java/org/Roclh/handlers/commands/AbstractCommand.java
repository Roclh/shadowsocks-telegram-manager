package org.Roclh.handlers.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.Roclh.data.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.utils.i18n.I18N;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public abstract class AbstractCommand<T extends PartialBotApiMethod<? extends Serializable>> implements Command<T> {

    protected final TelegramUserService telegramUserService;
    @Getter
    protected I18N i18N;

    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.MANAGER);
    }

    @Override
    public Command<T> setI18N(Locale locale) {
        this.i18N = I18N.from(locale);
        return this;
    }
}
