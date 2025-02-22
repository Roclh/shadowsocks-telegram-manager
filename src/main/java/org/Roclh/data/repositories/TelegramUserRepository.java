package org.Roclh.data.repositories;

import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUserModel, Long> {
    Optional<TelegramUserModel> findByTelegramIdAndRole(Long telegramId, Role role);

    boolean existsByTelegramId(Long telegramId);

    Optional<TelegramUserModel> findByTelegramId(Long telegramId);

    @Transactional
    @Modifying
    @Query("update TelegramUserModel t set t.role = ?1 where t.telegramId = ?2")
    int updateRoleByTelegramId(Role role, Long telegramId);

    @Transactional
    @Modifying
    long deleteByTelegramId(Long telegramId);

    @Transactional
    @Modifying
    @Query("delete from TelegramUserModel e where e.telegramId != :id")
    void deleteAllExcept(@Param("id") Long id);
}
