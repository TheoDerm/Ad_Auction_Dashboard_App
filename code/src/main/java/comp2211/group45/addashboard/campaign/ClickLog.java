package comp2211.group45.addashboard.campaign;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import comp2211.group45.addashboard.utils.*;

/**
 * Class to manage click log data
 */
public class ClickLog extends FileHandlerBase {
  private static final int numColumns = 3;
  private static final String tableCreateQuery = "CREATE TABLE IF NOT EXISTS ClickLog "+
      "(Date TEXT, "+
      "ID INTEGER, "+
      "ClickCost REAL)";
  private static final String preparedQuery = "INSERT INTO ClickLog VALUES (?, ?, ?)";

  /**
   * Instantiate ClickLog, performs calls to the SQLite library
   *
   * @param fileName the location of the data to read in as click logs
   * @throws Exception when database or file IO errors occur
   */
  public ClickLog( String fileName, String databaseName ) throws FileNotFoundException, SQLException, IOException, ArrayIndexOutOfBoundsException {
    this.fileName = fileName;
    this.databaseName = databaseName;
    databaseURL = "jdbc:sqlite:" + databaseName;
    connect();
    importCSV();
  }

  @Override
  protected void connect() throws SQLException {
    connection = DriverManager.getConnection( databaseURL );
    connection.setAutoCommit( false );
    if ( connection == null ) {
      System.out.println( "no connection" );
    }
  }

  @Override
  protected void importCSV() throws FileNotFoundException, SQLException, IOException, ArrayIndexOutOfBoundsException {
    var fileReader = new BufferedReader( new FileReader( fileName ) );
    var statement = connection.createStatement();
    statement.execute( tableCreateQuery );
    statement.execute( "DELETE FROM ClickLog" );
    PreparedStatement preparedStatement = connection.prepareStatement( preparedQuery );

    String line;
    String[] data = new String[numColumns];
    fileReader.readLine();
    while ( ( line = fileReader.readLine() ) != null ) {
      data = line.split( "," );
      if ( data.length != numColumns )
        continue;
      preparedStatement.setString( 1, Utils.toUTC( data[0] ) ); //Date
      preparedStatement.setLong( 2, Long.parseLong( data[1] ) ); //ID
      preparedStatement.setFloat( 3, Float.parseFloat( data[2] ) ); //Click cost
      preparedStatement.addBatch();
    }
    preparedStatement.executeBatch();
    connection.commit();
    statement.execute( "CREATE INDEX idx_click ON ClickLog(ID)" );
    connection.commit();
    statement.execute( "CREATE INDEX idx_date1 ON ClickLog(Date)" );
    connection.commit();
    fileReader.close();
  }

  /*
  Below are methods to calculate the raw metrics of a campaign related to the click log
  SQL for filtering:
  SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost
    FROM ClickLog INNER JOIN ImpressionLog ON ClickLog.ID = ImpressionLog.ID
    WHERE ImpressionLog.Context = "Social Media"
      AND ImpressionLog.Gender = "Male
      AND...
  (add other filters to end)
  */

  public Integer clickCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT COUNT(*) FROM ClickLog " +
                   "INNER JOIN ImpressionLog ON ClickLog.ID = ImpressionLog.ID WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }

  public Integer uniqueClickCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT COUNT(DISTINCT ClickLog.ID) FROM ClickLog " +
                   "INNER JOIN ImpressionLog ON ClickLog.ID = ImpressionLog.ID WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }

  public Double totalClickCost( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT TOTAL(ClickCost) FROM ClickLog " +
                   "INNER JOIN ImpressionLog ON ClickLog.ID = ImpressionLog.ID WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).doubleValue();
  }

  public Double costPerClick( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    Integer clickCount = clickCount( ageFilter, genderFilter, incomeFilter, contextFilter );
    return clickCount == 0 ? 0 : totalClickCost( ageFilter, genderFilter, incomeFilter, contextFilter ) / clickCount;
  }
}
