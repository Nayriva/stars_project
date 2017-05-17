package guiApp.tablesResources;

import cz.muni.fi.pv168.db_backend.backend.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/** Assignment table model
 *
 * Created by nayriva on 2.5.2017.
 */
public class AssignmentTableModel extends AbstractTableModel {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTableModel.class);
    private List<Assignment> data = new ArrayList<>();
    private Map<Long, String> missions = new HashMap<>();
    private Map<Long, String> agents = new HashMap<>();

    @SuppressWarnings("unchecked")
    public AssignmentTableModel() {
        try (ObjectInputStream agentsOIS = new ObjectInputStream(new FileInputStream(
                "additionalResources/serialization/agentStrings.ser"));
             ObjectInputStream missionsOIS = new ObjectInputStream(new FileInputStream("" +
                     "additionalResources/serialization/missionStrings.ser")))
        {
            logger.debug("Deserialization of hashmaps...");
            agents = (HashMap) agentsOIS.readObject();
            missions = (HashMap) missionsOIS.readObject();
            logger.debug("Deserialization of hashmaps finished...");
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Deserialization failed", ex);
            System.exit(3);
        }
    }

    private enum Column {
        AGENT(String.class, Assignment::getAgent),
        MISSION(String.class, Assignment::getMission),
        START(LocalDate.class, Assignment::getStart),
        END(LocalDate.class, Assignment::getEnd);

        Column(Class columnType, Function<Assignment, Object> valueExtractor) {
            this.columnType = columnType;
            this.valueExtractor = valueExtractor;
        }

        private final Class columnType;
        private final Function<Assignment, Object> valueExtractor;
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
        Assignment assignment = data.get(rowIndex);
        switch (columnIndex) {
            case 0: {
                Long id = (Long) getColumn(columnIndex).valueExtractor.apply(assignment);
                return agents.get(id);
            }
            case 1: {
                Long id = (Long) getColumn(columnIndex).valueExtractor.apply(assignment);
                return missions.get(id);
            }
            default:
                return getColumn(columnIndex).valueExtractor.apply(assignment);
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).name();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumn(columnIndex).columnType;
    }

    public void editData(int index, Assignment assignment) {
        data.set(index, assignment);
        int lastRow = data.size() - 1;
        fireTableRowsUpdated(lastRow, lastRow);
    }

    public Long getAssignmentId(int index) {
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

    public void addData(Assignment assignment) {
        data.add(assignment);
        int lastRow = data.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    public Assignment getAssignment(int assignmentIndex) {
        return data.get(assignmentIndex);
    }

    public boolean isEnded(int index) {
        return data.get(index).getEnd() != null;
    }

    public void addMissionString(Long missionId, String missionString) {
        missions.put(missionId, missionString);
    }

    public void addAgentString(Long agentId, String agentString) {
        agents.put(agentId, agentString);
    }

    public void editMissionString(Long missionId, String missionString) {
        missions.put(missionId, missionString);
    }

    public void editAgentString(Long agentId, String agentString) {
        agents.put(agentId, agentString);
    }

    public Map<Long, String> getMissions() {
        return missions;
    }

    public Map<Long, String> getAgents() {
        return agents;
    }
}
