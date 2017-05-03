package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.MissionBuilder;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;

public class AddMissionDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddMissionDialog.class);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTextField nameField, taskField, placeField;
    private JSpinner minAgRankSpinner;

    public AddMissionDialog() {
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
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogNameWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (taskField.getText() == null || taskField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogTaskWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (placeField.getText() == null  || placeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogPlaceWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (minAgRankSpinner.getValue() == null || ((int) minAgRankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogMinAgRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String task = taskField.getText();
        String place = placeField.getText();
        int minAgentRank = (int) minAgRankSpinner.getValue();

        Mission mission = new MissionBuilder().name(name).task(task).place(place).finished(false)
                .successful(false).minAgentRank(minAgentRank).build();
        Thread createMission = new Thread(() -> {
            synchronized (AppGui.LOCK) {
                try {
                    AppGui.getMissionManager().createMission(mission);
                } catch (ServiceFailureException ex) {
                    logger.error("Cannot add mission into DB, DB problem - mission: {}", mission, ex.getMessage());
                    JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                            AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                } catch (EntityValidationException ex) {
                    logger.error("Cannot add mission into DB, validation problem - mission: {}", mission, ex.getMessage());
                    JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                            AppGui.getRb().getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        createMission.start();
        AppGui.getMissionTableModel().addData(mission);
        dispose();
    }
}
