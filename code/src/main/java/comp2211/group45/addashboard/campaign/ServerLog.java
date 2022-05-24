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
 * Class to manage server log data
 */
public class ServerLog extends FileHandlerBase {
  public static BounceDef definitionOfBounce = BounceDef.SINGLE_PAGE_VIEW;
  public static int bounceTime;

  private static final int numColumns = 5;
  private static final String tableCreateQuery = "CREATE TABLE IF NOT EXISTS ServerLog "+
      "(EntryDate TEXT, "+
      "ID INTEGER, "+
      "ExitDate TEXT, "+
      "PagesViewed INTEGER, "+
      "Conversion INTEGER)";
  private static final String preparedQuery = "INSERT INTO ServerLog VALUES (?, ?, ?, ?, ?)";

  public ServerLog( String fileName, String databaseName ) throws FileNotFoundException, SQLException, IOException, ArrayIndexOutOfBoundsException {
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
    statement.execute( "DELETE FROM ServerLog" );
    PreparedStatement preparedStatement = connection.prepareStatement( preparedQuery );

    String line;
    String[] data = new String[numColumns];
    fileReader.readLine();
    while ( ( line = fileReader.readLine() ) != null ) {
      data = line.split( "," );
      if ( data.length != numColumns )
        continue;
      preparedStatement.setString( 1, Utils.toUTC( data[0] ) ); //Entry date
      preparedStatement.setLong( 2, Long.parseLong( data[1] ) ); //ID
      if ( data[2].equals( "n/a" ) ) { //If we hit an n/a exit date, just set to NULL
        preparedStatement.setNull( 3, java.sql.Types.VARCHAR );
      } else {
        preparedStatement.setString( 3, Utils.toUTC( data[2] ) );
      }
      preparedStatement.setInt( 4, Integer.parseInt( data[3] ) ); //Pages viewed
      preparedStatement.setInt( 5, data[4].equals( "Yes" ) ? 1 : 0 ); //Conversion
      preparedStatement.addBatch();
    }
    preparedStatement.executeBatch();
    connection.commit();
    statement.execute( "CREATE INDEX idx_server ON ServerLog(ID)" );
    connection.commit();
    statement.execute( "CREATE INDEX idx_date3 ON ServerLog(EntryDate)" );
    connection.commit();
    statement.execute( "CREATE INDEX idx_date4 ON ServerLog(ExitDate)" );
    connection.commit();
    fileReader.close();
  }

  /*
  Below are methods to calculate the raw metrics of a campaign related to the server log
  SQL for filtering:
  SELECT ServerLog.EntryDate, ServerLog.ID, ServerLog.ExitDate, ServerLog.PagesViewed, ServerLog.Conversion
    FROM ServerLog INNER JOIN ImpressionLog ON ServerLog.ID = ImpressionLog.ID
    WHERE ImpressionLog.Context = "Social Media"
      AND ImpressionLog.Gender = "Male"
      AND...;
  (add other filters to end)
  */

  public Integer bounceCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String bounceQuery = switch ( definitionOfBounce ) {
    case TIME_SPENT -> "strftime(\"%s\", ExitDate) - strftime(\"%s\", EntryDate) < " + Integer.toString( bounceTime ) ;
    case SINGLE_PAGE_VIEW -> "PagesViewed = 1";
    };

    String query = "SELECT COUNT(*) FROM ServerLog " +
                   "INNER JOIN ImpressionLog ON ServerLog.ID = ImpressionLog.ID WHERE " +
                   bounceQuery + " AND " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }

  public Integer conversionCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT COUNT(*) FROM ServerLog " +
                   "INNER JOIN ImpressionLog ON ServerLog.ID = ImpressionLog.ID WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }
}
