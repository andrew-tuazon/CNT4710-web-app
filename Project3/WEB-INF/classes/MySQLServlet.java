

/*
    Name: Andrew Tuazon
 	Course: CNT 4714–Summer2021–Project Three
 	Assignment title: A Three-Tier Distributed Web-Based Application
 	Date: Sunday August 1, 2021
*/

import java.io.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.util.Properties;
import com.mysql.cj.jdbc.MysqlDataSource;

public class MySQLServlet extends HttpServlet {

	private Connection connection;	//normal user command connection
	private Statement statement;
	/*
	 * The init() method connect to the MySQL backend and reports all error to the web-browser screen.
	 * Call is implicitly initiated by the servlet container prior to any servlet instance handling a request.
	 */
	
	@Override
	public void init() throws ServletException {
		try {
			ServletConfig config = getServletConfig();
			String dbDriver = config.getInitParameter( "databaseDriver");
			String dbURL = config.getInitParameter( "databaseName");
			String username = config.getInitParameter( "username");
			String password = config.getInitParameter( "password");
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(dbURL, username, password);
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new UnavailableException( exception.getMessage() );
		}
	}
	
	public void destroy() {
		try {
			statement.close();
			connection.close();
		}catch(SQLException e) {
			System.out.println("Error closing the db connection: " + e.getMessage());
		}
	}
	
	/*
	 * The doGet() method executes the actual query. It takes
	 * the text from the HTML area and checks it to determine if it is a SELECT or an UPDATE statement. Based upon the
	 * result, the command is passed to the appropriate executor.
	 * All the results of the query are then passed to the
	 * SQLFormatter class for conversion into a format that can
	 * be deciphered by any web browser(HTML). All errors or responses by the server are reported to the web-browser.
	 */
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doGet(request,response);
	}
	
	private int bonusBusinessLogic(Connection connection, Statement statement, String sqlStatement) {
		int numRowsAffected = 0;
		int throwaway = 0;
		String bBLCommand = "update suppliers set status = status + 5 where snum in (select distinct snum from shipments left join beforeShipments"+
							" using (snum, pnum, jnum, quantity) where beforeShipments.snum is null and quantity >= 100);";
		try {
			//create before image of shipments table
			numRowsAffected = statement.executeUpdate("drop table if exists beforeShipments;");
			numRowsAffected = statement.executeUpdate("create table beforeShipments like shipments;");
			numRowsAffected = statement.executeUpdate("insert into beforeShipments select * from shipments;");
			//execute original user update command
			numRowsAffected = statement.executeUpdate(sqlStatement);
			//execute the bonus version of the business logic
			numRowsAffected = statement.executeUpdate(bBLCommand);
			//drop the beforeShipments table
			throwaway = statement.executeUpdate("drop table beforeShipments;");
		} catch (SQLException e) {
			System.out.println("Error opening the db connection: " + e.getMessage());
		} //end try-catch
		return numRowsAffected;
	}//end bonusBusinessLogic() method
	
	private int simpleBusinessLogic(Connection connection, Statement statement, String sqlStatement) {
		int numRowsAffected = 0;
		String sBLCommand = "update suppliers set status = status + 5 where snum in (select distinct snum from shipments where quantity >= 100);";
		try {
			//execute original user update command
			numRowsAffected = statement.executeUpdate(sqlStatement);
			//execute the simple version of the business logic
			numRowsAffected = statement.executeUpdate(sBLCommand);
		} catch (SQLException e) {
			System.out.println("Error opening the db connection: " + e.getMessage());
		} //end try-catch
		return numRowsAffected;
	}//end bonusBusinessLogic() method
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		//get the user SQL command from the user interface(index.jsp)
		String sqlStatement = request.getParameter("sqlStatement");
		//set up the return message string object
		String message = " ";
		
		try {
			//create a Statement object on the Connection object that was created in init() above
			statement = connection.createStatement();
			//massage the incoming user SQL command to eliminate any leading/trailing whitespace
			sqlStatement = sqlStatement.trim();
			//grab the leading six characters from the user SQL command - we're looking for queries vs non-queries
			String sqlType = sqlStatement.substring(0, 6);
			//set up a return value for MySQL return status
			int mysqlReturnVal;
			//handle user command
			// top case is a query - all others are non-queries
			if(sqlType.equalsIgnoreCase("select")) {
				//process query on MySQL-side - get ResultSet object back
				ResultSet resultSet = statement.executeQuery(sqlStatement);
				//convert the ResultSet object into an HTML table using SQLFormatter class
				message = SQLFormatter.getHtmlRows(resultSet);
			}
			//updating (non-query) command
			//first case is an update involving the shipments table - business logic fires
			else { //updating command is issued by the user - non bonus version
				if (sqlStatement.contains("shipments")){ //business logic to fire
					//uncomment next line to run the bonus version of the business logic
					mysqlReturnVal = bonusBusinessLogic(connection, statement, sqlStatement);
					//uncomment the next line to run the simple version (non-bonus) version of the business logic
					//mysqlReturnVal = simpleBusinessLogic(connection, statement, sqlStatement);
					//build appropriate return message for business logic case
					message = "<p style='background-color:chartreuse; border:3px; display: inline-block;" 
							+ "border-style:solid; border-color:black; text-align:center'>"
							+ "<b>The statement executed succesfully.<br>"
							+ mysqlReturnVal + " row(s) affected.<br><br>"
							+ "Business Logic Detected! - Updating Supplier Status <br><br>"
							+ "Business Logic updated " + mysqlReturnVal + " supplier status marks.</b><br></p>";
					
			} // end if for business logic
			//second option on updating command does not involve the shipments table - business logic does not trigger/fire
			else { //no business logic (shipment table not involved) - just execute user update
				mysqlReturnVal = statement.executeUpdate(sqlStatement); //run user update
				if (mysqlReturnVal != 0) { //success
					//build appropriate return message for business logic not triggering - but command successful
					message = "<p style='background-color:chartreuse; border:3px; display: inline-block;" 
							+ "border-style:solid; border-color:black; text-align:center'>"
							+ "<b>The statement executed succesfully.<br><br>"
							+ "Business Logic Not Triggered!</b><br></p>";
				}
				else //statement executed successfully but no rows were updated
					//build message for update successful but no rows updated
					message = "<p style='background-color:chartreuse; border:3px; display: inline-block;" 
							+ "border-style:solid; border-color:black; text-align:center'>"
							+ "<b>The statement executed succesfully.<br>"
							+ mysqlReturnVal + " row(s) affected.</b><br></p>";
			}
			statement.close();
			} //end else
		}
		catch(SQLException e ) {
			message = "<tr bgcolor=#ff0000><td><font color=#ffffff><b>Error executing the SQL statement:</b><br>" + e.getMessage() + "</tr></td></font>";
		}
		//identify session variables and set their attributes for return to calling page via RequestDispatcher object
		HttpSession session = request.getSession();
		session.setAttribute("message", message);
		session.setAttribute("sqlStatement", sqlStatement);
		//tell dispatcher where to forward results
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
		//have dispatcher forward results
		dispatcher.forward(request, response);
	}
}
