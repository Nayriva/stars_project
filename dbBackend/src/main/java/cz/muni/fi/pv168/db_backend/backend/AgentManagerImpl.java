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
 * Class implements methods to manage Agents.
 *
 * Created by nayriva on 7.3.2017.
 */
public class AgentManagerImpl implements AgentManager {

    private DataSource dataSource;
    private final static Logger logger = LoggerFactory.getLogger(AgentManagerImpl.class);

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            logger.error("Datasource in agentManager not set!");
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException
    {
        logger.debug("Create agent {} ...", agent);
        checkDataSource();
        validate(agent, true);
        if (agent.getId() != null) {
            logger.error("Error while creating agent: already assigned ID ... {}", agent);
            throw new IllegalEntityException("Entity has already got assigned ID.");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO agents (name, sp_power, alive, rank)" +
                            " VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
            {
                stm.setString(1, agent.getName());
                stm.setString(2, agent.getSpecialPower());
                stm.setBoolean(3, agent.isAlive());
                stm.setInt(4, agent.getRank());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, agent, true);
                Long generatedId = DBUtils.getId(stm.getGeneratedKeys());
                agent.setId(generatedId);

                conn.commit();
            } catch (SQLException ex) {
                String errorMsg = "Error when inserting new agent into DB!";
                logger.error(errorMsg, ex);
                conn.rollback();
                throw new ServiceFailureException(errorMsg, ex);
            } catch (ServiceFailureException ex) {
                logger.error("Problem with service occurred!", ex);
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Create agent finished {} ...", agent);
    }

    @Override
    public void updateAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException
    {
        logger.debug("Update agent {} ...", agent);
        checkDataSource();
        validate(agent, false);
        if (agent.getId() == null) {
            logger.error("Error while updating agent: agent without assigned ID ... {}", agent);
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (agent.getId() < 0) {
            logger.error("Error while updating agent: agentId < 0 ... {}", agent);
            throw new IllegalEntityException("Agent ID must be >= 0!");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "UPDATE agents SET name = ?, sp_power = ?, alive = ?, rank = ?"
                            + " WHERE id = ?"))
            {
                stm.setString(1, agent.getName());
                stm.setString(2, agent.getSpecialPower());
                stm.setBoolean(3, agent.isAlive());
                stm.setInt(4, agent.getRank());
                stm.setLong(5, agent.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, agent, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when updating agent in DB!";
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
        logger.debug("Update agent finished {} ...", agent);
    }

    @Override
    public void deleteAgent(Agent agent) throws ServiceFailureException, IllegalEntityException {
        logger.debug("Delete agent {} ...", agent);
        checkDataSource();
        if (agent == null) {
            logger.error("Error while deleting agent: agent is NULL!");
            throw new IllegalArgumentException("Agent is NULL!");
        } else if (agent.getId() == null) {
            logger.error("Error while deleting agent: agentId is NULL ... {}", agent);
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (agent.getId() < 0) {
            logger.error("Error while deleting agent: agentId < 0 ... {}", agent);
            throw new IllegalEntityException("Agent ID must be >= 0");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "DELETE FROM agents WHERE id = ?"))
            {
                stm.setLong(1, agent.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, agent, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when deleting agent from DB!";
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
        logger.debug("Delete agent finished {} ...", agent);
    }

    @Override
    public Agent findAgentById(Long id) throws ServiceFailureException, IllegalArgumentException {
        logger.debug("Find agent by id: {} ...", id);
        checkDataSource();
        if (id == null) {
            logger.error("ID is NULL!");
            throw new IllegalArgumentException("ID is null!");
        } else if (id < 0) {
            logger.error("ID < 0!");
            throw new IllegalArgumentException("ID must be >= 0");
        }

        Agent result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE id = ?")) {
                stm.setLong(1, id);
                result = executeQueryForSingleAgent(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when trying to find agent by ID!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find agent by id finished ... {}", result);
        return result;
    }

    @Override
    public List<Agent> findAllAgents() throws ServiceFailureException {
        logger.debug("Find all agents ...");
        checkDataSource();
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents")) {
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all agents from DB!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find all agents finished ...");
        return result;
    }

    @Override
    public List<Agent> findAgentsByAlive(boolean isAlive) throws ServiceFailureException {
        logger.debug("Find agents by alive: {} ...", isAlive);
        checkDataSource();
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE alive = ?")) {
                stm.setBoolean(1, isAlive);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with alive = " + isAlive + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find agents by alive finished ...");
        return result;
    }

    @Override
    public List<Agent> findAgentsBySpecialPower(String specialPower)
            throws ServiceFailureException, IllegalArgumentException
    {
        logger.debug("Find agents by special power: {} ...", specialPower);
        checkDataSource();
        if (specialPower == null) {
            logger.error("specialPower is NULL!");
            throw new IllegalArgumentException("SpecialPower is null!");
        } else if (specialPower.isEmpty()) {
            logger.error("specialPower is EMPTY!");
            throw new IllegalArgumentException("SpecialPower is empty!");
        }

        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE sp_power = ?")) {
                stm.setString(1, specialPower);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with special power = " + specialPower + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find agents by special power finished...");
        return result;
    }

    @Override
    public List<Agent> findAgentsByRank(int rank)
            throws ServiceFailureException, IllegalArgumentException
    {
        logger.debug("Find agents by rank {} ...", rank);
        checkDataSource();
        if (rank <= 0) {
            logger.error("rank <= 0");
            throw new IllegalArgumentException("Rank must be > 0!");
        }
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE rank = ?")) {
                stm.setInt(1, rank);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with rank = " + rank + "!";
                logger.error(errorMsg, ex);
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            logger.error(errorMsg, ex);
            throw new ServiceFailureException(errorMsg, ex);
        }
        logger.debug("Find agents by rank finished...");
        return result;
    }

    private void validate(Agent agent, boolean insert) {
        if (agent == null) {
            logger.error("Invalid agent: agent is NULL");
            throw new IllegalArgumentException("Agent is null");
        }
        if (agent.getName() == null || agent.getName().isEmpty()) {
            logger.error("Invalid agent: invalid name field ... {}", agent);
            throw new EntityValidationException("Invalid name field of agent!");
        }
        if (insert && !agent.isAlive()) {
            logger.error("Invalid agent: cannot create dead agent ... {}", agent);
            throw new EntityValidationException("Cannot create dead agent!");
        }
        if (agent.getRank() <= 0) {
            logger.error("Invalid agent: invalid rank field ... {}", agent);
            throw new EntityValidationException("Invalid rank field of agent!");
        }
        if (agent.getSpecialPower() == null || agent.getSpecialPower().isEmpty()) {
            logger.error("Invalid agent: invalid specialPower field ... {}", agent);
            throw new EntityValidationException("Invalid special power field of agent!");
        }
    }

    private Agent executeQueryForSingleAgent(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for single agent ...");
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            Agent result = rowToAgent(rs);
            if (rs.next()) {
                logger.error("Error: multiple agents with same ID found");
                throw new ServiceFailureException("Internal integrity error: more agents with the same ID found!");
            }
            return result;
        }

        return null;
    }

    private List<Agent> executeQueryForMoreAgents(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        logger.debug("Execute query for more agents ...");
        ResultSet rs = stm.executeQuery();
        List<Agent> resultList = new ArrayList<>();
        while (rs.next()) {
            resultList.add(rowToAgent(rs));
        }
        return resultList;
    }

    private Agent rowToAgent(ResultSet rs) throws SQLException {
        Agent result = new Agent();

        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setSpecialPower(rs.getString("sp_power"));
        result.setAlive(rs.getBoolean("alive"));
        result.setRank(rs.getInt("rank"));

        return result;
    }
}
