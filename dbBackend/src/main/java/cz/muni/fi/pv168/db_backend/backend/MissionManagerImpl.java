package cz.muni.fi.pv168.db_backend.backend;

import cz.muni.fi.pv168.db_backend.common.DBUtils;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.IllegalEntityException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implements methods to manage missions.
 *
 * Created by nayriva on 7.3.2017.
 */
public class MissionManagerImpl implements MissionManager {

    private DataSource dataSource;
    private final static Logger logger = LoggerFactory.getLogger(MissionManagerImpl.class);

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            logger.error("Datasource in missionManager not set!");
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createMission(Mission mission)
            throws ServiceFailureException, IllegalEntityException,
            IllegalArgumentException, EntityValidationException
    {
        logger.debug("Create mission {} ...", mission);
        checkDataSource();
        validate(mission, true);
        if (mission.getId() != null) {
            logger.error("Error while creating mission: already assigned ID ... {}", mission);
            throw new IllegalEntityException("Entity has already got assigned ID.");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO missions (name, task, place, suc, fin, min_ag_rank)" +
                            " VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
            {
                stm.setString(1, mission.getName());
                stm.setString(2, mission.getTask());
                stm.setString(3, mission.getPlace());
                stm.setBoolean(4, mission.isSuccessful());
                stm.setBoolean(5, mission.isFinished());
                stm.setInt(6, mission.getMinAgentRank());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, mission, true);
                Long generatedId = DBUtils.getId(stm.getGeneratedKeys());
                mission.setId(generatedId);

                conn.commit();
            } catch (SQLException ex) {
                String errorMsg = "Error when inserting new mission into DB!";
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
        logger.debug("Create mission finished {} ...", mission);
    }

    @Override
    public void updateMission(Mission mission)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException
    {
        logger.debug("Update mission {} ...", mission);
        checkDataSource();
        validate(mission, false);
        if (mission.getId() == null) {
            logger.error("Error while updating mission: mission without assigned ID ... {}", mission);
            throw new IllegalEntityException("Mission hasn't got associated ID!");
        } else if (mission.getId() < 0) {
            logger.error("Error while updating mission: missionId < 0 ... {}", mission);
            throw new IllegalEntityException("Mission ID must be >= 0");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "UPDATE missions SET name = ?, task = ?, place = ?, suc = ?, fin = ?, min_ag_rank = ?"
                            + " WHERE id = ?"))
            {
                stm.setString(1, mission.getName());
                stm.setString(2, mission.getTask());
                stm.setString(3, mission.getPlace());
                stm.setBoolean(4, mission.isSuccessful());
                stm.setBoolean(5, mission.isFinished());
                stm.setInt(6, mission.getMinAgentRank());
                stm.setLong(7, mission.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, mission, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when updating mission in DB!";
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
        logger.debug("Update mission finished {} ...", mission);
    }

    @Override
    public void deleteMission(Mission mission) throws ServiceFailureException, IllegalEntityException {
        logger.debug("Delete mission {} ...", mission);
        checkDataSource();
        if (mission == null) {
            logger.error("Error while deleting mission: mission id is NULL");
            throw new IllegalArgumentException("Mission is NULL!");
        } else if (mission.getId() == null) {
            logger.error("Error while deleting mission: mission without assigned ID ... {}", mission);
            throw new IllegalEntityException("Mission hasn't got associated ID!");
        } else if (mission.getId() < 0) {
            logger.error("Error while deleting mission: missionId < 0 ... {}", mission);
            throw new IllegalEntityException("Mission ID must be >= 0");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "DELETE FROM missions WHERE id = ?"))
            {
                stm.setLong(1, mission.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, mission, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when deleting mission from DB!";
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
        logger.debug("Delete mission finished {} ...", mission);
    }

    @Override
    public Mission findMissionById(Long id) throws ServiceFailureException, IllegalArgumentException {
        logger.debug("Find mission by id {} ...", id);
        checkDataSource();
        if (id == null) {
            logger.error("ID is null!");
            throw new IllegalArgumentException("ID is null!");
        } else if (id < 0) {
            logger.error("ID is < 0!", id);
            throw new IllegalArgumentException("ID must be >= 0");
        }

        Mission result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM missions WHERE id = ?")) {
                stm.setLong(1, id);
                result = executeQueryForSingleMission(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when trying to find mission by ID!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find mission by id finished ... {}", result);
        return result;
    }

    @Override
    public List<Mission> findAllMissions() throws ServiceFailureException {
        logger.debug("Find all missions ...");
        checkDataSource();
        List<Mission> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM missions")) {
                result = executeQueryForMoreMissions(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all missions from DB!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find all missions finished ...");
        return result;
    }

    @Override
    public List<Mission> findMissionsBySuccess(boolean isSuccessful) throws ServiceFailureException {
        logger.debug("Find missions by success: {} ...", isSuccessful);
        checkDataSource();
        List<Mission> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM missions WHERE suc = ?")) {
                stm.setBoolean(1, isSuccessful);
                result = executeQueryForMoreMissions(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing missions from DB with success = " + isSuccessful + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find missions by success finished ...");
        return result;
    }

    @Override
    public List<Mission> findMissionsByFinished(boolean isFinished) throws ServiceFailureException {
        logger.debug("Find missions by finished: {} ...", isFinished);
        checkDataSource();
        List<Mission> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM missions WHERE fin = ?")) {
                stm.setBoolean(1, isFinished);
                result = executeQueryForMoreMissions(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing missions from DB with success = " + isFinished + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find missions by finished completed ...");
        return result;
    }

    @Override
    public List<Mission> findMissionsByMinAgentRank(int minRank)
            throws ServiceFailureException, IllegalArgumentException
    {
        logger.debug("Find missions by min agent rank: {} ...", minRank);
        checkDataSource();
        if (minRank <= 0) {
            logger.error("minRank <= 0!");
            throw new IllegalArgumentException("MinRank must be > 0!");
        }

        List<Mission> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM missions WHERE min_ag_rank = ?")) {
                stm.setInt(1, minRank);
                result = executeQueryForMoreMissions(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing missions from DB with minAgentRank = " + minRank + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find missions by min agent rank finished ...");
        return result;
    }

    private static void validate(Mission mission, boolean insert) {
        if (mission == null) {
            logger.error("Validation error: mission is null!");
            throw new IllegalArgumentException("Mission is null");
        }
        if (mission.getName() == null || mission.getName().isEmpty()) {
            logger.error("Validation error: invalid name field ... {}", mission);
            throw new EntityValidationException("Invalid name field of mission!");
        } else if (mission.getTask() == null || mission.getTask().isEmpty()) {
            logger.debug("Validation error: invalid task field ... {}", mission);
            throw new EntityValidationException("Invalid task field of mission!");
        } else if (mission.getPlace() == null || mission.getPlace().isEmpty()) {
            logger.error("Validation error: invalid place field");
            throw new EntityValidationException("Invalid place field of mission!");
        } else if (insert && mission.isSuccessful()) {
            logger.error("Validation error: cannot create successful mission ... {}", mission);
            throw new EntityValidationException("Cannot create successful mission!");
        } else if (insert && mission.isFinished()) {
            logger.error("Validation error: cannot create finished mission ... {}", mission);
            throw new EntityValidationException("Cannot create finished mission!");
        } else if (mission.getMinAgentRank() <= 0) {
            logger.error("Validation error: invalid minAgentRank field ... {}", mission);
            throw new EntityValidationException("Invalid minAgentRank field of mission!");
        }
    }

    private Mission rowToMission(ResultSet rs) throws SQLException {
        Mission result = new Mission();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setTask(rs.getString("task"));
        result.setPlace(rs.getString("place"));
        result.setFinished(rs.getBoolean("fin"));
        result.setSuccessful(rs.getBoolean("suc"));
        result.setMinAgentRank(rs.getInt("min_ag_rank"));

        return result;
    }

    private Mission executeQueryForSingleMission(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for single mission ...");
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            Mission result = rowToMission(rs);
            if (rs.next()) {
                logger.error("Error: multiple missions with same ID found");
                throw new ServiceFailureException("Internal integrity error: more missions with the same ID found!");
            }
            return result;
        }
        return null;
    }

    private List<Mission> executeQueryForMoreMissions(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for more missions ...");
        ResultSet rs = stm.executeQuery();
        List<Mission> resultList = new ArrayList<>();
        while (rs.next()) {
            resultList.add(rowToMission(rs));
        }
        return resultList;
    }
}