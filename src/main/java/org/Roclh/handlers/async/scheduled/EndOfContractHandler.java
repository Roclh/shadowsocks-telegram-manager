package org.Roclh.handlers.async.scheduled;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.ContractService;
import org.Roclh.data.services.UserService;
import org.Roclh.sh.scripts.DisableShadowsocksServerScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EndOfContractHandler {
    private final ContractService contractService;
    private final DisableShadowsocksServerScript disableScript;
    private final UserService userService;

    @Scheduled(cron = "0 */1 * * * *")
    public void disableExpiredContract() {
        contractService.getAllExpiredContracts(LocalDateTime.now()).forEach(contractModel -> {
            contractModel.getUserModel().setAdded(false);
            if (userService.saveUser(contractModel.getUserModel())) {
                disableScript.execute(contractModel.getUserModel());
                log.info("{} was disabled", contractModel.getUserModel().getUserModel().getTelegramName());
            }
        });
    }
}
