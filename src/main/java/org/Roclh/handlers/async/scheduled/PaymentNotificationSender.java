package org.Roclh.handlers.async.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.services.ContractService;
import org.Roclh.utils.MessageUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationSender {
    private final Map<Long, Timer> scheduledNotifications = new ConcurrentHashMap<>();


    private final TelegramBotStorage telegramBotStorage;
    private final ContractService contractService;

    @Scheduled(cron = "0 */5 * * * *")
    public void setupNotificationsAboutPayments() {
        log.info("Setting up notifications about payments");
        contractService.getAllExpiredContracts(LocalDateTime.now().plusDays(3)).stream()
                .filter(contractModel -> !scheduledNotifications.containsKey(contractModel.getUserModel().getUserModel().getChatId()))
                .forEach(contractModel -> {
                    contractModel.setWasNotified(true);
                    contractService.saveContract(contractModel);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SendMessage sendMessage = MessageUtils.sendMessage(contractModel.getUserModel().getUserModel()).text("Пришло время платить денюжку! Наш договор заканчивается " + contractModel.getEndDate()).build();
                            scheduledNotifications.remove(contractModel.getUserModel().getUserModel().getChatId());
                            log.info("Notifying user with id {} about ending of the contract", contractModel.getUserModel().getUserModel().getTelegramId());

                            telegramBotStorage.getTelegramBot().sendMessage(sendMessage);
                        }
                    }, Date.from(LocalDateTime.now().withHour(9).withMinute(5).withSecond(0).atZone(ZoneId.systemDefault()).toInstant()));
                    log.info("Scheduled a notification for user with id {} at {}", contractModel.getUserModel().getUserModel().getTelegramId(), contractModel.getEndDate());
                    scheduledNotifications.put(contractModel.getUserModel().getUserModel().getChatId(), timer);
                });
    }

    @Scheduled(cron = "0 * 6 * * *")
    public void resetNotifyStatus() {
        log.info("Notification reset");
        contractService.resetNotify();
    }
}
