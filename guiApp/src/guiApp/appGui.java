package guiApp;

import javax.swing.*;

/**
 * Created by nayriva on 27.4.2017.
 */
public class appGui {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JButton addAgentButton;
    private JButton editAgentButton;
    private JButton deleteAgentButton;
    private JButton listAllAgentsButton;
    private JRadioButton aliveRadioButton;
    private JRadioButton deadRadioButton;
    private JButton listAliveAgentsButton;
    private JSpinner rankSpinner;
    private JTable agentTable;
    private JButton addMissionButton;
    private JButton editMissionButton;
    private JButton deleteMissionButton;
    private JButton listAllMissionsButton;
    private JTextField spPowerField;
    private JSplitPane agentSplitPane;
    private JSplitPane missionSplitPane;
    private JCheckBox finishedCheckBox;
    private JCheckBox successfulCheckBox;
    private JButton listSucFinMissionsButton;
    private JSpinner spinner2;
    private JButton listMinAgRkMissionsButton;
    private JTable missionTable;
    private JButton listSpPowAgentsButton;
    private JButton listRankButton;
    private JSplitPane assignmentSplitPane;
    private JButton createAssignmentButton;
    private JButton endAssignmentButton;
    private JButton deleteAssignmentButton;
    private JTable assignmentTable;
    private ButtonGroup agentRadioGroup;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        aliveRadioButton = new JRadioButton("alive");
        aliveRadioButton.setSelected(true);

        deadRadioButton = new JRadioButton("dead");

        agentRadioGroup = new ButtonGroup();
        agentRadioGroup.add(aliveRadioButton);
        agentRadioGroup.add(deadRadioButton);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("S.T.A.R.S. Management system");
        frame.setContentPane(new appGui().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
