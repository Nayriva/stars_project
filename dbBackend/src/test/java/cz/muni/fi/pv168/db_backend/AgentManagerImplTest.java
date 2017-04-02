package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.AgentManager;
import cz.muni.fi.pv168.db_backend.backend.AgentManagerImpl;
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
 * Class provides tests for Agent manager.
 *
 * Created by Hana Navratilova on 14.3.2017.
 */
public class AgentManagerImplTest {

    private AgentManagerImpl manager;
    private DataSource ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        manager = new AgentManagerImpl();
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, AgentManagerImpl.class.getResource("createTables.sql"));
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws Exception {
        DBUtils.executeSqlScript(ds, AgentManagerImpl.class.getResource("dropTables.sql"));
    }

    private AgentBuilder sampleAgent() {
        return new AgentBuilder()
                .id(null)
                .name("Mark")
                .specialPower(1)
                .alive(true)
                .rank(2)
                .onMission(false);
    }

    private AgentBuilder sampleAgent2() {
        return new AgentBuilder()
                .id(null)
                .name("Rob")
                .specialPower(2)
                .alive(true)
                .rank(3)
                .onMission(false);
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:agent-mgrTest");
        ds.setCreateDatabase("create");
        return ds;
    }

    //----------------------------------------------------------------
    // Tests for AgentManagerImpl.createAgent(Agent) operation.
    //----------------------------------------------------------------

    @Test
    public void createAgent() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        Agent result = manager.findAgentById(agent.getId());

        assertNotNull(result);
        assertThat(result.getId()).isNotNull();
        assertThat(result).isNotSameAs(agent).isEqualToComparingFieldByField(agent);
    }

    @Test
    public void createNullAgent() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createAgent(null);
    }

    @Test
    public void createAgentWithExistingId() {
        Agent agent1 = sampleAgent().build();
        manager.createAgent(agent1);
        Agent agent2 = sampleAgent2().id(agent1.getId()).build();

        expectedException.expect(IllegalEntityException.class);
        manager.createAgent(agent2);
    }

    @Test
    public void createAgentWithNullName() {
        Agent agent = sampleAgent().name(null).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithEmptyName() {
        Agent agent = sampleAgent().name("").build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithNegativeSpecialPower() {
        Agent agent = sampleAgent().specialPower(-1).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithZeroSpecialPower() {
        Agent agent = sampleAgent().specialPower(0).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithBigSpecialPower() {
        Agent agent = sampleAgent().specialPower(12).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createDeadAgent() {
        Agent agent = sampleAgent().alive(false).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithNegativeRank() {
        Agent agent = sampleAgent().rank(-1).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithZeroRank() {
        Agent agent = sampleAgent().rank(0).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    @Test
    public void createAgentWithOnMissionTrue() {
        Agent agent = sampleAgent().onMission(true).build();

        expectedException.expect(EntityValidationException.class);
        manager.createAgent(agent);
    }

    //------------------------------------------------------
    // Tests for all AgentManagerImpl "FIND" operations
    //------------------------------------------------------

    @Test
    public void findAllAgents() {
        Agent agent1 = sampleAgent().build();
        Agent agent2 = sampleAgent2().build();
        manager.createAgent(agent1);
        manager.createAgent(agent2);

        assertThat(manager.findAllAgents())
                .usingFieldByFieldElementComparator()
                .containsOnly(agent1, agent2);
    }

    @Test
    public void findAgentById() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        assertNotNull(agent.getId());
        Agent result = manager.findAgentById(agent.getId());
        assertThat(result).isNotNull().isNotSameAs(agent).isEqualToComparingFieldByField(agent);
    }

    @Test
    public void findAgentByIdPassingNullId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentById(null);
    }

    @Test
    public void findAgentByIdPassingNegativeId() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentById(-1L);
    }

    @Test
    public void findAgentByIdPassingNonExistingId() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        manager.deleteAgent(agent);

        assertNotNull(agent.getId());
        assertThat(manager.findAgentById(agent.getId())).isNull();
    }

    @Test
    public void findAgentsByAlive() {
        Agent agent1 = sampleAgent().build();
        Agent agent2 = sampleAgent2().build();
        manager.createAgent(agent1);
        manager.createAgent(agent2);
        agent2.setAlive(false);
        manager.updateAgent(agent2);

        assertThat(manager.findAgentsByAlive(true))
                .usingFieldByFieldElementComparator().containsOnly(agent1);
        assertThat(manager.findAgentsByAlive(false))
                .usingFieldByFieldElementComparator().containsOnly(agent2);
    }

    @Test
    public void findAgentsByRank() {
        Agent agent1 = sampleAgent().rank(2).build();
        Agent agent2 = sampleAgent2().rank(3).build();
        manager.createAgent(agent1);
        manager.createAgent(agent2);

        assertThat(manager.findAgentsByRank(2))
                .usingFieldByFieldElementComparator().containsOnly(agent1);
        assertThat(manager.findAgentsByRank(3))
                .usingFieldByFieldElementComparator().containsOnly(agent2);
        assertThat(manager.findAgentsByRank(4)).isEmpty();
    }

    @Test
    public void findAgentsBySpecialPower() {
        Agent agent1 = sampleAgent().specialPower(1).build();
        Agent agent2 = sampleAgent2().specialPower(2).build();
        manager.createAgent(agent1);
        manager.createAgent(agent2);

        assertThat(manager.findAgentsBySpecialPower(1))
                .usingFieldByFieldElementComparator().containsOnly(agent1);
        assertThat(manager.findAgentsBySpecialPower(2))
                .usingFieldByFieldElementComparator().containsOnly(agent2);
        assertThat(manager.findAgentsBySpecialPower(3)).isEmpty();
    }

    @Test
    public void findAgentsByRankPassingNegativeRank() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentsByRank(-1);
    }

    @Test
    public void findAgentsByRankPassingZeroRank() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentsByRank(0);
    }

    @Test
    public void findAgentsBySpecialPowerPassingNegativeSpecPower() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentsBySpecialPower(-1);
    }

    @Test
    public void findAgentsBySpecialPowerPassingZeroSpecPower() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentsBySpecialPower(0);
    }

    @Test
    public void findAgentsBySpecialPowerPassingBigSpecPower() {
        expectedException.expect(IllegalArgumentException.class);
        manager.findAgentsBySpecialPower(12);

    }

    //----------------------------------------------------------
    // Tests for AgentManagerImpl.update(Agent) operation.
    //----------------------------------------------------------

    @Test
    public void updateAgentName() {
        testUpdateAgent((agent) -> agent.setName("Karl"));
    }

    @Test
    public void updateAgentSpecialPower() {
        testUpdateAgent((agent) -> agent.setSpecialPower(8));
    }

    @Test
    public void updateAgentRank() {
        testUpdateAgent((agent) -> agent.setRank(5));
    }

    @Test
    public void updateAgentAlive() {
        testUpdateAgent((agent) -> agent.setAlive(false));
    }

    @Test
    public void updateAgentOnMission() {
        testUpdateAgent((agent) -> agent.setOnMission(true));
    }

    @Test
    public void updateNullAgent() {
        expectedException.expect(IllegalArgumentException.class);
        manager.updateAgent(null);
    }

    @Test
    public void updateAgentWithNullId() {
        Agent agent = sampleAgent().id(null).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentWithNegativeId() {
        Agent agent = sampleAgent().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentWithNonExistingId() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        manager.deleteAgent(agent);

        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToNullName() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setName(null);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToEmptyName() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setName("");

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToNegativeRank() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setRank(-1);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToZeroRank() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setRank(0);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToNegativeSpecialPower() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setSpecialPower(-1);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToZeroSpecialPower() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setSpecialPower(0);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    @Test
    public void updateAgentToBigSpecialPower() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        agent.setSpecialPower(12);

        expectedException.expect(EntityValidationException.class);
        manager.updateAgent(agent);
    }

    //----------------------------------------------------------------
    // Tests for AgentManagerImpl.deleteAgent(Agent) operation
    //----------------------------------------------------------------

    @Test
    public void deleteAgent() {
        assertThat(manager.findAllAgents()).isEmpty();
        Agent agent1 = sampleAgent().build();
        Agent agent2 = sampleAgent2().build();
        manager.createAgent(agent1);
        manager.createAgent(agent2);

        assertNotNull("Id of agent shouldn't be null", manager.findAgentById(agent1.getId()));
        assertNotNull("Id of agent shouldn't be null", manager.findAgentById(agent2.getId()));

        manager.deleteAgent(agent1);

        assertNull("Agent should be deleted", manager.findAgentById(agent1.getId()));
        assertNotNull("Agent shouldn't be deleted", manager.findAgentById(agent2.getId()));
    }

    @Test
    public void deleteNullAgent() {
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteAgent(null);
    }

    @Test
    public void deleteAgentPassingAgentWithNullId() {
        Agent agent = sampleAgent().id(null).build();

        expectedException.expect(IllegalEntityException.class);
        manager.deleteAgent(agent);
    }

    @Test
    public void deleteAgentPassingAgentWithNegativeId() {
        Agent agent = sampleAgent().id(-1L).build();

        expectedException.expect(IllegalEntityException.class);
        manager.deleteAgent(agent);
    }

    @Test
    public void deleteAgentPassingNonExistingAgent() {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);
        manager.deleteAgent(agent);

        expectedException.expect(ServiceFailureException.class);
        expectedException.expectCause(isA(IllegalEntityException.class));
        manager.deleteAgent(agent);
    }

    //------------------------------------------------------------
    // Tests for exceptions thrown when Database failure appears
    //------------------------------------------------------------

    @Test
    public void createAgentWithSQLExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);

        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        Agent agent = sampleAgent().build();
        assertThatThrownBy(() -> manager.createAgent(agent))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateAgentWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((agentManager) -> manager.updateAgent(agent));
    }

    @Test
    public void deleteAgentWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.deleteAgent(agent));
    }

    @Test
    public void findAgentByIdWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.findAgentById(agent.getId()));
    }

    @Test
    public void findAgentsBySpecialPowerWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.findAgentsBySpecialPower(1));
    }

    @Test
    public void findAgentsByRankWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.findAgentsByRank(1));
    }

    @Test
    public void findAgentsByAliveWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.findAgentsByAlive(true));
    }

    @Test
    public void findAllAgentsWithSQLExceptionThrown() throws SQLException {
        Agent agent = sampleAgent().build();
        manager.createAgent(agent);

        testExpectedServiceFailureException((manager) -> manager.findAllAgents());
    }

    //-----------------------
    // Additional resources
    //-----------------------

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateAgent(Operation<Agent> updateOperation) {
        Agent agentToUpdate = sampleAgent().build();
        Agent agent2 = sampleAgent2().build();
        manager.createAgent(agentToUpdate);
        manager.createAgent(agent2);

        updateOperation.callOn(agentToUpdate);
        manager.updateAgent(agentToUpdate);

        assertThat(agentToUpdate)
                .isEqualToComparingFieldByField(manager.findAgentById(agentToUpdate.getId()));
        assertThat(agent2)
                .isEqualToComparingFieldByField(manager.findAgentById(agent2.getId()));
    }

    private void testExpectedServiceFailureException(Operation<AgentManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);

        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
}