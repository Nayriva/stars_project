package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.common.AgentBuilder;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class EditAgentDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditAgentDialog.class);

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextField spPowerField;
    private JSpinner rankSpinner;
    private JCheckBox aliveCheckBox;

    private Long agentId;
    private int agentIndex;
    private Agent agent;

    public EditAgentDialog(Long agentId, int agentIndex) {
        this.agentId = agentId;
        this.agentIndex = agentIndex;
        try {
            agent = AppGui.getAgentManager().findAgentById(agentId);
        } catch (ServiceFailureException ex) {
            logger.error("Service failure", ex);
        }
        setValues();

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
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (spPowerField.getText() == null || spPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogSpPowerWarning"),
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (rankSpinner.getValue() == null || ((Integer) rankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogRankWarning"),
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String specialPower = spPowerField.getText();
        int rank = (Integer) rankSpinner.getValue();
        Thread editAgent = new Thread(() -> {
            agent = new AgentBuilder().id(agentId).name(name).specialPower(specialPower)
                    .alive(aliveCheckBox.isSelected()).rank(rank).build();
            try {
                AppGui.getAgentManager().updateAgent(agent);
            } catch (ServiceFailureException ex) {
                logger.error("Cannot edit agent, DB problem - agent: {}", agent, ex.getMessage());
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogFailedWarning"),
                        "Error", JOptionPane.WARNING_MESSAGE);
            } catch (EntityValidationException ex) {
                logger.error("Cannot edit agent, validation problem - agent: {}", agent, ex.getMessage());
                JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogFailedWarning"),
                        "Error", JOptionPane.WARNING_MESSAGE);
            }
            AppGui.getAgentTableModel().editData(agentIndex, agent);
        });
        editAgent.start();
        dispose();
    }

    private void setValues() {
        nameField.setText(agent.getName());
        spPowerField.setText(agent.getSpecialPower());
        if (agent.isAlive()) {
            aliveCheckBox.setSelected(true);
        }
        rankSpinner.setValue(agent.getRank());
    }
}
