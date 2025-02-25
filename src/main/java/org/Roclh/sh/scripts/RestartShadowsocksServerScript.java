package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestartShadowsocksServerScript extends AbstractShScript<Boolean> {
    private final EnableDefaultShadowsocksServerScript enableScript;
    private final EnableV2RayShadowsocksServerScript enableV2RayScript;
    private final DisableShadowsocksServerScript disableScript;

    protected RestartShadowsocksServerScript(EnableDefaultShadowsocksServerScript enableScript,
                                             EnableV2RayShadowsocksServerScript enableV2RayScript,
                                             DisableShadowsocksServerScript disableScript) {
        super(null, null);
        this.enableScript = enableScript;
        this.enableV2RayScript = enableV2RayScript;
        this.disableScript = disableScript;
    }

    @Override
    public Boolean execute(String... args) {
        return disableScript.execute(args[0], args[1]) && enableScript.execute(args[0], args[1], args[2]);
    }

    public Boolean execute(UserModel userModel, boolean soft){
        return (disableScript.execute(userModel) || soft) &&
                switch (userModel.getPlugin()) {
                    case DEFAULT -> enableScript.execute(userModel);
                    case V2RAY -> enableV2RayScript.execute(userModel);
                };
    }
}
