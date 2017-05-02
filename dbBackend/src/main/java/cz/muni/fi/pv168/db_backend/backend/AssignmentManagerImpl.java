package cz.muni.fi.pv168.db_backend.backend;

import cz.muni.fi.pv168.db_backend.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger logger = LoggerFactory.getLogger(AssignmentManagerImpl.class);

    private void checkDataSource() {
        if (dataSource == null) {
            logger.error("Datasource in assignmentManager not set!");
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
        logger.debug("Create assignment {} ...", assignment);
        checkDataSource();
        validate(assignment, true);
        if (assignment.getId() != null) {
            logger.debug("Error while creating assignment: already assigned ID ... {}", assignment);
            throw new IllegalEntityException("Assignment has already got assigned ID!");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO assignments(mission_id, agent_id, starting, ending) " +
                            "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
            {
                stm.setLong(1, assignment.getMission());
                stm.setLong(2, assignment.getAgent());
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
                logger.error(errorMsg, ex);
                conn.rollback();
                assignment.setStart(null);
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                logger.error("Service failure", ex);
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Create assignment finished {} ...", assignment);
    }

    @Override
    public void updateAssignment(Assignment assignment)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException {

        logger.debug("Update assignment {} ...", assignment);
        checkDataSource();
        validate(assignment, false);
        if (assignment.getId() == null) {
            logger.error("Error while updating assignment: assignment without associated ID ... {}", assignment);
            throw new IllegalEntityException("Assignment hasn't got associated ID!");
        } else if (assignment.getId() < 0) {
            logger.error("Error while updating assignment: assignmentId < 0 ... {}", assignment);
            throw new IllegalEntityException("Assignment ID must be >= 0!");
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "UPDATE assignments SET mission_id = ?, agent_id = ?, ending = ? WHERE id = ?"))
            {
                stm.setLong(1, assignment.getMission());
                stm.setLong(2, assignment.getAgent());
                stm.setDate(3, toSQLDate(assignment.getEnd()));
                stm.setLong(4, assignment.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, assignment, false);
                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when updating assignment in DB!";
                logger.error(errorMsg, ex);
                conn.rollback();
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                logger.error("Service failure", ex);
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Update assignment finished {} ...", assignment);
    }

    @Override
    public void deleteAssignment(Assignment assignment)
            throws ServiceFailureException, IllegalEntityException {

        logger.debug("Delete assignment {} ...", assignment);
        checkDataSource();
        if (assignment == null) {
            logger.error("Error while deleting assignment: assignment is NULL");
            throw new IllegalArgumentException("Assignment is NULL!");
        } else if (assignment.getId() == null) {
            logger.error("Error while deleting assignment: assignment without associated ID ... {}", assignment);
            throw new IllegalEntityException("Assignment hasn't got associated ID!");
        } else if (assignment.getId() < 0) {
            logger.error("Error while deleting assignment: assignmentId < 0 ... {}", assignment);
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
                logger.error(errorMsg, ex);
                conn.rollback();
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                conn.rollback();
                logger.error("Service failure", ex);
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Delete assignment finished {} ...", assignment);
    }

    @Override
    public Assignment findAssignmentById(Long id) throws ServiceFailureException {
        logger.debug("Find assignment by id {} ...", id);
        checkDataSource();
        if (id == null) {
            logger.error("ID is NULL");
            throw new IllegalArgumentException("ID is null!");
        } else if (id < 0) {
            logger.error("ID < 0");
            throw new IllegalArgumentException("ID must be >= 0");
        }

        Assignment result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM assignments WHERE id = ?")) {
                stm.setLong(1, id);
                result = executeQueryForSingleAssignment(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when trying to find assignment by ID!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find assignment by id finished {} ...", result);
        return result;
    }

    @Override
    public List<Assignment> findAllAssignments() throws ServiceFailureException {
        logger.debug("Find all assignments ...");
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM assignments")) {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments from DB!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find all assignments finished ...");
        return result;
    }

    @Override
    public List<Assignment> findAssignmentsOfAgent(Long agent)
            throws ServiceFailureException, IllegalEntityException {

        logger.debug("Find all assignments of agent {} ...", agent);
        checkDataSource();
        if (agent == null) {
            logger.error("Error: agent is NULL");
            throw new IllegalArgumentException("Agent is null!");
        } else if (agent < 0) {
            logger.error("Error: agentId < 0");
            throw new IllegalEntityException("Agent's ID must be >= 0!");
        }

        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE agent_id = ?"))
            {
                stm.setLong(1, agent);
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments of agent " + agent + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find all assignments of agent {} finished ...", agent);
        return result;
    }

    @Override
    public List<Assignment> findAssignmentsOfMission(Long mission)
            throws ServiceFailureException, IllegalEntityException {

        logger.debug("Find assignments of mission {} ...", mission);
        checkDataSource();
        if (mission == null) {
            logger.error("Error: mission is NULL");
            throw new IllegalArgumentException("Mission is null!");
        } else if (mission < 0) {
            logger.error("Error: missionId < 0");
            throw new IllegalEntityException("Mission's ID must be >= 0!");
        }

        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE mission_id = ?"))
            {
                stm.setLong(1, mission);
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all assignments of " + mission + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find assignments of mission {} finished ...", mission);
        return result;
    }

    @Override
    public List<Assignment> findActiveAssignments() throws ServiceFailureException {
        logger.debug("Find active assignments ...");
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE ending IS NULL"))
            {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all active assignments!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find active assignments finished ...");
        return result;
    }

    @Override
    public List<Assignment> findEndedAssignments() throws ServiceFailureException {
        logger.debug("Find ended assignments ...");
        checkDataSource();
        List<Assignment> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement(
                    "SELECT * FROM assignments WHERE ending IS NOT NULL"))
            {
                result = executeQueryForMoreAssignments(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all past assignments!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find ended assignments finished");
        return result;
    }

    private void validate(Assignment assignment, boolean insert) {
        if (assignment == null) {
            logger.error("Validation error: assignment is NULL");
            throw new IllegalArgumentException("Assignment is null!");
        }
        if (assignment.getStart() == null) {
            logger.error("Validation error: start field is null");
            throw new EntityValidationException("Invalid start field: start is null!");
        }
        if (assignment.getMission() == null || assignment.getMission() < 0) {
            logger.error("Validation error: invalid mission field - id of mission");
            throw new EntityValidationException("Invalid mission field: id of mission!");
        }
        if (assignment.getAgent() == null || assignment.getAgent() < 0) {
            logger.error("Validation error: invalid mission field - id of agent");
            throw new EntityValidationException("Invalid agent field: id of agent!");
        }
        if (insert && assignment.getEnd() != null) {
            logger.error("Validation error: cannot create assignment with end date set");
            throw new EntityValidationException("Invalid end field: end is not null when creating assignment!");
        }
        Agent agent = getAgent(assignment);
        Mission mission = getMission(assignment);
        if (insert && agent == null) {
            logger.error("Validation error: cannot create assignment - invalid agent field " +
                    "- agent not in DB");
            throw new IllegalEntityException("Invalid agent field: agent not in DB!");
        }
        if (insert && mission == null) {
            logger.error("Validation error: cannot create assignment - invalid mission field " +
                    "- mission not in DB");
            throw new IllegalEntityException("Invalid mission field: mission not in DB!");
        }
        if (insert && mission.isSuccessful()) {
            logger.error("Validation error: cannot create assignment - invalid mission field " +
                    "- mission already marked as successful");
            throw new AssignmentException("Invalid mission field: mission already marked as successful!");
        }
        if (insert &&  mission.isFinished()) {
            logger.error("Validation error: cannot create assignment - invalid mission field - " +
                    "mission already marked as finished");
            throw new AssignmentException("Invalid mission field: mission already marked as finished!");
        }
        if (insert && ! agent.isAlive()) {
            logger.error("Validation error: cannot create assignment - invalid agent field - agent is dead");
            throw new AssignmentException("Invalid agent field: agent is dead!");
        }
        if (insert && !validateAgentNotOnMission(agent.getId())) {
            logger.error("Validation error: cannot create assignment - invalid agent field " +
                    "- agent already assigned to a mission");
            throw new AssignmentException("Invalid agent field: agent is already on mission!");
        }
        if (insert && agent.getRank() < mission.getMinAgentRank()) {
            logger.error("Validation error: cannot create assignment " +
                    "- agent rank is not high enough for this mission");
            throw new AssignmentException(
                    "Invalid assignment: agent's rank is not high enough for this mission!");
        }
        if (insert && assignment.getStart().isBefore(LocalDate.now(clock))) {
            logger.error("Validation error: cannot create past assignment");
            throw new EntityValidationException("Cannot create past assignment");
        }
        if (!insert && assignment.getEnd() != null && assignment.getEnd().isBefore(LocalDate.now(clock))) {
            logger.error("Validation error: cannot update end to past date");
            throw new EntityValidationException("Cannot update end to past date!");
        }
    }

    private boolean validateAgentNotOnMission(Long agentId) {
        List<Assignment> activeOfAgent = findAssignmentsOfAgent(agentId);
        activeOfAgent.removeIf((assignment) -> assignment.getEnd() != null);
        return activeOfAgent.isEmpty();
    }

    private Agent getAgent(Assignment assignment) {
        AgentManager agentManager = new AgentManagerImpl();
        agentManager.setDataSource(dataSource);
        return agentManager.findAgentById(assignment.getAgent());
    }

    private Mission getMission(Assignment assignment) {
        MissionManager missionManager = new MissionManagerImpl();
        missionManager.setDataSource(dataSource);
        return missionManager.findMissionById(assignment.getMission());
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
        result.setMission(rs.getLong("mission_id"));
        result.setAgent(rs.getLong("agent_id"));
        result.setStart(toLocalDate(rs.getDate("starting")));
        result.setEnd(toLocalDate(rs.getDate("ending")));

        return result;
    }

    private Assignment executeQueryForSingleAssignment(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for single assignment ...");
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            Assignment result = rowToAssignment(rs);
            if (rs.next()) {
                logger.error("Error: multiple assignments with same ID found");
                throw new ServiceFailureException("Internal integrity error: more Assignments with the same ID found!");
            }
            return result;
        }

        return null;
    }

    private List<Assignment> executeQueryForMoreAssignments(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for more assignments ...");
        ResultSet rs = stm.executeQuery();
        List<Assignment> resultList = new ArrayList<>();
        while (rs.next()) {
            resultList.add(rowToAssignment(rs));
        }
        return resultList;
    }
}
