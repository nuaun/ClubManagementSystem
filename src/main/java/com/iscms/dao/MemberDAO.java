package com.iscms.dao;

import com.iscms.model.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberDAO {
    void insert(Member member);
    void update(Member member);
    void updateStatus(int memberId, String status);
    void updateFailedAttempts(int memberId, int attempts);
    void updateLockStatus(int memberId, boolean isLocked);
    Optional<Member> findById(int memberId);
    Optional<Member> findByPhone(String phone);
    List<Member> findAll();
    List<Member> findByStatus(String status);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    // ── Clean layered architecture — no inline SQL above DAO ────
    void resetPassword(int memberId, String hashedPassword);
    void setArchivedAt(int memberId);
    List<Integer> findArchivedMemberIdsBefore(LocalDateTime cutoff);
    void anonymize(int memberId);
}
