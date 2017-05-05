package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.AssignmentBuilder;

import guiApp.tableModels.AssignmentAgentTableModel;
import guiApp.tableModels.AssignmentMissionTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AddAssignmentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddAssignmentDialog.class);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTable missionTable, agentTable;

    private AssignmentMissionTableModel missionTableModel = new AssignmentMissionTableModel();
    private AssignmentAgentTableModel agentTableModel = new AssignmentAgentTableModel();

    public AddAssignmentDialog() {
        try {
            GetTableDataSwingWorker swingWorker = new GetTableDataSwingWorker();
            swingWorker.execute();
        }  catch (TableDataException ex) {
            logger.error("TableDataException", ex);
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogTableDataFailure"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        missionTable.setModel(missionTableModel);
        agentTable.setModel(agentTableModel);

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
        if (agentTable.getSelectedRow() < 0 || missionTable.getSelectedRow() < 0) {
            return;
        }

        Mission mission = missionTableModel.getMission(missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
        Agent agent = agentTableModel.getAgent(agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
        if (mission.getMinAgentRank() > agent.getRank()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("assignmentRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        AddAssignmentSwingWorker swingWorker = new AddAssignmentSwingWorker();
        swingWorker.setAssignmentToAdd(new AssignmentBuilder().agent(agent.getId()).mission(mission.getId())
                .start(LocalDate.now()).end(null).build());
        swingWorker.execute();
        dispose();
    }

    private class AddAssignmentSwingWorker extends SwingWorker<Void, Void> {
        private Assignment assignmentToAdd;

        public void setAssignmentToAdd(Assignment assignmentToAdd) {
            this.assignmentToAdd = assignmentToAdd;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getAssignmentTableModel().addData(assignmentToAdd);
            }  catch (ExecutionException ex) {
                logger.error("Error while executing addAssignment - Assignment: {}" , assignmentToAdd, ex.getCause());
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("assignmentDialogAddFailed"),
                        AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getAssignmentManager().createAssignment(assignmentToAdd);
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
