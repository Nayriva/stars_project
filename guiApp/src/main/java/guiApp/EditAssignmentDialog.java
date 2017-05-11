package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.AssignmentBuilder;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import guiApp.tableModels.AssignmentAgentTableModel;
import guiApp.tableModels.AssignmentMissionTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EditAssignmentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditAssignmentDialog.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTable missionTable, agentTable;
    private JTextField missionTextField, agentTextField;

    private Long assignmentId;
    private int assignmentIndex;
    private Assignment assignment;
    private AssignmentMissionTableModel missionTableModel = new AssignmentMissionTableModel();
    private AssignmentAgentTableModel agentTableModel = new AssignmentAgentTableModel();

    public EditAssignmentDialog(Long assignmentId, int assignmentIndex) {
        this.assignmentId = assignmentId;
        this.assignmentIndex = assignmentIndex;
        try {
            assignment = AppGui.getAssignmentManager().findAssignmentById(assignmentId);
            if (assignment == null) {
                JOptionPane.showMessageDialog(contentPane, rb.getString("dialogEntityNotFound"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            GetTableDataSwingWorker swingWorker = new GetTableDataSwingWorker();
            swingWorker.execute();
        } catch (ServiceFailureException ex) {
            logger.error("Service failure", ex);
            JOptionPane.showMessageDialog(contentPane, rb.getString("dialogServiceFailure"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (TableDataException ex) {
            logger.error("TableDataException", ex);
            JOptionPane.showMessageDialog(contentPane, rb.getString("dialogTableDataFailure"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        missionTable.setModel(missionTableModel);
        agentTable.setModel(agentTableModel);

        missionTextField.setText(AppGui.getAssignmentTableModel().getMissions().get(assignment.getMission()));
        agentTextField.setText(AppGui.getAssignmentTableModel().getAgents().get(assignment.getAgent()));

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((ActionEvent e) -> onOK());

        buttonCancel.addActionListener((ActionEvent e) -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction((ActionEvent e) -> dispose()
                , KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction((ActionEvent e) -> buttonOK.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String agentParts[] = agentTextField.getText().split(", ");
        String missionParts[] = missionTextField.getText().split(", ");
        Long missionId = assignment.getMission();
        Long agentId = assignment.getAgent();
        int agentRank= Integer.parseInt(agentParts[2]);
        int missionMinAgRank = Integer.parseInt(missionParts[3]);

        if (agentTable.getSelectedRow() >= 0) {
            Agent agent = agentTableModel.getAgent(
                    agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
            agentId = agent.getId();
            agentRank = agent.getRank();
        }

        if (missionTable.getSelectedRow() >= 0) {
            Mission mission =  missionTableModel.getMission(
                    missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
            missionId = mission.getId();
            missionMinAgRank = mission.getMinAgentRank();
        }

        if (missionMinAgRank > agentRank) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("assignmentEditRankWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!assignment.getMission().equals(missionId) || !assignment.getAgent().equals(agentId)) {
            EditAssignmentSwingWorker swingWorker= new EditAssignmentSwingWorker();
            swingWorker.setAssignmentToEdit(new AssignmentBuilder().id(assignmentId).mission(missionId)
                    .agent(agentId).start(assignment.getStart()).end(assignment.getEnd()).build());
            swingWorker.setAssignmentIndex(assignmentIndex);
            swingWorker.execute();
        }
        dispose();
    }

    private class EditAssignmentSwingWorker extends SwingWorker<Void, Void> {
        private Assignment assignmentToEdit;
        private int assignmentIndex;

        public void setAssignmentToEdit(Assignment assignmentToEdit) {
            this.assignmentToEdit = assignmentToEdit;
        }

        public void setAssignmentIndex(int assignmentIndex) {
            this.assignmentIndex = assignmentIndex;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getAssignmentTableModel().editData(assignmentIndex, assignmentToEdit);
            }  catch (ExecutionException ex) {
                logger.error("Error while executing editAssignment - Assignment: {}" , assignmentToEdit, ex.getCause());
                JOptionPane.showMessageDialog(contentPane, rb.getString("assignmentDialogEditFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getAssignmentManager().updateAssignment(assignmentToEdit);
            return null;
        }
    }

    private class GetTableDataSwingWorker extends SwingWorker<Void, Void> {
        @Override
        protected void done() {
            try {
                get();
            } catch (ExecutionException | InterruptedException ex) {
                throw new TableDataException(ex);
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            List<Assignment> active = AppGui.getAssignmentManager().findActiveAssignments();
            List<Long> activeAssignmentAgId = active.stream().map(Assignment::getAgent).collect(Collectors.toList());
            List<Agent> agents = AppGui.getAgentManager().findAgentsByAlive(true);
            agents.removeIf((agent) -> activeAssignmentAgId.contains(agent.getId()));

            List<Mission> missions = AppGui.getMissionManager().findMissionsByFinished(false);
            missions.removeIf(Mission::isSuccessful);
            agentTableModel.addData(agents);
            missionTableModel.addData(missions);
            return null;
        }
    }
}
