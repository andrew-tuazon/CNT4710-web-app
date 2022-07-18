<!doctype html">

<%-- 
    Name: Andrew Tuazon
    Course: CNT 4714–Summer2021–Project Three
    Assignment title: A Three-Tier Distributed Web-Based Application
    Date: Sunday August 1, 2021
--%>
<%-- start scriptlet --%>
<%
    String sqlStatement = (String) session.getAttribute("sqlStatement");
    if (sqlStatement == null) sqlStatement = "select * from suppliers";
    String message = (String) session.getAttribute("message");
    if (message == null) message = " ";
%>

<html>
<head>
   <title>CNT 4714 Remote Database Management System</title>
   <style>
    <!--
	    body{background-color: blue; text-align: center; font-family: Arial;}
        h1 {color:white; font-size:28pt;}
        h2 {color:white; font-size:24pt;}
        input {color:yellow; background:#665D1E; font-weight:bold; font-size: 16pt;}
        p {color:black; font-size:13pt;}
        table{font-family: Verdana; border:3px solid black;}
        textarea {background: black; color: lime; font-family: Verdana; font-size:15pt; width: 900px; height: 275px;}
        th, td {padding: 5px; border: 1px solid black;}
        .main {color:white;}
        #bl {color: #708090;}
	-->
	</style>
    <script type="text/javascript">
        function eraseText() {
            document.getElementById("cmd").innerHTML = " ";
        }
    </script>
</head>

<body>
    <h1>Welcome to the Summer 2021 Project 3 Enterprise Database System</h1>
    <h2>A Servlet/JSP-based Multi-tiered Enterprise Application Using A Tomcat Container </h2>
    <hr>
    <p class="main">You are connected to the Project 3 Enterprise System database as a root user. <br />
        Please enter any valid SQL query or update command. <br />
    </p>
    <form action="MySQLServlet" method="post">
        <textarea id="cmd" name="sqlStatement" cols=60 rows=8><%=sqlStatement%></textarea><br>
        <br>
        <input type="submit" value="Execute Command" /> &nbsp; &nbsp; &nbsp;
        <input type="reset" value="Reset Form" onclick="javascript:eraseText();" />
    </form>
    <p class="main"> <br /> All execution results will appear below this line. </p>
    <hr>
    <center>
        <p>
            <b class="main">Database Results:</b><br>
            <table>
                <%=message%>
            </table>
        </p>    
    </center>
</body>
</html>