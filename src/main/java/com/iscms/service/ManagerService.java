package com.iscms.service;

import com.iscms.dao.ManagerDAOImpl;
import com.iscms.model.Manager;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Manager operations (used by AdminDashboard).
 * AdminDashboard calls this — never DAO directly.
 */
public class ManagerService {

    private final ManagerDAOImpl managerDAO = new ManagerDAOImpl();

    public List<Manager> getAllManagers() {
        return managerDAO.findAll();
    }

    public Optional<Manager> getManagerByEmail(String email) {
        return managerDAO.findByEmail(email);
    }

    public void addManager(Manager manager) {
        // FIX-9: Hash işi Service katmanına taşındı — DAO artık hashlemez
        manager.setPassword(AuthService.hashPassword(manager.getPassword()));
        managerDAO.insert(manager);
    }

    public void removeManager(int managerId) {
        managerDAO.delete(managerId);
    }

    public void setLockStatus(int managerId, boolean locked) {
        managerDAO.updateLockStatus(managerId, locked);
    }
}
