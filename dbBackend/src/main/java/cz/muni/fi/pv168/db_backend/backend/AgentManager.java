package cz.muni.fi.pv168.db_backend.backend;

import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.IllegalEntityException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;

import javax.sql.DataSource;
import java.util.List;

/**
 * Interface provides method to manage Agents.
 *
 * Created by nayriva on 7.3.2017.
 */
public interface AgentManager {

    /**
     * Create new agent in database.
     * @param agent to be created.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when agent is null.
     * @throws EntityValidationException when one or more fields of Agent are invalid.
     * (Name is null or empty, specialPower is null or empty, rank is less or equal 0, alive is false).
     * @throws IllegalEntityException when agent has already got assigned ID.
     */
    void createAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException;

    /**
     * Update agent in database.
     * @param agent to be updated
     * @throws IllegalArgumentException when agent is null.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws EntityValidationException when one or more fields of Agent are invalid.
     * (Name is null or empty, specialPower is null or empty, rank is less or equal 0).
     * @throws IllegalEntityException when ID is null or agent is not in DB.
     */
    void updateAgent(Agent agent)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException;

    /**
     * Delete agent from database.
     * @param agent to be removed.
     * @throws IllegalArgumentException when agent is null.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalEntityException when ID is null or agent is not in DB.
     */
    void deleteAgent(Agent agent)
            throws ServiceFailureException, IllegalEntityException;

    /**
     * Find agent with specified ID in database.
     * @param id primary key of requested agent.
     * @return agent with given id, null if no such exists.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when id is null or less than 0.
     */
    Agent findAgentById(Long id) throws ServiceFailureException, IllegalArgumentException;

    /**
     * List all agents from database.
     * @return List of all Agents, empty list if no agent is stored in database.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Agent> findAllAgents() throws ServiceFailureException;

    /**
     * Find alive/dead Agents by specifying if agents are alive or not.
     * @param isAlive True if alive Agents should be found, false otherwise.
     * @return List of alive/dead Agents, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Agent> findAgentsByAlive(boolean isAlive) throws ServiceFailureException;

    /**
     * Find agents with given special power.
     * @param specialPower string representing specialPower
     * @return List of agents with given special power, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when specialPower is null or empty
     */
    List<Agent> findAgentsBySpecialPower(String specialPower)
            throws ServiceFailureException, IllegalArgumentException;

    /**
     * Find agents of specified rank.
     * @param rank int value of rank.
     * @return List of agents with given rank, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when rank param is less or equal 0.
     */
    List<Agent> findAgentsByRank(int rank)
            throws ServiceFailureException, IllegalArgumentException;

    /**
     * Set data source of class.
     * @param dataSource of data.
     * @throws IllegalArgumentException when dataSource is null.
     */
    void setDataSource(DataSource dataSource);
}
