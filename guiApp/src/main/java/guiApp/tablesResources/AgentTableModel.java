package guiApp.tablesResources;

import cz.muni.fi.pv168.db_backend.backend.Agent;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Table model for AgentTable.
 *
 * Created by nayriva on 2.5.2017.
 */
public class AgentTableModel extends AbstractTableModel {
    private List<Agent> data = new ArrayList<>();

    private enum Column {
        NAME(String.class, Agent::getName),
        SPECIAL_POWER(String.class, Agent::getSpecialPower),
        ALIVE(Boolean.class, Agent::isAlive),
        RANK(Integer.class, Agent::getRank);

        private Column(Class<?> columnType, Function<Agent, Object> valueExtractor) {
            this.columnType = columnType;
            this.valueExtractor = valueExtractor;
        }

        private final Class columnType;
        private final Function<Agent, Object> valueExtractor;
    }

    private Column getColumn(int columnIndex) {
        return Column.values() [columnIndex];
    }



    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Agent agent = data.get(rowIndex);
        return getColumn(columnIndex).valueExtractor.apply(agent);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).name();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumn(columnIndex).columnType;
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

    public Agent getAgent(int agentIndex) {
        return data.get(agentIndex);
    }
}
