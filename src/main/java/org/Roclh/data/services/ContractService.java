package org.Roclh.data.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.ContractModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.repositories.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository repository;

    public boolean saveContract(ContractModel contractModel){
        if (contractModel == null) {
            return false;
        }
        try {
            repository.saveAndFlush(getContract(contractModel.getUserModel())
                    .map(contract -> {
                        if (contractModel.getStartDate() != null) {
                            contract.setStartDate(contractModel.getStartDate());
                        }
                        if(contractModel.getEndDate() != null){
                            contract.setEndDate(contractModel.getEndDate());
                        }
                        contract.setWasNotified(contractModel.isWasNotified());
                        return contract;
                    }).orElse(contractModel));
            return true;
        } catch (Exception e) {
            log.error("Failed to save a contract {}", contractModel, e);
        }
        return false;
    }

    public List<ContractModel> getAllExpiredContracts(LocalDateTime dateTime){
        return repository.findByWasNotifiedFalseAndUserModel_IsAddedTrueAndEndDateLessThan(dateTime);
    }

    public int resetNotify(){
        return repository.updateWasNotifiedByWasNotifiedTrue();
    }

    public List<ContractModel> getAllContracts(){
        return repository.findAll();
    }

    public Optional<ContractModel> getContract(UserModel userModel){
        return repository.findByUserModel(userModel);
    }

    public Optional<ContractModel> getContract(Long telegramId){
        return repository.findByUserModel_UserModel_TelegramId(telegramId);
    }
}
