package cz.muni.fi.pv168.db_backend.backend;

import cz.muni.fi.pv168.db_backend.common.EntityValidationException;
import cz.muni.fi.pv168.db_backend.common.IllegalEntityException;
import cz.muni.fi.pv168.db_backend.common.ServiceFailureException;

import javax.sql.DataSource;
import java.util.List;

/**
 * Interface provides method to manage Missions.
 *
 * Created by nayriva on 7.3.2017.
 */
public interface MissionManager {

    /**
     * Create new mission in database.
     * @param mission to be created
     * @throws ServiceFailureException when database error occurs.
     * @throws EntityValidationException when one or more fields of Mission are invalid.
     * (Name is null or empty, task is null or empty, place is null or empty, minAgentRank is less or equal 0).
     * @throws IllegalEntityException when mission has already assigned ID.
     * @throws IllegalArgumentException when mission is null.
     */
    void createMission(Mission mission)
            throws ServiceFailureException, IllegalEntityException,
            IllegalArgumentException, EntityValidationException;

    /**
     * Update mission in database.
     * @param mission to be updated
     * @throws ServiceFailureException when problem with database occurs.
     * @throws EntityValidationException when one or more fields of Mission are invalid.
     * (Name is null or empty, task is null or empty, place is null or empty, minAgentRank is less or equal 0).
     * @throws IllegalEntityException when ID is null or mission is not in DB.
     * @throws IllegalArgumentException when mission is null.
     */
    void updateMission(Mission mission)
            throws ServiceFailureException, EntityValidationException, IllegalEntityException;

    /**
     * Delete mission from database.
     * @param mission to be removed.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalEntityException when ID is null or mission is not in DB.
     * @throws IllegalArgumentException when mission is null.
     */
    void deleteMission(Mission mission)
            throws ServiceFailureException, IllegalEntityException;

    /**
     * Find mission with specified ID in database.
     * @param id primary key of requested mission.
     * @return mission with given id, null if no such exists.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when id is null or less than 0.
     */
    Mission findMissionById(Long id) throws ServiceFailureException, IllegalArgumentException;

    /**
     * List all missions from database.
     * @return List of all missions, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Mission> findAllMissions() throws ServiceFailureException;

    /**
     * List all Missions by specifying if a Mission was successful or not.
     * @param isSuccessful True if successful Missions should be found, false otherwise.
     * @return List of successful/unsuccessful Missions, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Mission> findMissionsBySuccess(boolean isSuccessful) throws ServiceFailureException;

    /**
     * List all Missions by specifying if a Mission was successful or not.
     * @param isFinished True if finished Missions should be found, false otherwise.
     * @return List of finished/unfinished Missions, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     */
    List<Mission> findMissionsByFinished(boolean isFinished) throws ServiceFailureException;

    /**
     * List all Missions with specified minimal agents rank.
     * @param minRank Minimal agents rank of Missions to be found.
     * @return List of Missions with specified minimal Agent's rank, empty list if none has been found.
     * @throws ServiceFailureException when problem with database occurs.
     * @throws IllegalArgumentException when minRank is less or equal 0.
     */
    List<Mission> findMissionsByMinAgentRank(int minRank)
            throws ServiceFailureException, IllegalArgumentException;

    /**
     * Set data source of class.
     * @param dataSource of data.
     * @throws IllegalArgumentException when dataSource is null.
     */
    void setDataSource(DataSource dataSource);
}
