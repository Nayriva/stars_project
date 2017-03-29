package db_backend.backend;


import db_backend.common.DBUtils;
import db_backend.common.EntityValidationException;
import db_backend.common.IllegalEntityException;
import db_backend.common.ServiceFailureException;

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

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException
    {
        checkDataSource();
        validate(agent, true);
        if (agent.getId() != null) {
            throw new IllegalEntityException("Entity has already got assigned ID.");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO agents (name, sp_power, alive, rank, on_mission)" +
                            " VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
            {
                stm.setString(1, agent.getName());
                stm.setInt(2, agent.getSpecialPower());
                stm.setBoolean(3, agent.isAlive());
                stm.setInt(4, agent.getRank());
                stm.setBoolean(5, agent.isOnMission());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, agent, true);
                Long generatedId = DBUtils.getId(stm.getGeneratedKeys());
                agent.setId(generatedId);

                conn.commit();
            } catch (SQLException ex) {
                String errorMsg = "Error when inserting new agent into DB!";
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
    public void updateAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException
    {
        checkDataSource();
        validate(agent, false);
        if (agent.getId() == null) {
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (agent.getId() < 0) {
            throw new IllegalEntityException("Agent ID must be >= 0!");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(
                    "UPDATE agents SET name = ?, sp_power = ?, alive = ?, rank = ?, on_mission = ?"
                            + " WHERE id = ?"))
            {
                stm.setString(1, agent.getName());
                stm.setInt(2, agent.getSpecialPower());
                stm.setBoolean(3, agent.isAlive());
                stm.setInt(4, agent.getRank());
                stm.setBoolean(5, agent.isOnMission());
                stm.setLong(6, agent.getId());

                int count = stm.executeUpdate();
                DBUtils.checkUpdatesCount(count, agent, false);

                conn.commit();
            } catch (SQLException | IllegalEntityException ex) {
                String errorMsg = "Error when updating agent in DB!";
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
    public void deleteAgent(Agent agent) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (agent == null) {
            throw new IllegalArgumentException("Agent is NULL!");
        } else if (agent.getId() == null) {
            throw new IllegalEntityException("Agent hasn't got associated ID!");
        } else if (agent.getId() < 0) {
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
    public Agent findAgentById(Long id) throws ServiceFailureException, IllegalArgumentException {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException("ID is null!");
        } else if (id < 0) {
            throw new IllegalArgumentException("ID must be >= 0");
        }

        Agent result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE id = ?")) {
                stm.setLong(1, id);
                result = executeQueryForSingleAgent(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when trying to find agent by ID!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Agent> findAllAgents() throws ServiceFailureException {
        checkDataSource();
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents")) {
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing all agents from DB!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Agent> findAgentsByAlive(boolean isAlive) throws ServiceFailureException {
        checkDataSource();
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE alive = ?")) {
                stm.setBoolean(1, isAlive);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with alive = " + isAlive + "!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Agent> findAgentsBySpecialPower(int specialPower)
            throws ServiceFailureException, IllegalArgumentException
    {
        checkDataSource();
        if (specialPower <= 0) {
            throw new IllegalArgumentException("SpecialPower must be > 0!");
        } else if (specialPower >= 12) {
            throw new IllegalArgumentException("SpecialPower must be < 12!");
        }

        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE sp_power = ?")) {
                stm.setInt(1, specialPower);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with special power = " + specialPower + "!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    @Override
    public List<Agent> findAgentsByRank(int rank)
            throws ServiceFailureException, IllegalArgumentException
    {
        checkDataSource();
        if (rank <= 0) {
            throw new IllegalArgumentException("Rank must be > 0!");
        }
        List<Agent> result;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stm = conn.prepareStatement("SELECT * FROM agents WHERE rank = ?")) {
                stm.setInt(1, rank);
                result = executeQueryForMoreAgents(stm);
            } catch (SQLException ex ) {
                String errorMsg = "Error when listing agents from DB with rank = " + rank + "!";
                throw new ServiceFailureException(errorMsg, ex);
            }
        } catch (SQLException ex) {
            String errorMsg = "Database connection failure!";
            throw new ServiceFailureException(errorMsg, ex);
        }
        return result;
    }

    private void validate(Agent agent, boolean insert) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent is null");
        }
        if (agent.getName() == null || agent.getName().isEmpty()) {
            throw new EntityValidationException("Invalid name field of agent!");
        }
        if (insert && !agent.isAlive()) {
            throw new EntityValidationException("Cannot create dead agent!");
        }
        if (insert && agent.isOnMission()) {
            throw new EntityValidationException("Cannot create agent who is on a mission!");
        }
        if (agent.getRank() <= 0) {
            throw new EntityValidationException("Invalid rank field of agent!");
        }
        if (agent.getSpecialPower() <= 0 || agent.getSpecialPower() >= 12) {
            throw new EntityValidationException("Invalid special power field of agent!");
        }
    }

    private Agent executeQueryForSingleAgent(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            Agent result = rowToAgent(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more agents with the same ID found!");
            }
            return result;
        }

        return null;
    }

    private List<Agent> executeQueryForMoreAgents(PreparedStatement stm)
            throws SQLException, ServiceFailureException
    {
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
        result.setSpecialPower(rs.getInt("sp_power"));
        result.setAlive(rs.getBoolean("alive"));
        result.setRank(rs.getInt("rank"));
        result.setOnMission(rs.getBoolean("on_mission"));

        return result;
    }
}
