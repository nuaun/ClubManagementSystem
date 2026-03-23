package com.iscms.dao;

import com.iscms.model.Manager;
import java.util.List;
import java.util.Optional;

public interface ManagerDAO {
    void insert(Manager manager);
    void delete(int managerId);
    List<Manager> findAll();
    Optional<Manager> findByEmail(String email);
    Optional<Manager> findByUsername(String username);
    void updateFailedAttempts(int managerId, int attempts);
    void updateLockStatus(int managerId, boolean isLocked);
    void updatePassword(int managerId, String hashedPassword);
}
