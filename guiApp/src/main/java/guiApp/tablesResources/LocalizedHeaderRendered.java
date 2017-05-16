package guiApp.tablesResources;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * TODO: create javadoc
 *
 * @author Dominik Frantisek Bucik
 */
public class LocalizedHeaderRendered extends DefaultTableCellRenderer {

    private ResourceBundle rs;

    public LocalizedHeaderRendered(ResourceBundle rs) {
        super();
        this.rs = rs;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.PINK);
        switch(table.getModel().getColumnName(column)) {
            case "NAME": setText(rs.getString("nameLabel"));
                break;
            case "SPECIAL_POWER": setText(rs.getString("spPowerLabel"));
                break;
            case "ALIVE": setText(rs.getString("aliveLabel"));
                break;
            case "RANK": setText(rs.getString("rankLabel"));
                break;
            case "TASK": setText(rs.getString("taskLabel"));
                break;
            case "PLACE": setText(rs.getString("placeLabel"));
                break;
            case "SUCCESSFUL": setText(rs.getString("successfulLabel"));
                break;
            case "FINISHED": setText(rs.getString("finishedLabel"));
                break;
            case "MIN_AG_RANK": setText(rs.getString("minAgRankLabel"));
                break;
            case "AGENT": setText(rs.getString("agentLabel"));
                break;
            case "MISSION": setText(rs.getString("missionLabel"));
                break;
            case "START": setText(rs.getString("startLabel"));
                break;
            case "END": setText(rs.getString("endLabel"));
                break;
        }
        return this;
    }
}
