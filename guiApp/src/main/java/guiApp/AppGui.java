package guiApp;

import cz.muni.fi.pv168.db_backend.backend.*;
import cz.muni.fi.pv168.db_backend.Main;
import cz.muni.fi.pv168.db_backend.common.DBUtils;
import guiApp.tablesResources.AgentTableModel;
import guiApp.tablesResources.AssignmentTableModel;
import guiApp.tablesResources.LocalizedHeaderRendered;
import guiApp.tablesResources.MissionTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.*;


/**
 * Main class for GUI of application.
 *
 * Created by nayriva on 27.4.2017.
 */
public class AppGui {
    private final static Logger logger = LoggerFactory.getLogger(AppGui.class);

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JSplitPane agentSplitPane, missionSplitPane, assignmentSplitPane;
    //agent
    private JButton addAgentButton, editAgentButton, deleteAgentButton, listAllAgentsButton, listAliveAgentsButton,
            listSpPowAgentsButton, listRankButton;
    private JSpinner rankSpinner;
    private JTextField spPowerField;
    private JCheckBox aliveCheckBox;
    private JTable agentTable;
    //mission
    private JButton addMissionButton, editMissionButton, deleteMissionButton, listAllMissionsButton,
            listSucFinMissionsButton, listMinAgRkMissionsButton;
    private JCheckBox finishedCheckBox, successfulCheckBox;
    private JSpinner minAgRankSpinner;
    private JTable missionTable;
    //assignment
    private JButton addAssignmentButton, editAssignmentButton, endAssignmentButton, deleteAssignmentButton,
            listAllAssignmentsButton, listAssignmentsButton;
    private JTable assignmentTable;
    private JCheckBox activeCheckBox;
    private JButton helpButton;
    //other
    private static DataSource ds;
    private static AgentManager agentManager;
    private static AssignmentManager assignmentManager;
    private static MissionManager missionManager;
    private static ResourceBundle rb;
    private static AgentTableModel agentTableModel;
    private static AssignmentTableModel assignmentTableModel;
    private static MissionTableModel missionTableModel;

    private static Object[] dialogLocalizedOptions;

