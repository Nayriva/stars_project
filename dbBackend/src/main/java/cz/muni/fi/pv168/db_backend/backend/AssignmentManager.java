package cz.muni.fi.pv168.db_backend.backend;

import cz.muni.fi.pv168.db_backend.common.AssignmentException;
import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.IllegalEntityException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;

import javax.sql.DataSource;
import java.util.List;

/**
 * Interface provides method to manage Assignments.
 *
 * Created by nayriva on 7.3.2017.
 */
public interface AssignmentManager {

    /**
     * Create new assignment in database.
     * @param assignment to be created.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws EntityValidationException when one or more fields of Assignment are null.
     * (Mission is null or has invalid field, Agent is null or has invalid field, start is not NOW, end is not null).
     * @throws IllegalArgumentException when assignment is null.
     * @throws IllegalEntityException when assignment has already assigned ID.
     * @throws AssignmentException when agent's rank is less than mission's minAgentRank, agent is dead,
     * mission is marked as finished or successful.
     */
    void createAssignment(Assignment assignment)
            throws ServiceFailureException, IllegalEntityException,
            IllegalArgumentException, EntityValidationException, AssignmentException;

    /**
     * Update assignment in database.
     * @param assignment to be updated
     * @throws ServiceFailureException when problem with database occurs.
     * @throws EntityValidationException when one or more fields of Assignment are null.
     * (Mission is null, Agent is null, start is null)
     * @throws IllegalEntityException when ID is null or assignment is not in DB.
     * @throws IllegalArgumentException when assignment is null.
     */
    void updateAssignment(Assignment assignment)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException, AssignmentException;

    /**
     * Delete assignment from database.
     * @param assignment to be deleted
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalEntityException when ID is null or assignment is nod in DB.
     * @throws IllegalArgumentException when assignment is null.
     */
    void deleteAssignment(Assignment assignment)
            throws ServiceFailureException, IllegalEntityException;

    /**
     * Find assignment with specified ID in database.
     * @param id primary key of requested Assignment.
     * @return assignment with specified ID, null if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when id is less than 0.
     */
    Assignment findAssignmentById(Long id) throws ServiceFailureException;

    /**
     * Lists all Assignments.
     * @return List of all Assignments, empty collection if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Assignment> findAllAssignments() throws ServiceFailureException;

    /**
     * Find Assignment associated with specified Agent.
     * @param agent of whose assignments should be found.
     * @return Collection of specified agent's assignments, empty collection if no such has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalEntityException when agent is null or is not in DB.
     * @throws IllegalArgumentException when agent is null.
     */
    List<Assignment> findAssignmentsOfAgent(Long agent)
            throws ServiceFailureException, IllegalEntityException;

    /**
     * Find Assignment associated with specified Mission.
     * @param mission of which assignments should be found.
     * @return Collection of specified mission's assignments, empty collection if no such has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalEntityException when mission is null or is not in DB.
     * @throws IllegalArgumentException when mission is null.
     */
    List<Assignment> findAssignmentsOfMission(Long mission)
            throws ServiceFailureException, IllegalEntityException;

    /**
     * Find all Assignments where End (date) is greater or equal to actual date.
     * @return Collection of specified active assignments, empty collection if no such has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Assignment> findActiveAssignments() throws ServiceFailureException;

    /**
     * Find all Assignments where end (date) is less than actual date.
     * @return Collection of specified expired assignments, empty collection if no such has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Assignment> findEndedAssignments() throws ServiceFailureException;

    /**
     * Set data source of class.
     * @param dataSource of data.
     * @throws IllegalArgumentException when dataSource is null.
     */
    void setDataSource(DataSource dataSource);
}
