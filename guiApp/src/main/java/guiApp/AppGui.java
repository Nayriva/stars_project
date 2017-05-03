package guiApp;

import cz.muni.fi.pv168.db_backend.backend.*;
import cz.muni.fi.pv168.db_backend.Main;
import cz.muni.fi.pv168.db_backend.common.DBUtils;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import guiApp.tableModels.AgentTableModel;
import guiApp.tableModels.AssignmentTableModel;
import guiApp.tableModels.MissionTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
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
    private JButton createAssignmentButton, editAssignmentButton, endAssignmentButton, deleteAssignmentButton,
            listAllAssignmentsButton, listAssignmentsButton;
    private JTable assignmentTable;
    private JCheckBox activeCheckBox;

    private static DataSource ds;
    private static AgentManager agentManager;
    private static AssignmentManager assignmentManager;
    private static MissionManager missionManager;
    private static Locale locale;
    private static ResourceBundle rb;
    private static AgentTableModel agentTableModel;
    private static AssignmentTableModel assignmentTableModel;
    private static MissionTableModel missionTableModel;

    public static final Object LOCK = new Object();

    public AppGui() {
        initializeAgentComponents();
        callAgentFind(new Object[] { "findAllAgents" });
        initializeMissionComponents();
    }

    private void createUIComponents() {
        assignmentTableModel = new AssignmentTableModel();
        missionTableModel = new MissionTableModel();
        agentTableModel = new AgentTableModel();
        agentTable = new JTable(agentTableModel);
        assignmentTable = new JTable(assignmentTableModel);
        missionTable = new JTable(missionTableModel);
    }

    public static ResourceBundle getRb() {
        return rb;
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        locale = Locale.getDefault();
        rb = ResourceBundle.getBundle("guiApp.localization", locale);
        prepareDataSourceAndDb();

        JFrame frame = new JFrame(rb.getString("mainTitle"));
        frame.setContentPane(new AppGui().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Includes driver for DB, prepares datasource and managers.
     */
    private static void prepareDataSourceAndDb() {
        agentManager = new AgentManagerImpl();
        assignmentManager = new AssignmentManagerImpl(Clock.systemUTC());
        missionManager = new MissionManagerImpl();

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            ds = Main.createDB();
            DBUtils.tryCreateTables(ds, Main.class.getResource("backend/createTables.sql"));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            System.exit(1);
        } catch (IOException | SQLException e) {
            System.exit(2);
        }

        agentManager.setDataSource(ds);
        assignmentManager.setDataSource(ds);
        missionManager.setDataSource(ds);
    }

    private void callAgentFind(Object[] args ) {
        List<Agent> agents = new ArrayList<>();
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<Agent>> result = es.submit(new Callable<List<Agent>>() {
            public List<Agent> call() throws Exception {
                try {
                    switch (args[0].toString()) {
                        case "findAllAgents":
                            return agentManager.findAllAgents();
                        case "findAgentsBySpecialPower":
                            return agentManager.findAgentsBySpecialPower((String) args[1]);
                        case "findAgentsByAlive":
                            return agentManager.findAgentsByAlive((boolean) args[1]);
                        case "findAgentsByRank":
                            return agentManager.findAgentsByRank((int) args[1]);
                        default:
                            throw new IllegalArgumentException("Cannot parse operation");
                    }
                } catch (ServiceFailureException ex) {
                    logger.error("Service failure", ex);
                    return null;
                }
            }
        });

        try {
            agents = result.get();
        } catch (Exception ex) { /* left blank intentionally */ }
        es.shutdown();
        agentTableModel.deleteAllData();
        for (Agent a : agents) {
            agentTableModel.addData(a);
        }
    }

    private void callMissionFind(Object[] args ) {
        List<Mission> missions = new ArrayList<>();
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<Mission>> result = es.submit(new Callable<List<Mission>>() {
            public List<Mission> call() throws Exception {
                try {
                    switch (args[0].toString()) {
                        case "findAllMissions":
                            return missionManager.findAllMissions();
                        case "findMissionsByFinAndSucc": {
                            List<Mission> result = missionManager.findMissionsBySuccess((boolean) args[1]);
                            result.removeIf((mission ->  mission.isFinished() != ((boolean) args[2])));
                            return result;
                        }
                        case "findMissionsByMinAgRank":
                            return missionManager.findMissionsByMinAgentRank((int) args[1]);
                        default:
                            throw new IllegalArgumentException("Cannot parse operation");
                    }
                } catch (ServiceFailureException ex) {
                    logger.error("Service failure", ex);
                    return null;
                }
            }
        });

        try {
            missions = result.get();
        } catch (Exception ex) { /* left blank intentionally */ }
        es.shutdown();
        missionTableModel.deleteAllData();
        for (Mission m : missions) {
            missionTableModel.addData(m);
        }
    }

    private void initializeAgentComponents() {
        addAgentButton.addActionListener((ActionEvent e) -> createAddAgentDialog());

        editAgentButton.addActionListener((ActionEvent e) -> createEditAgentDialog());

        deleteAgentButton.addActionListener((ActionEvent e) -> deleteAgent());

        listAllAgentsButton.addActionListener((ActionEvent e) -> callAgentFind(new Object[] { "findAllAgents" }));

        listAliveAgentsButton.addActionListener(
                (ActionEvent e) -> callAgentFind(new Object[] { "findAgentsByAlive", aliveCheckBox.isSelected() }));

        listSpPowAgentsButton.addActionListener(
                (ActionEvent e) -> callAgentFind(new Object[] { "findAgentsBySpecialPower", spPowerField.getText() }));

        listRankButton.addActionListener(
                (ActionEvent e) -> callAgentFind(new Object[] { "findAgentsByRank", rankSpinner.getValue() }));
    }

    private void createAddAgentDialog() {
        AddAgentDialog addAgentDialog = new AddAgentDialog();
        addAgentDialog.setTitle(AppGui.getRb().getString("addAgentDialogTitle"));
        addAgentDialog.pack();
        addAgentDialog.setVisible(true);
    }

    private void createEditAgentDialog() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        Long agentToEditId = agentTableModel.getAgentId(
                agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));

        EditAgentDialog editAgentDialog = new EditAgentDialog(agentToEditId,
                agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
        editAgentDialog.setTitle(AppGui.getRb().getString("editAgentDialogTitle"));
        editAgentDialog.pack();
        editAgentDialog.setVisible(true);
    }

    private void deleteAgent() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        Long agentToDeleteId = agentTableModel.getAgentId(
                agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
        Thread deleteAgent = new Thread(() -> {
            synchronized (LOCK) {
                Agent agentToDelete = agentManager.findAgentById(agentToDeleteId);
                agentManager.deleteAgent(agentToDelete);
                List<Assignment> toEnd = assignmentManager.findAssignmentsOfAgent(agentToDelete.getId());
                toEnd.removeIf((assignment) -> assignment.getEnd() != null);
                for (Assignment a: toEnd) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    getAssignmentManager().updateAssignment(a);
                }
            }
        });
        deleteAgent.start();
        agentTableModel.deleteData(agentTable.getSelectedRow());
    }

    private void initializeAssignmentComponents() {

    }

    private void initializeMissionComponents() {
        addMissionButton.addActionListener((ActionEvent e) -> createAddMissionDialog());

        editMissionButton.addActionListener((ActionEvent e) -> createEditMissionDialog());

        deleteMissionButton.addActionListener((ActionEvent e) -> deleteMission());

        listAllMissionsButton.addActionListener(
                (ActionEvent e) -> callMissionFind(new Object[] { "findAllMissions" }));

        listSucFinMissionsButton.addActionListener((ActionEvent e) -> {
                callMissionFind(new Object[] { "findMissionsByFinAndSucc", successfulCheckBox.isSelected(),
                        finishedCheckBox.isSelected()});
        });

        listMinAgRkMissionsButton.addActionListener(
                (ActionEvent e) -> callMissionFind(new Object[] { "findMissionsByMinAgRank", minAgRankSpinner.getValue() }));
    }

    private void createAddMissionDialog() {
        AddMissionDialog addMissionDialog = new AddMissionDialog();
        addMissionDialog.setTitle(AppGui.getRb().getString("addMissionDialogTitle"));
        addMissionDialog.pack();
        addMissionDialog.setVisible(true);
    }

    private void createEditMissionDialog() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        Long missionToEditId = missionTableModel.getMissionId(
                missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));

        EditMissionDialog editMissionDialog = new EditMissionDialog(missionToEditId,
                missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
        editMissionDialog.setTitle(AppGui.getRb().getString("editMissionDialogTitle"));
        editMissionDialog.pack();
        editMissionDialog.setVisible(true);
    }

    private void deleteMission() {
        if (missionTable.getSelectedRow() < 0) {
            return;
        }
        Long missionToDeleteId = missionTableModel.getMissionId(
                missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));

        Thread deleteMission = new Thread(() -> {
            synchronized (LOCK) {
                Mission missionToDelete = missionManager.findMissionById(missionToDeleteId);
                missionManager.deleteMission(missionToDelete);
                List<Assignment> toEnd = assignmentManager.findAssignmentsOfMission(missionToDeleteId);
                toEnd.removeIf((assignment) -> assignment.getEnd() != null);
                for (Assignment a: toEnd) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    getAssignmentManager().updateAssignment(a);
                }
            }
        });
        deleteMission.start();
        missionTableModel.deleteData(missionTable.getSelectedRow());
    }
}
