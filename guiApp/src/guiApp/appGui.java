package guiApp;

import cz.muni.fi.pv168.db_backend.Main;
import cz.muni.fi.pv168.db_backend.backend.*;
import cz.muni.fi.pv168.db_backend.common.DBUtils;

import javax.sql.DataSource;
import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;

/**
 * Created by nayriva on 27.4.2017.
 */
public class appGui {
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
    private JButton createAssignmentButton, endAssignmentButton, deleteAssignmentButton, listAllAssignmentsButton,
            listAssignmentsButton;
    private JRadioButton activeRadioButton, pastRadioButton;
    private JTable assignmentTable;

    private static DataSource ds;
    private static AgentManager agentManager;
    private static AssignmentManager assignmentManager;
    private static MissionManager missionManager;

    private void createUIComponents() {
        aliveRadioButton = new JRadioButton("alive");
        aliveRadioButton.setSelected(true);
        deadRadioButton = new JRadioButton("dead");
        ButtonGroup agentRadioGroup = new ButtonGroup();
        agentRadioGroup.add(aliveRadioButton);
        agentRadioGroup.add(deadRadioButton);

        activeRadioButton = new JRadioButton("active");
        activeRadioButton.setSelected(true);
        pastRadioButton = new JRadioButton("past");
        ButtonGroup assignmentRadioGroup = new ButtonGroup();
        assignmentRadioGroup.add(activeRadioButton);
        assignmentRadioGroup.add(pastRadioButton);
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        prepareDataSourceAndDb();

        JFrame frame = new JFrame("S.T.A.R.S. Management system");
        frame.setContentPane(new appGui().mainPanel);
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
