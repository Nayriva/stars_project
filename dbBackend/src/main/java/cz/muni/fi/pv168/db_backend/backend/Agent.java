package cz.muni.fi.pv168.db_backend.backend;

/**
 * Class represents entity - Agent.
 *
 * Created by nayriva on 7.3.2017.
 */
public class Agent {
    private Long id;
    private String name;
    private String specialPower;
    private boolean alive;
    private int rank;

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

    public String getSpecialPower() {
        return specialPower;
    }

    public void setSpecialPower(String specialPower) {
        this.specialPower = specialPower;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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
        } else if (! (obj instanceof Agent)) {
            return false;
        } else if (this.id == null) {
            return false;
        }

        Agent objAgent = (Agent) obj;

        return this.id.equals(objAgent.id);
    }

    @Override
    public String toString() {
        return id + ", name: " + name + ", rank: " + rank;
    }
}
