package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.MissionBuilder;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class EditMissionDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditMissionDialog.class);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JCheckBox successfulCheckBox, finishedCheckBox;
    private JTextField nameField, taskField, placeField;
    private JSpinner minAgRankSpinner;

    private Long missionId;
    private int missionIndex;
    private Mission mission;

    public EditMissionDialog(Long missionId, int missionIndex) {
        this.missionId = missionId;
        this.missionIndex = missionIndex;
        try {
            mission = AppGui.getMissionManager().findMissionById(missionId);
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
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogNameWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (taskField.getText() == null || taskField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogTaskWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (placeField.getText() == null || placeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogPlaceWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (minAgRankSpinner.getValue() == null || ((int) minAgRankSpinner.getValue()) < 0) {
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("missionDialogRankWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String task = taskField.getText();
        String place = placeField.getText();
        int minAgRank = (int) minAgRankSpinner.getValue();

        if (! name.equals(mission.getName()) || ! task.equals(mission.getTask()) || ! place.equals(mission.getPlace())
                 || minAgRank != mission.getMinAgentRank() || successfulCheckBox.isSelected() != mission.isSuccessful()
                || finishedCheckBox.isSelected() != mission.isFinished()) {
            boolean oldSuccessful = mission.isSuccessful();
            boolean oldFinished = mission.isFinished();
            mission = new MissionBuilder().id(missionId).name(name).task(task).place(place)
                    .successful(successfulCheckBox.isSelected()).finished(finishedCheckBox.isSelected()).minAgentRank(minAgRank).build();
            Thread editMission = new Thread(() -> {
                synchronized (AppGui.LOCK) {
                    updateInDB(oldSuccessful, oldFinished);
                }
            });
            editMission.start();
            AppGui.getMissionTableModel().editData(missionIndex, mission);
            dispose();
        }
    }

    private void updateInDB(boolean oldSuccessful, boolean oldFinished) {
        try {
            AppGui.getMissionManager().updateMission(mission);
            if (oldSuccessful != mission.isSuccessful() || oldFinished != mission.isFinished()) {
                List<Assignment> ofMission = AppGui.getAssignmentManager().findAssignmentsOfMission(mission.getId());
                ofMission.removeIf((assignment -> assignment.getEnd() != null));
                for (Assignment a: ofMission) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    AppGui.getAssignmentManager().updateAssignment(a);
                }
            }
        } catch (ServiceFailureException ex) {
            logger.error("Cannot edit mission, DB problem - mission: {}", mission, ex.getMessage());
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
        } catch (EntityValidationException ex) {
            logger.error("Cannot edit mission, validation problem - mission: {}", mission, ex.getMessage());
            JOptionPane.showMessageDialog(contentPane, AppGui.getRb().getString("operationFailedWarning"),
                    AppGui.getRb().getString("errorDialogTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setValues() {
        nameField.setText(mission.getName());
        taskField.setText(mission.getTask());
        placeField.setText(mission.getPlace());
        if (mission.isSuccessful()) {
            successfulCheckBox.setSelected(true);
        }
        if (mission.isFinished()) {
            finishedCheckBox.setSelected(true);
        }
        minAgRankSpinner.setValue(mission.getMinAgentRank());
    }
}
