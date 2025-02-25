package org.Roclh.data.services;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.repositories.UserRepository;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableV2RayShadowsocksServerScript;
import org.Roclh.ss.ShadowsocksProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ShadowsocksProperties shadowsocksProperties;
    private final EnableDefaultShadowsocksServerScript enableScript;
    private final EnableV2RayShadowsocksServerScript enableV2RayScript;

    @EventListener(ContextRefreshedEvent.class)
    @Order(10)
    public void init() {
        getActiveUsers().forEach(user -> {
            switch (user.getPlugin()){
                case DEFAULT -> enableScript.execute(user);
                case V2RAY -> enableV2RayScript.execute(user);
            }
        });
    }

    public boolean saveUser(@NonNull UserModel userModel) {
        try {
            userRepository.saveAndFlush(getUser(userModel.getUserModel().getId()).map(user -> {
                        user.getUserModel().setRole(userModel.getUserModel().getRole());
                        user.getUserModel().setTelegramId(userModel.getUserModel().getTelegramId());
                        user.getUserModel().setTelegramName(userModel.getUserModel().getTelegramName());
                        user.getUserModel().setChatId(userModel.getUserModel().getChatId());
                        user.setPassword(userModel.getPassword());
                        user.setEnabled(userModel.isEnabled());
                        user.setUsedPort(userModel.getUsedPort());
                        user.setPlugin(userModel.getPlugin());
                        return user;
                    }
            ).orElse(userModel));
            return true;
        } catch (Exception e) {
            log.error("Failed to add user", e);
            return false;
        }
    }

    public Optional<UserModel> getUser(@NonNull Long userId) {
        return userRepository.findByUserModel_TelegramId(userId);
    }

    public boolean changePassword(Long telegramId, String password) {
        return userRepository.updatePasswordByUserModel_TelegramId(password, telegramId) > 0;
    }

    public boolean deleteUser(@NonNull Long telegramId) {
        return userRepository.deleteByUserModel_TelegramId(telegramId) > 0;
    }

    public boolean delUser(@NonNull UserModel userModel) {
        return userRepository.deleteByUserModel_TelegramId(userModel.getUserModel().getTelegramId()) > 0;
    }

    public boolean delUser(@NonNull Long port) {
        return userRepository.deleteByUsedPort(port) > 0;
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserModel> getUsers(int pageSize, int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return userRepository.findAll(pageable).toList();
    }

    public List<UserModel> getActiveUsers(){
        return userRepository.findByIsEnabledTrueAndUsedPortNotNullAndPasswordNotNull();
    }

    public long size(){
        return userRepository.count();
    }

    public boolean isAddedUser(TelegramUserModel telegramUser){
        return userRepository.findByUserModel(telegramUser) != null;
    }

    public boolean isAddedUser(Long telegramId){
        return userRepository.findByUserModel_TelegramId(telegramId).isPresent();
    }

    public boolean isPortInUse(Long port) {
        return userRepository.findByUsedPort(port) != null;
    }

    public boolean isPortNotUsed(Long port) {
        return userRepository.findByUsedPort(port) == null;
    }

    public List<Long> getAvailablePorts(int amount){
        return shadowsocksProperties.getPortRange().range().stream().filter(this::isPortNotUsed).toList().subList(0, amount);
    }

    /**
     * IMPORTANT NOT USE, ONLY FOR TESTS
     */
    public void clear(){
        userRepository.deleteAll();
    }
}
