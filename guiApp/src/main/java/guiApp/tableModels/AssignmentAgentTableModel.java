package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Agent;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * TODO: create javadoc
 *
 * @author Dominik Frantisek Bucik
 */
public class AssignmentAgentTableModel extends AbstractTableModel {
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);
    private List<Agent> data = new ArrayList<>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Agent agent = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return agent.getName();
            case 1:
                return agent.getSpecialPower();
            case 2:
                return agent.getRank();
            default:
                throw new IllegalArgumentException("Invalid rowIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rb.getString("nameLabel");
            case 1:
                return rb.getString("spPowerLabel");
            case 2:
                return rb.getString("rankLabel");
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    public Agent getAgent(int index) {
        return data.get(index);
    }

    public void addData(List<Agent> agents) {
        data.addAll(agents);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
