package guiApp.tablesResources;

import cz.muni.fi.pv168.db_backend.backend.Mission;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Mission table model.
 *
 * Created by nayriva on 2.5.2017.
 */
public class MissionTableModel extends AbstractTableModel {

    private List<Mission> data = new ArrayList<>();

    private enum Column {
        NAME(String.class, Mission::getName),
        TASK(String.class, Mission::getTask),
        PLACE(String.class, Mission::getPlace),
        SUCCESSFUL(Boolean.class, Mission::isSuccessful),
        FINISHED(Boolean.class, Mission::isFinished),
        MIN_AG_RANK(Integer.class, Mission::getMinAgentRank);

        Column(Class columnType, Function<Mission, Object> valueExtractor) {
            this.columnType = columnType;
            this.valueExtractor = valueExtractor;
        }

        private final Class columnType;
        private final Function<Mission, Object> valueExtractor;
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
        Mission mission = data.get(rowIndex);
        return getColumn(columnIndex).valueExtractor.apply(mission);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).name();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumn(columnIndex).columnType;
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
