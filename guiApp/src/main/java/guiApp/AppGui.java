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
import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
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
    private JRadioButton aliveRadioButton, deadRadioButton;
    private JSpinner rankSpinner;
    private JTextField spPowerField;
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
    private JRadioButton activeRadioButton, pastRadioButton;
    private JTable assignmentTable;

    private static DataSource ds;
    private static AgentManager agentManager;
    private static AssignmentManager assignmentManager;
    private static MissionManager missionManager;
    private static Locale locale;
    private static ResourceBundle rb;

    public AppGui() {
        addAgentButton.addActionListener((ActionEvent e) -> {
            AddAgentDialog addAgentDialog = new AddAgentDialog();
            addAgentDialog.setTitle(AppGui.getRb().getString("addAgentDialogTitle"));
            addAgentDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addAgentDialog.pack();
            addAgentDialog.setVisible(true);
        });

        editAgentButton.addActionListener((ActionEvent e) -> {
            EditAgentDialog editAgentDialog = new EditAgentDialog();
            editAgentDialog.setTitle(AppGui.getRb().getString("editAgentDialogTitle"));
            editAgentDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            editAgentDialog.pack();
            editAgentDialog.setVisible(true);
        });

        deleteAgentButton.addActionListener((ActionEvent e) -> {
            AgentTableModel agentTableModel = (AgentTableModel) agentTable.getModel();
            Long agentToDeleteId = agentTableModel.getAgentId(
                    agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
            agentTableModel.deleteData(agentTable.getSelectedRow());
            Thread deleteAgent = new Thread(() -> {
                Agent agentToDelete = agentManager.findAgentById(agentToDeleteId);
                agentManager.deleteAgent(agentToDelete);
            });
            deleteAgent.start();
        });

        listAllAgentsButton.addActionListener((ActionEvent e) -> {
            List<Agent> agents;
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<List<Agent>> result = es.submit(new Callable<List<Agent>>() {
                public List<Agent> call() throws Exception {
                    try {
                        return agentManager.findAllAgents();
                    } catch (ServiceFailureException ex) {
                        logger.error("Service failure", ex);
                        return null;
                    }
                }
            });

            try {
                agents = result.get();
            } catch (Exception ex) {
                return;
            }
            es.shutdown();
            AgentTableModel agentTableModel = (AgentTableModel) agentTable.getModel();
            for (Agent a : agents) {
                agentTableModel.addData(a);
            }
        });
    }

    private void createUIComponents() {
        aliveRadioButton = new JRadioButton(rb.getString("aliveRadioButton"));
        aliveRadioButton.setSelected(true);
        deadRadioButton = new JRadioButton(rb.getString("deadRadioButton"));
        ButtonGroup agentRadioGroup = new ButtonGroup();
        agentRadioGroup.add(aliveRadioButton);
        agentRadioGroup.add(deadRadioButton);

        activeRadioButton = new JRadioButton(rb.getString("activeRadioButton"));
        activeRadioButton.setSelected(true);
        pastRadioButton = new JRadioButton(rb.getString("pastRadioButton"));
        ButtonGroup assignmentRadioGroup = new ButtonGroup();
        assignmentRadioGroup.add(activeRadioButton);
        assignmentRadioGroup.add(pastRadioButton);

        agentTable = new JTable(new AgentTableModel());
        assignmentTable = new JTable(new AssignmentTableModel());
        missionTable = new JTable(new MissionTableModel());
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

    public static ResourceBundle getRb() {
        return rb;
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
}
