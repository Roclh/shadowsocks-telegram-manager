package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisableShadowsocksServerScript extends AbstractShScript<Boolean> {
    protected DisableShadowsocksServerScript() {
        super("disable_user_java_script.sh", """
                #!/bin/bash
                          DIR=/etc/shadowsocks-libev
                
                          if [ $(grep ${1} ${DIR}/justusers.txt | wc -l) -ne 0 ]; then
                                  grep -v ${1} ${DIR}/justusers.txt > ${DIR}/tmpfile2; mv ${DIR}/tmpfile2 ${DIR}/justusers.txt
                                  screen -S ${1} -X quit
                                  rm ${DIR}/${1}.json
                                  echo successfully removed ${1}
                          else
                                  echo no user ${1} check screen -ls and netstat -ltp
                          fi
                
                          netstat -ltp | grep ss-server
                """);
    }

    @Override
    public Boolean execute(String... args) {
        init();
        return ScriptRunner.runCommand(new String[]{"./" + fileName, args[0], args[1]},
                (output) -> output.contains("successfully removed"));
    }

    public Boolean execute(UserModel userModel){
        if (userModel.getUsedPort() == null || userModel.getPassword() == null) {
            return false;
        }
        return this.execute(userModel.getUserModel().getTelegramName() + ":" + userModel.getUserModel().getTelegramId(),
                userModel.getUsedPort().toString());
    }
}
