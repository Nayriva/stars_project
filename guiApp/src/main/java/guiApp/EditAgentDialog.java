package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.common.AgentBuilder;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditAgentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditAgentDialog.class);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTextField nameField, spPowerField;
    private JSpinner rankSpinner;
    private JCheckBox aliveCheckBox;

    private Long agentId;
    private int agentIndex;
    private Agent agent;
    private boolean oldAlive;

    public EditAgentDialog(Long agentId, int agentIndex) {
        this.agentId = agentId;
        this.agentIndex = agentIndex;
        try {
            agent = AppGui.getAgentManager().findAgentById(agentId);
            if (agent == null) {
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogEntityNotFound"),
                        AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.oldAlive = agent.isAlive();
        } catch (ServiceFailureException ex) {
            logger.error("Service failure", ex);
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("dialogServiceFailure"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        setInitialValues();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((ActionEvent e ) -> onOK());

        buttonCancel.addActionListener((ActionEvent e) -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction((ActionEvent e) -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction((ActionEvent e) -> buttonOK.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogNameWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (spPowerField.getText() == null || spPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogSpPowerWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (rankSpinner.getValue() == null || ((int) rankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText();
        String specialPower = spPowerField.getText();
        int rank = (int) rankSpinner.getValue();

        if (!name.equals(agent.getName()) || !specialPower.equals(agent.getSpecialPower()) || rank != agent.getRank()
                || aliveCheckBox.isSelected() != agent.isAlive()) {
            EditAgentSwingWorker swingWorker = new EditAgentSwingWorker();
            swingWorker.setAgentToEdit(new AgentBuilder().id(agentId).name(name).specialPower(specialPower)
                    .alive(aliveCheckBox.isSelected()).rank(rank).build());
            swingWorker.execute();
            dispose();
        }
    }

    private void setInitialValues() {
        nameField.setText(agent.getName());
        spPowerField.setText(agent.getSpecialPower());
        if (agent.isAlive()) {
            aliveCheckBox.setSelected(true);
        }
        rankSpinner.setValue(agent.getRank());
    }

    private class EditAgentSwingWorker extends SwingWorker<Void, Void> {
        private Agent agentToEdit;

        public void setAgentToEdit(Agent agentToEdit) {
            this.agentToEdit = agentToEdit;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getAgentTableModel().editData(agentIndex, agentToEdit);
                AppGui.getAssignmentTableModel().editAgentString(agentToEdit.getId(),
                        agentToEdit.getName() + ", " + agentToEdit.getSpecialPower() + ", " + agentToEdit.getRank());
            } catch (ExecutionException ex) {
                logger.error("Error while executing editAgent - Agent: {}" , agentToEdit, ex.getCause());
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogEditFailed"),
                        AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getAgentManager().updateAgent(agentToEdit);
            if (oldAlive != agentToEdit.isAlive()) {
                List<Assignment> ofAgent = AppGui.getAssignmentManager().findAssignmentsOfAgent(agent.getId());
                ofAgent.removeIf((assignment -> assignment.getEnd() != null));
                for (Assignment a : ofAgent) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    AppGui.getAssignmentManager().updateAssignment(a);
                }
            }
            return null;
        }
    }
}
