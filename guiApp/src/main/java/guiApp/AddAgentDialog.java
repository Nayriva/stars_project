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
    private JButton buttonOK, buttonCancel;
    private JTextField nameField, specialPowerField;
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
                dispose();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogNameWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (specialPowerField.getText() == null || specialPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogSpPowerWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (rankSpinner.getValue() == null || ((int) rankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String specialPower = specialPowerField.getText();
        int rank = (int) rankSpinner.getValue();

        Agent agent = new AgentBuilder().name(name).specialPower(specialPower).alive(true).rank(rank).build();
        Thread createAgent = new Thread(() -> {
            synchronized (AppGui.LOCK) {
                try {
                    AppGui.getAgentManager().createAgent(agent);
                } catch (ServiceFailureException ex) {
                    logger.error("Cannot add agent into DB, DB problem - agent: {}", agent, ex.getMessage());
                    JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                            AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                } catch (EntityValidationException ex) {
                    logger.error("Cannot add agent into DB, validation problem - agent: {}", agent, ex.getMessage());
                    JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                            AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        createAgent.start();
        AppGui.getAgentTableModel().addData(agent);
        dispose();
    }
}
