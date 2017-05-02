package guiApp;

import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class EditAgentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextField spPowerField;
    private JRadioButton aliveRadioButton;
    private JRadioButton deadRadioButton;
    private JSpinner spinner1;

    public EditAgentDialog() {

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
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        EditAgentDialog dialog = new EditAgentDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        Locale locale = Locale.getDefault();
        ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);
        aliveRadioButton = new JRadioButton(rb.getString("aliveRadioButton"));
        deadRadioButton = new JRadioButton(rb.getString("deadRadioButton"));
        ButtonGroup agentRadioGroup = new ButtonGroup();
        agentRadioGroup.add(aliveRadioButton);
        agentRadioGroup.add(deadRadioButton);
    }
}
