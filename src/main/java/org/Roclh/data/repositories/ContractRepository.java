package org.Roclh.data.repositories;

import org.Roclh.data.entities.ContractModel;
import org.Roclh.data.entities.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository  extends JpaRepository<ContractModel, Long> {
    @Transactional
    @Modifying
    @Query("update ContractModel c set c.wasNotified = False where c.wasNotified = true")
    int updateWasNotifiedByWasNotifiedTrue();


    List<ContractModel> findByWasNotifiedFalseAndUserModel_IsAddedTrueAndEndDateLessThan(LocalDateTime endDate);

    Optional<ContractModel> findByUserModel(UserModel userModel);

    Optional<ContractModel> findByUserModel_UserModel_TelegramId(Long telegramId);


}
