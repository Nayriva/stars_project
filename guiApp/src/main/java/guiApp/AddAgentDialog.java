package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.common.AgentBuilder;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;

public class AddAgentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddAgentDialog.class);

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextField specialPowerField;
    private JSpinner rankSpinner;

    public AddAgentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogNameWarning"),
                    "Error", JOptionPane.WARNING_MESSAGE);
        } else if (specialPowerField.getText() == null || specialPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogSpPowerWarning"),
                    "Error", JOptionPane.WARNING_MESSAGE);
        } else if (rankSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogRankWarning"),
                    "Error", JOptionPane.WARNING_MESSAGE);
        }
        String name = nameField.getText();
        String specialPower = specialPowerField.getText();
        int rank = (Integer) rankSpinner.getValue();
        Thread createAgent = new Thread(() -> {
            Agent agent = new AgentBuilder().name(name).specialPower(specialPower).alive(true).rank(rank).build();
            try {
                AppGui.getAgentManager().createAgent(agent);
            } catch (ServiceFailureException ex) {
                logger.error("Cannot add agent into DB, DB problem - agent: {}", agent, ex.getMessage());
            } catch (EntityValidationException ex) {
                logger.error("Cannot add agent into DB, validation problem - agent: {}", agent, ex.getMessage());
            }
            AppGui.getAgentTableModel().addData(agent);
        });
        createAgent.start();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AddAgentDialog dialog = new AddAgentDialog();
        dialog.setTitle(AppGui.getRb().getString("addAgentDialogTitle"));
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
