package db_backend;

import org.junit.Before;
import org.junit.Test;
import db_backend.backend.Mission;

import static org.junit.Assert.*;

/**
 * Class provides tests for overridden methods of Mission class.
 *
 * Created by nayriva on 19.3.2017.
 */
public class MissionTest {
    
    private Mission sampleMission;
    private Mission equalMission;
    private Mission unequalMission;
    
    @Before
    public void setUp() {
        sampleMission = new Mission();
        sampleMission.setId(1L);
        sampleMission.setName("Butterfly");
        sampleMission.setPlace("Russia");
        sampleMission.setSuccessful(true);
        sampleMission.setFinished(true);
        sampleMission.setMinAgentRank(1);

        equalMission = new Mission();
        equalMission.setId(1L);
        equalMission.setName("Butterfly");
        equalMission.setPlace("Russia");
        equalMission.setSuccessful(true);
        equalMission.setFinished(true);
        equalMission.setMinAgentRank(1);

        unequalMission = new Mission();
        unequalMission.setId(2L);
        unequalMission.setName("Sunrise");
        unequalMission.setPlace("USA");
        unequalMission.setSuccessful(false);
        unequalMission.setFinished(false);
        unequalMission.setMinAgentRank(3);
    }

    @Test
    public void equalsOK() {
        assertTrue(sampleMission.equals(equalMission));
        assertTrue(equalMission.equals(sampleMission));
    }

    @Test
    public void equalsNULL() {
        assertFalse(sampleMission.equals(null));
    }

    @Test
    public void equalsNOK() {
        assertFalse(sampleMission.equals(unequalMission));
        assertFalse(unequalMission.equals(sampleMission));
    }

    @Test
    public void hashCodeOK() {
        assertEquals(sampleMission.hashCode(), equalMission.hashCode());
    }

    @Test
    public void hashCodeNOK() {
        assertNotEquals(sampleMission.hashCode(), unequalMission.hashCode());
    }

    @Test
    public void toStringOK() {
        String equalString = sampleMission.getId()
                + ", name: " + sampleMission.getName()
                + ", task: " + sampleMission.getTask();

        assertEquals(equalString, sampleMission.toString());
    }

    @Test
    public void toStringNOK() {
        String unequalString = sampleMission.getId()
                + ", name:" + sampleMission.getName()
                + ",task: " + sampleMission.getTask();

        assertNotEquals(unequalString, sampleMission.toString());
    }
}