package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Class provides tests for overridden methods of Agent class.
 *
 * Created by nayriva on 19.3.2017.
 */
public class AgentTest {
    private Agent sampleAgent;
    private Agent equalAgent;
    private Agent unequalAgent;

    @Before
    public void setUp() throws Exception {
        sampleAgent = new Agent();
        sampleAgent.setId(1L);
        sampleAgent.setSpecialPower(AgentManagerImplTest.SP_POWER_IN_DB1);
        sampleAgent.setName("Mark");
        sampleAgent.setAlive(true);
        sampleAgent.setRank(1);

        equalAgent = new Agent();
        equalAgent.setId(1L);
        equalAgent.setSpecialPower(AgentManagerImplTest.SP_POWER_IN_DB1);
        equalAgent.setName("Mark");
        equalAgent.setAlive(true);
        equalAgent.setRank(1);

        unequalAgent = new Agent();
        unequalAgent.setId(2L);
        unequalAgent.setSpecialPower(AgentManagerImplTest.SP_POWER_NOT_IN_DB);
        unequalAgent.setName("Marcus");
        unequalAgent.setAlive(false);
        unequalAgent.setRank(2);
    }

    @Test
    public void equalsOK() {
        assertTrue(sampleAgent.equals(equalAgent));
        assertTrue(equalAgent.equals(sampleAgent));
    }

    @Test
    public void equalsNULL() {
        assertFalse(sampleAgent.equals(null));
    }

    @Test
    public void equalsNOK() {
        assertFalse(sampleAgent.equals(unequalAgent));
        assertFalse(unequalAgent.equals(sampleAgent));
    }

    @Test
    public void hashcodeOK() {
        assertEquals(sampleAgent.hashCode(), equalAgent.hashCode());
    }

    @Test
    public void hashcodeNOK() {
        assertNotEquals(sampleAgent.hashCode(), unequalAgent.hashCode());
    }

    @Test
    public void toStringOK() {
        String equalString = sampleAgent.getId()
                + ", name: " + sampleAgent.getName()
                + ", rank: " + sampleAgent.getRank();

        assertEquals(equalString, sampleAgent.toString());
    }

    @Test
    public void toStringNOK() {
        String unequalString = sampleAgent.getId()
                + ",name: " + sampleAgent.getName()
                + " rank: " + sampleAgent.getRank();

        assertNotEquals(unequalString, sampleAgent.toString());
    }
}