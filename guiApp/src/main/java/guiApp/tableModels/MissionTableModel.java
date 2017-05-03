package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import guiApp.AppGui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nayriva on 2.5.2017.
 */
public class MissionTableModel extends AbstractTableModel {

    private List<Mission> data = new ArrayList<>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
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
                return mission.isSuccessful();
            case 4:
                return mission.isFinished();
            case 5:
                return mission.getMinAgentRank();
            default:
                throw new IllegalArgumentException("Invalid rowIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return AppGui.getRb().getString("missionTableNameLabel");
            case 1:
                return AppGui.getRb().getString("missionTableTaskLabel");
            case 2:
                return AppGui.getRb().getString("missionTablePlaceLabel");
            case 3:
                return AppGui.getRb().getString("missionTableSuccessfulLabel");
            case 4:
                return AppGui.getRb().getString("missionTableFinishedLabel");
            case 5:
                return AppGui.getRb().getString("missionTableMinAgentRankLabel");
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
            case 4:
                return Boolean.class;
            case 5:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    public void editData(int index, Mission mission) {
        data.set(index, mission);
        int lastRow = data.size() - 1;
        fireTableRowsUpdated(lastRow, lastRow);
    }

    public Long getMissionId(int index) {
        return data.get(index).getId();
    }

    public void deleteData(int index) {
        data.remove(index);
        int lastRow = data.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }

    public void deleteAllData() {
        data.clear();
        int lastRow = data.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }

    public void addData(Mission mission) {
        data.add(mission);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
