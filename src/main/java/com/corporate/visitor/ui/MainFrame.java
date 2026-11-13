package com.corporate.visitor.ui;

import com.corporate.visitor.model.Visit;
import com.corporate.visitor.repository.VisitRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class MainFrame extends JFrame {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final VisitRepository repository = new VisitRepository();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Visitor", "Company", "Host", "Checked in", "Allowed", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public MainFrame() {
        super("Corporate Visitor Management");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(950, 560);
        setLocationRelativeTo(null);
        buildUi();
        refreshVisits();
        new Timer(30_000, event -> {
            refreshVisits();
            showOverdueReminders();
        }).start();
    }

    private void buildUi() {
        JButton checkIn = new JButton("Check in visitor");
        checkIn.addActionListener(event -> showCheckInDialog());
        JButton checkOut = new JButton("Check out selected");
        checkOut.addActionListener(event -> checkOutSelected());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(event -> refreshVisits());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(checkIn);
        actions.add(checkOut);
        actions.add(refresh);
        add(actions, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Reminder checks run every 30 seconds; overdue visits are highlighted in the status column."),
                BorderLayout.SOUTH);
    }

    private void showCheckInDialog() {
        JTextField visitor = new JTextField();
        JTextField company = new JTextField();
        JTextField host = new JTextField();
        JTextField phone = new JTextField();
        JSpinner duration = new JSpinner(new SpinnerNumberModel(60, 1, 1440, 1));
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Visitor name *")); panel.add(visitor);
        panel.add(new JLabel("Company *")); panel.add(company);
        panel.add(new JLabel("Host employee *")); panel.add(host);
        panel.add(new JLabel("Phone")); panel.add(phone);
        panel.add(new JLabel("Allowed minutes *")); panel.add(duration);

        int result = JOptionPane.showConfirmDialog(this, panel, "Check in visitor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (visitor.getText().isBlank() || company.getText().isBlank() || host.getText().isBlank()) {
            showError("Visitor name, company, and host employee are required.");
            return;
        }
        try {
            repository.checkIn(visitor.getText().trim(), company.getText().trim(), host.getText().trim(),
                    phone.getText().trim(), (Integer) duration.getValue());
            refreshVisits();
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void checkOutSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select an active visitor first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        long visitId = (Long) tableModel.getValueAt(modelRow, 0);
        try {
            repository.checkOut(visitId);
            refreshVisits();
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void refreshVisits() {
        try {
            List<Visit> visits = repository.findActive();
            tableModel.setRowCount(0);
            LocalDateTime now = LocalDateTime.now();
            for (Visit visit : visits) {
                tableModel.addRow(new Object[]{visit.id(), visit.visitorName(), visit.company(), visit.hostName(),
                        TIME.format(visit.checkedInAt()), visit.allowedMinutes() + " min",
                        visit.isOverdue(now) ? "OVERDUE (" + visit.overdueMinutes(now) + " min)" : "Active"});
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void showOverdueReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            for (Visit visit : repository.findActive()) {
                if (visit.isOverdue(now) && !visit.reminderSent()) {
                    repository.markReminderSent(visit.id());
                    JOptionPane.showMessageDialog(this,
                            visit.visitorName() + " (" + visit.company() + ") has exceeded the allowed "
                                    + visit.allowedMinutes() + " minutes by " + visit.overdueMinutes(now)
                                    + " minutes.\nPlease check out the visitor.",
                            "Checkout reminder", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void showDatabaseError(SQLException exception) {
        showError("Database error: " + exception.getMessage()
                + "\nCheck MySQL is running and db.properties is configured.");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Visitor management", JOptionPane.ERROR_MESSAGE);
    }
}
