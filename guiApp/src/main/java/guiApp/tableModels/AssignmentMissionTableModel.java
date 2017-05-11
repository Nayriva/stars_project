package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Mission;

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
public class AssignmentMissionTableModel extends AbstractTableModel {
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);
    private List<Mission> data = new ArrayList<>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Mission mission = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return mission.getName();
            case 1:
                return mission.getTask();
            case 2:
                return mission.getPlace();
            case 3:
                return mission.getMinAgentRank();
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
                return rb.getString("taskLabel");
            case 2:
                return rb.getString("placeLabel");
            case 3:
                return rb.getString("minAgRankLabel");
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    public Mission getMission(int index) {
        return data.get(index);
    }

    public int getMissionMinAgRank(Long id) {
        for (Mission m: data) {
            if (m.getId().equals(id)) {
                return m.getMinAgentRank();
            }
        }
        return -1;
    }

    public void addData(List<Mission> missions) {
        data.addAll(missions);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
