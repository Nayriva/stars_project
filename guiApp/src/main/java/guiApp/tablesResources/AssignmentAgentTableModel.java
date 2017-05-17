package guiApp.tablesResources;

import cz.muni.fi.pv168.db_backend.backend.Agent;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Table model for agent table inside assignment dialogs
 *
 * @author Dominik Frantisek Bucik
 */
public class AssignmentAgentTableModel extends AbstractTableModel {
    private List<Agent> data = new ArrayList<>();

    private enum Column {
        NAME(String.class, Agent::getName),
        SPECIAL_POWER(String.class, Agent::getSpecialPower),
        RANK(Integer.class, Agent::getRank);

        Column(Class<?> columnType, Function<Agent, Object> valueExtractor) {
            this.columnType = columnType;
            this.valueExtractor = valueExtractor;
        }

        private final Class columnType;
        private final Function<Agent, Object> valueExtractor;
    }

    private Column getColumn(int columnIndex) {
        return Column.values()[columnIndex];
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

    public Agent getAgent(int index) {
        return data.get(index);
    }

    public void addData(List<Agent> agents) {
        data.addAll(agents);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
}
