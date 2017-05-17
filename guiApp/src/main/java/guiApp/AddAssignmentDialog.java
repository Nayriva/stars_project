package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.AssignmentBuilder;

import guiApp.tablesResources.AssignmentAgentTableModel;
import guiApp.tablesResources.AssignmentMissionTableModel;
import guiApp.tablesResources.LocalizedHeaderRendered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AddAssignmentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddAssignmentDialog.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

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
            JOptionPane.showMessageDialog(contentPane, rb.getString("dialogTableDataFailure"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        missionTable.setModel(missionTableModel);
        missionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        missionTable.getTableHeader().setDefaultRenderer(new LocalizedHeaderRendered(rb));
        agentTable.setModel(agentTableModel);
        agentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentTable.getTableHeader().setDefaultRenderer(new LocalizedHeaderRendered(rb));

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
            JOptionPane.showMessageDialog(contentPane, rb.getString("addAssignmentEntitiesWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mission mission = missionTableModel.getMission(missionTable.convertColumnIndexToModel(missionTable.getSelectedRow()));
        Agent agent = agentTableModel.getAgent(agentTable.convertColumnIndexToModel(agentTable.getSelectedRow()));
        if (mission.getMinAgentRank() > agent.getRank()) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("assignmentRankWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(contentPane, rb.getString("assignmentDialogAddFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
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
