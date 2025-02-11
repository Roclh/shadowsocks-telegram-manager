package org.Roclh.handlers.commands.manager;


import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.ContractModel;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.repositories.BandwidthRepository;
import org.Roclh.data.services.BandwidthService;
import org.Roclh.data.services.ContractService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ExportCsvCommand extends AbstractCommand<PartialBotApiMethod<? extends Serializable>> {


    private final UserService userService;
    private final BandwidthService bandwidthService;
    private final ContractService contractService;

    public ExportCsvCommand(TelegramUserService telegramUserService, UserService userManager,
                            BandwidthService bandwidthService, BandwidthRepository bandwidthRepository,
                            UserService userService, ContractService contractService) {
        super(telegramUserService);
        this.bandwidthService = bandwidthService;
        this.userService = userService;
        this.contractService = contractService;
    }

    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.ROOT);
    }

    @Override
    public PartialBotApiMethod<? extends Serializable> handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        log.info("CSV command was requested");
        String[] command = commandData.getCommand().split(" ");
        if (command.length != 2) {
            return MessageUtils.sendMessage(messageData)
                    .text(i18N.get("common.validation.not.enough.argument", 2)).build();
        }
        String fileType = command[1];
        FileDataTypes dataTypes;
        try {
            dataTypes = FileDataTypes.valueOf(fileType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file type {}", fileType, e);
            return MessageUtils.sendMessage(messageData)
                    .text(i18N.get("command.manager.export.csv.wrong.argument")).build();
        }
        return switch (dataTypes) {
            case USER ->{
                File file = userModelToCsv(userService.getAllUsers());
                if (file != null){
                    yield SendDocument.builder()
                            .chatId(messageData.getChatId())
                            .document(new InputFile(file))
                            .build();
                }
                yield MessageUtils.sendMessage(messageData)
                        .text(i18N.get("command.manager.export.csv.cant.create.file"))
                        .build();
            }

            case BANDWIDTH -> {
                File file = bandwidthUserModelToCsv(bandwidthService.getAll());
                if (file != null){
                    yield SendDocument.builder()
                            .chatId(messageData.getChatId())
                            .document(new InputFile(file))
                            .build();
                }
                yield MessageUtils.sendMessage(messageData)
                        .text(i18N.get("command.manager.export.csv.cant.create.file"))
                        .build();
            }
            case CONTRACT -> {
                File file = contractModelToCsv(contractService.getAllContracts());
                if (file != null){
                    yield SendDocument.builder()
                            .chatId(messageData.getChatId())
                            .document(new InputFile(file))
                            .build();
                }
                yield MessageUtils.sendMessage(messageData)
                        .text(i18N.get("command.manager.export.csv.cant.create.file"))
                        .build();
            }
            case TGUSER -> {
                File file = telegramUserModelToCsv(telegramUserService.getUsers());
                if (file != null){
                    yield SendDocument.builder()
                            .chatId(messageData.getChatId())
                            .document(new InputFile(file))
                            .build();
                }
                yield MessageUtils.sendMessage(messageData)
                        .text(i18N.get("command.manager.export.csv.cant.create.file"))
                        .build();
            }
            case ALL -> {
                List<File> attachments = new ArrayList<>();
                attachments.add(userModelToCsv(userService.getAllUsers()));
                attachments.add(bandwidthUserModelToCsv(bandwidthService.getAll()));
                attachments.add(contractModelToCsv(contractService.getAllContracts()));
                attachments.add(telegramUserModelToCsv(telegramUserService.getUsers()));
                if (attachments.stream().filter(Objects::nonNull).toList().isEmpty()){
                    yield MessageUtils.sendMessage(messageData)
                            .text(i18N.get("command.manager.export.csv.cant.create.file"))
                            .build();
                }
                List<InputMedia> medias = new ArrayList<>(attachments.stream()
                        .map(file -> (InputMedia) InputMediaDocument
                        .builder().media("attach://" + file.getName())
                        .mediaName(file.getName()).isNewMedia(true)
                        .newMediaFile(file).build()).toList());
                medias.get(medias.size() - 1).setCaption("Take your data, sir.");
                yield SendMediaGroup.builder()
                        .medias(medias)
                        .chatId(messageData.getChatId())
                        .build();
            }
        };
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("csv", "exportcsv");
    }

    private File userModelToCsv(List<UserModel> users) {
        StringBuilder csvStringBuilder = new StringBuilder();
        csvStringBuilder.append("id,tgId,password,port,isAdded\n");
        for (UserModel user : users) {
            Long id = user.getId();
            Long tgid = user.getUserModel().getId();
            String password = user.getPassword();
            Long port = user.getUsedPort();
            boolean isAdded = user.isAdded();
            csvStringBuilder.append(id).append(',')
                    .append(tgid).append(',')
                    .append(password).append(',')
                    .append(port).append(',')
                    .append(isAdded).append('\n');
        }
        String csvString = csvStringBuilder.toString();
        File file = new File("userModel.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvString);
        } catch (IOException e) {
            log.error("Failed to export csv", e);
            return null;
        }
        return file;
    }

    private File telegramUserModelToCsv(List<TelegramUserModel> users) {
        StringBuilder csvStringBuilder = new StringBuilder();
        csvStringBuilder.append("id,tgId,role,chatId,tgName\n");
        for (TelegramUserModel user : users) {
            Long id = user.getId();
            Long tgId = user.getTelegramId();
            Role role = user.getRole();
            Long chatId = user.getChatId();
            String tgName = user.getTelegramName();
            csvStringBuilder.append(id).append(',')
                    .append(tgId).append(',')
                    .append(role).append(',')
                    .append(chatId).append(',')
                    .append(tgName).append('\n');
        }
        String csvString = csvStringBuilder.toString();
        File file = new File("tgUserModel.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvString);
        } catch (IOException e) {
            log.error("Failed to export csv", e);
            return null;
        }
        return file;
    }

    private File bandwidthUserModelToCsv(List<BandwidthModel> models) {
        File bandwidthFile = new File("bandwidth.csv");
        StringBuilder csvStringBuilder = new StringBuilder();
        csvStringBuilder.append("id,bandwidth,userModelId\n");
        for (BandwidthModel model : models) {
            long id = model.getId();
            Long userModelId = model.getUserModel().getId();
            String bandwidth;
            if (model.getBandwidth() == null) {
                bandwidth = "None";
            } else {
                bandwidth = model.getBandwidth().name();
            }
            csvStringBuilder.append(id).append(',')
                    .append(bandwidth).append(',')
                    .append(userModelId).append('\n');
        }
        String csvString = csvStringBuilder.toString();
        try (FileWriter writer = new FileWriter(bandwidthFile)) {
            writer.write(csvString);
        } catch (IOException e) {
            log.error("Failed to export csv", e);
            return null;
        }
        return bandwidthFile;
    }

    private File contractModelToCsv(List<ContractModel> models) {
        File contractFile = new File("contract.csv");
        StringBuilder csvStringBuilder = new StringBuilder();
        csvStringBuilder.append("id,userModelId,startDate,endDate,wasNotified\n");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        for (ContractModel model : models) {
            Long id = model.getId();
            Long userModelId = model.getUserModel().getId();
            String startDate = model.getStartDate().format(formatter);
            String endDate = model.getEndDate().format(formatter);
            boolean wasNotified = model.isWasNotified();
            csvStringBuilder.append(id).append(',')
                    .append(userModelId).append(',')
                    .append(startDate).append(',')
                    .append(endDate).append(',')
                    .append(wasNotified).append('\n');
        }
        String csvString = csvStringBuilder.toString();
        try (FileWriter writer = new FileWriter(contractFile)) {
            writer.write(csvString);
        } catch (IOException e) {
            log.error("Failed to export csv", e);
            return null;
        }
        return contractFile;
    }

    public enum FileDataTypes {
        USER, TGUSER, BANDWIDTH, CONTRACT, ALL
    }
}