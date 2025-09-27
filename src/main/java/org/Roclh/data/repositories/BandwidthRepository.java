package org.Roclh.data.repositories;

import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface BandwidthRepository extends JpaRepository<BandwidthModel, Long> {
    boolean existsByUserModel_UserModel_TelegramId(Long telegramId);

    Optional<BandwidthModel> findByUserModel(UserModel userModel);

    Optional<BandwidthModel> findByUserModel_UserModel_TelegramId(Long telegramId);

    @Transactional
    @Modifying
    long deleteByUserModel(UserModel userModel);
}
