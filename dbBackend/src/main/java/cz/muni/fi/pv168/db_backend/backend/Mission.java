package cz.muni.fi.pv168.db_backend.backend;

/**
 * Class represents entity - Mission.
 * Param minAgentRank must satisfy 0 < x < 12.
 *
 * Created by nayriva on 7.3.2017.
 */
public class Mission {
    private Long id;
    private String name;
    private String task;
    private String place;
    private boolean successful;
    private boolean finished;
    private int minAgentRank;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getMinAgentRank() {
        return minAgentRank;
    }

    public void setMinAgentRank(int minAgentRank) {
        this.minAgentRank = minAgentRank;
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
        } else if (! (obj instanceof Mission)) {
            return false;
        } else if (this.id == null) {
            return false;
        }

        Mission objMission = (Mission) obj;
        return this.id.equals(objMission.id);
    }

    @Override
    public String toString() {
        return id + ", name: " + name + ", task: " + task;
    }
}
