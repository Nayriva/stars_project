<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="missionManager">
    <c:if test="${toEdit eq 'mission'}">
        <br/>
        <div id="editMission" var="missionToUpdate">
            <form action="${pageContext.request.contextPath}/app/updateMission?oldSuc=${missionToUpdate.successful}?oldFin=${missionToUpdate.finished}" method="post">
                <table class="editTables">
                    <tr class="tableHeader"><th colspan="3">Edit mission</th></tr>
                    <tr>
                        <th>Id:</th>
                        <td><input type="text" name="uMissionId"
                                   value="<c:out value='${missionToUpdate.id}'/>" readonly />
                        </td>
                        <td rowspan="7"><input class="tableButtons" type="submit" value="Save"></td>
                    </tr>
                    <tr>
                        <th>Name:</th>
                        <td><input type="text" name="uMissionName"
                                   value="<c:out value='${missionToUpdate.name}'/>" />
                        </td>
                    </tr>
                    <tr>
                        <th>Task:</th>
                        <td><input type="text" name="uMissionTask"
                                   value="<c:out value='${missionToUpdate.task}'/>" />
                        </td>
                    </tr>
                    <tr>
                        <th>Place:</th>
                        <td><input type="text" name="uMissionPlace"
                                   value="<c:out value='${missionToUpdate.place}'/>" />
                        </td>
                    </tr>
                    <tr>
                        <th>Suc:</th>
                        <td><input type="checkbox" name="uMissionSuccessful"
                                   <c:if test="${missionToUpdate.successful}">checked="checked"</c:if> />
                        </td>
                    </tr>
                    <tr>
                        <th>Fin:</th>
                        <td><input type="checkbox" name="uMissionFinished"
                                   <c:if test="${missionToUpdate.finished}">checked="checked"</c:if> />
                        </td>
                    </tr>
                    <tr>
                        <th>M_A_R:</th>
                        <td><input type="number" name="uMissionMinAgentRank"
                                   value="<c:out value='${missionToUpdate.minAgentRank}'/>"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </c:if>

    <div id="addMission" style="display: none">
        <br/>
        <form action="${pageContext.request.contextPath}/app/addMission" method="post">
            <table class="createTables">
                <tr class="tableHeader"><th colspan="3">Add new mission</th></tr>
                <tr>
                    <th>Name:</th>
                    <td>
                        <input type="text" name="crMissionName" value="<c:out value='${param.crMissionName}'/>"/>
                    </td>
                    <td class="btnInTable" rowspan="4"><input class="tableButtons" type="Submit" value="Create"/></td>
                </tr>
                <tr>
                    <th>Task:</th>
                    <td>
                        <input type="text" name="crMissionTask" value="<c:out value='${param.crMissionTask}'/>"/>
                    </td>
                </tr>
                <tr>
                    <th>Place:</th>
                    <td>
                        <input type="text" name="crMissionPlace" value="<c:out value='${param.crMissionPlace}'/>"/>
                    </td>
                </tr>
                <tr>
                    <th>M_A_R:</th>
                    <td>
                        <input type="number" name="crMissionMinAgentRank"
                               value="<c:out value='${param.missionMinAgentRank}'/>"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <br/>
    <table class="listTables">
        <tr class="tableHeader"><th colspan="9">Missions in DB</th></tr>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Task</th>
            <th>Place</th>
            <th>Suc</th>
            <th>Fin</th>
            <th>M_A_R</th>
            <th colspan="2" class="btnInTable">
                <button class="tableButtons" type="button" onclick="showDiv('addMission')">Add new mission</button>
            </th>
        </tr>
        <c:forEach items="${missions}" var="mission">
            <tr>
                <td><c:out value="${mission.id}" /></td>
                <td><c:out value="${mission.name}" /></td>
                <td><c:out value="${mission.task}" /></td>
                <td><c:out value="${mission.place}" /></td>
                <td><c:out value="${mission.successful}" /></td>
                <td><c:out value="${mission.finished}" /></td>
                <td><c:out value="${mission.minAgentRank}" /></td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}/app/deleteMission?id=${mission.id}" >
                    <input class="tableButtons" type="submit" value="Delete" />
                    </form>
                </td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}
                              /app/missionStartUpdate?id=${mission.id}" >
                        <input class="tableButtons" type="submit" value="Edit" />
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>