package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Assignment;

import java.time.LocalDate;

/**
 * Builder class for Assignments.
 *
 * Created by nayriva on 9.3.2017.
 */
public class AssignmentBuilder {
    private Long id;
    private Long mission;
    private Long agent;
    private LocalDate start;
    private LocalDate end;

    public AssignmentBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AssignmentBuilder mission(Long mission) {
        this.mission = mission;
        return this;
    }

    public AssignmentBuilder agent(Long agent) {
        this.agent = agent;
        return this;
    }

    public AssignmentBuilder start(LocalDate start) {
        this.start = start;
        return this;
    }

    public AssignmentBuilder end(LocalDate end) {
        this.end = end;
        return this;
    }

    public Assignment build() {
        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setMission(mission);
        assignment.setAgent(agent);
        assignment.setStart(start);
        assignment.setEnd(end);
        return assignment;
    }
}
