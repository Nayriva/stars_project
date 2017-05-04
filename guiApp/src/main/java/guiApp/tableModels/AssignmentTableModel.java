package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Assignment;
import guiApp.AppGui;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nayriva on 2.5.2017.
 */
public class AssignmentTableModel extends AbstractTableModel {

    private List<Assignment> data = new ArrayList<>();

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
        Assignment assignment = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return assignment.getAgent();
            case 1:
                return assignment.getMission();
            case 2:
                return assignment.getStart();
            case 3:
                return assignment.getEnd();
            default:
                throw new IllegalArgumentException("Invalid rowIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return AppGui.getRb().getString("agentLabel");
            case 1:
                return AppGui.getRb().getString("missionLabel");
            case 2:
                return AppGui.getRb().getString("startLabel");
            case 3:
                return AppGui.getRb().getString("endLabel");
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
                return LocalDate.class;
            case 3:
                return LocalDate.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    public Assignment getAssignment(int index) {
        return data.get(index);
    }

    public void addData(Assignment assignment) {
        data.add(assignment);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
