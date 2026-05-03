package edu.psu.view;

import edu.psu.behavior.AbsTicketState;
import edu.psu.core.*;
import edu.psu.processing.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.time.Duration;
import java.util.List;

/**
 * Main GUI for the Incident Management System.
 * Layout:
 *   Top : ticket creation + flood test
 *   Left : incident/ticket hierarchy
 *   Middle : Ticket info
 *   Right : Features
 */
public class TicketGUI extends JFrame {

    private final SoftwareTicketBuilder  swBuilder  = new SoftwareTicketBuilder();
    private final HardwareTicketBuilder  hwBuilder  = new HardwareTicketBuilder();
    private final TicketScheduler        scheduler  = new TicketScheduler();

    private JTextField titleField;
    private JTextArea  descField;
    private JTextField priorityField;
    private JComboBox<String> typeCombo;

    private JPanel     hwFieldsPanel;
    private JTextField hwSerialField;
    private JTextField hwMakeModelField;
    private JTextField hwLocationField;
    private JTextField hwFailureTypeField;
    private JCheckBox  hwWarrantyCheck;

    private JTree     incidentTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode treeRoot;

    private JTextArea detailArea;

    private JButton assignBtn;
    private JButton activeBtn;
    private JButton resolveBtn;
    private JButton closeBtn;
    private JButton reopenBtn;
    private JButton escalateBtn;
    private JButton requestBtn;
    private JButton manageIncidentBtn;

    private JCheckBox auditCheck;
    private JCheckBox slaCheck;
    private JCheckBox boostCheck;

    private JButton toggleAuditBtn;
    private JButton toggleSlaBtn;
    private JButton toggleBoostBtn;

    private JTextArea decoratorInfoArea;

