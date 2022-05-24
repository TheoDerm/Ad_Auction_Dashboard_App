package comp2211.group45.addashboard.campaign;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import comp2211.group45.addashboard.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class Campaign implements AutoCloseable {
  private static final boolean DEBUG = true;
  private Path zipFilePath;
  private Path tempDirPath;
  private String databaseName;
  private ClickLog clickLog;
  private ServerLog serverLog;
  private ImpressionLog impressionLog;

  private boolean valid = true;

  public String name;

  // *INDENT-OFF*
  public Path zipFilePath(){ return zipFilePath; }

  private Instant clickLogStart;
  public Instant clickLogStart() { return clickLogStart; }
  private Instant clickLogEnd;
  public Instant clickLogEnd() { return clickLogEnd; }
  private Instant serverLogEarliestEntry;
  public Instant serverLogEarliestEntry() { return serverLogEarliestEntry; }
  private Instant serverLogLatestExit;
  public Instant serverLogLatestExit() { return serverLogLatestExit; }
  private Instant impressionLogStart;
  public Instant impressionLogStart() { return impressionLogStart; }
  private Instant impressionLogEnd;
  public Instant impressionLogEnd() { return impressionLogEnd; }
  public ClickLog clickLog() { return clickLog; }
  public ImpressionLog impressionLog() { return impressionLog; }
  public ServerLog serverLog() { return serverLog; }
  // *INDENT-OFF*

  /**
   * Creates a campaign from a zip file
   * Ensure the file passed in is a zip file
   * @param zipFile Path object pointing to a campaign zip
   */
  public Campaign( Path zipFile ) {
    this.name = zipFile.toString();
    try {
      zipFilePath = zipFile;
      tempDirPath = Files.createTempDirectory( "CAMPAIGN_" );
      databaseName = tempDirPath.toString() + "/campaign.db";
      unzip( zipFilePath, tempDirPath );  //Unzip to temp directory
    } catch ( IOException e ) {
      d( "Failed to create temp directory" );
      valid = false;
      return;
    }
  }

  /**
   * Load the campaign (instantiate handlers for each file)
   */
  public void load() {
    if ( !isZipValid() ) {
      d( "Not loading" );
      return;
    }
    try {
      clickLog = new ClickLog( tempDirPath.toString() + "/click_log.csv", databaseName );
      serverLog = new ServerLog( tempDirPath.toString() + "/server_log.csv", databaseName );
      impressionLog = new ImpressionLog ( tempDirPath.toString() + "/impression_log.csv", databaseName );
      d( "Loaded " + toString() );
    } catch ( Exception e ) {
      d( "Something not in correct format" );
      e.printStackTrace();
    }
    setDateRanges();
  }

  /**
   * See if all the correct files exist in the temp directory
   * @return True if the files are correct
   */
  public boolean isZipValid() {
    if ( ( new File( tempDirPath.toString() + "/click_log.csv" ) ).exists() &&
         ( new File( tempDirPath.toString() + "/server_log.csv" ) ).exists() &&
         ( new File( tempDirPath.toString() + "/impression_log.csv" ) ).exists() && valid )  {
      return true;
    } else {
      d( "Zip file invalid" );
      return false;
    }
  }

  /**
   * Add the date ranges that the different CSV files cover
   */
  private void setDateRanges() {
    this.clickLogStart = Instant.parse( Utils.toUTC( clickLog.stringQuery( "SELECT min(Date) FROM ClickLog" ) ) );
    this.clickLogEnd = Instant.parse( Utils.toUTC( clickLog.stringQuery( "SELECT max(Date) FROM ClickLog" ) ) );
    this.impressionLogStart = Instant.parse( Utils.toUTC( impressionLog.stringQuery( "SELECT min(Date) FROM ImpressionLog" ) ) );
    this.impressionLogEnd = Instant.parse( Utils.toUTC( impressionLog.stringQuery( "SELECT max(Date) FROM ImpressionLog" ) ) );
    this.serverLogEarliestEntry = Instant.parse( Utils.toUTC( serverLog.stringQuery( "SELECT min(EntryDate) FROM ServerLog" ) ) );
    this.serverLogLatestExit = Instant.parse( Utils.toUTC( serverLog.stringQuery( "SELECT max(ExitDate) FROM ServerLog" ) ) );
  }

  /**
   * Query the database and return a result set
   * @param q String containing query
   * @return ResultSet containing results
   */
  public ResultSet dbQuery(String q){
    try {
      return clickLog.query( q );
    } catch ( SQLException e ) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Run a query against the click log
   * @param q String containing the query
   * @return A ResultSet with the result, null if there was an error
   */
  public ResultSet clickLogQuery( String q ) {
    try {
      return clickLog.query( q );
    } catch ( SQLException e ) {
      d( "SQL error in click log" );
      return null;
    }
  }

  /**
   * Run a query against the impression log
   * @param q String containing the query
   * @return A ResultSet with the result, null if there was an error
   */
  public ResultSet impressionLogQuery( String q ) {
    try {
      return impressionLog.query( q );
    } catch ( SQLException e ) {
      d( "SQL error in impression log" );
      return null;
    }
  }

  /**
   * Run a query against the server log
   * @param q String containing the query
   * @return A ResultSet with the result, null if there was an error
   */
  public ResultSet serverLogQuery( String q ) {
    try {
      return serverLog.query( q );
    } catch ( SQLException e ) {
      d( "SQL error in server log" );
      return null;
    }
  }

  /**
  * Unzip a zip file to a directory (zip file must only contain files, no directories)
  * @param sourceZip Path containing the source zip
  * @param targetDir Path to extract the zip to
  * @throws IOException
  */
  private void unzip( Path sourceZip, Path targetDir ) throws IOException {
    try ( InputStream inputStream = Files.newInputStream( sourceZip );
            ZipInputStream zipStream = new ZipInputStream( inputStream ) ) {
      ZipEntry entry = zipStream.getNextEntry();
      while ( entry != null ) {
        Path path = targetDir.resolve( entry.getName() ).normalize();
        if ( !path.startsWith( targetDir ) ) {
          throw new IOException( "Invalid ZIP" );
        }
        try ( OutputStream outputStream = Files.newOutputStream( path ) ) {
          byte[] buf = new byte[1024];
          int len;
          while ( ( len = zipStream.read( buf ) ) > 0 ) {
            outputStream.write( buf, 0, len );
          }
        }
        entry = zipStream.getNextEntry();
      }
      zipStream.closeEntry();
    }
  }

  /**
   * Get the click count from the click log database
   * @return Integer containing the click count
   */
  public Integer clickCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return clickLog.clickCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the unique click count (clicks with a distinct ID) from the click log database
   * @return Integer containing the number of unique click counts
   */
  public Integer uniqueClickCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return clickLog.uniqueClickCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the total cost of all clicks from the click log database
   * @return Double containing the total cost of all clicks (in pence)
   */
  private Double totalClickCost(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return clickLog.totalClickCost(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the average cost per click from the click log database
   * @return Double containing the average cost per click (in pence)
   */
  public Double costPerClick(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return (clickCount(ageFilter, genderFilter, incomeFilter, contextFilter) == 0) ? 0 : totalClickCost(ageFilter, genderFilter, incomeFilter, contextFilter) / clickCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the number of impressions from the impression log database
   * @return Integer containing the total number of impressions
   */
  public Integer impressionCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return impressionLog.impressionCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the number of unique impressions from the impression log database (distinct IDs)
   * @return Integer containing the number of unique impressions
   */
  public Integer uniqueImpressionCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return impressionLog.uniqueImpressionCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the Cost Per Mil (per Thousand) from the impression log database
   * It represents the average amount of money spent on an advertising campaign for every one thousand impressions
   * @return Double containing the CPM metric (in pence)
   */
  public Double cpm(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return impressionLog.cpm(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the total cost of all impressions in the impression log
   * @return Double containing the sum of all costs of impressions (in pence)
   */
  private Double totalImpressionCost(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return impressionLog.totalImpressionCost(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the number of bounces from the server log (no conversion or only 1 page view)
   * @return Integer with the number of bounces in the server log
   */
  public Integer bounceCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return serverLog.bounceCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the number of conversions from the server log
   * @return Integer containing the number of conversions
   */
  public Integer conversionCount(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return serverLog.conversionCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Calculate the total cost from the impression and click log files
   * @return Double containing the total cost (in pence)
   */
  public Double totalCost(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return totalClickCost(ageFilter, genderFilter, incomeFilter, contextFilter) + totalImpressionCost(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Calculate the bounce rate (average number of bounces per click)
   * @return Double containing the bounce rate
   */
  public Double bounceRate(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return (Double.valueOf( clickCount(ageFilter, genderFilter, incomeFilter, contextFilter) ) == 0) ? 0 : Double.valueOf( bounceCount(ageFilter, genderFilter, incomeFilter, contextFilter) ) / Double.valueOf( clickCount(ageFilter, genderFilter, incomeFilter, contextFilter) );
  }

  /**
   * Calculate the CPA metric (cost-per-acquisition);
   * The average amount of money spent on an advertising campaign for each acquisition (conversion)
   * @return Double containing CPA (in pence)
   */
  public Double cpa(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return (conversionCount(ageFilter, genderFilter, incomeFilter, contextFilter) == 0) ? 0 : totalImpressionCost(ageFilter, genderFilter, incomeFilter, contextFilter) / conversionCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
   * Get the click-through-rate (CTR);
   * the average number of clicks per impression
   * @return Double containing the CTR
   */
  public Double ctr(AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter) {
    return (impressionCount(ageFilter, genderFilter, incomeFilter, contextFilter) == 0) ? 0 : Double.valueOf( clickCount(ageFilter, genderFilter, incomeFilter, contextFilter) ) / impressionCount(ageFilter, genderFilter, incomeFilter, contextFilter);
  }

  /**
  * Print debug message for campaign
  * @param args
  */
  public static void d( Object... args ) {
    if ( DEBUG ) {
      System.out.print( "[\u001B[34mCampaign\u001B[0m] " );
      System.out.println( args[0] );
    }
  }

  /**
   * Pretty printing campaigns
   */
  @Override
  public String toString() {
    return "Campaign managing " + zipFilePath.toString() + " with temp directory " + tempDirPath.toString();
  }

  /**
  * Deletes temporary directory and closes data sources
  */
  @Override
  public void close() {
    //Close handlers
    clickLog.close();
    serverLog.close();
    impressionLog.close();
    //Delete temp dir
    try {
      FileUtils.deleteDirectory( tempDirPath.toFile() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    d( "Closing " + toString() );
  }
}
