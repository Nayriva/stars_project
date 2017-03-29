package db_backend;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import db_backend.backend.*;
import db_backend.common.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.*;
import java.util.List;

import org.apache.derby.jdbc.EmbeddedDataSource;

import static java.time.Month.FEBRUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class provides tests for Assignment manager.
 *
 * Created by nayriva on 9.3.2017.
 */
public class AssignmentManagerImplTest {

    private AssignmentManagerImpl manager;
    private MissionManagerImpl missionManager;
    private AgentManagerImpl agentManager;
    private DataSource ds;

    private Agent existingAgent1;
    private Agent existingAgent2;
    private Agent notInDBAgent;
    private Mission existingMission1;
    private Mission existingMission2;
    private Mission notInDBMission;

    private final static ZonedDateTime NOW
            = LocalDateTime.of(2015, FEBRUARY, 20, 14, 00).atZone(ZoneId.of("UTC"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        manager = new AssignmentManagerImpl(prepareClockMock(NOW));
        missionManager = new MissionManagerImpl();
        agentManager = new AgentManagerImpl();
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, AgentManagerImpl.class.getResource("createTables.sql"));

        initMissions();
        initAgents();

        missionManager.setDataSource(ds);
        missionManager.createMission(existingMission1);
        missionManager.createMission(existingMission2);

        agentManager.setDataSource(ds);
        agentManager.createAgent(existingAgent1);
        agentManager.createAgent(existingAgent2);

        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws Exception {
        DBUtils.executeSqlScript(ds, AssignmentManagerImpl.class.getResource("dropTables.sql"));
    }

    private AssignmentBuilder sampleAssignment() {
        return new AssignmentBuilder()
                .id(null)
                .mission(existingMission1)
                .agent(existingAgent1)
                .start(NOW.toLocalDate())
                .end(null);
    }

    private AssignmentBuilder anotherAssignment() {
        return new AssignmentBuilder()
                .id(null)
                .mission(existingMission2)
                .agent(existingAgent2)
                .start(NOW.toLocalDate())
                .end(null);
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:assignment-mgrTest");
        ds.setCreateDatabase("create");
        return ds;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        return Clock.fixed(now.toInstant(), now.getZone());
    }

    private void initMissions() {
        existingMission1 = new MissionBuilder()
                .id(null)
                .name("Butterfly")
                .task("Eliminate suspect")
                .place("Russia")
                .successful(false)
                .finished(false)
                .minAgentRank(1)
                .build();
        existingMission2 = new MissionBuilder()
                .id(null)
                .name("Condor")
                .task("Eliminate suspect")
                .place("USA")
                .successful(false)
                .finished(false)
                .minAgentRank(1)
                .build();
        notInDBMission = new MissionBuilder()
                .id(null)
                .name("Butterfly")
                .task("Eliminate suspect")
                .place("Russia")
                .successful(false)
                .finished(false)
                .minAgentRank(1)
                .build();
    }

    private void initAgents() {
        existingAgent1 = new AgentBuilder()
                .id(null)
                .name("Antonio")
                .specialPower(1)
                .alive(true)
                .rank(1)
                .onMission(false)
                .build();

        existingAgent2 = new AgentBuilder()
                .id(null)
                .name("Albert")
                .specialPower(2)
                .alive(true)
                .rank(2)
                .onMission(false)
                .build();

        notInDBAgent = new AgentBuilder()
                .id(null)
                .name("John")
                .specialPower(3)
                .alive(true)
                .rank(3)
                .onMission(false)
                .build();
    }

    //---------------------------------------------------------------
    // Tests of AssignmentManagerImpl.create(Assignment) operation
    //---------------------------------------------------------------

    @Test
    public void createAssignment() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);

        Assignment result = manager.findAssignmentById(assignment.getId());

