package org.Roclh.data.services;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.repositories.BandwidthRepository;
import org.Roclh.sh.scripts.CreateBandwidthRuleScript;
import org.Roclh.sh.scripts.DeleteBandwidthRuleScript;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BandwidthService {

    private final BandwidthRepository bandwidthRepository;
    private final CreateBandwidthRuleScript createBandwidthRuleScript;
    private final DeleteBandwidthRuleScript deleteBandwidthRuleScript;

    @EventListener(ContextRefreshedEvent.class)
    @Order(11)
    private void init() {
        bandwidthRepository.findAll().stream()
                .filter(bandwidthModel -> bandwidthModel.getBandwidth() != null && bandwidthModel.getUserModel().getUsedPort() != null)
                .forEach(createBandwidthRuleScript::execute);
    }

    @PreDestroy
    private void destroy(){
        bandwidthRepository.findAll().stream()
                .filter(bandwidthModel -> bandwidthModel.getBandwidth() != null && bandwidthModel.getUserModel().getUsedPort() != null)
                .forEach(deleteBandwidthRuleScript::execute);
    }

    public boolean setRule(@Nullable BandwidthModel bandwidthModel) {
        if (bandwidthModel == null) {
            return false;
        }
        try {
            bandwidthRepository.saveAndFlush(getRule(bandwidthModel.getUserModel())
                    .map(rule -> {
                        if (bandwidthModel.getBandwidth() != null) {
                            rule.setBandwidth(bandwidthModel.getBandwidth());
                        }
                        return rule;
                    }).orElse(bandwidthModel));
            return true;
        } catch (Exception e) {
            log.error("Failed to save a bandwidth rule {}", bandwidthModel, e);
        }
        return false;
    }

    public Optional<BandwidthModel> getRule(UserModel userModel) {
        return bandwidthRepository.findByUserModel(userModel);
    }

    public Optional<BandwidthModel> getRule(Long telegramId) {
        return bandwidthRepository.findByUserModel_UserModel_TelegramId(telegramId);
    }

    public boolean deleteRule(BandwidthModel bandwidthModel) {
        return bandwidthRepository.deleteByUserModel(bandwidthModel.getUserModel()) > 0;
    }

    public List<BandwidthModel> getAll() {
        return bandwidthRepository.findAll();
    }

    public boolean hasRule(@NonNull Long telegramId) {
        return bandwidthRepository.existsByUserModel_UserModel_TelegramId(telegramId);
    }

    public boolean hasAnyRule() {
        return !bandwidthRepository.findAll().isEmpty();
    }
}