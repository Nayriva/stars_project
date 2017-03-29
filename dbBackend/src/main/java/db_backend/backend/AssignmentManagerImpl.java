package db_backend.backend;

import db_backend.common.*;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implements methods to manage Assignments.
 *
 * Created by nayriva on 7.3.2017.
 */
public class AssignmentManagerImpl implements AssignmentManager{

    private DataSource dataSource;
    private final Clock clock;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    public AssignmentManagerImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void createAssignment(Assignment assignment)
            throws ServiceFailureException, IllegalEntityException,
            IllegalArgumentException, EntityValidationException
    {
        checkDataSource();
        validate(assignment, true);
        if (assignment.getId() != null) {
            throw new IllegalEntityException("Assignment has already got assigned ID!");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO assignments(mission_id, agent_id, starting, ending) " +
                            "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
            {
                stm.setLong(1, assignment.getMission().getId());
                stm.setLong(2, assignment.getAgent().getId());
                assignment.setStart(LocalDate.now(clock));
                stm.setDate(3, toSQLDate(assignment.getStart()));
                stm.setDate(4, toSQLDate(assignment.getEnd()));

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, assignment, true);
                Long generatedId = DBUtils.getId(stm.getGeneratedKeys());
                assignment.setId(generatedId);

                conn.commit();
            } catch (SQLException ex) {
                String errorMsg = "Error when inserting new assignment into DB!";
                conn.rollback();
                assignment.setStart(null);
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }

    }

    @Override
    public void updateAssignment(Assignment assignment)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException {

        checkDataSource();
        validate(assignment, false);
        if (assignment.getId() == null) {
            throw new IllegalEntityException("Assignment hasn't got associated ID!");
        } else if (assignment.getId() < 0) {
            throw new IllegalEntityException("Assignment ID must be >= 0!");
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "UPDATE assignments SET mission_id = ?, agent_id = ?, ending = ? WHERE id = ?"))
            {
                stm.setLong(1, assignment.getMission().getId());
                stm.setLong(2, assignment.getAgent().getId());
                stm.setDate(3, toSQLDate(assignment.getEnd()));
                stm.setLong(4, assignment.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, assignment, false);
                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when updating assignment in DB!";
                conn.rollback();
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
    }

    @Override
    public void deleteAssignment(Assignment assignment)
            throws ServiceFailureException, IllegalEntityException {

        checkDataSource();
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is NULL!");
        }
        else if (assignment.getId() == null) {
            throw new IllegalEntityException("Assignment hasn't got associated ID!");
        } else if (assignment.getId() < 0) {
            throw new IllegalEntityException("Assignment ID must be >= 0!");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "DELETE FROM assignments WHERE id = ?"))
            {
                stm.setLong(1, assignment.getId());
                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, assignment, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when deleting assignment from DB!";
                conn.rollback();
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
    }

    @Override
    public Assignment findAssignmentById(Long id) throws ServiceFailureException {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException("ID is null!");
        } else if (id < 0) {
            throw new IllegalArgumentException("ID must be >= 0");
        }

        Assignment result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM assignments WHERE id = ?")) {
                stm.setLong(1, id);
                result = executeQueryForSingleAssignment(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when trying to find assignment by ID!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Assignment> findAllAssignments() throws ServiceFailureException {
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM assignments")) {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments from DB!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Assignment> findAssignmentsOfAgent(Agent agent)
            throws ServiceFailureException, IllegalEntityException {

        checkDataSource();
        if (agent == null) {
            throw new IllegalArgumentException("Agent is null!");
        } else if (agent.getId() == null) {
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (agent.getId() < 0) {
            throw new IllegalEntityException("Agent's ID must be >= 0!");
        }

        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE agent_id = ?"))
            {
                stm.setLong(1, agent.getId());
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments of agent " + agent + "!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Assignment> findAssignmentsOfMission(Mission mission)
            throws ServiceFailureException, IllegalEntityException {

        checkDataSource();
        if (mission == null) {
            throw new IllegalArgumentException("Mission is null!");
        } else if (mission.getId() == null) {
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (mission.getId() < 0) {
            throw new IllegalEntityException("Agent's ID must be >= 0!");
        }

        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE mission_id = ?"))
            {
                stm.setLong(1, mission.getId());
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments of " + mission + "!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Assignment> findActiveAssignments() throws ServiceFailureException {
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE ending IS NULL"))
            {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all active assignments!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Assignment> findEndedAssignments() throws ServiceFailureException {
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE ending IS NOT NULL"))
            {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all past assignments!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    private void validate(Assignment assignment, boolean insert) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is null!");
        }
        if (assignment.getMission() == null) {
            throw new EntityValidationException("Invalid mission field: mission is null!");
        }
        if (assignment.getAgent() == null) {
            throw new EntityValidationException("Invalid agent field: agent is null!");
        }
        if (assignment.getStart() == null) {
            throw new EntityValidationException("Invalid start field: start is null!");
        }
        if (insert && assignment.getEnd() != null) {
            throw new EntityValidationException("Invalid end field: end is not null when creating assignment!");
        }
        if (assignment.getMission().getId() == null || assignment.getMission().getId() < 0) {
            throw new EntityValidationException("Invalid mission field: id of mission!");
        }
        if (assignment.getMission().isSuccessful()) {
            throw new AssignmentException("Invalid mission field: mission already marked as successful!");
        }
        if (assignment.getMission().isFinished()) {
            throw new AssignmentException("Invalid mission field: mission already marked as finished!");
        }
        if (assignment.getAgent().getId() == null || assignment.getAgent().getId() < 0) {
            throw new EntityValidationException("Invalid agent field: id of agent!");
        }
        if (!assignment.getAgent().isAlive()) {
            throw new AssignmentException("Invalid agent field: agent is dead!");
        }
        if (assignment.getAgent().isOnMission()) {
            throw new AssignmentException("Invalid agent field: agent already assigned to mission!");
        }
        if (assignment.getAgent().getRank() < assignment.getMission().getMinAgentRank()) {
            throw new AssignmentException(
                    "Invalid assignment: agent's rank is not high enough for this mission!");
        }
        if (insert && assignment.getStart().isBefore(LocalDate.now(clock))) {
            throw new EntityValidationException("Cannot create past assignment");
        }
        if (!insert && assignment.getEnd() != null && assignment.getEnd().isBefore(LocalDate.now(clock))) {
            throw new EntityValidationException("Cannot update end to past date!");
        }
    }

    private static Date toSQLDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Assignment rowToAssignment(ResultSet rs) throws SQLException {
        MissionManagerImpl missionManager = new MissionManagerImpl();
        AgentManagerImpl agentManager = new AgentManagerImpl();
        missionManager.setDataSource(dataSource);
        agentManager.setDataSource(dataSource);

        Assignment result = new Assignment();
        result.setId(rs.getLong("id"));
        result.setMission(missionManager.findMissionById(rs.getLong("mission_id")));
        result.setAgent(agentManager.findAgentById(rs.getLong("agent_id")));
        result.setStart(toLocalDate(rs.getDate("starting")));
        result.setEnd(toLocalDate(rs.getDate("ending")));

        return result;
    }

    private Assignment executeQueryForSingleAssignment(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            Assignment result = rowToAssignment(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more Assignments with the same ID found!");
            }
            return result;
        }

        return null;
    }

    private List<Assignment> executeQueryForMoreAssignments(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        ResultSet rs = stm.executeQuery();
        List<Assignment> resultList = new ArrayList<>();
        while (rs.next()) {
            resultList.add(rowToAssignment(rs));
        }
        return resultList;
    }
}
