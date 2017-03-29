package cz.muni.fi.pv168.db_backend.backend;

import java.time.LocalDate;

/**
 * Class represents entity - Assignment. Assignment connects agent with mission.
 *
 * Created by nayriva on 7.3.2017.
 */
public class Assignment {
    private Long id;
    private Mission mission;
    private Agent agent;
    private LocalDate start;
    private LocalDate end;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (id != null) {
            hash = 31 * hash + id.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (! (obj instanceof Assignment)) {
            return false;
        } else if (this.id == null) {
            return false;
        }

        Assignment objAssignment = (Assignment) obj;
        return this.id.equals(objAssignment.id);
    }

    @Override
    public String toString() {
        return id  + ", mission: " + mission.getId() + ", agent: " + agent.getId();
    }
}
