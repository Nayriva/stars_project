package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.common.AgentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class AddAgentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddAgentDialog.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTextField nameField, specialPowerField;
    private JSpinner rankSpinner;

    public AddAgentDialog() {
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

        contentPane.registerKeyboardAction((ActionEvent e) -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction((ActionEvent e) -> buttonOK.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("agentDialogNameWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (specialPowerField.getText() == null || specialPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("agentDialogSpPowerWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (rankSpinner.getValue() == null || ((int) rankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("agentDialogRankWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText();
        String specialPower = specialPowerField.getText();
        int rank = (int) rankSpinner.getValue();

        AddAgentSwingWorker swingWorker = new AddAgentSwingWorker();
        swingWorker.setAgentToAdd(
                new AgentBuilder().name(name).specialPower(specialPower).alive(true).rank(rank).build());
        swingWorker.execute();
        dispose();
    }

    private class AddAgentSwingWorker extends SwingWorker <Void, Void> {
        private Agent agentToAdd;

        public void setAgentToAdd(Agent agentToAdd) {
            this.agentToAdd = agentToAdd;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getAgentTableModel().addData(agentToAdd);
                AppGui.getAssignmentTableModel().addAgentString(agentToAdd.getId(),
                        agentToAdd.getName() + ", " + agentToAdd.getSpecialPower() + ", " + agentToAdd.getRank());
            } catch (ExecutionException ex) {
                logger.error("Error while executing addAgent - Agent: {}" , agentToAdd, ex.getCause());
                JOptionPane.showMessageDialog(contentPane, rb.getString("agentDialogAddFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getAgentManager().createAgent(agentToAdd);
            return null;
        }
    }
}
