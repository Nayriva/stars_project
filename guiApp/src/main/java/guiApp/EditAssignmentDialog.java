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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditAssignmentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditAssignmentDialog.class);

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
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogEntityNotFound"),
                        AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            GetTableDataSwingWorker swingWorker = new GetTableDataSwingWorker();
            swingWorker.execute();
        } catch (ServiceFailureException ex) {
            logger.error("Service failure", ex);
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogServiceFailure"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (TableDataException ex) {
            logger.error("TableDataException", ex);
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogTableDataFailure"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
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
        Mission mission = null;
        Agent agent = null;
        Long missionId, agentId;
        if (agentTable.getSelectedRow() < 0) {
            agentId = assignment.getId();
        } else {
            agent = agentTableModel.getAgent(agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
            agentId = agent.getId();
        }

        if (missionTable.getSelectedRow() < 0) {
            missionId = assignment.getMission();
        } else {
            mission = missionTableModel.getMission(missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
            missionId = mission.getId();
        }
        if (mission != null && agent != null && mission.getMinAgentRank() > agent.getRank()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("assignmentRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!assignment.getMission().equals(missionId) || !assignment.getAgent().equals(agentId)) {
            EditAssignmentSwingWorker swingWorker= new EditAssignmentSwingWorker();
            swingWorker.setAssignmentToEdit(new AssignmentBuilder().id(assignmentId).mission(missionId)
                    .agent(agentId).start(assignment.getStart()).end(assignment.getEnd()).build());
            swingWorker.setAssignmentIndex(assignmentIndex);
            swingWorker.execute();
            dispose();
        }
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
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("assignmentDialogEditFailed"),
                        AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
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
            List<Agent> agents = AppGui.getAgentManager().findAgentsByAlive(true);
            List<Mission> missions = AppGui.getMissionManager().findMissionsByFinished(false);
            missions.removeIf(Mission::isSuccessful);
            agentTableModel.addData(agents);
            missionTableModel.addData(missions);
            return null;
        }
    }
}