        assertNotNull(result);
        assertThat(result.getId()).isNotNull();
        assertThat(result).isNotSameAs(assignment).isEqualToComparingFieldByField(assignment);
    }

    @Test
    public void createNullAssignment() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createAssignment(null);
    }

    @Test
    public void createAssignmentWithExistingId() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Assignment anotherAssignment = anotherAssignment().id(sampleAssignment.getId()).build();

        expectedException.expect(IllegalEntityException.class);
        manager.createAssignment(anotherAssignment);
    }

    @Test
    public void createAssignmentWithNullMission() {
        Assignment sampleAssignment = sampleAssignment().mission(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithNotInDBMission() {
        Assignment sampleAssignment = sampleAssignment().mission(notInDBMission).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithNegativeMissionId() {
        Mission mission = notInDBMission;
        mission.setId(-1L);
        Assignment sampleAssignment = sampleAssignment().mission(notInDBMission).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithSuccessfulMission() {
        Mission mission = existingMission1;
        mission.setSuccessful(true);
        missionManager.updateMission(mission);
        Assignment assignment = sampleAssignment().mission(mission).build();

        expectedException.expect(AssignmentException.class);
        manager.createAssignment(assignment);
    }

    @Test
    public void createAssignmentWithFinishedMission() {
        Mission mission = existingMission1;
        existingMission1.setFinished(true);
        missionManager.updateMission(mission);
        Assignment assignment = sampleAssignment().mission(mission).build();

        expectedException.expect(AssignmentException.class);
        manager.createAssignment(assignment);
    }

    @Test
    public void createAssignmentWithNullAgent() {
        Assignment sampleAssignment = sampleAssignment().agent(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithNotInDBAgent() {
        Assignment sampleAssignment = sampleAssignment().agent(notInDBAgent).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithNegativeAgentId() {
        Agent agent = notInDBAgent;
        agent.setId(-1L);
        Assignment sampleAssignment = sampleAssignment().agent(notInDBAgent).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithDeadAgent() {
        Agent agent = existingAgent1;
        agent.setAlive(false);
        agentManager.updateAgent(agent);
        Assignment assignment = sampleAssignment().agent(agent).build();

        expectedException.expect(AssignmentException.class);
        manager.createAssignment(assignment);
    }

    @Test
    public void createAssignmentWithAgentWhoIsOnMission() {
        Agent agent = existingAgent1;
        agent.setOnMission(true);
        agentManager.updateAgent(agent);
        Assignment assignment = sampleAssignment().agent(agent).build();

        expectedException.expect(AssignmentException.class);
        manager.createAssignment(assignment);
    }

    @Test
    public void createAssignmentWithLowRankAgent() {
        Mission mission = existingMission1;
        mission.setMinAgentRank(3);
        missionManager.updateMission(mission);
        Agent agent = existingAgent1;
        agent.setRank(2);
        agentManager.updateAgent(agent);

        expectedException.expect(AssignmentException.class);
        manager.createAssignment(sampleAssignment().mission(mission).agent(agent).build());
    }

    @Test
    public void createAssignmentWithNullStart() {
        Assignment sampleAssignment = sampleAssignment().start(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithPastStart() {
        Assignment sampleAssignment = sampleAssignment().start(NOW.toLocalDate().minusDays(1)).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }

    @Test
    public void createAssignmentWithSetEnd() {
        Assignment sampleAssignment = sampleAssignment().end(NOW.plusDays(1).toLocalDate()).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAssignment(sampleAssignment);
    }


    //----------------------------------
    // Tests of all "Find" operations
    //----------------------------------

    @Test
    public void findAllAssignments() {
        Assignment a1 = sampleAssignment().build();
        Assignment a2 = anotherAssignment().build();

        manager.createAssignment(a1);
        manager.createAssignment(a2);

        assertThat(manager.findAllAssignments())
                .usingFieldByFieldElementComparator()
                .containsOnly(a1, a2);
    }

    @Test
    public void findAssignmentById() {
        Assignment a1 = sampleAssignment().build();
        manager.createAssignment(a1);

        assertNotNull(a1.getId());
        Assignment result = manager.findAssignmentById(a1.getId());
        assertThat(result).isNotNull().isEqualToComparingFieldByField(a1);
    }

    @Test
    public void findAssignmentsOfAgent() {
        Agent agent1 = existingAgent1;
        Agent agent2 = existingAgent2;
        Assignment a1 = sampleAssignment().agent(agent1).build();
        Assignment a2 = anotherAssignment().agent(agent2).build();
        List<Assignment> result;

        manager.createAssignment(a1);
        manager.createAssignment(a2);

        result = manager.findAssignmentsOfAgent(agent1);

        assertEquals(1, result.size());
        assertNotSame(a1, result.get(0));
        assertThat(result.get(0)).isEqualToComparingFieldByField(a1);
    }

    @Test
    public void findAssignmentsOfMission() {
        Mission mission1 = existingMission1;
        Mission mission2 = existingMission2;
        Assignment a1 = sampleAssignment().mission(mission1).build();
        Assignment a2 = sampleAssignment().mission(mission2).build();
        List<Assignment> result;

        manager.createAssignment(a1);
        manager.createAssignment(a2);

        result = manager.findAssignmentsOfMission(mission1);

        assertEquals(1, result.size());
        assertNotSame(a1, result.get(0));
        assertThat(result.get(0)).isEqualToComparingFieldByField(a1);
    }

    @Test
    // initial assignments need to be added manually
    // id of active assignment is 1
    public void findActiveAssignments() throws SQLException {
        DBUtils.executeSqlScript(ds, AgentManagerImpl.class.getResource("insertInitialAssignments.sql"));
        Assignment a1 = sampleAssignment().id(1L).build();

        assertThat(manager.findActiveAssignments()).usingFieldByFieldElementComparator().containsOnly(a1);
    }

    @Test
    // initial assignments need to be added manually
    // id of expired assignment is 2
    public void findExpiredAssignments() throws SQLException {
        DBUtils.executeSqlScript(ds, AgentManagerImpl.class.getResource("insertInitialAssignments.sql"));
        Assignment a1 = anotherAssignment().id(2L)
                .start(NOW.toLocalDate().minusDays(2)).end(NOW.toLocalDate().minusDays(1)).build();

        assertThat(manager.findEndedAssignments()).usingFieldByFieldElementComparator().containsOnly(a1);
    }

    @Test
    public void findAssignmentByIdPassingNullId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAssignmentById(null);
    }

    @Test
    public void findAssignmentByIdPassingNegativeId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAssignmentById(-1L);
    }

    @Test
    public void findAssignmentByIdPassingNonExistingId() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        manager.deleteAssignment(assignment);

        assertNotNull(assignment.getId());
        assertThat(manager.findAssignmentById(assignment.getId())).isNull();
    }

    @Test
    public void findAssignmentsOfMissionPassingNullMission() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAssignmentsOfMission(null);
    }

    @Test
    public void findAssignmentsOfMissionPassingMissionWithNullID() {
        Mission mission = notInDBMission;
        mission.setId(null);

        expectedException.expect(IllegalEntityException.class);
        manager.findAssignmentsOfMission(mission);
    }

    @Test
    public void findAssignmentsOfMissionPassingMissionWithNegativeId() {
        Mission mission = notInDBMission;
        mission.setId(-1L);

        expectedException.expect(IllegalEntityException.class);
        manager.findAssignmentsOfMission(mission);
    }

    @Test
    public void findAssignmentsOfAgentPassingNullAgent() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAssignmentsOfAgent(null);
    }

    @Test
    public void findAssignmentsOfAgentPassingAgentWithNullID() {
        Agent agent = notInDBAgent;
        notInDBAgent.setId(null);

        expectedException.expect(IllegalEntityException.class);
        manager.findAssignmentsOfAgent(agent);
    }

    @Test
    public void findAssignmentsOfAgentPassingAgentWithNegativeId() {
        Agent agent = notInDBAgent;
        agent.setId(-1L);

        expectedException.expect(IllegalEntityException.class);
        manager.findAssignmentsOfAgent(agent);
    }

    //--------------------------------------------------
    // Tests of AssignmentManagerImpl.update operation
    //--------------------------------------------------

    @Test
    public void updateAssignmentUpdatingMission() {
        testUpdateAssignment((assignment) -> assignment.setMission(existingMission2));
    }

    @Test
    public void updateAssignmentUpdatingAgent() {
        testUpdateAssignment((assignment) -> assignment.setAgent(existingAgent2));
    }

    @Test
    public void updateAssignmentUpdatingEnd() {
        testUpdateAssignment((assignment) -> assignment.setEnd(NOW.toLocalDate().plusDays(1)));
    }

    @Test
    public void updateNullAssignment() {
        expectedException.expect(IllegalArgumentException.class);
        manager.updateAssignment(null);
    }

    @Test
    public void updateAssignmentWithNullId() {
        Assignment assignment = sampleAssignment().id(null).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentWithNegativeId() {
        Assignment assignment = sampleAssignment().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentWithNonExistingId() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        manager.deleteAssignment(assignment);

        assertNotNull(assignment.getId());
        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentWithNullMission() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        sampleAssignment.setMission(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentWithNullMissionID() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        notInDBMission.setId(null);
        sampleAssignment.setMission(notInDBMission);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentWithNegativeMissionId() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Mission mission = sampleAssignment.getMission();
        mission.setId(-1L);
        sampleAssignment.setMission(mission);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentMissionToSuccessfulMission() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Mission mission = sampleAssignment.getMission();
        mission.setSuccessful(true);
        missionManager.updateMission(mission);
        sampleAssignment.setMission(mission);

        expectedException.expect(AssignmentException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentMissionToFinishedMission() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Mission mission = sampleAssignment.getMission();
        mission.setFinished(true);
        missionManager.updateMission(mission);
        sampleAssignment.setMission(mission);

        expectedException.expect(AssignmentException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentWithNullAgent() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        assignment.setAgent(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentWithNullAgentID() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        notInDBAgent.setId(null);
        assignment.setAgent(notInDBAgent);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentWithNegativeAgentID() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        Agent agent = notInDBAgent;
        notInDBAgent.setId(-1L);
        assignment.setAgent(agent);

        expectedException.expect(EntityValidationException.class);
        manager.updateAssignment(assignment);
    }

    @Test
    public void updateAssignmentAgentToDeadAgent() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Agent agent = sampleAssignment.getAgent();
        agent.setAlive(false);
        agentManager.updateAgent(agent);
        sampleAssignment.setAgent(agent);

        expectedException.expect(AssignmentException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentAgentToAgentAlreadyOnMission() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Agent agent = sampleAssignment.getAgent();
        agent.setOnMission(true);
        agentManager.updateAgent(agent);
        sampleAssignment.setAgent(agent);

        expectedException.expect(AssignmentException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentAgentToWithLowRank() {
        Assignment sampleAssignment = sampleAssignment().build();
        manager.createAssignment(sampleAssignment);
        Mission mission = sampleAssignment.getMission();
        mission.setMinAgentRank(2);
        missionManager.updateMission(mission);
        sampleAssignment.setMission(mission);
        Agent agent = sampleAssignment.getAgent();
        agent.setRank(1);
        agentManager.updateAgent(agent);
        sampleAssignment.setAgent(agent);

        expectedException.expect(AssignmentException.class);
        manager.updateAssignment(sampleAssignment);
    }

    @Test
    public void updateAssignmentToPastEnd() {
        expectedException.expect(EntityValidationException.class);
        testUpdateAssignment((assignment) -> assignment.setEnd(NOW.toLocalDate().minusDays(1)));
    }

    //--------------------------------------------------
    // Tests of AssignmentManagerImpl.delete operation
    //--------------------------------------------------

    @Test
    public void deleteAssignment() {
        Assignment a1 = sampleAssignment().build();
        Assignment a2 = anotherAssignment().build();

        manager.createAssignment(a1);
        manager.createAssignment(a2);

        manager.deleteAssignment(a1);
        assertThat(manager.findAllAssignments()).usingFieldByFieldElementComparator().containsOnly(a2);
    }

    @Test
    public void deleteNullAssignment() {
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteAssignment(null);
    }

    @Test
    public void deleteAssignmentPassingNullId() {
        expectedException.expect(IllegalEntityException.class);
        manager.deleteAssignment(sampleAssignment().id(null).build());
    }

    @Test
    public void deleteAssignmentPassingNonExistingId() {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        manager.deleteAssignment(assignment);

        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.deleteAssignment(assignment);

    }

    @Test
    public void deleteAssignmentPassingNegativeId() {
        Assignment assignment = sampleAssignment().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.deleteAssignment(assignment);

    }

    //------------------------------------------------------------
    // Tests for exceptions thrown when Database failure appears
    //------------------------------------------------------------

    @Test
    public void createAssignmentWithSQLExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        Assignment assignment = sampleAssignment().build();
        assertThatThrownBy(() -> manager.createAssignment(assignment))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateAssignmentWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException((manager) -> manager.updateAssignment(assignment));
    }

    @Test
    public void deleteAssignmentWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException((manager) -> manager.deleteAssignment(assignment));
    }

    @Test
    public void findAssignmentByIdWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException((manager) -> manager.findAssignmentById(assignment.getId()));
    }

    @Test
    public void findAssignmentsOfMissionWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException(
                (manager) -> manager.findAssignmentsOfMission(assignment.getMission())
        );
    }

    @Test
    public void findAssignmentsOfAgentWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException(
                (manager) -> manager.findAssignmentsOfAgent(assignment.getAgent())
        );
    }

    @Test
    public void findActiveAssignmentsWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException(
                (manager) -> manager.findActiveAssignments()
        );
    }

    @Test
    public void findExpiredAssignmentsWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException(
                (manager) -> manager.findEndedAssignments()
        );
    }

    @Test
    public void findAllAssignmentsWithSQLExceptionThrown() throws SQLException {
        Assignment assignment = sampleAssignment().build();
        manager.createAssignment(assignment);
        testExpectedServiceFailureException(
                (manager) -> manager.findActiveAssignments()
        );
    }

    //-----------------------
    // Additional resources
    //-----------------------

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateAssignment(Operation<Assignment> updateOperation) {
        Assignment assignmentToUpdate = sampleAssignment().build();
        Assignment anotherAssignment = anotherAssignment().build();

        manager.createAssignment(assignmentToUpdate);
        manager.createAssignment(anotherAssignment);

        updateOperation.callOn(assignmentToUpdate);

        manager.updateAssignment(assignmentToUpdate);
        assertThat(assignmentToUpdate)
                .isEqualToComparingFieldByField(manager.findAssignmentById(assignmentToUpdate.getId()));
        assertThat(anotherAssignment)
                .isEqualToComparingFieldByField(manager.findAssignmentById(anotherAssignment.getId()));
    }

    private void testExpectedServiceFailureException(Operation<AssignmentManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
}