    public AppGui() {
        initializeAgentComponents();
        initializeMissionComponents();
        initializeAssignmentComponents();
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                listAllAgentsButton.doClick();
            } else if (tabbedPane.getSelectedIndex() == 1) {
                listAllMissionsButton.doClick();
            } else if (tabbedPane.getSelectedIndex() == 2) {
                listAllAssignmentsButton.doClick();
            }
        });

        helpButton.addActionListener((ActionEvent e) -> JOptionPane.showMessageDialog(
                mainPanel, rb.getString("helpMessage"),
                    rb.getString("helpTitle"),
                    JOptionPane.INFORMATION_MESSAGE)
        );
    }

    private void createUIComponents() {
        assignmentTableModel = new AssignmentTableModel();
        missionTableModel = new MissionTableModel();
        agentTableModel = new AgentTableModel();

        agentTable = new JTable(agentTableModel);
        agentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentTable.getTableHeader().setDefaultRenderer(new LocalizedHeaderRendered(rb));

        assignmentTable = new JTable(assignmentTableModel);
        assignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignmentTable.getTableHeader().setDefaultRenderer(new LocalizedHeaderRendered(rb));

        missionTable = new JTable(missionTableModel);
        missionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        missionTable.getTableHeader().setDefaultRenderer(new LocalizedHeaderRendered(rb));
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public static MissionManager getMissionManager() {
        return missionManager;
    }

    public static AgentTableModel getAgentTableModel() {
        return agentTableModel;
    }

    public static AssignmentTableModel getAssignmentTableModel() {
        return assignmentTableModel;
    }

    public static MissionTableModel getMissionTableModel() {
        return missionTableModel;
    }

    /**
     * Includes driver for DB, prepares datasource and managers.
     */
    private static void prepareDataSourceAndDb() {
        agentManager = new AgentManagerImpl();
        assignmentManager = new AssignmentManagerImpl(Clock.systemUTC());
        missionManager = new MissionManagerImpl();

        try {
            ds = Main.createDB();
            DBUtils.tryCreateTables(ds, Main.class.getResource("backend/createTables.sql"));
        } catch (IOException | SQLException e) {
            System.exit(1);
        }

        agentManager.setDataSource(ds);
        assignmentManager.setDataSource(ds);
        missionManager.setDataSource(ds);
    }

    private void initializeAgentComponents() {
        addAgentButton.addActionListener((ActionEvent e) -> createAddAgentDialog());

        editAgentButton.addActionListener((ActionEvent e) -> createEditAgentDialog());

        deleteAgentButton.addActionListener((ActionEvent e) -> deleteAgent());

        listAllAgentsButton.addActionListener((ActionEvent e) -> {
<<<<<<< HEAD
            FindAgentsSwingWorker swingWorker = new FindAgentsSwingWorker();
            swingWorker.setOperation("findAllAgents");
            swingWorker.execute();
=======
            FindAgentsSwingWorker sw = new FindAgentsSwingWorker();
            sw.setArgs(new Object[] {"findAllAgents"});
            sw.execute();
>>>>>>> 155efb123758cce2b2c8e16162f4cb8b74e0a772
        });

        listAliveAgentsButton.addActionListener((ActionEvent e) -> {
            FindAgentsSwingWorker swingWorker = new FindAgentsSwingWorker();
            swingWorker.setOperation("findAgentsByAlive");
            swingWorker.setAlive(aliveCheckBox.isSelected());
            swingWorker.execute();
        });

        listSpPowAgentsButton.addActionListener((ActionEvent e) -> {
            if (spPowerField.getText() == null || spPowerField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, rb.getString("listAgentsBySpPow"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            FindAgentsSwingWorker swingWorker = new FindAgentsSwingWorker();
            swingWorker.setOperation("findAgentsBySpecialPower");
            swingWorker.setSpecialPower(spPowerField.getText());
            swingWorker.execute();
        });

        listRankButton.addActionListener((ActionEvent e) -> {
            if (rankSpinner.getValue() == null || ((int) rankSpinner.getValue() <= 0)) {
                JOptionPane.showMessageDialog(mainPanel, rb.getString("listAgentsByRank"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            FindAgentsSwingWorker swingWorker = new FindAgentsSwingWorker();
            swingWorker.setOperation("findAgentsByRank");
            swingWorker.setRank((int) rankSpinner.getValue());
            swingWorker.execute();
        });
    }

    private void createAddAgentDialog() {
        AddAgentDialog addAgentDialog = new AddAgentDialog();
        addAgentDialog.setTitle(rb.getString("addAgentDialogTitle"));
        addAgentDialog.pack();
        addAgentDialog.setVisible(true);
    }
    
    private void createEditAgentDialog() {
        if (agentTable.getSelectedRow() < 0) {
            return;
        }
        Long agentToEditId = agentTableModel.getAgentId(
                agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));

        EditAgentDialog editAgentDialog = new EditAgentDialog(agentToEditId,
                agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
        editAgentDialog.setTitle(rb.getString("editAgentDialogTitle"));
        editAgentDialog.pack();
        editAgentDialog.setVisible(true);
    }
    
    private void deleteAgent() {
        if (agentTable.getSelectedRow() < 0) {
            return;
        }
        int result = JOptionPane.showOptionDialog(mainPanel, rb.getString("deleteQuestion"),
                rb.getString("deleteEntryTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, dialogLocalizedOptions, dialogLocalizedOptions[1]);
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        DeleteAgentSwingWorker swingWorker = new DeleteAgentSwingWorker();
        int selectedTableIndex = agentTable.convertColumnIndexToModel(agentTable.getSelectedRow());
        Long agentToDeleteId = agentTableModel.getAgentId(selectedTableIndex);
        swingWorker.setAgentToDeleteId(agentToDeleteId);
        swingWorker.setAgentTableIndex(selectedTableIndex);
        swingWorker.execute();
    }

    private void initializeMissionComponents() {
        addMissionButton.addActionListener((ActionEvent e) -> createAddMissionDialog());

        editMissionButton.addActionListener((ActionEvent e) -> createEditMissionDialog());

        deleteMissionButton.addActionListener((ActionEvent e) -> deleteMission());

        listAllMissionsButton.addActionListener((ActionEvent e) -> {
            FindMissionsSwingWorker swingWorker = new FindMissionsSwingWorker();
            swingWorker.setOperation("findAllMissions");
            swingWorker.execute();
        });

        listSucFinMissionsButton.addActionListener((ActionEvent e) -> {
            FindMissionsSwingWorker swingWorker = new FindMissionsSwingWorker();
            swingWorker.setOperation("findMissionsBySucFin");
            swingWorker.setFinished(finishedCheckBox.isSelected());
            swingWorker.setSuccessful(successfulCheckBox.isSelected());
            swingWorker.execute();
        });

        listMinAgRkMissionsButton.addActionListener((ActionEvent e) -> {
            if (minAgRankSpinner.getValue() == null || ((int) minAgRankSpinner.getValue() <= 0)) {
                JOptionPane.showMessageDialog(mainPanel, rb.getString("listMissionsByMinAgRank"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            FindMissionsSwingWorker swingWorker = new FindMissionsSwingWorker();
            swingWorker.setOperation("findMissionsByMinAgRank");
            swingWorker.setMinAgRank((int) minAgRankSpinner.getValue());
            swingWorker.execute();
        });
    }

    private void createAddMissionDialog() {
        AddMissionDialog addMissionDialog = new AddMissionDialog();
        addMissionDialog.setTitle(rb.getString("addMissionDialogTitle"));
        addMissionDialog.pack();
        addMissionDialog.setVisible(true);
    }

    private void createEditMissionDialog() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        EditMissionDialog editMissionDialog = new EditMissionDialog(
                missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
        editMissionDialog.setTitle(rb.getString("editMissionDialogTitle"));
        editMissionDialog.pack();
        editMissionDialog.setVisible(true);
    }

    private void deleteMission() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        int result = JOptionPane.showOptionDialog(mainPanel, rb.getString("deleteQuestion"),
                rb.getString("deleteEntryTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, dialogLocalizedOptions, dialogLocalizedOptions[1]);
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        DeleteMissionSwingWorker swingWorker = new DeleteMissionSwingWorker();
        int selectedTableIndex = missionTable.convertColumnIndexToModel(missionTable.getSelectedRow());
        Long missionToDeleteId = missionTableModel.getMissionId(selectedTableIndex);
        swingWorker.setMissionToDeleteId(missionToDeleteId);
        swingWorker.setMissionTableIndex(selectedTableIndex);
        swingWorker.execute();
    }

    private void initializeAssignmentComponents() {
        addAssignmentButton.addActionListener((ActionEvent e) -> createAddAssignmentDialog());

        editAssignmentButton.addActionListener((ActionEvent e) -> createEditAssignmentDialog());

        deleteAssignmentButton.addActionListener((ActionEvent e) -> deleteAssignment());

        endAssignmentButton.addActionListener((ActionEvent e) -> endAssignment());

        listAllAssignmentsButton.addActionListener((ActionEvent e) -> {
            FindAssignmentsSwingWorker swingWorker = new FindAssignmentsSwingWorker();
            swingWorker.setOperation("findAllAssignments");
            swingWorker.execute();
        });

        listAssignmentsButton.addActionListener((ActionEvent e) -> {
            FindAssignmentsSwingWorker swingWorker = new FindAssignmentsSwingWorker();
            if (activeCheckBox.isSelected()) {
                swingWorker.setOperation("findActiveAssignments");
            } else {
                swingWorker.setOperation("findEndedAssignments");
            }
            swingWorker.execute();
        });
    }

    private void createAddAssignmentDialog() {
        AddAssignmentDialog addAssignmentDialog = new AddAssignmentDialog();
        addAssignmentDialog.setTitle(rb.getString("addAssignmentDialogTitle"));
        addAssignmentDialog.pack();
        addAssignmentDialog.setVisible(true);
    }

    private void createEditAssignmentDialog() {
        if (assignmentTable.getSelectedRow() < 0) {
            return;
        }
        int selectedIndex = assignmentTable.convertColumnIndexToModel(assignmentTable.getSelectedRow());
        if (assignmentTableModel.isEnded(selectedIndex)) {
            JOptionPane.showMessageDialog(mainPanel, rb.getString("editEndedAssignment"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        EditAssignmentDialog editAssignmentDialog = new EditAssignmentDialog(
                assignmentTable.convertColumnIndexToModel(assignmentTable.getSelectedRow()));
        editAssignmentDialog.setTitle(rb.getString("editAssignmentDialogTitle"));
        editAssignmentDialog.pack();
        editAssignmentDialog.setVisible(true);
    }

    private void deleteAssignment() {
        if (assignmentTable.getSelectedRow() < 0) {
            return;
        }
        int result = JOptionPane.showOptionDialog(mainPanel, rb.getString("deleteQuestion"),
                rb.getString("deleteEntryTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, dialogLocalizedOptions, dialogLocalizedOptions[1]);
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        DeleteAssignmentSwingWorker swingWorker = new DeleteAssignmentSwingWorker();
        int selectedTableIndex = assignmentTable.convertColumnIndexToModel(assignmentTable.getSelectedRow());
        Long assignmentToDeleteId = assignmentTableModel.getAssignmentId(selectedTableIndex);
        swingWorker.setAssignmentToDeleteId(assignmentToDeleteId);
        swingWorker.setAssignmentTableIndex(selectedTableIndex);
        swingWorker.execute();
    }

    private void endAssignment() {
        if (assignmentTable.getSelectedRow() < 0) {
            return;
        }
        int selectedTableIndex = assignmentTable.convertColumnIndexToModel(assignmentTable.getSelectedRow());
        if (assignmentTableModel.isEnded(selectedTableIndex)) {
            JOptionPane.showMessageDialog(mainPanel, rb.getString("endEndedAssignment"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        int result = JOptionPane.showOptionDialog(mainPanel, rb.getString("endQuestion"),
                rb.getString("endAssignmentTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, dialogLocalizedOptions, dialogLocalizedOptions[1]);
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        EndAssignmentSwingWorker swingWorker = new EndAssignmentSwingWorker();
        Long assignmentToEnd = assignmentTableModel.getAssignmentId(selectedTableIndex);
        swingWorker.setAssignmentToEndId(assignmentToEnd);
        swingWorker.setAssignmentTableIndex(selectedTableIndex);
        swingWorker.execute();
    }

    private class DeleteAgentSwingWorker extends SwingWorker<Void, Void> {
        private Long agentToDeleteId;
        private int agentTableIndex;

        public void setAgentTableIndex(int agentTableIndex) {
            this.agentTableIndex = agentTableIndex;
        }

        public void setAgentToDeleteId(Long agentToDeleteId) {
            this.agentToDeleteId = agentToDeleteId;
        }

        @Override
        protected void done() {
            try {
                get();
                agentTableModel.deleteData(agentTableIndex);
                assignmentTableModel.getAgents().remove(agentToDeleteId);
            } catch (ExecutionException ex) {
                logger.error("Error in executing deleteAgent: {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("agentDeleteFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            Agent agentToDelete = agentManager.findAgentById(agentToDeleteId);
            agentManager.deleteAgent(agentToDelete);
            List<Assignment> toEnd = assignmentManager.findAssignmentsOfAgent(agentToDeleteId);
            toEnd.removeIf((assignment) -> assignment.getEnd() != null);
            for (Assignment a : toEnd) {
                getAssignmentManager().deleteAssignment(a);
            }
            return null;
        }
    }

    private class FindAgentsSwingWorker extends SwingWorker<List<Agent>, Void> {
        private String operation;
        private String specialPower;
        private boolean alive;
        private int rank;

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public void setSpecialPower(String specialPower) {
            this.specialPower = specialPower;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        @Override
        protected void done() {
            try {
                List<Agent> agents = get();
                agentTableModel.deleteAllData();
                for (Agent a : agents) {
                    agentTableModel.addData(a);
                }
            } catch (ExecutionException ex) {
                logger.error("Error in executing " + operation + ": {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("agentFindFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected List<Agent> doInBackground() throws Exception {
            switch (operation) {
                case "findAllAgents":
                    return agentManager.findAllAgents();
                case "findAgentsBySpecialPower":
                    return agentManager.findAgentsBySpecialPower(specialPower);
                case "findAgentsByAlive":
                    return agentManager.findAgentsByAlive(alive);
                case "findAgentsByRank":
                    return agentManager.findAgentsByRank(rank);
                default:
                    throw new IllegalArgumentException("Cannot parse operation");
            }
        }
    }
    
    private class DeleteMissionSwingWorker extends SwingWorker<Void, Void> {
        private Long missionToDeleteId;
        private int missionTableIndex;

        public void setMissionToDeleteId(Long missionToDeleteId) {
            this.missionToDeleteId = missionToDeleteId;
        }

        public void setMissionTableIndex(int missionTableIndex) {
            this.missionTableIndex = missionTableIndex;
        }

        @Override
        protected void done() {
            try {
                get();
                missionTableModel.deleteData(missionTableIndex);
                assignmentTableModel.getMissions().remove(missionToDeleteId);
            } catch (ExecutionException ex) {
                logger.error("Error in executing deleteMission: {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("missionDeleteFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            Mission missionToDelete = missionManager.findMissionById(missionToDeleteId);
            missionManager.deleteMission(missionToDelete);
            List<Assignment> toEnd = assignmentManager.findAssignmentsOfMission(missionToDeleteId);
            toEnd.removeIf((assignment) -> assignment.getEnd() != null);
            for (Assignment a : toEnd) {
                assignmentManager.deleteAssignment(a);
            }
            return null;
        }
    }

    private class FindMissionsSwingWorker extends SwingWorker<List<Mission>, Void> {
        private String operation;
        private boolean finished;
        private boolean successful;
        private int minAgRank;

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public void setMinAgRank(int minAgRank) {
            this.minAgRank = minAgRank;
        }

        @Override
        protected void done() {
            try {
                List<Mission> missions = get();
                missionTableModel.deleteAllData();
                for (Mission m : missions) {
                    missionTableModel.addData(m);
                }
            } catch (ExecutionException ex) {
                logger.error("Error in executing " + operation + ": {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("missionFindFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected List<Mission> doInBackground() throws Exception {
            switch (operation) {
                case "findAllMissions":
                    return missionManager.findAllMissions();
                case "findMissionsBySucFin": {
                    List<Mission> result = missionManager.findMissionsBySuccess(successful);
                    result.removeIf((mission) -> mission.isFinished() != finished);
                    return result;
                }
                case "findMissionsByMinAgRank":
                    return missionManager.findMissionsByMinAgentRank(minAgRank);
                default:
                    throw new IllegalArgumentException("Cannot parse operation");
            }
        }
    }

    private class DeleteAssignmentSwingWorker extends SwingWorker<Void, Void> {
        private Long assignmentToDeleteId;
        private int assignmentTableIndex;

        public void setAssignmentToDeleteId(Long assignmentToDeleteId) {
            this.assignmentToDeleteId = assignmentToDeleteId;
        }

        public void setAssignmentTableIndex(int assignmentTableIndex) {
            this.assignmentTableIndex = assignmentTableIndex;
        }

        @Override
        protected void done() {
            try {
                get();
                assignmentTableModel.deleteData(assignmentTableIndex);
            } catch (ExecutionException ex) {
                logger.error("Error in executing deleteAssignment: {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("assignmentDeleteFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            Assignment assignment = assignmentManager.findAssignmentById(assignmentToDeleteId);
            assignmentManager.deleteAssignment(assignment);
            return null;
        }
    }

    private class EndAssignmentSwingWorker extends SwingWorker<Assignment, Void> {
        private Long assignmentToEndId;
        private int assignmentTableIndex;

        public void setAssignmentToEndId(Long assignmentToEndId) {
            this.assignmentToEndId = assignmentToEndId;
        }

        public void setAssignmentTableIndex(int assignmentTableIndex) {
            this.assignmentTableIndex = assignmentTableIndex;
        }

        @Override
        protected void done() {
            try {
                Assignment updated = get();
                assignmentTableModel.editData(assignmentTableIndex, updated);
            } catch (ExecutionException ex) {
                logger.error("Error in executing endAssignment: {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("assignmentEndFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Assignment doInBackground() throws Exception {
            Assignment assignment = assignmentManager.findAssignmentById(assignmentToEndId);
            assignment.setEnd(LocalDate.now());
            assignmentManager.updateAssignment(assignment);
            return assignment;
        }
    }

    private class FindAssignmentsSwingWorker extends SwingWorker<List<Assignment>, Void> {
        private String operation;

        public void setOperation(String operation) {
            this.operation = operation;
        }

        @Override
        protected void done() {
            try {
                List<Assignment> assignments = get();
                assignmentTableModel.deleteAllData();
                for (Assignment a : assignments) {
                    assignmentTableModel.addData(a);
                }
            } catch (ExecutionException ex) {
                logger.error("Error in executing " + operation + ": {}", ex.getMessage(), ex.getCause());
                JOptionPane.showMessageDialog(mainPanel, rb.getString("assignmentFindFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected List<Assignment> doInBackground() throws Exception {
            switch (operation) {
                case "findAllAssignments":
                    return assignmentManager.findAllAssignments();
                case "findActiveAssignments":
                    return assignmentManager.findActiveAssignments();
                case "findEndedAssignments":
                    return assignmentManager.findEndedAssignments();
                default:
                    throw new IllegalArgumentException("Cannot parse operation");
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Locale locale = Locale.getDefault();
        rb = ResourceBundle.getBundle("guiApp.localization", locale);
        dialogLocalizedOptions = new Object[] { rb.getString("OK"), rb.getString("cancel") };
        prepareDataSourceAndDb();

        JFrame frame = new JFrame(rb.getString("mainTitle"));
        frame.setContentPane(new AppGui().mainPanel);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try (ObjectOutputStream agentsOOS = new ObjectOutputStream(new FileOutputStream(
                        "additionalResources/serialization/agentStrings.ser"));
                     ObjectOutputStream missionsOOS = new ObjectOutputStream(new FileOutputStream(
                             "additionalResources/serialization/missionStrings.ser")))
                {
                    logger.debug("Serialization of Agent and Mission Strings hashMaps ...");
                    agentsOOS.writeObject(assignmentTableModel.getAgents());
                    missionsOOS.writeObject(assignmentTableModel.getMissions());
                    logger.debug("Serialization of Agent and Mission Strings hashMaps finished ...");
                } catch (IOException ex) {
                    logger.error("Serialization of Agent and Mission Strings hashMaps failed!", ex);
                    System.exit(3);
                }
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
