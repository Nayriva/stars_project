package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.db_backend.backend.*;
import cz.muni.fi.pv168.db_backend.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by nayriva on 29.3.17.
 */

@WebServlet (STARSServlet.URL_MAPPING + "/*")
public class STARSServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/app";
    private final static Logger log = LoggerFactory.getLogger(STARSServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        log.debug("doGet ...");
        showData(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        req.setCharacterEncoding("utf-8");
        String action = req.getPathInfo();
        switch (action) {
            case "/addAgent" : addAgent(req, resp); break;
            case "/addMission": addMission(req, resp); break;
            case "/addAssignment": addAssignment(req, resp); break;
            case "/updateAgent": updateAgent(req, resp); break;
            case "/updateMission": updateMission(req, resp); break;
            case "/updateAssignment": updateAssignment(req, resp); break;
            case "/deleteAgent": deleteAgent(req, resp); break;
            case "/deleteMission": deleteMission(req, resp); break;
            case "/deleteAssignment": deleteAssignment(req, resp); break;
            case "/endAssignment": endAssignment(req, resp); break;
            case "/agentStartUpdate" : {
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("toEdit", "agent");
                req.setAttribute("agentToUpdate", getAgentManager().findAgentById(id));
                showData(req, resp);
            } break;
            case "/missionStartUpdate" : {
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("toEdit", "mission");
                req.setAttribute("missionToUpdate", getMissionManager().findMissionById(id));
                showData(req, resp);
            } break;
            case "/assignmentStartUpdate" : {
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("toEdit", "assignment");
                req.setAttribute("assignmentToUpdate", getAssignmentManager().findAssignmentById(id));
                showData(req, resp);
            } break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    private MissionManager getMissionManager() {
        return (MissionManager) getServletContext().getAttribute("missionMgr");
    }

    private AgentManager getAgentManager() {
        return (AgentManager) getServletContext().getAttribute("agentMgr");
    }

    private AssignmentManager getAssignmentManager() {
        return (AssignmentManager) getServletContext().getAttribute("assignmentMgr");
    }

    private void showData(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        try {
            req.setAttribute("agents", getAgentManager().findAllAgents());
            req.setAttribute("missions", getMissionManager().findAllMissions());
            req.setAttribute("assignments", getAssignmentManager().findAllAssignments());
            req.getRequestDispatcher(LIST_JSP).forward(req, resp);
        } catch (ServiceFailureException ex) {
            log.error("Cannot show data!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void addAgent(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Creating agent...");
        String name = req.getParameter("crAgentName");
        String specialPower = req.getParameter("crAgentSpecialPower");
        String rank = req.getParameter("crAgentRank");

        if (!validateAgentFields(name, specialPower, rank)) {
            log.debug("Invalid data in addAgent");
            req.setAttribute("validationError", "Cannot create agent, all fields must be set!");
            showData(req, resp);
            return;
        }

        int rankInt = validateAgentRank(rank, req);
        if (rankInt < 1) {
            log.debug("rankInt does not satisfy 0 < x!");
            req.setAttribute("validationError", "Rank must satisfy 0 < x!");
            showData(req, resp);
            return;
        }

        try {
            Agent agent = new AgentBuilder().name(name).specialPower(specialPower).alive(true)
                    .rank(rankInt).build();
            getAgentManager().createAgent(agent);
            resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.debug("Cannot add agent into DB, DB problem!", ex.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.debug("Cannot add agent into DB, validation problem!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void updateAgent(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Updating agent...");
        boolean oldAlive = Boolean.valueOf(req.getParameter("oldAlive"));
        Agent toUpdate = getAgentToUpdate(req, resp);
        if (toUpdate != null) {
            try {
                if (!toUpdate.isAlive() && toUpdate.isAlive() != oldAlive) {
                    endAssignmentOfAgent(toUpdate.getId(), req, resp);
                }
                getAgentManager().updateAgent(toUpdate);
                resp.sendRedirect(req.getContextPath()+URL_MAPPING + "/");
            } catch (ServiceFailureException ex) {
                log.debug("Cannot update agent in DB, DB problem!", ex.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            } catch (EntityValidationException ex) {
                log.debug("Cannot update agent in DB, validation problem!", ex.getMessage());
                req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
                showData(req, resp);
            }
        } else {
            showData(req, resp);
        }
    }

    private void deleteAgent(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Deleting agent...");
        try {
            Long id = Long.valueOf(req.getParameter("id"));
            endAssignmentOfAgent(id, req, resp);
            getAgentManager().deleteAgent(new AgentBuilder().id(id).build());
            log.debug("redirecting after POST");
            resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.error("Cannot delete agent, DB problem!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.error("Cannot delete agent, validation error!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void addMission(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Creating mission...");
        String name = req.getParameter("crMissionName");
        String task = req.getParameter("crMissionTask");
        String place = req.getParameter("crMissionPlace");
        String minAgentRank = req.getParameter("crMissionMinAgentRank");

        if (!validateMissionFields(name, task, place, minAgentRank)) {
            log.debug("Invalid data in addMission");
            req.setAttribute("validationError", "Cannot create mission, all fields must be set!");
            showData(req, resp);
            return;
        }

        int minAgentRankInt = validateMissionMinAgentRank(minAgentRank, req);
        if (minAgentRankInt < 1) {
            log.debug("minAgentRankInt does not satisfy 0 < x!");
            req.setAttribute("validationError", "M_A_R must satisfy 0 < x!");
            showData(req, resp);
            return;
        }

        try {
            Mission mission = new MissionBuilder().name(name).task(task).place(place).successful(false).finished(false)
                    .minAgentRank(minAgentRankInt).build();
            getMissionManager().createMission(mission);
            resp.sendRedirect(req.getContextPath()+URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.debug("Cannot add mission into DB, DB problem!", ex.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.debug("Cannot validate input in DB, validation problem!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void updateMission(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Updating mission...");
        boolean oldFinished = Boolean.valueOf(req.getParameter("oldFin"));
        boolean oldSuccessful = Boolean.valueOf(req.getParameter("oldSuc"));
        Mission toUpdate = getMissionToUpdate(req, resp);
        if (toUpdate != null) {
            try {
                if (toUpdate.isFinished() != oldFinished || toUpdate.isSuccessful() != oldSuccessful) {
                    endAssignmentsOfMission(toUpdate.getId(), req, resp);
                }
                getMissionManager().updateMission(toUpdate);
                resp.sendRedirect(req.getContextPath()+URL_MAPPING + "/");
            } catch (ServiceFailureException ex) {
                log.debug("Cannot update agent in DB, DB problem!", ex.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            } catch (EntityValidationException ex) {
                log.debug("Cannot update agent in DB, validation problem!", ex.getMessage());
                req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
                showData(req, resp);
            }
        } else {
            showData(req, resp);
        }
    }

    private void deleteMission(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Deleting mission...");
        try {
            Long id = Long.valueOf(req.getParameter("id"));
            endAssignmentsOfMission(id, req, resp);
            getMissionManager().deleteMission(new MissionBuilder().id(id).build());
            log.debug("redirecting after POST");
            resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.error("Cannot delete mission, DB problem!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.error("Cannot delete mission, validation error!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void addAssignment(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Creating assignment...");
        String agentId = req.getParameter("crAssignmentAgentId");
        String missionId = req.getParameter("crAssignmentMissionId");

        if (! validateAssignmentFields(agentId, missionId)) {
            log.debug("Invalid data in addAssignment");
            req.setAttribute("validationError", "Cannot create assignment, all fields must be set!");
            showData(req, resp);
            return;
        }

        Long agentIdLong = getAgentIdForAssignment(agentId, req, resp);
        Long missionIdLong = getMissionIdForAssignment(missionId, req, resp);
        if (missionIdLong == null || agentIdLong == null) {
            showData(req, resp);
            return;
        }

        try {
            Mission mission = getMissionManager().findMissionById(missionIdLong);
            Agent agent = getAgentManager().findAgentById(agentIdLong);
            Assignment assignment = new AssignmentBuilder().mission(mission.getId()).agent(agent.getId())
                    .start(LocalDate.now(Clock.systemUTC())).end(null).build();
            getAssignmentManager().createAssignment(assignment);
            resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.debug("Cannot add assignment into DB, DB problem!", ex.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.debug("Cannot validate input in DB, validation problem!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        } catch (AssignmentException ex) {
            log.debug("Cannot add assignment into DB, assignment problem!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void updateAssignment(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Updating mission...");
        Assignment toUpdate = getAssignmentToUpdate(req, resp);
        if (toUpdate != null ) {
            try {
                Assignment oldVersion = getAssignmentManager().findAssignmentById(toUpdate.getId());
                toUpdate.setStart(oldVersion.getStart());
                toUpdate.setEnd(oldVersion.getEnd());
                getAssignmentManager().updateAssignment(toUpdate);
                resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
            } catch (ServiceFailureException ex) {
                log.debug("Cannot update assignment in DB, DB problem!", ex.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            } catch (EntityValidationException ex) {
                log.debug("Cannot update assignment in DB, validation problem!", ex.getMessage());
                req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
                showData(req, resp);
            } catch (AssignmentException ex) {
                log.debug("Cannot update assignment in DB, assignment problem!", ex.getMessage());
                req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
                showData(req, resp);
            }
        } else {
            showData(req, resp);
        }
    }

    private void deleteAssignment(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Deleting assignment...");
        try {
            Long id = Long.valueOf(req.getParameter("id"));
            getAssignmentManager().deleteAssignment(new AssignmentBuilder().id(id).build());
            log.debug("redirecting after POST");
            resp.sendRedirect(req.getContextPath() + URL_MAPPING + "/");
        } catch (ServiceFailureException ex) {
            log.error("Cannot delete assignment, DB problem!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.error("Cannot delete assignment, validation error!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private Agent getAgentToUpdate(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        Long id = Long.valueOf(req.getParameter("uAgentId"));
        String name = req.getParameter("uAgentName");
        String specialPower = req.getParameter("uAgentSpecialPower");
        boolean alive = req.getParameter("uAgentAlive") != null;
        String rank = req.getParameter("uAgentRank");

        if (!validateAgentFields(name, specialPower, rank)) {
            log.debug("Invalid data in updateAgent");
            req.setAttribute("validationError", "Cannot update agent, all fields must be set!");
            return null;
        }

        int rankInt = validateAgentRank(rank, req);
        if (rankInt < 1) {
            log.debug("rankInt does not satisfy 0 < x!");
            req.setAttribute("validationError", "Rank must satisfy 0 < x!");
            return null;
        }

        return new AgentBuilder().id(id).name(name).specialPower(specialPower).alive(alive)
                .rank(rankInt).build();
    }

    private boolean validateAgentFields(String name, String specialPower, String rank) {
        return ! (name == null || name.isEmpty() || specialPower == null || specialPower.isEmpty()
                || rank == null || rank.isEmpty());
    }

    private int validateAgentRank(String rank, HttpServletRequest req)
        throws ServletException, IOException
    {
        int rankInt = -1;
        try {
            rankInt = Integer.parseInt(rank);
        } catch (NumberFormatException ex) {
            log.debug("Invalid rank format!");
            req.setAttribute("validationError", "Invalid format in rank!");
        }
        return rankInt;
    }

    private boolean validateMissionFields(String name, String task, String place, String minAgentRank) {
        return ! (name == null || name.isEmpty() || task == null || task.isEmpty()
                || place == null || place.isEmpty() || minAgentRank == null || minAgentRank.isEmpty());
    }

    private int validateMissionMinAgentRank(String minAgentRank, HttpServletRequest req)
        throws ServletException, IOException
    {
        int minAgentRankInt = -1;
        try {
            minAgentRankInt = Integer.parseInt(minAgentRank);
        } catch (NumberFormatException ex) {
            log.debug("Invalid minAgentRank param format!");
            req.setAttribute("validationError", "Invalid param format in M_A_R!");
        }
        return minAgentRankInt;
    }

    private Mission getMissionToUpdate(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        Long id = Long.valueOf(req.getParameter("uMissionId"));
        String name = req.getParameter("uMissionName");
        String task = req.getParameter("uMissionTask");
        String place = req.getParameter("uMissionPlace");
        boolean successful = req.getParameter("uMissionSuccessful") != null;
        boolean finished = req.getParameter("uMissionFinished") != null;
        String minAgentRank = req.getParameter("uMissionMinAgentRank");

        if (!validateMissionFields(name, task, place, minAgentRank)) {
            log.debug("Invalid data in addMission");
            req.setAttribute("validationError", "Cannot create mission, all fields must be set!");
            return null;
        }

        int minAgentRankInt = validateMissionMinAgentRank(minAgentRank, req);
        if (minAgentRankInt < 1) {
            log.debug("minAgentRankInt does not satisfy 0 < x!");
            req.setAttribute("validationError", "M_A_R must satisfy 0 < x!");
            return null;
        }

        return new MissionBuilder().id(id).name(name).task(task).place(place)
                .successful(successful).finished(finished).minAgentRank(minAgentRankInt).build();
    }

    private void endAssignmentOfAgent(Long agentId, HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Ending assignments of deleted agent...");
        try {
            List<Assignment> ofAgent = getAssignmentManager().findAssignmentsOfAgent(agentId);
            ofAgent.removeIf((assignment) -> assignment.getEnd() != null);
            for (Assignment a: ofAgent) {
                a.setEnd(LocalDate.now(Clock.systemUTC()));
                getAssignmentManager().updateAssignment(a);
            }
        } catch (ServiceFailureException ex) {
            log.error("Cannot end assignments of deleted agent, DB problem!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.error("Cannot end assignments of deleted agent, validation error!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private void endAssignmentsOfMission(Long missionId, HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Ending assignments of deleted mission...");
        try {
            List<Assignment> ofMission = getAssignmentManager().findAssignmentsOfMission(missionId);
            ofMission.removeIf((assignment) -> assignment.getEnd() != null);
            log.debug("Ending assignments...");
            for (Assignment a: ofMission) {
                a.setEnd(LocalDate.now(Clock.systemUTC()));
                getAssignmentManager().updateAssignment(a);
            }
        } catch (ServiceFailureException ex) {
            log.error("Cannot end assignments of mission, DB problem!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.error("Cannot end assignments of mission, validation error!", ex.getMessage());
            req.setAttribute("validationError", "DB problem occurred, " + ex.getMessage());
            showData(req, resp);
        }
    }

    private Assignment getAssignmentToUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        Long id = Long.valueOf(req.getParameter("uAssignmentId"));
        String agentId = req.getParameter("uAssignmentAgentId");
        String missionId = req.getParameter("uAssignmentMissionId");

        if (! validateAssignmentFields(agentId, missionId)) {
            log.debug("Invalid data in updateAssignment");
            req.setAttribute("validationError", "Cannot update assignment, all fields must be set!");
            return null;
        }

        Long agentIdLong = getAgentIdForAssignment(agentId, req, resp);
        Long missionIdLong = getMissionIdForAssignment(missionId, req, resp);

        if (missionIdLong == null || agentIdLong == null) {
            return null;
        }

        return new AssignmentBuilder().id(id).agent(agentIdLong).mission(missionIdLong).build();
    }

    private boolean validateAssignmentFields(String agentId, String missionId) {
        return !(agentId == null || agentId.isEmpty() || missionId == null || missionId.isEmpty());
    }

    @SuppressWarnings("Duplicates")
    private Long getMissionIdForAssignment(String missionId, HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Parsing id of mission for assignment...");
        Long missionIdLong;
        try {
            missionIdLong = Long.parseLong(missionId);
            try {
                if (getMissionManager().findMissionById(missionIdLong) == null) {
                    log.debug("Cannot find entity in DB!");
                    req.setAttribute("validationError", "Specified mission is not stored in DB!");
                    return null;
                }
            } catch (ServiceFailureException ex) {
                log.debug("Cannot add assignment into DB, DB problem!", ex.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                return null;
            } catch (IllegalEntityException | IllegalArgumentException ex) {
                log.debug("Cannot validate input in DB!", ex.getMessage());
                req.setAttribute("validationError", ex.getMessage());
                return null;
            }
        } catch (NumberFormatException ex) {
            log.debug("Invalid missionId format!");
            req.setAttribute("validationError", "Invalid format in missionId!");
            return null;
        }
        return missionIdLong;
    }

    @SuppressWarnings("Duplicates")
    private Long getAgentIdForAssignment(String agentId, HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        log.debug("Parsing id of mission for assignment...");
        Long agentIdLong;
        try {
            agentIdLong = Long.parseLong(agentId);
            try {
                if (getAgentManager().findAgentById(agentIdLong) == null) {
                    log.debug("Cannot find entity in DB!");
                    req.setAttribute("validationError", "Specified agent is not stored in DB!");
                    return null;
                }
            } catch (ServiceFailureException ex) {
                log.debug("Cannot add assignment into DB, DB problem!", ex.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                return null;
            } catch (IllegalEntityException | IllegalArgumentException ex) {
                log.debug("Cannot validate input in DB!", ex.getMessage());
                req.setAttribute("validationError", ex.getMessage());
                return null;
            }
        } catch (NumberFormatException ex) {
            log.debug("Invalid agentId format!");
            req.setAttribute("validationError", "Invalid format in agentId!");
            return null;
        }
        return agentIdLong;
    }

    private void endAssignment(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        Long id = Long.valueOf(req.getParameter("id"));
        try {
            Assignment assignment = getAssignmentManager().findAssignmentById(id);
            assignment.setEnd(LocalDate.now(Clock.systemUTC()));
            getAssignmentManager().updateAssignment(assignment);
            showData(req, resp);
        } catch (ServiceFailureException ex) {
            log.debug("Cannot end assignment in DB, DB problem!", ex.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (EntityValidationException ex) {
            log.debug("Cannot end assignment in DB, validation problem!", ex.getMessage());
            req.setAttribute("validationError", "Entity validation problem: " + ex.getMessage());
            showData(req, resp);
        }
    }
}