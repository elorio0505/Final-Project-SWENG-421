package edu.psu.view;

import edu.psu.behavior.AbsTicketState;
import edu.psu.core.*;
import edu.psu.processing.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Main GUI for the Incident Management System.
 * Layout:
 *  Top : ticket creation (manual) + Import from File + flood test
 *  Left : incident/ticket hierarchy tree
 *  Center : ticket detail view + action buttons
 *  Right : feature decorator controls + feature info
 */
public class TicketGUI extends JFrame {

    private final SoftwareTicketBuilder swBuilder = new SoftwareTicketBuilder();
    private final HardwareTicketBuilder hwBuilder = new HardwareTicketBuilder();
    private final TicketScheduler       scheduler = new TicketScheduler();

    // top
    private JTextField    titleField;
    private JTextArea     descField;
    private JTextField    priorityField;
    private JComboBox<String> typeCombo;

    // Hardware creation fields
    private JPanel     hwFieldsPanel;
    private JTextField hwSerialField;
    private JTextField hwMakeModelField;
    private JTextField hwLocationField;
    private JTextField hwFailureTypeField;
    private JCheckBox  hwWarrantyCheck;

    //left
    private JTree                  incidentTree;
    private DefaultTreeModel       treeModel;
    private DefaultMutableTreeNode treeRoot;

    //middle
    private JTextArea detailArea;

    private JButton assignBtn;
    private JButton activeBtn;
    private JButton resolveBtn;
    private JButton closeBtn;
    private JButton reopenBtn;
    private JButton escalateBtn;
    private JButton requestBtn;
    private JButton manageIncidentBtn;

    // scheduler controls
    private JButton        schedulerToggleBtn;  // Start / Stop
    private JButton        pauseResumeBtn;      // Pause / Resume
    private JSpinner       rateSpinner;
    private JLabel         schedulerStatusLabel;
    private JLabel         queueLabel;

    //right
    private JCheckBox auditCheck;
    private JCheckBox slaCheck;
    private JCheckBox boostCheck;

    private JButton toggleAuditBtn;
    private JButton toggleSlaBtn;
    private JButton toggleBoostBtn;

    private JTextArea decoratorInfoArea;
    
