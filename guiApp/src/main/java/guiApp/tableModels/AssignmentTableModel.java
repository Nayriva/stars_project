package guiApp.tableModels;

import cz.muni.fi.pv168.db_backend.backend.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by nayriva on 2.5.2017.
 */
public class AssignmentTableModel extends AbstractTableModel {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTableModel.class);
    private Locale locale = Locale.getDefault();
    private ResourceBundle rb = ResourceBundle.getBundle("guiApp.localization", locale);

    private List<Assignment> data = new ArrayList<>();
    private Map<Long, String> missions = new HashMap<>();
    private Map<Long, String> agents = new HashMap<>();

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
                return agents.get(assignment.getAgent());
            case 1:
                return missions.get(assignment.getMission());
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
                return rb.getString("agentLabel");
            case 1:
                return rb.getString("missionLabel");
            case 2:
                return rb.getString("startLabel");
            case 3:
                return rb.getString("endLabel");
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
            case 3:
                return LocalDate.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex");
        }
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
