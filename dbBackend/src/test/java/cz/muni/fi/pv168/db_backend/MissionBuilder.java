package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.Mission;

/**
 * Builder class for Missions.
 *
 * Created by nayriva on 8.3.2017.
 */
public class MissionBuilder {

    private Long id;
    private String name;
    private String task;
    private String place;
    private boolean successful;
    private boolean finished;
    private int minAgentRank;

    public MissionBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public MissionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public MissionBuilder task(String task) {
        this.task = task;
        return this;
    }

    public MissionBuilder place(String place) {
        this.place = place;
        return this;
    }

    public MissionBuilder successful(boolean successful) {
        this.successful = successful;
        return this;
    }

    public MissionBuilder finished(boolean finished) {
        this.finished = finished;
        return this;
    }

    public MissionBuilder minAgentRank(int minAgentRank) {
        this.minAgentRank = minAgentRank;
        return this;
    }

    public Mission build() {
        Mission mission = new Mission();
        mission.setId(id);
        mission.setName(name);
        mission.setTask(task);
        mission.setPlace(place);
        mission.setFinished(finished);
        mission.setSuccessful(successful);
        mission.setMinAgentRank(minAgentRank);
        return mission;
    }
}
