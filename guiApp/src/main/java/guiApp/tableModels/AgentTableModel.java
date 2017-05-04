package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import guiApp.AppGui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for AgentTable.
 * Created by nayriva on 2.5.2017.
 */
public class AgentTableModel extends AbstractTableModel {

    private List<Agent> data = new ArrayList<>();

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
        Agent agent = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return agent.getName();
            case 1:
                return agent.getSpecialPower();
            case 2:
                return agent.isAlive();
            case 3:
                return agent.getRank();
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
                return AppGui.getRb().getString("spPowerLabel");
            case 2:
                return AppGui.getRb().getString("aliveLabel");
            case 3:
                return AppGui.getRb().getString("rankLabel");
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
                return Boolean.class;
            case 3:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
    }

    public void editData(int index, Agent agent) {
        data.set(index, agent);
        int lastRow = data.size() - 1;
        fireTableRowsUpdated(lastRow, lastRow);
    }

    public Long getAgentId(int index) {
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

    public void addData(Agent agent) {
        data.add(agent);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
