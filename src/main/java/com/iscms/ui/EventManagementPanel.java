package com.iscms.ui;

import com.iscms.model.Event;
import com.iscms.model.Manager;
import com.iscms.service.EventFactory;
import com.iscms.service.EventService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EventManagementPanel extends JPanel {

    private final Manager manager;
    private final EventService eventService = new EventService();

    private JTable eventTable;
    private DefaultTableModel tableModel;

    public EventManagementPanel(Manager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        initUI();
        loadEvents();
    }

    private void initUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCreate  = new JButton("Create Event");
        JButton btnCancel  = new JButton("Cancel Event");
        JButton btnDetail  = new JButton("View Registrations");

        btnCreate.setBackground(new Color(33, 87, 141));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setOpaque(true);
        btnCreate.setBorderPainted(false);

        btnCancel.setBackground(new Color(150, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setBorderPainted(false);

        toolbar.add(btnRefresh);
        toolbar.add(btnCreate);
        toolbar.add(btnCancel);
        toolbar.add(btnDetail);
        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Category", "Date", "Start", "End",
                "Location", "Capacity", "Registered", "Fee", "Min Tier", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadEvents());
        btnCreate.addActionListener(e -> showCreateDialog());
        btnCancel.addActionListener(e -> cancelSelectedEvent());
        btnDetail.addActionListener(e -> showRegistrations());
    }

    private void loadEvents() {
        tableModel.setRowCount(0);
        List<Event> events = eventService.getAllEvents();
        for (Event e : events) {
            int registered = eventService.countRegistered(e.getEventId());
            tableModel.addRow(new Object[]{
                    e.getEventId(), e.getEventName(), e.getCategory(),
                    e.getEventDate(), e.getStartTime(), e.getEndTime(),
                    e.getLocation(), e.getCapacity(), registered,
                    String.format("%.2f TL", e.getFee()),
                    e.getMinTier(), e.getStatus()
            });
        }
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Create Event", true);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JTextField txtName     = new JTextField(20);
        JComboBox<String> cbCat = new JComboBox<>(new String[]{
                "FITNESS", "YOGA", "SWIMMING", "HIIT", "WORKSHOP", "VIP_ONLY", "OTHER"});
        JTextField txtDate     = new JTextField("YYYY-MM-DD");
        JTextField txtStart    = new JTextField("HH:MM");
        JTextField txtEnd      = new JTextField("HH:MM");
        JTextField txtLocation = new JTextField(20);
        JTextField txtCapacity = new JTextField("50");
        JTextField txtFee      = new JTextField("0");
        JComboBox<String> cbTier = new JComboBox<>(new String[]{"CLASSIC", "GOLD", "VIP"});
        JTextArea  txtDesc     = new JTextArea(3, 20);

        String[] labels = {"Event Name *", "Category *", "Date * (YYYY-MM-DD)",
                "Start Time * (HH:MM)", "End Time * (HH:MM)", "Location",
                "Capacity *", "Fee (TL)", "Min Tier *", "Description"};
        Component[] inputs = {txtName, cbCat, txtDate, txtStart, txtEnd,
                txtLocation, txtCapacity, txtFee, cbTier,
                new JScrollPane(txtDesc)};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Create");
        btnSave.setBackground(new Color(33, 87, 141));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnSave, c);

        dialog.add(new JScrollPane(panel));

        btnSave.addActionListener(e -> {
            try {
                double fee = Double.parseDouble(txtFee.getText().trim());
                String minTier = (String) cbTier.getSelectedItem();
                LocalDate date = LocalDate.parse(txtDate.getText().trim());
                LocalTime start = LocalTime.parse(txtStart.getText().trim());
                LocalTime end   = LocalTime.parse(txtEnd.getText().trim());

                // EventFactory pattern
                Event event;
                if (fee == 0) {
                    event = EventFactory.createFreeEvent(
                            txtName.getText().trim(),
                            (String) cbCat.getSelectedItem(),
                            date, start, end,
                            txtLocation.getText().trim(),
                            Integer.parseInt(txtCapacity.getText().trim()),
                            minTier,
                            manager.getManagerId());
                } else {
                    event = EventFactory.createPaidEvent(
                            txtName.getText().trim(),
                            (String) cbCat.getSelectedItem(),
                            date, start, end,
                            txtLocation.getText().trim(),
                            Integer.parseInt(txtCapacity.getText().trim()),
                            fee, minTier,
                            manager.getManagerId());
                }
                event.setDescription(txtDesc.getText().trim());

                eventService.createEvent(event);
                JOptionPane.showMessageDialog(dialog, "Event created successfully!");
                dialog.dispose();
                loadEvents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void cancelSelectedEvent() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event first.");
            return;
        }
        int eventId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel event '" + name + "'? All registrations will be deleted.",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            eventService.cancelEvent(eventId);
            loadEvents();
        }
    }

    private void showRegistrations() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event first.");
            return;
        }
        int eventId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        var regs = eventService.getRegistrationsByEvent(eventId);

        String[] cols = {"Registration ID", "Member ID", "Date", "Payment", "Waitlist"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (var r : regs) {
            model.addRow(new Object[]{
                    r.getRegistrationId(), r.getMemberId(),
                    r.getRegistrationDate() != null ? r.getRegistrationDate().toLocalDate() : "-",
                    r.getPaymentStatus(),
                    r.getWaitlistPosition() != null ? "#" + r.getWaitlistPosition() : "Registered"
            });
        }

        JTable table = new JTable(model);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Registrations — " + name, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }
}