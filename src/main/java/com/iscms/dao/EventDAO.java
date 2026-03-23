package com.iscms.dao;

import com.iscms.model.Event;
import java.util.List;
import java.util.Optional;

public interface EventDAO {
    void insert(Event event);
    void update(Event event);
    void updateStatus(int eventId, String status);
    Optional<Event> findById(int eventId);
    List<Event> findAll();
    List<Event> findActiveEvents();
    List<Event> findByMinTier(String tier);
}