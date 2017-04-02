package cz.muni.fi.pv168.db_backend.common;

import cz.muni.fi.pv168.db_backend.backend.Agent;

/**
 * Builder class for Agents.
 *
 * Created by nayriva on 9.3.2017.
 */
public class AgentBuilder {
    private Long id;
    private String name;
    private int specialPower;
    private boolean alive;
    private int rank;
    private boolean onMission;

    public AgentBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AgentBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AgentBuilder specialPower(int specialPower) {
        this.specialPower = specialPower;
        return this;
    }

    public AgentBuilder alive(boolean alive) {
        this.alive = alive;
        return this;
    }

    public AgentBuilder rank(int rank) {
        this.rank = rank;
        return this;
    }

    public AgentBuilder onMission(boolean onMission) {
        this.onMission = onMission;
        return this;
    }

    public Agent build() {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName(name);
        agent.setSpecialPower(specialPower);
        agent.setAlive(alive);
        agent.setRank(rank);
        agent.setOnMission(onMission);
        return agent;
    }
}
