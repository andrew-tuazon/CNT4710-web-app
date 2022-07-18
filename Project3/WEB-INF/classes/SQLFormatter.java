
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/*
	Name: Andrew Tuazon
	Course: CNT 4714–Summer2021–Project Three
	Assignment title: A Three-Tier Distributed Web-Based Application
	Date: Sunday August 1, 2021
*/

public class SQLFormatter {

	public static synchronized String getHtmlRows(ResultSet results) throws SQLException {
		int color = 0;
		//create a StringBuffer project to hold the html table markup
		StringBuffer strBuf = new StringBuffer();
		//extract the meta data from the ResultSet object
		ResultSetMetaData metaData = results.getMetaData();
		//find out how many columns are in the returned ResultSet object
		int columnCount = metaData.getColumnCount();
		//begin creating html table markup - initial row - column headers
		strBuf.append("<tr bgcolor=#FF0000 align=center>");
		for(int i = 1; i <= columnCount; i++) {
			strBuf.append("<td><b>" + metaData.getColumnName(i) + "</td>");
		}//end for loop creating column headers
		strBuf.append("</tr>");
		// iterate through the ResultSet object extracting one row of data at a time
		while (results.next()) {
			if ((color % 2) == 0) {
					strBuf.append("<tr bgcolor=#D3D3D3 align=center>");
				} else {
					strBuf.append("<tr bgcolor=#FFFFFF align=center>");
				}
			//append table markup elements to [StringBufferObject]
			for(int i=1; i<= columnCount; i++ ) {
				strBuf.append("<td>" + results.getString(i) + "</td>");
			}
			strBuf.append("</tr>");
			color++;
			//close off the row
		}// end while loop
		//close up the html - end last row
	strBuf.append("</tr>");
	//return the big string to the caller
	return strBuf.toString();
}//end getHTMLrows method
}//end SQLFormatter
