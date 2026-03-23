package com.iscms.dao;

import com.iscms.model.Membership;
import java.util.List;
import java.util.Optional;

public interface MembershipDAO {
    void insert(Membership membership);
    void update(Membership membership);
    void updateStatus(int membershipId, String status);
    void incrementFreezeCount(int membershipId);
    Optional<Membership> findById(int membershipId);
    Optional<Membership> findActiveByMemberId(int memberId);
    Optional<Membership> findFrozenByMemberId(int memberId); // BUG-3 FIX: needed to detect expired freeze
    List<Membership> findAllByMemberId(int memberId);
    List<Membership> findExpiringSoon(int days);
}