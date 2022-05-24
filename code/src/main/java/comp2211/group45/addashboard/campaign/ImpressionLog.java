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
 * Class to manage impression log data
 */
public class ImpressionLog extends FileHandlerBase {
  private static final int numColumns = 7;
  private static final String tableCreateQuery = "CREATE TABLE IF NOT EXISTS ImpressionLog "+
      "(Date TEXT, "+
      "ID INTEGER, "+
      "Gender TEXT, "+
      "AgeRange TEXT, "+
      "Income INTEGER, "+
      "Context TEXT, "+
      "ImpressionCost FLOAT)";
  private static final String preparedQuery = "INSERT INTO ImpressionLog VALUES (?, ?, ?, ?, ?, ?, ?)";

  public ImpressionLog( String fileName, String databaseName ) throws FileNotFoundException, SQLException, IOException, ArrayIndexOutOfBoundsException {
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
    statement.execute( "DELETE FROM ImpressionLog" );
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
      preparedStatement.setString( 3, data[2] ); //Gender
      preparedStatement.setString( 4, data[3] ); //Age range
      preparedStatement.setInt( 5, calculateIncomeRange( data[4] ) ); //Income
      preparedStatement.setString( 6, data[5] ); //Context
      preparedStatement.setFloat( 7, Float.parseFloat( data[6] ) ); //Impression cost
      preparedStatement.addBatch();
    }
    preparedStatement.executeBatch();
    connection.commit();
    statement.execute( "CREATE INDEX idx_impression ON ImpressionLog(ID)" );
    connection.commit();
    statement.execute( "CREATE INDEX idx_date2 ON ImpressionLog(Date)" );
    connection.commit();
    fileReader.close();
  }

  /**
   * Convert income range string to integer
   * Low -> 0
   * Medium -> 1
   * High -> 2
   * @param incomeString String to convert
   * @return Integer from above mapping
   */
  private int calculateIncomeRange( String incomeString ) {
    if ( incomeString.equals( "Low" ) )
      return 0;
    if ( incomeString.equals( "Medium" ) )
      return 1;
    return 2;
  }

  /*
  Below are methods to calculate the raw metrics of a campaign related to the impression log
  */

  public Integer impressionCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT COUNT(*) FROM ImpressionLog WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }

  public Integer uniqueImpressionCount( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT COUNT(DISTINCT ID) FROM ImpressionLog WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return numberQuery( query ).intValue();
  }

  public Double cpm( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT TOTAL(ImpressionCost) FROM ImpressionLog WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    Integer impressionCount = impressionCount( ageFilter, genderFilter, incomeFilter, contextFilter );
    return impressionCount == 0 ? 0 : ( ( numberQuery( query ).doubleValue() / impressionCount ) * 1000.0 );
  }

  public Double totalImpressionCost( AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    String query = "SELECT TOTAL(ImpressionCost) FROM ImpressionLog WHERE " +
                   ageFilter.toSQLString() + " AND " +
                   genderFilter.toSQLString() + " AND " +
                   incomeFilter.toSQLString() + " AND " +
                   contextFilter.toSQLString();
    return  ( numberQuery( query ).doubleValue() );
  }

}
