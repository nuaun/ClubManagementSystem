package com.iscms.dao;

import com.iscms.model.RegistrationRequest;
import com.iscms.model.TierUpgradeRequest;
import java.util.List;

public interface RequestDAO {
    // Tier upgrade
    void insertTierUpgrade(TierUpgradeRequest req);
    void updateTierUpgradeStatus(int requestId, String status);
    List<TierUpgradeRequest> findPendingTierUpgrades();
    List<TierUpgradeRequest> findTierUpgradesByMember(int memberId);
    void expireOldTierUpgrades();

    // Registration
    void insertRegistration(RegistrationRequest req);
    void updateRegistrationStatus(int requestId, String status);
    List<RegistrationRequest> findPendingRegistrations();
    List<RegistrationRequest> findExpiredPendingRegistrations(); // BUG-2 FIX: needed to sync member.status
    void expireOldRegistrations();
}