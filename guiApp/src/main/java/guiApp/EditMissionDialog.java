package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.MissionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class EditMissionDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(EditMissionDialog.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JCheckBox successfulCheckBox, finishedCheckBox;
    private JTextField nameField, taskField, placeField;
    private JSpinner minAgRankSpinner;

    private int missionIndex;
    private Mission mission;
    private boolean oldFinished;
    private boolean oldSuccessful;

    public EditMissionDialog(int missionIndex) {
        this.missionIndex = missionIndex;
        mission = AppGui.getMissionTableModel().getMission(missionIndex);
        if (mission == null || mission.getId() == null) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("dialogEntityNotFound"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        oldSuccessful = mission.isSuccessful();
        oldFinished = mission.isFinished();
        setValues();

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
            JOptionPane.showMessageDialog(contentPane, rb.getString("missionDialogNameWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (taskField.getText() == null || taskField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("missionDialogTaskWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (placeField.getText() == null || placeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("missionDialogPlaceWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (minAgRankSpinner.getValue() == null || ((int) minAgRankSpinner.getValue()) < 0) {
            JOptionPane.showMessageDialog(contentPane, rb.getString("missionDialogRankWarning"),
                    rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = nameField.getText();
        String task = taskField.getText();
        String place = placeField.getText();
        int minAgRank = (int) minAgRankSpinner.getValue();

        if (! name.equals(mission.getName()) || ! task.equals(mission.getTask()) || ! place.equals(mission.getPlace())
                 || minAgRank != mission.getMinAgentRank() || successfulCheckBox.isSelected() != mission.isSuccessful()
                || finishedCheckBox.isSelected() != mission.isFinished()) {
            EditMissionSwingWorker swingWorker= new EditMissionSwingWorker();
            swingWorker.setMissionToEdit(new MissionBuilder().id(mission.getId()).name(name).task(task).place(place)
                    .successful(successfulCheckBox.isSelected()).finished(finishedCheckBox.isSelected())
                    .minAgentRank(minAgRank).build());
            swingWorker.execute();
            dispose();
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

    private class EditMissionSwingWorker extends SwingWorker<Void, Void> {
        private Mission missionToEdit;

        public void setMissionToEdit(Mission missionToEdit) {
            this.missionToEdit = missionToEdit;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getMissionTableModel().editData(missionIndex, missionToEdit);
                AppGui.getAssignmentTableModel().editMissionString(missionToEdit.getId(),
                        missionToEdit.getName() + ", " + missionToEdit.getTask() + ", "
                                + missionToEdit.getPlace() + ", " + missionToEdit.getMinAgentRank() );
            } catch (ExecutionException ex) {
                logger.error("Error while executing editMission - Mission: {}" , missionToEdit, ex.getCause());
                JOptionPane.showMessageDialog(contentPane, rb.getString("missionDialogEditFailed"),
                        rb.getString("errorDialogTitle"), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getMissionManager().updateMission(missionToEdit);
            if (oldSuccessful != mission.isSuccessful() || oldFinished != mission.isFinished()) {
                List<Assignment> ofMission = AppGui.getAssignmentManager().findAssignmentsOfMission(mission.getId());
                ofMission.removeIf((assignment -> assignment.getEnd() != null));
                for (Assignment a : ofMission) {
                    a.setEnd(LocalDate.now(Clock.systemUTC()));
                    AppGui.getAssignmentManager().updateAssignment(a);
                }
            }
            return null;
        }
    }
}
