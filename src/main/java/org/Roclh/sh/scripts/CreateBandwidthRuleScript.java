package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateBandwidthRuleScript extends AbstractShScript<Boolean> {
    protected CreateBandwidthRuleScript() {
        super("set_bandwidth_rule_script.sh", """
                #!/bin/bash
                set -eo pipefail
                
                DEVICE="eth0"
                PORT="$1"
                RATE="$2"
                BURST="$3"
                TC="/sbin/tc"
                CLASS_ID="1:${PORT}"
                
                initialize_qdisc() {
                    echo -n "Checking qdisc... "
                    if ! $TC qdisc show dev $DEVICE | grep "htb 1: root"; then
                        $TC qdisc add dev $DEVICE root handle 1: htb 2>/dev/null || {
                            echo "Error: Failed to create root qdisc"
                            exit 1
                        }
                        echo "created"
                    else
                        echo "exists"
                    fi
                }
                
                create_class() {
                    echo -n "Creating class $CLASS_ID... "
                    if ! $TC class show dev $DEVICE parent 1: | grep "$PORT"; then
                        $TC class add dev $DEVICE parent 1: classid $CLASS_ID htb \\
                        rate "$RATE" burst "$BURST" 2>/dev/null || {
                            echo "Error: Failed to create class"
                            exit 1
                        }
                        echo "success"
                    else
                        echo "exists"
                }
                
                add_filter() {
                    echo -n "Adding filter for port $PORT... "
                    $TC filter add dev $DEVICE parent 1: protocol ip \\
                        basic match "cmp(u16 at 0 layer transport eq $PORT)" \\
                        flowid $CLASS_ID 2>/dev/null || {
                            echo "Error: Failed to add filter"
                            exit 1
                        }
                    echo "success"
                }
                
                main() {
                    echo "=== Adding rule for port $PORT ==="
                    initialize_qdisc
                    create_class
                    add_filter
                    echo "SUCCESS: Rule for port $PORT ($RATE) added!"
                }
                
                main "$@"
                """);
    }

    @Override
    public Boolean execute(String... args) {
        init();
        return ScriptRunner.runCommand(new String[]{"./" + fileName, args[0],
                        args[1], args[2]},
                result -> result.contains("SUCCESS: Правило для порта "));
    }

    public Boolean execute(BandwidthModel bandwidthModel) {
        init();
        if (bandwidthModel.getBandwidth() == null || bandwidthModel.getUserModel().getUsedPort() == null) {
            return false;
        }
        return ScriptRunner.runCommand(new String[]{"./" + fileName, bandwidthModel.getUserModel().getUsedPort().toString(),
                        bandwidthModel.getBandwidth().getBandwidth(), bandwidthModel.getBandwidth().getBurst()},
                result -> result.contains("SUCCESS: Правило для порта "));
    }
}
