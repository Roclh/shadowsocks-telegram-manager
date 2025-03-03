package org.Roclh.data.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum Plugin {
    DEFAULT("", Map.of()), V2RAY("v2ray-plugin", Map.of("host", "poh.isgood.host"));

    Plugin(String pluginLinkPostfix, Map<String, String> pluginOpts) {
        this.pluginLinkPostfix = pluginLinkPostfix;
        this.pluginOpts = pluginOpts;
    }

    private final String pluginLinkPostfix;
    private final Map<String, String> pluginOpts;
}
