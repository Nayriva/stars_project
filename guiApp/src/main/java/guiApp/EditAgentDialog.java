package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.common.AgentBuilder;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

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
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (spPowerField.getText() == null || spPowerField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogSpPowerWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (rankSpinner.getValue() == null || ((int) rankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("agentDialogRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String specialPower = spPowerField.getText();
        int rank = (int) rankSpinner.getValue();

        if (! name.equals(agent.getName()) || ! specialPower.equals(agent.getSpecialPower()) || rank != agent.getRank()
                || aliveCheckBox.isSelected() != agent.isAlive()) {
            boolean oldAlive = agent.isAlive();
            agent = new AgentBuilder().id(agentId).name(name).specialPower(specialPower)
                    .alive(aliveCheckBox.isSelected()).rank(rank).build();
            Thread editAgent = new Thread(() -> {
                synchronized (AppGui.LOCK) {
                    updateInDB(oldAlive);
                }
            });
            editAgent.start();
            AppGui.getAgentTableModel().editData(agentIndex, agent);
            dispose();
        }
    }

    private void updateInDB(boolean oldAlive) {
        try {
            AppGui.getAgentManager().updateAgent(agent);
            if (oldAlive != agent.isAlive()) {
                List<Assignment> ofAgent = AppGui.getAssignmentManager().findAssignmentsOfAgent(agent.getId());
                ofAgent.removeIf((assignment -> assignment.getEnd() != null));
                for (Assignment a: ofAgent) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    AppGui.getAssignmentManager().updateAssignment(a);
                }
            }
        } catch (ServiceFailureException ex) {
            logger.error("Cannot edit agent, DB problem - agent: {}", agent, ex.getMessage());
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
        } catch (EntityValidationException ex) {
            logger.error("Cannot edit agent, validation problem - agent: {}", agent, ex.getMessage());
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
        }
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
