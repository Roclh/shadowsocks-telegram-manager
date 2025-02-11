package org.Roclh.data.repositories;

import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {


    Optional<UserModel> findByUserModel_TelegramId(Long telegramId);

    @Transactional
    @Modifying
    @Query("update UserModel u set u.password = ?1 where u.userModel = ?2")
    int updatePasswordByUserModel(String password, TelegramUserModel userModel);

    @Transactional
    @Modifying
    @Query("update UserModel u set u.password = ?1 where u.userModel.telegramId = ?2")
    long updatePasswordByUserModel_TelegramId(String password, Long telegramId);

    @Transactional
    @Modifying
    @Query("delete UserModel u where u.userModel.telegramId = ?1")
    int deleteByUserModel_TelegramId(Long telegramId);

    long deleteByUserModel(TelegramUserModel userModel);

    long deleteByUsedPort(Long usedPort);

    @Transactional
    @Modifying
    @Query("update UserModel u set u.isAdded = ?1 where u.userModel.telegramId = ?2")
    int updateIsAddedByUserModel_TelegramId(boolean isAdded, Long telegramId);

    boolean existsByUserModel_TelegramId(Long telegramId);

    UserModel findByUsedPort(Long usedPort);

    UserModel findByUserModel(TelegramUserModel userModel);

    List<UserModel> findByIsAddedTrueAndUsedPortNotNullAndPasswordNotNull();

}
