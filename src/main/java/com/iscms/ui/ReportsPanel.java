package com.iscms.ui;

import com.iscms.model.Member;
import com.iscms.model.Membership;
import com.iscms.model.Payment;
import com.iscms.service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    public ReportsPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Active Members",    buildActiveMembersReport());
        tabs.addTab("Expiring Soon",     buildExpiringSoonReport());
        tabs.addTab("BMI Distribution",  buildBmiReport());
        tabs.addTab("Monthly Revenue",   buildRevenueReport());
        tabs.addTab("Overdue Payments",  buildOverdueReport());
        tabs.addTab("Archived Members",   buildArchivedMembersReport());
        tabs.addTab("Anonymized (KVKK)",  buildAnonymizedReport());
        add(tabs, BorderLayout.CENTER);
    }
    // ── ARCHIVED MEMBERS ──────────────────────────────────────────
    private JPanel buildArchivedMembersReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnRefresh = new JButton("Refresh");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Phone", "Email", "Archived At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblCount, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            List<Member> archived = reportService.getArchivedMembers();
            for (Member m : archived) {
                model.addRow(new Object[]{
                        m.getMemberId(), m.getFullName(), m.getPhone(),
                        m.getEmail() != null ? m.getEmail() : "-",
                        m.getArchivedAt() != null ? m.getArchivedAt().toLocalDate() : "-"
                });
            }
            lblCount.setText("Total Archived: " + archived.size());
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return panel;
    }

    // ── ANONYMIZED (KVKK) ─────────────────────────────────────────
    private JPanel buildAnonymizedReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnRefresh = new JButton("Refresh");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(btnRefresh);

        JLabel lblInfo = new JLabel(
                "  ⚠ These members' personal data has been anonymized per KVKK Article 7 (2+ years after archiving).");
        lblInfo.setForeground(new Color(150, 50, 50));
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        toolbar.add(lblInfo);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Status", "Archived At", "Payment Records"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblCount, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                // Use ReportService — no SQL in UI layer
                List<Member> archived = reportService.getArchivedMembers().stream()
                        .filter(m -> "[DELETED]".equals(m.getFullName()))
                        .collect(java.util.stream.Collectors.toList());
                for (Member m : archived) {
                    List<Payment> payments = reportService.getPaymentsForMember(m.getMemberId());
                    model.addRow(new Object[]{
                            m.getMemberId(),
                            m.getStatus(),
                            m.getArchivedAt() != null ? m.getArchivedAt().toLocalDate() : "-",
                            payments.size() + " record(s)"
                    });
                }
                lblCount.setText("Total Anonymized: " + archived.size());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(panel, "Error: " + e.getMessage());
            }
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return panel;
    }
    // ── ACTIVE MEMBERS ────────────────────────────────────────────
    private JPanel buildActiveMembersReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cbTier = new JComboBox<>(new String[]{"ALL", "CLASSIC", "GOLD", "VIP"});
        JButton btnRefresh = new JButton("Refresh");
        toolbar.add(new JLabel("Filter by Tier:"));
        toolbar.add(cbTier);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Phone", "Tier", "Package", "End Date", "Days Left"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblCount, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            String tier = (String) cbTier.getSelectedItem();
            List<Member> members = reportService.getActiveMembers();
            int count = 0;
            for (Member m : members) {
                Optional<Membership> msOpt = reportService.getActiveMembership(m.getMemberId());
                if (msOpt.isEmpty()) continue;
                Membership ms = msOpt.get();
                if (!"ALL".equals(tier) && !ms.getTier().equals(tier)) continue;
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), ms.getEndDate());
                model.addRow(new Object[]{
                        m.getMemberId(), m.getFullName(), m.getPhone(),
                        ms.getTier(), ms.getPackageType(),
                        ms.getEndDate(), daysLeft + " days"
                });
                count++;
            }
            lblCount.setText("Total Active Members: " + count);
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cbTier.addActionListener(e -> load.run());
        return panel;
    }

    // ── EXPIRING SOON ─────────────────────────────────────────────
    private JPanel buildExpiringSoonReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cbDays = new JComboBox<>(new String[]{"7 days", "30 days", "60 days"});
        JButton btnRefresh = new JButton("Refresh");
        toolbar.add(new JLabel("Expiring within:"));
        toolbar.add(cbDays);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Phone", "Tier", "End Date", "Days Left"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblCount, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            String sel = (String) cbDays.getSelectedItem();
            int days = sel.startsWith("7") ? 7 : sel.startsWith("30") ? 30 : 60;
            List<Membership> expiring = reportService.getExpiringSoon(days);
            int count = 0;
            for (Membership ms : expiring) {
                reportService.getAllMembers().stream().filter(m2 -> m2.getMemberId() == ms.getMemberId()).findFirst().ifPresent(m -> {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), ms.getEndDate());
                    model.addRow(new Object[]{
                            m.getMemberId(), m.getFullName(), m.getPhone(),
                            ms.getTier(), ms.getEndDate(), daysLeft + " days"
                    });
                });
                count++;
            }
            lblCount.setText("Total: " + count + " member(s) expiring within " + days + " days");
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cbDays.addActionListener(e -> load.run());
        return panel;
    }

    // ── BMI DISTRIBUTION ──────────────────────────────────────────
    private JPanel buildBmiReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnRefresh = new JButton("Refresh");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        // Summary table
        String[] sumCols = {"BMI Category", "Member Count", "Percentage", "Upgrade Potential"};
        DefaultTableModel sumModel = new DefaultTableModel(sumCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable sumTable = new JTable(sumModel);
        sumTable.setRowHeight(28);

        // Detail table
        String[] detCols = {"ID", "Full Name", "Tier", "BMI", "Category"};
        DefaultTableModel detModel = new DefaultTableModel(detCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable detTable = new JTable(detModel);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(sumTable), new JScrollPane(detTable));
        split.setDividerLocation(160);
        panel.add(split, BorderLayout.CENTER);

        Runnable load = () -> {
            sumModel.setRowCount(0);
            detModel.setRowCount(0);

            List<Member> activeMembers = reportService.getActiveMembers();
            Map<String, Integer> counts = new LinkedHashMap<>();
            counts.put("UNDERWEIGHT", 0);
            counts.put("NORMAL", 0);
            counts.put("OVERWEIGHT", 0);
            counts.put("OBESE", 0);
            counts.put("NO DATA", 0);

            int total = 0;
            for (Member m : activeMembers) {
                total++;
                Optional<Membership> msOpt = reportService.getActiveMembership(m.getMemberId());
                String tier = msOpt.map(Membership::getTier).orElse("-");

                String cat = m.getBmiCategory() != null ? m.getBmiCategory() : "NO DATA";
                counts.merge(cat, 1, Integer::sum);

                detModel.addRow(new Object[]{
                        m.getMemberId(), m.getFullName(), tier,
                        m.getBmiValue() != null ? m.getBmiValue() : "N/A", cat
                });
            }

            final int finalTotal = total;
            counts.forEach((cat, cnt) -> {
                double pct = finalTotal > 0 ? (cnt * 100.0 / finalTotal) : 0;
                // Upgrade potential: OVERWEIGHT/OBESE classic members
                String potential = (cat.equals("OVERWEIGHT") || cat.equals("OBESE")) ? cnt + " members" : "-";
                sumModel.addRow(new Object[]{
                        cat, cnt, String.format("%.1f%%", pct), potential
                });
            });
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return panel;
    }

    // ── MONTHLY REVENUE ───────────────────────────────────────────
    private JPanel buildRevenueReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnRefresh = new JButton("Refresh");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Month", "Membership", "Installment", "Event", "Upgrade", "Manual Cash", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JLabel lblTotal = new JLabel("Grand Total: 0.00 TL");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotal.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            List<Payment> allPayments = reportService.getAllPayments().stream()
                    .filter(p -> "PAID".equals(p.getStatus()))
                    .collect(Collectors.toList());

            // Group by month
            Map<String, Map<String, Double>> byMonth = new TreeMap<>(Collections.reverseOrder());
            for (Payment p : allPayments) {
                String month = p.getPaymentDate().getYear() + "-"
                        + String.format("%02d", p.getPaymentDate().getMonthValue());
                byMonth.computeIfAbsent(month, k -> new HashMap<>());
                byMonth.get(month).merge(p.getPaymentType(), p.getAmount(), Double::sum);
            }

            double grandTotal = 0;
            for (Map.Entry<String, Map<String, Double>> entry : byMonth.entrySet()) {
                Map<String, Double> types = entry.getValue();
                double membership   = types.getOrDefault("MEMBERSHIP",    0.0);
                double installment  = types.getOrDefault("INSTALLMENT",   0.0);
                double event        = types.getOrDefault("EVENT",         0.0);
                double upgrade      = types.getOrDefault("UPGRADE",       0.0);
                double manual       = types.getOrDefault("MANUAL_CASH",   0.0);
                double total        = membership + installment + event + upgrade + manual;
                grandTotal         += total;
                model.addRow(new Object[]{
                        entry.getKey(),
                        String.format("%.2f", membership),
                        String.format("%.2f", installment),
                        String.format("%.2f", event),
                        String.format("%.2f", upgrade),
                        String.format("%.2f", manual),
                        String.format("%.2f TL", total)
                });
            }
            lblTotal.setText("Grand Total: " + String.format("%.2f TL", grandTotal));
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return panel;
    }

    // ── OVERDUE PAYMENTS ──────────────────────────────────────────
    private JPanel buildOverdueReport() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnRefresh = new JButton("Refresh");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Payment ID", "Member ID", "Member Name", "Amount", "Type", "Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblCount, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            List<Payment> overdue = reportService.getOverduePayments();
            List<Payment> pending = reportService.getAllPayments().stream().filter(p -> "PENDING".equals(p.getStatus())).collect(java.util.stream.Collectors.toList());
            List<Payment> all = new ArrayList<>();
            all.addAll(overdue);
            all.addAll(pending);

            for (Payment p : all) {
                String memberName = reportService.getAllMembers().stream()
                        .filter(m2 -> m2.getMemberId() == p.getMemberId())
                        .map(Member::getFullName).findFirst().orElse("Unknown");
                model.addRow(new Object[]{
                        p.getPaymentId(), p.getMemberId(), memberName,
                        String.format("%.2f TL", p.getAmount()),
                        p.getPaymentType(),
                        p.getPaymentDate().toLocalDate(),
                        p.getStatus()
                });
            }
            lblCount.setText("Total overdue/pending: " + all.size());
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return panel;
    }
}