    //Constructor
    public TicketGUI() {
        IncidentRegistry.syncStorage();

        scheduler.addDispatchListener((ticket, stats) -> SwingUtilities.invokeLater(() -> {
            refreshTree();
            selectTicketInTree(ticket);
            updateSchedulerUI(stats);
        }));

        setTitle("Incident Management System");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                scheduler.stop();
                dispose();
                System.exit(0);
            }
        });
        setLayout(new BorderLayout(4, 4));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildEastPanel(),   BorderLayout.EAST);

        refreshTree();
        updateButtonStates(null);
        updateSchedulerUI(scheduler.getStats());
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel outer = new JPanel(new BorderLayout(4, 4));
        outer.setBorder(BorderFactory.createTitledBorder("Ticket Creation"));
        JPanel commonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        typeCombo     = new JComboBox<>(new String[]{"Software", "Hardware"});
        titleField    = new JTextField(12);
        descField     = new JTextArea(2, 20);
        descField.setLineWrap(true);
        priorityField = new JTextField(3);

        JButton createBtn     = new JButton("Create Ticket");
        JButton importFileBtn = new JButton("Import from File (.eml / .html)");
        JButton floodBtn      = new JButton("Run Flood Test");

        createBtn    .addActionListener(e -> handleManualCreation());
        importFileBtn.addActionListener(e -> handleImportFromFile());
        floodBtn     .addActionListener(e -> runFloodTest());

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
        commonRow.add(importFileBtn);
        commonRow.add(new JSeparator(SwingConstants.VERTICAL));
        commonRow.add(floodBtn);

        // scheduler
        JPanel schedulerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        schedulerRow.setBorder(BorderFactory.createTitledBorder("Scheduler"));

        schedulerToggleBtn  = new JButton("Start Scheduler");
        pauseResumeBtn      = new JButton("Pause");
        pauseResumeBtn.setEnabled(false);

        SpinnerNumberModel rateModel = new SpinnerNumberModel(1.0, 0.1, 100.0, 0.5);
        rateSpinner = new JSpinner(rateModel);
        rateSpinner.setPreferredSize(new Dimension(65, rateSpinner.getPreferredSize().height));
        ((JSpinner.DefaultEditor) rateSpinner.getEditor()).getTextField().setColumns(4);

        schedulerStatusLabel = new JLabel("Stopped  |  Dispatched: 0  |  Throughput: 0.00/s");
        queueLabel           = new JLabel("Queue: 0 pending");
        queueLabel.setFont(queueLabel.getFont().deriveFont(Font.BOLD));

        schedulerToggleBtn.addActionListener(e -> handleSchedulerToggle());
        pauseResumeBtn    .addActionListener(e -> handlePauseResume());
        rateSpinner.addChangeListener(e -> {
            double rate = ((Number) rateSpinner.getValue()).doubleValue();
            scheduler.setRate(rate);
        });

        schedulerRow.add(schedulerToggleBtn);
        schedulerRow.add(pauseResumeBtn);
        schedulerRow.add(new JLabel("Rate (tickets/sec):"));
        schedulerRow.add(rateSpinner);
        schedulerRow.add(new JSeparator(SwingConstants.VERTICAL));
        schedulerRow.add(queueLabel);
        schedulerRow.add(new JSeparator(SwingConstants.VERTICAL));
        schedulerRow.add(schedulerStatusLabel);

        // hardware fields
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
        hwFieldsPanel.setVisible(false);

        JPanel northRows = new JPanel(new BorderLayout(0, 2));
        northRows.add(commonRow,    BorderLayout.NORTH);
        northRows.add(schedulerRow, BorderLayout.SOUTH);

        outer.add(northRows,      BorderLayout.NORTH);
        outer.add(hwFieldsPanel,  BorderLayout.SOUTH);
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

    // State buttons
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
        return state != null && state.validateTransition(event);
    }

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

    // Action handlers
    // manual
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

        TicketComponentIF ticket = buildTicket(priority);
        applyCreationDecorators(ticket);
        scheduler.submit(ticket);
        updateSchedulerUI(scheduler.getStats());

        titleField.setText("");
        descField.setText("");
        priorityField.setText("");
        clearHardwareFields();
    }

    //file import, email/webform (.eml/.html)
    private void handleImportFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Ticket: select a .eml or .html file");
        chooser.setFileFilter(
            new FileNameExtensionFilter("Ticket source files (*.eml, *.html)", "eml", "html", "htm"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        String fileName   = selectedFile.getName().toLowerCase();

        try {
            String rawContent = java.nio.file.Files.readString(selectedFile.toPath());
            boolean isHardware = rawContent.lines()
                .map(String::trim)
                .filter(l -> l.toLowerCase().startsWith("type:"))
                .map(l -> l.substring(l.indexOf(':') + 1).trim())
                .anyMatch(v -> v.equalsIgnoreCase("Hardware"));

            AbsTicketBuilder builder = isHardware ? hwBuilder : swBuilder;
            builder.reset();

            String sourceLabel;

            if (fileName.endsWith(".eml")) {
                new EmailSource().loadFromFile(selectedFile.toPath(), builder);
                sourceLabel = "Email";
            } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                new WebFormSource().loadFromFile(selectedFile.toPath(), builder);
                sourceLabel = "Web Form";
            } else {
                showError("Unsupported file type.\nPlease select a .eml or .html file.");
                return;
            }

            TicketComponentIF ticket = builder.getProduct();
            applyCreationDecorators(ticket);
            scheduler.submit(ticket);
            updateSchedulerUI(scheduler.getStats());

            String schedulerHint = scheduler.isRunning()
                ? "The scheduler will dispatch it automatically."
                : "Start the scheduler to dispatch it.";

            JOptionPane.showMessageDialog(this,
                "Ticket queued successfully from: " + selectedFile.getName()
                + "\n\nType:     " + (isHardware ? "Hardware" : "Software")
                + "\nTitle:    " + ticket.getTitle()
                + "\nPriority: " + ticket.getPriority()
                + "\nSource:   " + sourceLabel
                + "\n\n" + schedulerHint,
                "Ticket Queued", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            showError("Could not read the file:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError("Parsing failed: the file may be missing required fields.\n\n"
                + ex.getMessage()
                + "\n\nRequired fields:  type, title, description, priority"
                + "\nHardware also needs:  serial, make_model, location, failure, warranty");
        }
    }

    // Builder
    private TicketComponentIF buildTicket(int priority) {
        boolean isHardware = "Hardware".equals(typeCombo.getSelectedItem());

        if (isHardware) {
            hwBuilder.reset();
            hwBuilder.setBasics(titleField.getText().trim(), descField.getText().trim());
            hwBuilder.setMetaData("Hardware", priority);
            hwBuilder.setHardwareDetails(
                hwSerialField.getText().trim(),
                hwMakeModelField.getText().trim(),
                hwLocationField.getText().trim(),
                hwFailureTypeField.getText().trim(),
                hwWarrantyCheck.isSelected());
            return hwBuilder.getProduct();
        } else {
            swBuilder.reset();
            swBuilder.setBasics(titleField.getText().trim(), descField.getText().trim());
            swBuilder.setMetaData("Software", priority);
            return swBuilder.getProduct();
        }
    }

    private void applyCreationDecorators(TicketComponentIF ticket) {
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
    }

    private void clearHardwareFields() {
        hwSerialField.setText("");
        hwMakeModelField.setText("");
        hwLocationField.setText("");
        hwFailureTypeField.setText("");
        hwWarrantyCheck.setSelected(false);
    }

    // Feature toggle
    private void handleToggleDecorator(String type) {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;

        TicketComponentIF updated;

        switch (type) {
            case "audit" -> {
                if (hasDecorator(ticket, AuditLogger.class)) {
                    updated = removeDecorator(ticket, AuditLogger.class);
                    updated.addLog("AuditLogger removed");
                } else {
                    updated = new AuditLogger(ticket);
                    updated.addLog("AuditLogger added post-creation");
                }
            }
            case "sla" -> {
                if (hasDecorator(ticket, SLAMonitor.class)) {
                    updated = removeDecorator(ticket, SLAMonitor.class);
                    updated.addLog("SLAMonitor removed");
                } else {
                    updated = new SLAMonitor(ticket, Duration.ofHours(24));
                    updated.addLog("SLAMonitor added post-creation (24h window)");
                }
            }
            case "boost" -> {
                if (hasDecorator(ticket, PriorityBooster.class)) {
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

    // State
    private void handleAssign() {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;

        JTextField deptField = new JTextField(12);
        JTextField techField = new JTextField(12);
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Department:")); form.add(deptField);
        form.add(new JLabel("Technician:")); form.add(techField);

        if (JOptionPane.showConfirmDialog(this, form, "Assign Ticket",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String dept = deptField.getText().trim();
        String tech = techField.getText().trim();
        if (dept.isEmpty() || tech.isEmpty()) {
            showError("Both department and technician are required.");
            return;
        }

        ticket.setAssignee(tech);
        if (ticket instanceof Ticket t) t.setDepartment(dept);
        ticket.addLog("Assigned to " + tech + " (Dept: " + dept + ")");
        ticket.processEvent(AbsTicketState.ASSIGN_EVT);
        refreshAndReselect(ticket);
    }

    private void handleSetActive()   { applyEvent(AbsTicketState.ACTIVATE_EVT, "Set to Active"); }
    private void handleResolve()     { applyEvent(AbsTicketState.RESOLVE_EVT,  "Marked as Resolved"); }
    private void handleClose()       { applyEvent(AbsTicketState.CLOSE_EVT,    "Ticket Closed"); }
    private void handleRequestInfo() { applyEvent(AbsTicketState.PENDING_EVT,  "Info requested: ticket set to Pending"); }

    private void handleEscalate() {
        TicketComponentIF ticket = getSelectedTicket();
        if (ticket == null) return;
        int newPriority = ticket.getPriority() + 1;
        ticket.setPriority(newPriority);
        ticket.addLog("Escalated: priority increased to " + newPriority);
        ticket.processEvent(AbsTicketState.ESCALATE_EVT);
        refreshAndReselect(ticket);
    }

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
            IncidentComposite[] arr = allIncidents.toArray(new IncidentComposite[0]);
            IncidentComposite target = (IncidentComposite) JOptionPane.showInputDialog(
                    this, "Choose incident:", "Add to Incident",
                    JOptionPane.PLAIN_MESSAGE, null, arr, arr[0]);
            if (target == null || selected == target) return;
            if (selected instanceof TicketComponentIF tc) {
                target.addChild(tc);
                IncidentRegistry.syncStorage();
                JOptionPane.showMessageDialog(this, "Added to: " + target.getTitle());
            }

        } else if (choice == 1) {
            if (selected instanceof TicketComponentIF ticket) {
                List<IncidentComposite> containing = IncidentRegistry.getContainingIncidents(ticket);
                if (containing.isEmpty()) { showError("This ticket is not inside any incident."); return; }
                IncidentComposite[] arr = containing.toArray(new IncidentComposite[0]);
                IncidentComposite from = (IncidentComposite) JOptionPane.showInputDialog(
                        this, "Remove from which?", "Remove from Incident",
                        JOptionPane.PLAIN_MESSAGE, null, arr, arr[0]);
                if (from == null) return;
                from.remove(ticket);
                IncidentRegistry.syncStorage();
                JOptionPane.showMessageDialog(this, "Removed from: " + from.getTitle());
            }

        } else if (choice == 2) {
            JTextField incTitle = new JTextField(16);
            JTextField incDesc  = new JTextField(24);
            JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
            form.add(new JLabel("Incident Title:")); form.add(incTitle);
            form.add(new JLabel("Description:"));    form.add(incDesc);
            if (JOptionPane.showConfirmDialog(this, form, "Create New Incident",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            if (incTitle.getText().trim().isEmpty()) return;
            IncidentRegistry.newIncident(incTitle.getText().trim(), incDesc.getText().trim());
        }

        refreshTree();
    }

    // Scheduler controls

    private void handleSchedulerToggle() {
        if (scheduler.isRunning()) {
            scheduler.stop();
            schedulerToggleBtn.setText("Start Scheduler");
            pauseResumeBtn.setEnabled(false);
            pauseResumeBtn.setText("Pause");
        } else {
            double rate = ((Number) rateSpinner.getValue()).doubleValue();
            scheduler.setRate(rate);
            scheduler.start();
            schedulerToggleBtn.setText("Stop Scheduler");
            pauseResumeBtn.setEnabled(true);
        }
        updateSchedulerUI(scheduler.getStats());
    }

    private void handlePauseResume() {
        if (scheduler.isPaused()) {
            scheduler.resume();
            pauseResumeBtn.setText("Pause");
        } else {
            scheduler.pause();
            pauseResumeBtn.setText("Resume");
        }
        updateSchedulerUI(scheduler.getStats());
    }

    private void updateSchedulerUI(TicketScheduler.SchedulerStats stats) {
        // Queue label
        int n = stats.pending;
        queueLabel.setText("Queue: " + n + " pending");
        queueLabel.setForeground(n > 0 ? new Color(180, 80, 0) : new Color(0, 120, 0));

        // Status label
        String state;
        if (!scheduler.isRunning())   state = "Stopped";
        else if (scheduler.isPaused()) state = "Paused";
        else                           state = "Running";

        schedulerStatusLabel.setText(String.format(
            "%s  |  Dispatched: %d  |  Throughput: %.2f/s",
            state, stats.totalDispatched, stats.throughput()));
    }

    //Flood Test
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
            scheduler.submit(swBuilder.getProduct());
        }
        long submitMs = System.currentTimeMillis() - startMs;

        TicketScheduler.SchedulerStats stats = scheduler.getStats();
        updateSchedulerUI(stats);

        double rate = scheduler.getRate();
        double drainSec = stats.pending / rate;
        String drainMsg = scheduler.isRunning()
            ? String.format("At %.1f ticket/s, queue will drain in ~%.1f seconds.", rate, drainSec)
            : "Start the scheduler to begin dispatching.";

        JOptionPane.showMessageDialog(this,
            String.format("Flood Test Complete.\n%d tickets queued in %d ms.\n\n"
                + "Queue depth : %d pending\n"
                + "Total submitted : %d\n\n%s",
                count, submitMs, stats.pending, stats.totalSubmitted, drainMsg),
            "Flood Test Results", JOptionPane.INFORMATION_MESSAGE);
    }

    //middle
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
        sb.append(hasAudit ? "Audit Logging  : ON\n"  : "Audit Logging  : OFF\n");
        sb.append(hasBoost ? "Priority Boost : ON\n"  : "Priority Boost : OFF\n");
        sb.append(hasSLA   ? "SLA Monitoring : ON\n"  : "SLA Monitoring : OFF\n");

        if (hasSLA) {
            TicketComponentIF probe = t;
            while (probe instanceof AbsTicketDecorator dec) {
                if (probe instanceof SLAMonitor sla) {
                    sb.append("\nSLA Status: ")
                      .append(sla.checkSLAStatus() ? "VALID" : "EXPIRED")
                      .append("\n");
                    break;
                }
                probe = dec.decoratedComponent;
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

    //features
    private boolean hasDecorator(TicketComponentIF t, Class<?> cls) {
        TicketComponentIF cur = t;
        while (cur instanceof AbsTicketDecorator dec) {
            if (cls.isInstance(cur)) return true;
            cur = dec.decoratedComponent;
        }
        return false;
    }

    private TicketComponentIF removeDecorator(TicketComponentIF t, Class<?> cls) {
        if (!(t instanceof AbsTicketDecorator dec)) return t;
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

    //Helpers
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
    
    //Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new TicketGUI().setVisible(true);
        });
    }
}