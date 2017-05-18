<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="agentManager">
    <c:if test="${toEdit eq 'agent'}">
        <br/>
        <div id="editAgent" var="agentToUpdate">
            <form action="${pageContext.request.contextPath}/app/updateAgent?oldAlive=${agentToUpdate.alive}" method="post">
                <table class="editTables">
	                <tr class="tableHeader"><th colspan="3">Edit agent</th></tr>
                    <tr>
                        <th>Id:</th>
                        <td><input type="text" name="uAgentId"
                                   value="<c:out value='${agentToUpdate.id}'/>" readonly /></td>
                        <td rowspan="6"><input class="tableButtons" type="submit" value="Save"></td>
                    </tr>
                    <tr>
                        <th>Name:</th>
                        <td><input type="text" name="uAgentName"
                                   value="<c:out value='${agentToUpdate.name}'/>"/></td>
                    </tr>
                    <tr>
                        <th>Sp_p:</th>
                        <td><input type="text" name="uAgentSpecialPower"
                                   value="<c:out value='${agentToUpdate.specialPower}'/>"/></td>
                    </tr>
                    <tr>
                        <th>Alive:</th>
                        <td><input type="checkbox" name="uAgentAlive"
                                   <c:if test="${agentToUpdate.alive}">checked="checked"</c:if>/>
                        </td>
                    </tr>
                    <tr>
                        <th>Rank:</th>
                        <td><input type="number" name="uAgentRank"
                                   value="<c:out value='${agentToUpdate.rank}'/>"/></td>
                    </tr>
                </table>
            </form>
        </div>
    </c:if>

    <div id="addAgent" style="display: none;">
        <br/>
        <form action="${pageContext.request.contextPath}/app/addAgent" method="post">
            <table class="createTables">
                <tr class="tableHeader"><th colspan="3">Add new agent</th></tr>
                <tr>
                    <th>Name:</th>
                    <td>
                        <input type="text" name="crAgentName" value="<c:out value='${param.crAgentName}'/>"/>
                    </td>
                    <td class="btnInTable" rowspan="3"><input class="tableButtons" type="Submit" value="Create"/></td>
                </tr>
                <tr>
                    <th>Sp_p:</th>
                    <td>
                        <input type="text" name="crAgentSpecialPower"
                               value="<c:out value='${param.crSpecialPower}'/>"/>
                    </td>
                </tr>
                <tr>
                    <th>Rank:</th>
                    <td>
                        <input type="number" name="crAgentRank" value="<c:out value='${param.crAgentRank}'/>"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <br/>
    <table class="listTables">
        <tr class="tableHeader"><th colspan="8">Agents in DB</th></tr>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Sp_p</th>
            <th>Alive</th>
            <th>Rank</th>
            <th colspan="2" class="btnInTable">
                <button class="tableButtons" type="button" onclick="showDiv('addAgent')">Add new agent</button>
            </th>
        </tr>
        <c:forEach items="${agents}" var="agent">
            <tr>
                <td><c:out value="${agent.id}" /></td>
                <td><c:out value="${agent.name}" /></td>
                <td><c:out value="${agent.specialPower}" /></td>
                <td><c:out value="${agent.alive}" /></td>
                <td><c:out value="${agent.rank}" /></td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}/app/deleteAgent?id=${agent.id}" >
                        <input class="tableButtons" type="submit" value="Delete" />
                    </form>
                </td>
                <td class="btnInTable">
                    <form method="post"
                          action="${pageContext.request.contextPath}/app/agentStartUpdate?id=${agent.id}" >
                        <input class="tableButtons" type="submit" value="Edit" />
                    </form>

                </td>
            </tr>
        </c:forEach>
    </table>

</div>
