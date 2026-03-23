package com.iscms.dao;

import com.iscms.model.EventRegistration;
import java.util.List;
import java.util.Optional;

public interface EventRegistrationDAO {
    void insert(EventRegistration reg);
    void deleteByMemberAndEvent(int memberId, int eventId);
    void deleteByEventId(int eventId);
    void cancelAllByEventId(int eventId);
    void promoteFromWaitlist(int registrationId);
    void reorderWaitlistAfterPromotion(int eventId); // FIX-KRİTİK-3
    boolean existsByMemberAndEvent(int memberId, int eventId);
    int countRegistered(int eventId);
    int getMaxWaitlistPosition(int eventId);
    List<EventRegistration> findByEventId(int eventId);
    List<EventRegistration> findByMemberId(int memberId);
    Optional<EventRegistration> findFirstWaitlist(int eventId);
}