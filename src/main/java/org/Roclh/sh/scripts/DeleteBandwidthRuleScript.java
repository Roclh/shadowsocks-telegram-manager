package org.Roclh.sh.scripts;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DeleteBandwidthRuleScript extends AbstractShScript<Boolean> {

    protected DeleteBandwidthRuleScript() {
        super("delete_bandwidth_rule.sh", """
                #!/bin/bash
                         set -eo pipefail
                
                         DEVICE="eth0"
                         PORT="$1"
                         TC="/sbin/tc"
                         CLASS_ID="1:${PORT}"
                
                         delete_filter() {
                             echo -n "Searching filter for port $PORT... "
                             local filter_handle=$($TC -p filter show dev $DEVICE parent 1: | \\
                                 awk -v port="$PORT" '/cmp\\(u16 at 0 layer 2 eq 'port'\\)/ {gsub(/:/, "", $10); print $10}')
                
                             if [ -n "$filter_handle" ]; then
                                 echo "found (handle: $filter_handle)"
                                 $TC filter del dev $DEVICE parent 1: handle $filter_handle 2>/dev/null || {
                                     echo "Error: Failed to delete filter"
                                     return 1
                                 }
                             else
                                 echo "not found"
                             fi
                         }
                
                         delete_class() {
                             echo -n "Deleting class $CLASS_ID... "
                             if $TC class show dev $DEVICE | grep "htb $CLASS_ID "; then
                                 $TC class del dev $DEVICE parent 1: classid $CLASS_ID 2>/dev/null || {
                                     echo "Error: Failed to delete class"
                                     return 1
                                 }
                                 echo "success"
                             else
                                 echo "not found"
                             fi
                         }
                
                         verify_removal() {
                             ! $TC class show dev $DEVICE | grep "classid $CLASS_ID" && \\
                             ! $TC filter show dev $DEVICE | grep "cmp(u16 at 0 layer 2 eq $PORT)"
                         }
                
                         main() {
                             echo "=== Removing rule for port $PORT ==="
                             delete_filter
                             delete_class
                
                             if verify_removal; then
                                 echo "SUCCESS: All rules for port $PORT removed!"
                             else
                                 echo "ERROR: Failed to remove all components. Remaining:"
                                 $TC -p class show dev $DEVICE | grep "$CLASS_ID" || true
                                 $TC -p filter show dev $DEVICE | grep "$PORT" || true
                                 exit 1
                             fi
                         }
                
                         main "$@"
                """);
    }

    @Override
    public Boolean execute(String... args) {
        init();
        return ScriptRunner.runCommandWithResult(new String[]{"./" + fileName, args[0]},
                (output) -> output.contains("SUCCESS: Все правила для порта "));
    }

    public boolean execute(@NonNull BandwidthModel bandwidthModel) {
        init();
        if(bandwidthModel.getUserModel().getUsedPort() == null){
            return false;
        }
        return execute(bandwidthModel.getUserModel().getUsedPort().toString());
    }
}
