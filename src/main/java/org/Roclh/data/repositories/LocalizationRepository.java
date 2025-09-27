package org.Roclh.data.repositories;

import org.Roclh.data.entities.LocalizationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LocalizationRepository extends JpaRepository<LocalizationModel, Long> {
    @Transactional
    @Modifying
    @Query("update LocalizationModel l set l.locale = ?1 where l.telegramId = ?2")
    int updateLocaleByTelegramId(String locale, Long telegramId);
}