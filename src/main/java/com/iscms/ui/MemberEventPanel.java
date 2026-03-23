package com.iscms.ui;

import com.iscms.model.Event;
import com.iscms.model.EventRegistration;
import com.iscms.model.Member;
import com.iscms.model.Membership;
import com.iscms.service.EventService;
import com.iscms.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class MemberEventPanel extends JPanel {

    private final Member member;
    private final EventService eventService   = new EventService();
    private final MemberService memberService = new MemberService();

    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabs;

    public MemberEventPanel(Member member) {
        this.member = member;
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();
        tabs.addTab("Available Events", buildAvailablePanel());
        tabs.addTab("My Registrations", buildMyRegistrationsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── AVAILABLE EVENTS ─────────────────────────────────────────
    private JPanel buildAvailablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh  = new JButton("Refresh");
        JButton btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(33, 120, 80));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);
        toolbar.add(btnRefresh);
        toolbar.add(btnRegister);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Category", "Date", "Start", "Location",
                "Spots Left", "Fee", "Min Tier", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadEvents());
        btnRegister.addActionListener(e -> registerToEvent());

        loadEvents();
        return panel;
    }

    private void loadEvents() {
        tableModel.setRowCount(0);
        List<Event> events = eventService.getActiveEvents();
        for (Event e : events) {
            int registered = eventService.countRegistered(e.getEventId());
            int spotsLeft  = e.getCapacity() - registered;
            tableModel.addRow(new Object[]{
                    e.getEventId(), e.getEventName(), e.getCategory(),
                    e.getEventDate(), e.getStartTime(), e.getLocation(),
                    spotsLeft > 0 ? spotsLeft : "FULL",
                    e.getFee() == 0 ? "Free" : String.format("%.2f TL", e.getFee()),
                    e.getMinTier(), e.getStatus()
            });
        }
    }

    private void registerToEvent() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event first.");
            return;
        }
        int eventId = (int) tableModel.getValueAt(row, 0);

        Optional<Membership> msOpt = memberService.getActiveMembership(member.getMemberId());
        String tier   = msOpt.map(Membership::getTier).orElse("CLASSIC");
        String status = member.getStatus();

        try {
            String result = eventService.registerMember(member.getMemberId(), eventId, tier, status);
            if (result != null && result.startsWith("WAITLISTED:")) {
                String pos = result.split(":")[1];
                JOptionPane.showMessageDialog(this,
                        "Event is full. You have been added to the waitlist (position " + pos + ").",
                        "Waitlist", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Successfully registered for the event!");
            }
            loadEvents();
            refreshMyRegistrations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── MY REGISTRATIONS ─────────────────────────────────────────
    private JPanel myRegPanel;
    private DefaultTableModel myRegModel;

    private JPanel buildMyRegistrationsPanel() {
        myRegPanel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCancel  = new JButton("Cancel Registration");
        btnCancel.setBackground(new Color(150, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setBorderPainted(false);
        toolbar.add(btnRefresh);
        toolbar.add(btnCancel);
        myRegPanel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Reg ID", "Event Name", "Date", "Category", "Payment", "Waitlist"};
        myRegModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable myRegTable = new JTable(myRegModel);
        myRegTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myRegPanel.add(new JScrollPane(myRegTable), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refreshMyRegistrations());
        btnCancel.addActionListener(e -> {
            int row = myRegTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a registration first.");
                return;
            }
            int regId   = (int) myRegModel.getValueAt(row, 0);
            // find eventId from registrations
            List<EventRegistration> regs = eventService.getRegistrationsByMember(member.getMemberId());
            regs.stream()
                    .filter(r -> r.getRegistrationId() == regId)
                    .findFirst()
                    .ifPresent(r -> {
                        try {
                            eventService.cancelRegistration(member.getMemberId(), r.getEventId());
                            JOptionPane.showMessageDialog(this, "Registration cancelled.");
                            refreshMyRegistrations();
                            loadEvents();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
        });

        refreshMyRegistrations();
        return myRegPanel;
    }

    private void refreshMyRegistrations() {
        myRegModel.setRowCount(0);
        List<EventRegistration> regs = eventService.getRegistrationsByMember(member.getMemberId());
        for (EventRegistration r : regs) {
            eventService.getEventById(r.getEventId()).ifPresent(e -> {
                myRegModel.addRow(new Object[]{
                        r.getRegistrationId(), e.getEventName(), e.getEventDate(),
                        e.getCategory(), r.getPaymentStatus(),
                        r.getWaitlistPosition() != null ? "#" + r.getWaitlistPosition() : "Confirmed"
                });
            });
        }
    }
}