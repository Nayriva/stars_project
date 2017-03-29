package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Agent;
import cz.muni.fi.pv168.db_backend.backend.Assignment;
import cz.muni.fi.pv168.db_backend.backend.Mission;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.*;

/**
 * Class provides tests for overridden methods of Assignment class.
 *
 * Created by nayriva on 19.3.2017.
 */
public class AssignmentTest {
    private Assignment sampleAssignment;
    private Assignment equalAssignment;
    private Assignment unequalAssignment;

    @Before
    public void setUp() {
        sampleAssignment = new Assignment();
        sampleAssignment.setId(1L);
        sampleAssignment.setStart(LocalDate.of(2017, Month.DECEMBER, 11));
        sampleAssignment.setEnd(LocalDate.of(2017, Month.DECEMBER, 11));

        equalAssignment = new Assignment();
        equalAssignment.setId(1L);
        equalAssignment.setStart(LocalDate.of(2017, Month.DECEMBER, 11));
        equalAssignment.setEnd(LocalDate.of(2017, Month.DECEMBER, 11));

        unequalAssignment = new Assignment();
        unequalAssignment.setId(2L);
        unequalAssignment.setStart(LocalDate.of(2016, Month.DECEMBER, 11));
        unequalAssignment.setEnd(LocalDate.of(2016, Month.DECEMBER, 11));

        setUpMissions();
        setUpAgents();
    }

    @Test
    public void equalsOK() {
        assertTrue(sampleAssignment.equals(equalAssignment));
        assertTrue(equalAssignment.equals(sampleAssignment));
    }

    @Test
    public void equalsNULL() {
        assertFalse(sampleAssignment.equals(null));
    }

    @Test
    public void equalsNOK() {
        assertFalse(sampleAssignment.equals(unequalAssignment));
        assertFalse(unequalAssignment.equals(sampleAssignment));
    }

    @Test
    public void hashCodeOK() {
        assertEquals(sampleAssignment.hashCode(), equalAssignment.hashCode());
    }

    @Test
    public void hashCodeNOK() {
        assertNotEquals(sampleAssignment.hashCode(), unequalAssignment.hashCode());
    }

    @Test
    public void toStringOK() {
        String equalString = sampleAssignment.getId()
                + ", mission: " + sampleAssignment.getMission().getId()
                + ", agent: " + sampleAssignment.getAgent().getId();

        assertEquals(equalString, sampleAssignment.toString());
    }

    @Test
    public void toStringNOK() {
        String unequalString = sampleAssignment.getId()
                + ",mission: " + sampleAssignment.getMission().getId()
                + "agent: " + sampleAssignment.getAgent().getId();

        assertNotEquals(unequalString, sampleAssignment.toString());
    }

    //-----------------------
    // Additional resources
    //-----------------------

    private void setUpMissions() {
        Mission sampleMission =  new Mission();
        sampleMission.setId(1L);
        sampleMission.setName("Butterfly");
        sampleMission.setPlace("Russia");
        sampleMission.setSuccessful(true);
        sampleMission.setFinished(true);
        sampleMission.setMinAgentRank(1);

        Mission anotherMission = new Mission();
        anotherMission.setId(2L);
        anotherMission.setName("Sunrise");
        anotherMission.setPlace("USA");
        anotherMission.setSuccessful(false);
        anotherMission.setFinished(false);
        anotherMission.setMinAgentRank(3);

        sampleAssignment.setMission(sampleMission);
        equalAssignment.setMission(sampleMission);
        unequalAssignment.setMission(anotherMission);
    }

    private void setUpAgents() {
        Agent sampleAgent = new Agent();
        sampleAgent.setId(1L);
        sampleAgent.setSpecialPower(1);
        sampleAgent.setName("Mark");
        sampleAgent.setAlive(true);
        sampleAgent.setRank(1);

        Agent anotherAgent = new Agent();
        anotherAgent.setId(2L);
        anotherAgent.setSpecialPower(2);
        anotherAgent.setName("Marcus");
        anotherAgent.setAlive(false);
        anotherAgent.setRank(2);

        sampleAssignment.setAgent(sampleAgent);
        equalAssignment.setAgent(sampleAgent);
        unequalAssignment.setAgent(anotherAgent);
    }

}