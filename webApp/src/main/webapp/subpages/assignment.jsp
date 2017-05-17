<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="assignmentManager">
    <c:if test="${toEdit eq 'assignment'}">
        <br/>
        <div id="editAssignment" var="assignmentToUpdate">
            <form action="${pageContext.request.contextPath}/app/updateAssignment" method="post">
                <table class="editTables">
                    <tr class="tableHeader"><th colspan="3">Edit assignment</th></tr>
                    <tr>
                        <th>Id:</th>
                        <td><input type="text" name="uAssignmentId"
                                   value="<c:out value='${assignmentToUpdate.id}'/>" readonly />
                        </td>
                        <td rowspan="3"><input class="tableButtons" type="submit" value="Save"></td>
                    </tr>
                    <tr>
                        <th>Ag_id:</th>
                        <td><input type="number" name="uAssignmentAgentId"
                                   value="<c:out value='${assignmentToUpdate.agent}'/>" />
                        </td>
                    </tr>
                    <tr>
                        <th>Mis_Id:</th>
                        <td><input type="number" name="uAssignmentMissionId"
                                   value="<c:out value='${assignmentToUpdate.mission}'/>" />
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </c:if>

    <div id="addAssignment" style="display: none">
        <br/>
        <form action="${pageContext.request.contextPath}/app/addAssignment" method="post">
            <table class="createTables">
                <tr class="tableHeader"><th colspan="3">Add new assignment</th></tr>
                <tr>
                    <th>Ag_id:</th>
                    <td>
                        <input type="number" name="crAssignmentAgentId" value="<c:out value='${param.crAssignmentAgentId}'/>"/>
                    </td>
                    <td class="btnInTable" rowspan="3"><input class="tableButtons" type="Submit" value="Create"/></td>
                </tr>
                <tr>
                    <th>Mis_id:</th>
                    <td>
                        <input type="number" name="crAssignmentMissionId" value="<c:out value='${param.crAssignmentMissionId}'/>"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <br/>
    <table class="listTables">
        <tr class="tableHeader"><th colspan="8">Assignments in DB</th></tr>
        <tr>
            <th>ID</th>
            <th>Ag_id</th>
            <th>Mis_id</th>
            <th>Start</th>
            <th>End</th>
            <th colspan="3" class="btnInTable">
                <button class="tableButtons" type="button" onclick="showDiv('addAssignment')">Add new assignment</button>
            </th>
        </tr>
        <c:forEach items="${assignments}" var="assignment">
            <tr>
                <td><c:out value="${assignment.id}" /></td>
                <td><c:out value="${assignment.agent}" /></td>
                <td><c:out value="${assignment.mission}" /></td>
                <td><c:out value="${assignment.start}" /></td>
                <td><c:out value="${assignment.end}" /></td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}/app/deleteAssignment?id=${assignment.id}" >
                        <input class="tableButtons" type="submit" value="Delete" />
                    </form>
                </td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}
                              /app/assignmentStartUpdate?id=${assignment.id}" >
                        <input class="tableButtons" type="submit" value="Edit"
                               <c:if test="${assignment.end ne null}">disabled="disabled"</c:if>/>
                    </form>
                </td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}
                              /app/endAssignment?id=${assignment.id}" >
                        <input class="tableButtons" type="submit" value="End"
                               <c:if test="${assignment.end ne null}">disabled="disabled"</c:if>/>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>