    public TicketGUI() {
        IncidentRegistry.syncStorage();

        setTitle("Incident Management System");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(4, 4));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildEastPanel(),   BorderLayout.EAST);

        refreshTree();
        updateButtonStates(null);
        setLocationRelativeTo(null);
    }

    // Panels
    private JPanel buildTopPanel() {
        JPanel outer = new JPanel(new BorderLayout(4, 4));
        outer.setBorder(BorderFactory.createTitledBorder("Manual Ticket Creation"));

        JPanel commonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        typeCombo     = new JComboBox<>(new String[]{"Software", "Hardware"});
        titleField    = new JTextField(12);
        descField     = new JTextArea(2, 20);
        descField.setLineWrap(true);
        priorityField = new JTextField(3);

        JButton createBtn = new JButton("Create Ticket");
        JButton floodBtn  = new JButton("Run Flood Test");
        createBtn.addActionListener(e -> handleManualCreation());
        floodBtn .addActionListener(e -> runFloodTest());

        //Show/hide hardware fields when type changes
        typeCombo.addActionListener(e -> {
            boolean hw = "Hardware".equals(typeCombo.getSelectedItem());
            hwFieldsPanel.setVisible(hw);
            outer.revalidate();
            outer.repaint();
        });

        commonRow.add(new JLabel("Type:"));
        commonRow.add(typeCombo);
        commonRow.add(new JLabel("Title:"));
        commonRow.add(titleField);
        commonRow.add(new JLabel("Description:"));
        commonRow.add(new JScrollPane(descField));
        commonRow.add(new JLabel("Priority:"));
        commonRow.add(priorityField);
        commonRow.add(createBtn);
        commonRow.add(new JSeparator(SwingConstants.VERTICAL));
        commonRow.add(floodBtn);

        //hardware modifier section
        hwFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        hwFieldsPanel.setBorder(BorderFactory.createTitledBorder("Hardware Details"));
        hwSerialField      = new JTextField(10);
        hwMakeModelField   = new JTextField(12);
        hwLocationField    = new JTextField(10);
        hwFailureTypeField = new JTextField(12);
        hwWarrantyCheck    = new JCheckBox("Under Warranty");

        hwFieldsPanel.add(new JLabel("Serial #:"));
        hwFieldsPanel.add(hwSerialField);
        hwFieldsPanel.add(new JLabel("Make/Model:"));
        hwFieldsPanel.add(hwMakeModelField);
        hwFieldsPanel.add(new JLabel("Location:"));
        hwFieldsPanel.add(hwLocationField);
        hwFieldsPanel.add(new JLabel("Failure Type:"));
        hwFieldsPanel.add(hwFailureTypeField);
        hwFieldsPanel.add(hwWarrantyCheck);
        hwFieldsPanel.setVisible(false); // hidden by default (Software selected)

        outer.add(commonRow,    BorderLayout.NORTH);
        outer.add(hwFieldsPanel, BorderLayout.SOUTH);
        return outer;
    }

    private JScrollPane buildLeftPanel() {
        treeRoot  = new DefaultMutableTreeNode("System");
        treeModel = new DefaultTreeModel(treeRoot);
        incidentTree = new JTree(treeModel);
        incidentTree.setRootVisible(true);
        incidentTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        incidentTree.addTreeSelectionListener(e -> onSelectionChanged());

        JScrollPane scroll = new JScrollPane(incidentTree);
        scroll.setPreferredSize(new Dimension(270, 0));
        scroll.setBorder(BorderFactory.createTitledBorder("Incidents & Tickets"));
        return scroll;
    }

    private JPanel buildCenterPanel() {
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailScroll = new JScrollPane(detailArea);

        JPanel btnPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        btnPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        assignBtn         = new JButton("Assign");
        activeBtn         = new JButton("Set Active");
        resolveBtn        = new JButton("Resolve");
        closeBtn          = new JButton("Close");
        reopenBtn         = new JButton("Re-Open");
        escalateBtn       = new JButton("Escalate");
        requestBtn        = new JButton("Request Info");
        manageIncidentBtn = new JButton("Manage Incident");

        assignBtn        .addActionListener(e -> handleAssign());
        activeBtn        .addActionListener(e -> handleSetActive());
        resolveBtn       .addActionListener(e -> handleResolve());
        closeBtn         .addActionListener(e -> handleClose());
        reopenBtn        .addActionListener(e -> handleReopen());
        escalateBtn      .addActionListener(e -> handleEscalate());
        requestBtn       .addActionListener(e -> handleRequestInfo());
        manageIncidentBtn.addActionListener(e -> handleManageIncident());

        btnPanel.add(assignBtn);
        btnPanel.add(activeBtn);
        btnPanel.add(resolveBtn);
        btnPanel.add(closeBtn);
        btnPanel.add(reopenBtn);
        btnPanel.add(escalateBtn);
        btnPanel.add(requestBtn);
        btnPanel.add(manageIncidentBtn);

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.add(detailScroll, BorderLayout.CENTER);
        center.add(btnPanel,     BorderLayout.SOUTH);
        return center;
    }

    private JPanel buildEastPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 6));
        panel.setPreferredSize(new Dimension(230, 0));

        JPanel checkPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        checkPanel.setBorder(BorderFactory.createTitledBorder("Enable on Creation"));
        auditCheck = new JCheckBox("Audit Logging");
        slaCheck   = new JCheckBox("SLA Monitoring (24h)");
        boostCheck = new JCheckBox("Priority Boost (+1)");
        checkPanel.add(auditCheck);
        checkPanel.add(slaCheck);
        checkPanel.add(boostCheck);

        // ── Post-creation toggles ─────────────────────────────────────────────
        JPanel togglePanel = new JPanel(new GridLayout(3, 1, 4, 4));
        togglePanel.setBorder(BorderFactory.createTitledBorder("Toggle on Selected Ticket"));
        toggleAuditBtn = new JButton("Toggle Audit Log");
        toggleSlaBtn   = new JButton("Toggle SLA Monitor");
        toggleBoostBtn = new JButton("Toggle Priority Boost");
        toggleAuditBtn.addActionListener(e -> handleToggleDecorator("audit"));
        toggleSlaBtn  .addActionListener(e -> handleToggleDecorator("sla"));
        toggleBoostBtn.addActionListener(e -> handleToggleDecorator("boost"));
        togglePanel.add(toggleAuditBtn);
        togglePanel.add(toggleSlaBtn);
        togglePanel.add(toggleBoostBtn);

        // ── Feature info read-out ─────────────────────────────────────────────
        decoratorInfoArea = new JTextArea();
        decoratorInfoArea.setEditable(false);
        decoratorInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        decoratorInfoArea.setText("Select a ticket to view\nfeature details.");
        JScrollPane infoScroll = new JScrollPane(decoratorInfoArea);
        infoScroll.setBorder(BorderFactory.createTitledBorder("Feature Info"));

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(checkPanel,  BorderLayout.NORTH);
        topPanel.add(togglePanel, BorderLayout.SOUTH);

        panel.add(topPanel,   BorderLayout.NORTH);
        panel.add(infoScroll, BorderLayout.CENTER);
        return panel;
    }

    private void onSelectionChanged() {
        Object selected = getSelectedObject();
        updateButtonStates(selected);
        updateDetailView(selected);
        updateDecoratorInfo(selected);
        updateToggleButtonLabels(selected);
    }

    private void updateButtonStates(Object selected) {
        boolean isTicket   = (selected instanceof TicketComponentIF)
                              && !(selected instanceof IncidentComposite);
        boolean isAnything = (selected != null);

        assignBtn.setEnabled(false);
        activeBtn.setEnabled(false);
        resolveBtn.setEnabled(false);
        closeBtn.setEnabled(false);
        reopenBtn.setEnabled(false);
        escalateBtn.setEnabled(false);
        requestBtn.setEnabled(false);
        manageIncidentBtn.setEnabled(isAnything);
        toggleAuditBtn.setEnabled(isTicket);
        toggleSlaBtn  .setEnabled(isTicket);
        toggleBoostBtn.setEnabled(isTicket);

        if (!isTicket) return;

        TicketComponentIF ticket = (TicketComponentIF) selected;
        AbsTicketState state = ticket.getState();
        if (state == null) return;

        assignBtn  .setEnabled(isLegalTransition(state, AbsTicketState.ASSIGN_EVT));
        activeBtn  .setEnabled(isLegalTransition(state, AbsTicketState.ACTIVATE_EVT));
        resolveBtn .setEnabled(isLegalTransition(state, AbsTicketState.RESOLVE_EVT));
        closeBtn   .setEnabled(isLegalTransition(state, AbsTicketState.CLOSE_EVT));
        reopenBtn  .setEnabled(isLegalTransition(state, AbsTicketState.REOPEN_EVT));
        escalateBtn.setEnabled(isLegalTransition(state, AbsTicketState.ESCALATE_EVT));
        requestBtn .setEnabled(isLegalTransition(state, AbsTicketState.PENDING_EVT));
    }

    private boolean isLegalTransition(AbsTicketState state, int event) {
        if (state == null) return false;
        return state.validateTransition(event);
    }

    /** Reflect the current decorator state in the toggle button labels. */
    private void updateToggleButtonLabels(Object selected) {
        if (!(selected instanceof TicketComponentIF t)
                || selected instanceof IncidentComposite) {
            toggleAuditBtn.setText("Toggle Audit Log");
            toggleSlaBtn  .setText("Toggle SLA Monitor");
            toggleBoostBtn.setText("Toggle Priority Boost");
            return;
        }
        boolean hasAudit = hasDecorator(t, AuditLogger.class);
        boolean hasSLA   = hasDecorator(t, SLAMonitor.class);
        boolean hasBoost = hasDecorator(t, PriorityBooster.class);

        toggleAuditBtn.setText((hasAudit ? "ON " : "OFF ") + ": Audit Log");
        toggleSlaBtn  .setText((hasSLA   ? "ON " : "OFF ") + ": SLA Monitor");
        toggleBoostBtn.setText((hasBoost ? "ON " : "OFF ") + ": Priority Boost");
    }

    //Actions
    private void handleManualCreation() {
        String title = titleField.getText().trim();
        String desc  = descField.getText().trim();
        String prStr = priorityField.getText().trim();

        if (title.isEmpty()) { showError("Title cannot be empty."); return; }
        if (desc.isEmpty())  { showError("Description cannot be empty."); return; }
        int priority;
        try {
            priority = Integer.parseInt(prStr);
            if (priority < 1 || priority > 10) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Priority must be a number between 1 and 10.");
            return;
        }

        boolean isHardware = "Hardware".equals(typeCombo.getSelectedItem());
        TicketComponentIF ticket;

        if (isHardware) {
            hwBuilder.reset();
            hwBuilder.setBasics(title, desc);
            hwBuilder.setMetaData("Hardware", priority);
            hwBuilder.setHardwareDetails(
                hwSerialField.getText().trim(),
                hwMakeModelField.getText().trim(),
                hwLocationField.getText().trim(),
                hwFailureTypeField.getText().trim(),
                hwWarrantyCheck.isSelected()
            );
            ticket = hwBuilder.getProduct();
            // Clear hardware fields
            hwSerialField.setText("");
            hwMakeModelField.setText("");
            hwLocationField.setText("");
            hwFailureTypeField.setText("");
            hwWarrantyCheck.setSelected(false);
        } else {
            swBuilder.reset();
            swBuilder.setBasics(title, desc);
            swBuilder.setMetaData("Software", priority);
            ticket = swBuilder.getProduct();
        }

        // Apply creation-time decorators
        if (auditCheck.isSelected()) {
            ticket = new AuditLogger(ticket);
            ticket.addLog("AuditLogger applied at creation");
        }
        if (slaCheck.isSelected()) {
            ticket = new SLAMonitor(ticket, Duration.ofHours(24));
            ticket.addLog("SLAMonitor applied (24h window)");
        }
        if (boostCheck.isSelected()) {
            PriorityBooster pb = new PriorityBooster(ticket);
            pb.boost();
            ticket = pb;
            ticket.addLog("PriorityBooster applied: priority now " + ticket.getPriority());
        }

        titleField.setText("");
        descField.setText("");
        priorityField.setText("");
        refreshTree();
        selectTicketInTree(ticket);
    }

    //feature toggler
    private void handleToggleDecorator(String type) {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;

        boolean hasIt;
        TicketComponentIF updated;

        switch (type) {
            case "audit" -> {
                hasIt = hasDecorator(ticket, AuditLogger.class);
                if (hasIt) {
                    updated = removeDecorator(ticket, AuditLogger.class);
                    updated.addLog("AuditLogger removed");
                } else {
                    updated = new AuditLogger(ticket);
                    updated.addLog("AuditLogger added post-creation");
                }
            }
            case "sla" -> {
                hasIt = hasDecorator(ticket, SLAMonitor.class);
                if (hasIt) {
                    updated = removeDecorator(ticket, SLAMonitor.class);
                    updated.addLog("SLAMonitor removed");
                } else {
                    updated = new SLAMonitor(ticket, Duration.ofHours(24));
                    updated.addLog("SLAMonitor added post-creation (24h window)");
                }
            }
            case "boost" -> {
                hasIt = hasDecorator(ticket, PriorityBooster.class);
                if (hasIt) {
                    updated = removeDecorator(ticket, PriorityBooster.class);
                    updated.addLog("PriorityBooster removed");
                } else {
                    PriorityBooster pb = new PriorityBooster(ticket);
                    pb.boost();
                    updated = pb;
                    updated.addLog("PriorityBooster added: priority now " + updated.getPriority());
                }
            }
            default -> { return; }
        }

        IncidentRegistry.syncStorage();
        refreshAndReselect(updated);
    }

    //State handler
    private void handleAssign() {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;

        JTextField deptField = new JTextField(12);
        JTextField techField = new JTextField(12);
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Department:")); form.add(deptField);
        form.add(new JLabel("Technician:")); form.add(techField);

        int result = JOptionPane.showConfirmDialog(this, form, "Assign Ticket",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String dept = deptField.getText().trim();
        String tech = techField.getText().trim();
        if (dept.isEmpty() || tech.isEmpty()) {
            showError("Both department and technician are required.");
            return;
        }

        ticket.setAssignee(tech);
        if (ticket instanceof Ticket) ((Ticket) ticket).setDepartment(dept);
        ticket.addLog("Assigned to " + tech + " (Dept: " + dept + ")");
        ticket.processEvent(AbsTicketState.ASSIGN_EVT);
        refreshAndReselect(ticket);
    }

    private void handleSetActive()   { applyEvent(AbsTicketState.ACTIVATE_EVT, "Set to Active"); }
    private void handleResolve()     { applyEvent(AbsTicketState.RESOLVE_EVT,  "Marked as Resolved"); }
    private void handleClose()       { applyEvent(AbsTicketState.CLOSE_EVT,    "Ticket Closed"); }
    private void handleEscalate() {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;
        int newPriority = ticket.getPriority() + 1;
        ticket.setPriority(newPriority);
        ticket.addLog("Escalated — priority increased to " + newPriority);
        ticket.processEvent(AbsTicketState.ESCALATE_EVT);
        refreshAndReselect(ticket);
    }
    private void handleRequestInfo() { applyEvent(AbsTicketState.PENDING_EVT, "Info requested — ticket set to Pending"); }

    private void handleReopen() {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;
        String reason = JOptionPane.showInputDialog(this, "Reason for re-opening:");
        if (reason == null || reason.trim().isEmpty()) return;
        ticket.addLog("Re-opened: " + reason.trim());
        ticket.processEvent(AbsTicketState.REOPEN_EVT);
        refreshAndReselect(ticket);
    }

    private void applyEvent(int event, String logMsg) {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;
        ticket.addLog(logMsg);
        ticket.processEvent(event);
        refreshAndReselect(ticket);
    }

    private void handleManageIncident() {
        Object selected = getSelectedObject();
        if (selected == null) return;

        List<IncidentComposite> allIncidents = IncidentRegistry.getAllIncidents();
        String[] options = {"Add to Incident", "Remove from Incident", "Create New Incident", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "What would you like to do?", "Manage Incident",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 3 || choice < 0) return;

        if (choice == 0) {
            if (allIncidents.isEmpty()) { showError("No incidents exist. Create one first."); return; }
            IncidentComposite[] incArray = allIncidents.toArray(new IncidentComposite[0]);
            IncidentComposite target = (IncidentComposite) JOptionPane.showInputDialog(
                    this, "Choose incident to add to:", "Add to Incident",
                    JOptionPane.PLAIN_MESSAGE, null, incArray, incArray[0]);
            if (target == null) return;
            if (selected == target) { showError("An incident cannot be added to itself."); return; }
            if (selected instanceof TicketComponentIF) {
                target.addChild((TicketComponentIF) selected);
                IncidentRegistry.syncStorage();
                JOptionPane.showMessageDialog(this, "Added to incident: " + target.getTitle());
            }
        } else if (choice == 1) {
            if (selected instanceof TicketComponentIF ticket) {
                List<IncidentComposite> containing = IncidentRegistry.getContainingIncidents(ticket);
                if (containing.isEmpty()) { showError("This ticket is not inside any incident."); return; }
                IncidentComposite[] incArray = containing.toArray(new IncidentComposite[0]);
                IncidentComposite from = (IncidentComposite) JOptionPane.showInputDialog(
                        this, "Remove from which incident?", "Remove from Incident",
                        JOptionPane.PLAIN_MESSAGE, null, incArray, incArray[0]);
                if (from == null) return;
                from.remove(ticket);
                IncidentRegistry.syncStorage();
                JOptionPane.showMessageDialog(this, "Ticket removed from: " + from.getTitle());
            } else if (selected instanceof IncidentComposite) {
                showError("Removing incidents from parent incidents is not yet supported.");
            }
        } else if (choice == 2) {
            JTextField incTitle = new JTextField(16);
            JTextField incDesc  = new JTextField(24);
            JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
            form.add(new JLabel("Incident Title:")); form.add(incTitle);
            form.add(new JLabel("Description:"));    form.add(incDesc);
            int r = JOptionPane.showConfirmDialog(this, form, "Create New Incident",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION || incTitle.getText().trim().isEmpty()) return;
            IncidentRegistry.newIncident(incTitle.getText().trim(), incDesc.getText().trim());
        }

        refreshTree();
    }

    // Flood Test
    private void runFloodTest() {
        int count = 20;
        String input = JOptionPane.showInputDialog(this,
                "Number of tickets to flood (default 20):", "Flood Test", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try { count = Integer.parseInt(input.trim()); } catch (NumberFormatException ignored) {}
        }

        long startMs = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            swBuilder.reset();
            swBuilder.setBasics("Flood-" + (i + 1), "Stress test ticket #" + (i + 1));
            swBuilder.setMetaData("FloodTool", (i % 10) + 1);
            TicketComponentIF t = swBuilder.getProduct();
            scheduler.submit(t);
        }
        long elapsed = System.currentTimeMillis() - startMs;
        refreshTree();
        JOptionPane.showMessageDialog(this,
                "Flood Test Complete.\n" + count + " tickets created in " + elapsed + " ms.\n" +
                "Scheduler queue depth: " + scheduler.getPendingCount());
    }

    // Features
    private void updateDetailView(Object selected) {
        if (selected == null) {
            detailArea.setText("Select a ticket or incident to view details.");
            return;
        }
        if (selected instanceof TicketComponentIF t) {
            detailArea.setText(t.displayDetails());
        } else {
            detailArea.setText("Unknown selection type.");
        }
        detailArea.setCaretPosition(0);
    }

    private void updateDecoratorInfo(Object selected) {
        if (!(selected instanceof TicketComponentIF t)
                || selected instanceof IncidentComposite) {
            decoratorInfoArea.setText("");
            return;
        }

        boolean hasAudit = hasDecorator(t, AuditLogger.class);
        boolean hasSLA   = hasDecorator(t, SLAMonitor.class);
        boolean hasBoost = hasDecorator(t, PriorityBooster.class);

        StringBuilder sb = new StringBuilder();
        sb.append("Active features:\n");
        sb.append(hasAudit ? "Audit Logging : ON\n"  : "Audit Logging OFF\n");
        sb.append(hasBoost ? "Priority Boost : ON\n" : "Priority Boost : OFF\n");
        sb.append(hasSLA   ? "SLA Monitoring : ON\n" : "SLA Monitoring : OFF\n");

        if (hasSLA) {
            TicketComponentIF probe = t;
            while (probe instanceof AbsTicketDecorator) {
                if (probe instanceof SLAMonitor sla) {
                    sb.append("\nSLA Status: ")
                      .append(sla.checkSLAStatus() ? "VALID" : "EXPIRED")
                      .append("\n");
                    break;
                }
                probe = ((AbsTicketDecorator) probe).decoratedComponent;
            }
        }

        if (hasAudit) {
            List<String> logs = t.getActivityLog();
            sb.append("\nAudit log (").append(logs.size()).append(" entries):\n");
            for (String log : logs) sb.append("  ").append(log).append("\n");
        }

        decoratorInfoArea.setText(sb.toString());
        decoratorInfoArea.setCaretPosition(0);
    }

    private boolean hasDecorator(TicketComponentIF t, Class<?> cls) {
        TicketComponentIF cur = t;
        while (cur instanceof AbsTicketDecorator) {
            if (cls.isInstance(cur)) return true;
            cur = ((AbsTicketDecorator) cur).decoratedComponent;
        }
        return false;
    }

    private TicketComponentIF removeDecorator(TicketComponentIF t, Class<?> cls) {
        if (!(t instanceof AbsTicketDecorator dec)) return t; // base ticket, nothing to strip
        TicketComponentIF newInner = removeDecorator(dec.decoratedComponent, cls);

        if (cls.isInstance(t)) {
            IncidentRegistry.replaceInAllIncidents(t, newInner);
            return newInner;
        }
        dec.decoratedComponent = newInner;
        return dec;
    }

    //Tree for left panel
    private void refreshTree() {
        treeRoot.removeAllChildren();

        for (IncidentComposite incident : IncidentRegistry.getAllIncidents()) {
            DefaultMutableTreeNode incNode = new DefaultMutableTreeNode(incident);
            for (TicketComponentIF child : incident.getChildren()) {
                incNode.add(new DefaultMutableTreeNode(child));
            }
            treeRoot.add(incNode);
        }

        for (TicketComponentIF ticket : IncidentRegistry.getStandaloneTickets()) {
            treeRoot.add(new DefaultMutableTreeNode(ticket));
        }

        treeModel.reload();
        for (int i = 0; i < incidentTree.getRowCount(); i++) incidentTree.expandRow(i);
    }

    private void selectTicketInTree(TicketComponentIF target) {
        selectNode(treeRoot, target);
    }

    private boolean selectNode(DefaultMutableTreeNode node, Object target) {
        if (node.getUserObject() == target) {
            TreePath path = new TreePath(node.getPath());
            incidentTree.setSelectionPath(path);
            incidentTree.scrollPathToVisible(path);
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (selectNode((DefaultMutableTreeNode) node.getChildAt(i), target)) return true;
        }
        return false;
    }

    private void refreshAndReselect(TicketComponentIF ticket) {
        refreshTree();
        selectTicketInTree(ticket);
        updateDetailView(ticket);
        updateButtonStates(ticket);
        updateDecoratorInfo(ticket);
        updateToggleButtonLabels(ticket);
    }

    // Helpers
    private Object getSelectedObject() {
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) incidentTree.getLastSelectedPathComponent();
        if (node == null || node == treeRoot) return null;
        return node.getUserObject();
    }

    private TicketComponentIF getSelectedTicket() {
        Object obj = getSelectedObject();
        if (obj instanceof TicketComponentIF t && !(obj instanceof IncidentComposite)) return t;
        return null;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new TicketGUI().setVisible(true);
        });
    }
}