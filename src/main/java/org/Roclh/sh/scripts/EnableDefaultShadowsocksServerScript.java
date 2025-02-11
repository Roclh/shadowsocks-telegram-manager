package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnableDefaultShadowsocksServerScript extends AbstractShScript<Boolean> {
    protected EnableDefaultShadowsocksServerScript() {
        super("add_user_java_script.sh", """
                #!/bin/bash
                                    
                DIR=/etc/shadowsocks-libev
                BASE_CONFIG=${DIR}/${1}.json
                if [ $(netstat -tlp | grep -o ${2}" " | wc -l) -eq 0 ]; then
                        cp ${DIR}/config-example.json ${BASE_CONFIG}
                    
                        sed -i '/server_port/c\\    "server_port":'${2}',' ${BASE_CONFIG}
                        sed -i '/password/c\\    "password":"'${3}'",' ${BASE_CONFIG}
                    
                        screen -dmS ${1} ss-server -c /etc/shadowsocks-libev/${1}.json -u
                        echo screen ${1} started
                        echo screen -dmS ${1} ss-server -c /etc/shadowsocks-libev/${1}.json -u >> /etc/shadowsocks-libev/allactiveusers.txt
                        echo ${1}>> /etc/shadowsocks-libev/justusers.txt
                else
                        echo screen ${1} port ${2} already exists or other problem
                fi
                    
                netstat -tlp | grep ss-server
                                    
                """);
    }

    @Override
    public Boolean execute(String... args) {
        init();
        return ScriptRunner.runCommand(new String[]{"./" + fileName, args[0], args[1], args[2]},
                (output) -> output.contains("started"));
    }

    public Boolean execute(UserModel userModel) {
        if (userModel.getUsedPort() == null || userModel.getPassword() == null) {
            log.error("Failed to execute - password or port not exists");
            return false;
        }
        return this.execute(userModel.getUserModel().getTelegramName() + ":" + userModel.getUserModel().getTelegramId(),
                userModel.getUsedPort().toString(), userModel.getPassword());
    }
}
