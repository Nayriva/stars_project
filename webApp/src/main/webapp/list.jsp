<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: nayriva
  abbrevs in naming: cr = create, u = update
--%>

<html>
<head>
    <title>S.T.A.R.S. manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/style.css" />
    <link rel="icon" href="${pageContext.request.contextPath}/resources/favicon.ico"/>
    <script type="text/javascript">
        function showDiv(clickedId) {
            var divToShow = document.getElementById(clickedId);
            if (divToShow.style.display == 'none') {
                divToShow.style.display = 'block';
            } else {
                divToShow.style.display = 'none';
            }
        }
    </script>
    <script type="text/javascript">
        function showHelp() {
            alert("This application serves as central manager for S.T.A.R.S. organisation." +
                " Add, delete or edit missions and agents, create assignments, update them!\n\n" +
                "Abbreviations used in this application:\n" +
                "Sp_p: special power\n" +
                "Fin: mission is marked as finished\n" +
                "Suc: mission is marked as successful\n" +
                "M_A_R: minimal agent rank\n" +
                "Mis_id: id of mission\n" +
                "Ag_id: id of agent");
        }
    </script>
</head>

<body>
    <div id="container">
        <div id="banner">
            <img id="bannerImg" src="${pageContext.servletContext.contextPath}/resources/banner.jpg"/>
        </div>

        <ul id="navbar">
            <li><button class="navBarButtons" type="button" onclick="showDiv('agentManager')">Manage agents</button></li>
            <li><button class="navBarButtons" type="button" onclick="showDiv('missionManager')">Manage missions</button></li>
            <li><button class="navBarButtons" type="button" onclick="showDiv('assignmentManager')">Manage assignments</button></li>
            <li><button class="navBarButtons" type="button" onclick="showHelp()" id="helpBut">Help</button></li>
        </ul>

        <c:if test="${not empty validationError}">
            <br/>
            <div class="alert">
                <span class="closebtn" onclick="this.parentElement.style.display='none';">&times;</span>
                <c:out value="${validationError}"/>
            </div>
        </c:if>

        <%@ include file="subpages/agent.jsp"%>
        <%@ include file="subpages/mission.jsp"%>
        <%@ include file="subpages/assignment.jsp"%>
    </div>

</body>
</html>
