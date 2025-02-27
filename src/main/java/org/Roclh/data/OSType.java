package org.Roclh.data;

import org.Roclh.utils.i18n.I18N;

public enum OSType {
    PC("command.common.guide.pc.alias"),IOS("command.common.guide.ios.alias"),

    ANDROID("command.common.guide.android.alias");

    private final String localized;

    public String localize(I18N i18N) {
        return i18N.get(localized);
    }

    OSType(String localized) {
        this.localized = localized;
    }

}
