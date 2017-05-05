package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Mission;
import guiApp.AppGui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: create javadoc
 *
 * @author Dominik Frantisek Bucik
 */
public class AssignmentMissionTableModel extends AbstractTableModel {
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
                return AppGui.getRb().getString("nameLabel");
            case 1:
                return AppGui.getRb().getString("taskLabel");
            case 2:
                return AppGui.getRb().getString("placeLabel");
            case 3:
                return AppGui.getRb().getString("minAgRankLabel");
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

    public void addData(List<Mission> missions) {
        data.addAll(missions);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
