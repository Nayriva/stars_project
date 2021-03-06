package guiApp;

import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.common.MissionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class AddMissionDialog extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger(AddMissionDialog.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

    private JPanel contentPane;
    private JButton buttonOK, buttonCancel;
    private JTextField nameField, taskField, placeField;
    private JSpinner minAgRankSpinner;

    public AddMissionDialog() {
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

        contentPane.registerKeyboardAction((ActionEvent e) -> dispose()
                , KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction((ActionEvent e) -> buttonOK.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane,
                    rb.getString("missionDialogNameWarning"),
                    rb.getString("errorDialogTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else if (taskField.getText() == null || taskField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane,
                    rb.getString("missionDialogTaskWarning"),
                    rb.getString("errorDialogTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else if (placeField.getText() == null  || placeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPane,
                    rb.getString("missionDialogPlaceWarning"),
                    rb.getString("errorDialogTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else if (minAgRankSpinner.getValue() == null || ((int) minAgRankSpinner.getValue()) < 1) {
            JOptionPane.showMessageDialog(contentPane,
                    rb.getString("missionDialogMinAgRankWarning"),
                    rb.getString("errorDialogTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText();
        String task = taskField.getText();
        String place = placeField.getText();
        int minAgentRank = (int) minAgRankSpinner.getValue();

        AddMissionSwingWorker swingWorker = new AddMissionSwingWorker();
        swingWorker.setMissionToAdd(new MissionBuilder().name(name).task(task).place(place).finished(false)
                .successful(false).minAgentRank(minAgentRank).build());
        swingWorker.execute();
        dispose();
    }

    private class AddMissionSwingWorker extends SwingWorker<Void, Void> {
        private Mission missionToAdd;

        public void setMissionToAdd(Mission missionToAdd) {
            this.missionToAdd = missionToAdd;
        }

        @Override
        protected void done() {
            try {
                get();
                AppGui.getMissionTableModel().addData(missionToAdd);
                AppGui.getAssignmentTableModel().addMissionString(missionToAdd.getId(),
                        missionToAdd.getName() + ", " + missionToAdd.getTask() + ", "
                                + missionToAdd.getPlace() + ", " + missionToAdd.getMinAgentRank() );
            } catch (ExecutionException ex) {
                logger.error("Error while executing addMission - Mission: {}" , missionToAdd, ex.getCause());
                JOptionPane.showMessageDialog(contentPane,
                        rb.getString("missionDialogAddFailed"),
                        rb.getString("errorDialogTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                //left blank intentionally, this should never happen
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            AppGui.getMissionManager().createMission(missionToAdd);
            return null;
        }
    }
}
