package comp2211.group45.addashboard.campaign;

import java.sql.*;

/**
 * A file handling class for csv
 */
public abstract class FileHandlerBase implements AutoCloseable {
  protected String fileName;
  protected String databaseName;
  protected String databaseURL;
  protected Connection connection;

  /**
   * Clean up after yourself
   */
  @Override
  public void close() {
    try {
      connection.close();
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Run a query on the database that returns a single Number (for example a COUNT() query)
   * @param q Query that retuns a number
   * @return Integer containing result
   */
  public final Number numberQuery( String q ) {
    try {
      ResultSet result = query( q );
      return ( ( ( Number ) result.getObject( 1 ) ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Run a query on the database that returns a single String (e.g. getting the earliest date in a database)
   * @param q Query that returns a String
   * @return String containing result
   */
  public final String stringQuery( String q ) {
    try {
      ResultSet result = query( q );
      return ( ( String ) ( result.getObject( 1 ) ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Perform a generic query on the database
   * @param q String containing the SQL query
   * @return ResultSet containing the query
   * @throws SQLException
   */
  public final ResultSet query( String q ) throws SQLException {
    var stmt = this.connection.createStatement();
    ResultSet r = null;
    try {
      r = stmt.executeQuery( q );
    } catch ( SQLException e ) {
      if ( e.getMessage().equals( "query does not return ResultSet" ) ) { //Not an exception we need to worry about
        return r;
      } else {
        e.printStackTrace();
      }
    }
    return r;
  }

  /**
   * Create and establish a JDBC connection to this class' database
   * @throws Exception
   */
  protected abstract void connect() throws Exception;

  /**
   * Import the data from the given CSV file into the database
   * @throws Exception
   */
  protected abstract void importCSV() throws Exception;
}