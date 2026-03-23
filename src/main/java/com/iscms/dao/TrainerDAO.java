package com.iscms.dao;

import com.iscms.model.Trainer;
import java.util.List;
import java.util.Optional;

public interface TrainerDAO {
    void insert(Trainer trainer);
    void updateActiveStatus(int trainerId, boolean isActive);
    void updateFailedAttempts(int trainerId, int attempts);
    void updateLockStatus(int trainerId, boolean isLocked);
    void updateCredentials(int trainerId, String username, String password);
    void updateProfile(int trainerId, String username, String specialty, String hashedPassword);
    Optional<Trainer> findById(int trainerId);
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findAll();
    List<Trainer> findActive();
}
