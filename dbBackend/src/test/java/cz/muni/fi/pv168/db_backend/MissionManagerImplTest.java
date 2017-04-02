package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Mission;
import cz.muni.fi.pv168.db_backend.backend.MissionManager;
import cz.muni.fi.pv168.db_backend.backend.MissionManagerImpl;
import cz.muni.fi.pv168.db_backend.common.*;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Class provides tests for Mission manager.
 *
 * Created by nayriva on 8.3.2017.
 */
public class MissionManagerImplTest {

    private MissionManagerImpl manager;
    private DataSource ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        manager = new MissionManagerImpl();
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, MissionManagerImpl.class.getResource("createTables.sql"));
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws Exception {
        DBUtils.executeSqlScript(ds, MissionManagerImpl.class.getResource("dropTables.sql"));
    }

    private MissionBuilder sampleMission() {
        return new MissionBuilder()
                .id(null)
                .name("Butterfly")
                .task("Rescue hostages")
                .place("Russia")
                .finished(false)
                .successful(false)
                .minAgentRank(1);
    }

    private MissionBuilder anotherMission() {
        return new MissionBuilder()
                .id(null)
                .name("Condor")
                .task("Eliminate suspect")
                .place("USA")
                .finished(false)
                .successful(false)
                .minAgentRank(2);
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:mission-mgrTest");
        ds.setCreateDatabase("create");
        return ds;
    }

    //----------------------------------------------------------------
    // Tests for MissionManagerImpl.createMission(Mission) operation.
    //----------------------------------------------------------------

    @Test
    public void createMission() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        Mission result = manager.findMissionById(mission.getId());

        assertNotNull(result);
        assertThat(result.getId()).isNotNull();
        assertThat(result).isNotSameAs(mission).isEqualToComparingFieldByField(mission);
    }

    @Test
    public void createNullMission() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createMission(null);
    }

    @Test
    public void createMissionWithExistingId() {
        Mission mission1 = sampleMission().build();
        manager.createMission(mission1);
        Mission mission2 = anotherMission().id(mission1.getId()).build();

        expectedException.expect(IllegalEntityException.class);
        manager.createMission(mission2);
    }

    @Test
    public void createMissionWithNullName() {
        Mission mission = sampleMission().name(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithEmptyName() {
        Mission mission = sampleMission().name("").build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithNullTask() {
        Mission mission = sampleMission().task(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithEmptyTask() {
        Mission mission = sampleMission().task("").build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithNullPlace() {
        Mission mission = sampleMission().place(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithEmptyPlace() {
        Mission mission = sampleMission().place("").build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithSuccessfulTrue() {
        Mission mission = sampleMission().successful(true).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithFinishedTrue() {
        Mission mission = sampleMission().finished(true).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithZeroMinAgentRank() {
        Mission mission = sampleMission().minAgentRank(0).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    @Test
    public void createMissionWithNegativeMinAgentRank() {
        Mission mission = sampleMission().minAgentRank(-1).build();

        expectedException.expect(EntityValidationException.class);
        manager.createMission(mission);
    }

    //------------------------------------------------------
    // Tests for all MissionManagerImpl "FIND" operations
    //------------------------------------------------------

    @Test
    public void findAllMissions() {
        Mission m1 = sampleMission().build();
        Mission m2 = anotherMission().build();
        manager.createMission(m1);
        manager.createMission(m2);

        assertThat(manager.findAllMissions())
                .usingFieldByFieldElementComparator()
                .containsOnly(m1, m2);
    }

    @Test
    public void findMissionById() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        assertNotNull(mission.getId());
        Mission result = manager.findMissionById(mission.getId());
        assertThat(result).isNotNull().isEqualToComparingFieldByField(mission);
    }

    @Test
    public void findMissionsByIdPassingNullId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findMissionById(null);
    }

    @Test
    public void findMissionByIdPassingNegativeId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findMissionById(-1L);
    }

    @Test
    public void findMissionByIdPassingNonExistingId() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        manager.deleteMission(mission);

        assertNotNull(mission.getId());
        assertThat(manager.findMissionById(mission.getId())).isNull();
    }

    @Test
    public void findMissionsBySuccess() {
        Mission m1 = sampleMission().build();
        Mission m2 = anotherMission().build();
        manager.createMission(m1);
        manager.createMission(m2);
        m1.setSuccessful(true);
        manager.updateMission(m1);

        assertThat(manager.findMissionsBySuccess(true))
                .usingFieldByFieldElementComparator().containsOnly(m1);
        assertThat(manager.findMissionsBySuccess(false))
                .usingFieldByFieldElementComparator().containsOnly(m2);
    }

    @Test
    public void findMissionsByFinished() {
        Mission m1 = sampleMission().build();
        Mission m2 = anotherMission().build();
        manager.createMission(m1);
        manager.createMission(m2);
        m1.setFinished(true);
        manager.updateMission(m1);

        assertThat(manager.findMissionsByFinished(true))
                .usingFieldByFieldElementComparator().containsOnly(m1);
        assertThat(manager.findMissionsByFinished(false))
                .usingFieldByFieldElementComparator().containsOnly(m2);
    }

    @Test
    public void findMissionsByMinAgentRank() {
        Mission m1 = sampleMission().minAgentRank(2).build();
        Mission m2 = anotherMission().minAgentRank(3).build();
        manager.createMission(m1);
        manager.createMission(m2);

        assertThat(manager.findMissionsByMinAgentRank(2))
                .usingFieldByFieldElementComparator().containsOnly(m1);
        assertThat(manager.findMissionsByMinAgentRank(3))
                .usingFieldByFieldElementComparator().containsOnly(m2);
        assertThat(manager.findMissionsByMinAgentRank(4)).isEmpty();
    }

    @Test
    public void findMissionsByMinAgentRankPassingNegativeRank() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findMissionsByMinAgentRank(-1);
    }

    @Test
    public void findMissionsByMinAgentRankPassingZeroRank() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findMissionsByMinAgentRank(0);
    }

    //----------------------------------------------------------
    // Tests for MissionManagerImpl.update(Mission) operation.
    //----------------------------------------------------------

    @Test
    public void updateMissionName() {
        testUpdateMission((mission) -> mission.setName("Phoenix"));
    }

    @Test
    public void updateMissionTask() {
        testUpdateMission((mission) -> mission.setTask("Defuse bomb"));
    }

    @Test
    public void updateMissionPlace() {
        testUpdateMission((mission) -> mission.setPlace("Ukraine"));
    }

    @Test
    public void updateMissionSuccessful() {
        testUpdateMission((mission) -> mission.setSuccessful(true));
    }

    @Test
    public void updateMissionFinished() {
        testUpdateMission((mission) -> mission.setFinished(true));
    }

    @Test
    public void updateMissionMinAgentRank() {
        testUpdateMission((mission) -> mission.setMinAgentRank(8));
    }

    @Test
    public void updateNullMission() {
        expectedException.expect(IllegalArgumentException.class);
        manager.updateMission(null);
    }

    @Test
    public void updateMissionWithNullId() {
        Mission mission = sampleMission().id(null).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionWithNegativeId() {
        Mission mission = sampleMission().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionWithNonExistingId() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        manager.deleteMission(mission);

        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToNullName() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setName(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToEmptyName() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setName("");

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToNullTask() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setTask(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToEmptyTask() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setTask("");

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToNullPlace() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setPlace(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToEmptyPlace() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setPlace("");

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToNegativeMinAgentRank() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setMinAgentRank(-1);

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    @Test
    public void updateMissionToZeroMinAgentRank() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        mission.setMinAgentRank(0);

        expectedException.expect(EntityValidationException.class);
        manager.updateMission(mission);
    }

    //----------------------------------------------------------------
    // Tests for MissionManagerImpl.deleteMission(Mission) operation
    //----------------------------------------------------------------

    @Test
    public void deleteMission() {
        assertThat(manager.findAllMissions()).isEmpty();
        Mission m1 = sampleMission().build();
        Mission m2 = anotherMission().build();
        manager.createMission(m1);
        manager.createMission(m2);

        assertNotNull("Id of mission shouldn't be null", manager.findMissionById(m1.getId()));
        assertNotNull("Id of mission shouldn't be null", manager.findMissionById(m2.getId()));

        manager.deleteMission(m1);

        assertNull("Mission should be deleted", manager.findMissionById(m1.getId()));
        assertNotNull("Mission shouldn't be deleted", manager.findMissionById(m2.getId()));
    }

    @Test
    public void deleteNullMission() {
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteMission(null);
    }

    @Test
    public void deleteMissionWithNullId() {
        Mission mission = sampleMission().id(null).build();

        expectedException.expect(IllegalEntityException.class);
        manager.deleteMission(mission);
    }

    @Test
    public void deleteMissionWithNegativeId() {
        Mission mission = sampleMission().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.deleteMission(mission);
    }

    @Test
    public void deleteMissionPassingNonExistingId() {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        manager.deleteMission(mission);

        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.deleteMission(mission);
    }

    //------------------------------------------------------------
    // Tests for exceptions thrown when Database failure appears
    //------------------------------------------------------------

    @Test
    public void createMissionWithSQLExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        Mission mission = sampleMission().build();

        assertThatThrownBy(() -> manager.createMission(mission))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateMissionWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);
        testExpectedServiceFailureException((missionManager) -> manager.updateMission(mission));
    }

    @Test
    public void deleteMissionWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.deleteMission(mission));
    }

    @Test
    public void findMissionByIdWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.findMissionById(mission.getId()));
    }

    @Test
    public void findMissionBySuccessWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.findMissionsBySuccess(true));
    }

    @Test
    public void findMissionByFinishedWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.findMissionsByFinished(true));
    }

    @Test
    public void findMissionByMinAgentRankWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.findMissionsByMinAgentRank(2));
    }

    @Test
    public void findAllMissionsWithSQLExceptionThrown() throws SQLException {
        Mission mission = sampleMission().build();
        manager.createMission(mission);

        testExpectedServiceFailureException((manager) -> manager.findAllMissions());
    }

    //-----------------------
    // Additional resources
    //-----------------------

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateMission(Operation<Mission> updateOperation) {
        Mission missionToUpdate = sampleMission().build();
        Mission anotherMission = anotherMission().build();

        manager.createMission(missionToUpdate);
        manager.createMission(anotherMission);

        updateOperation.callOn(missionToUpdate);
        manager.updateMission(missionToUpdate);

        assertThat(missionToUpdate)
                .isEqualToComparingFieldByField(manager.findMissionById(missionToUpdate.getId()));
        assertThat(anotherMission)
                .isEqualToComparingFieldByField(manager.findMissionById(anotherMission.getId()));
    }

    private void testExpectedServiceFailureException(Operation<MissionManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);

        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
}