package comp2211.group45.addashboard;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import comp2211.group45.addashboard.campaign.*;
import comp2211.group45.addashboard.utils.*;

/**
 * Unit test for simple App.
 */
public class CampaignTest {

  static Campaign campaign;

  //--------------------------------------------------------------------------
  //------------------Testing of small dataset--------------------------------
  //--------------------------------------------------------------------------
  @BeforeClass
  public static void setUp() {
    campaign = new Campaign( Path.of( "test_data/small_dataset.zip" ) );
    campaign.load();
  }

  @Test
  public void testImpressionCount() {
    assertEquals( ( Integer )999, campaign.impressionCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )0, campaign.impressionCount( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.HOBBIES ) );
    assertEquals( ( Integer )2, campaign.impressionCount( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.BLOG ) );
    assertEquals( ( Integer )8, campaign.impressionCount( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.SOC_MED ) );
    assertEquals( ( Integer )3, campaign.impressionCount( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.LOW,
                  ContextFilter.SHOPPING ) );
  }

  @Test
  public void testClickCount() {
    assertEquals( ( Integer )42, campaign.clickCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )2, campaign.clickCount( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.NEWS ) );
    assertEquals( ( Integer )0, campaign.clickCount( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.LOW,
                  ContextFilter.SOC_MED ) );
    assertEquals( ( Integer )0, campaign.clickCount( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.BLOG ) );
    assertEquals( ( Integer )0, campaign.clickCount( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.HOBBIES ) );
  }

  @Test
  public void testUniques() {
    assertEquals( ( Integer )42, campaign.uniqueClickCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )6, campaign.uniqueClickCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.LOW,
                  ContextFilter.SOC_MED ) );
    assertEquals( ( Integer )1, campaign.uniqueClickCount( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.SHOPPING ) );
    assertEquals( ( Integer )0, campaign.uniqueClickCount( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.HIGH,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )0, campaign.uniqueClickCount( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.MALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.SOC_MED ) );
  }

  @Test
  public void testCPM() {
    assertEquals( ( Double )1.0052462482733562, campaign.cpm( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.cpm( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.MALE,
                  IncomeFilter.LOW,
                  ContextFilter.BLOG ) );
    assertEquals( ( Double )1.075270591194139, campaign.cpm( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.cpm( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
    assertEquals( ( Double )0.36069999914616346, campaign.cpm( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.NEWS ) );
  }

  @Test
  public void testCost() {
    assertEquals( ( Double )215.43511201185174, campaign.totalCost( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.totalCost( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
    assertEquals( ( Double )6.199593979050405, campaign.totalCost( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.MEDIUM,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.013328000204637647, campaign.totalCost( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.LOW,
                  ContextFilter.BLOG ) );
    assertEquals( ( Double )0.00919500004965812, campaign.totalCost( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.SHOPPING ) );
  }

  @Test
  public void testBounces() {
    assertEquals( ( Integer )20, campaign.bounceCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )0, campaign.bounceCount( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.ANY,
                  ContextFilter.HOBBIES ) );
    assertEquals( ( Integer )0, campaign.bounceCount( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.NEWS ) );
    assertEquals( ( Integer )2, campaign.bounceCount( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )0, campaign.bounceCount( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
  }

  @Test
  public void testConversions() {
    assertEquals( ( Integer )42, campaign.conversionCount( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Integer )0, campaign.conversionCount( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
    assertEquals( ( Integer )0, campaign.conversionCount( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.FEMALE,
                  IncomeFilter.LOW,
                  ContextFilter.SHOPPING ) );
    assertEquals( ( Integer )1, campaign.conversionCount( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.BLOG ) );
    assertEquals( ( Integer )0, campaign.conversionCount( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.ANY,
                  ContextFilter.HOBBIES ) );
  }

  @Test
  public void testBounceRate() {
    assertEquals( ( Double )0.47619047619047616, campaign.bounceRate( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.bounceRate( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.NEWS ) );
    assertEquals( ( Double )0.0, campaign.bounceRate( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.SHOPPING ) );
    assertEquals( ( Double )0.0, campaign.bounceRate( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.LOW,
                  ContextFilter.HOBBIES ) );
    assertEquals( ( Double )0.0, campaign.bounceRate( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
  }

  @Test
  public void testCPA() {
    assertEquals( ( Double ) 0.023910500048216255, campaign.cpa( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double ) 0.0, campaign.cpa( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.MALE,
                  IncomeFilter.LOW,
                  ContextFilter.SOC_MED ) );
    assertEquals( ( Double ) 0.0, campaign.cpa( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.BLOG ) );
    assertEquals( ( Double ) 0.0, campaign.cpa( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
    assertEquals( ( Double ) 0.0, campaign.cpa( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.SHOPPING ) );
  }

  @Test
  public void testCTR() {
    assertEquals( ( Double )0.042042042042042045, campaign.ctr( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.011049723756906077,campaign.ctr( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.ctr( AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.LOW,
                  ContextFilter.BLOG ) );
    assertEquals( ( Double )0.1111111111111111, campaign.ctr( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.SHOPPING ) );
    assertEquals( ( Double )0.037037037037037035, campaign.ctr( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.ANY,
                  IncomeFilter.MEDIUM,
                  ContextFilter.NEWS ) );
  }

  @Test
  public void testCPC() {
    assertEquals( ( Double )5.105496928805397, campaign.costPerClick( AgeFilter.ANY,
                  GenderFilter.ANY,
                  IncomeFilter.ANY,
                  ContextFilter.ANY ) );
    assertEquals( ( Double )0.0, campaign.costPerClick( AgeFilter.LT_TWENTY_FIVE,
                  GenderFilter.MALE,
                  IncomeFilter.LOW,
                  ContextFilter.BLOG ) );
    assertEquals( ( Double )0.0, campaign.costPerClick( AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.MEDIUM,
                  ContextFilter.HOBBIES ) );
    assertEquals( ( Double )0.0, campaign.costPerClick( AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                  GenderFilter.FEMALE,
                  IncomeFilter.HIGH,
                  ContextFilter.NEWS ) );
    assertEquals( ( Double )0.0, campaign.costPerClick( AgeFilter.GT_FIFTY_FOUR,
                  GenderFilter.MALE,
                  IncomeFilter.HIGH,
                  ContextFilter.TRAVEL ) );
  }

  @Test
  public void testDateRanges() {
    //Start and end dates taken from small_dataset csv files
    assertEquals( Instant.parse( "2015-01-01T12:01:21Z" ), campaign.clickLogStart() );
    assertEquals( Instant.parse( "2015-01-01T16:52:34Z" ), campaign.clickLogEnd() );
    assertEquals( Instant.parse( "2015-01-01T12:00:02Z" ), campaign.impressionLogStart() );
    assertEquals( Instant.parse( "2015-01-01T12:28:09Z" ), campaign.impressionLogEnd() );
    assertEquals( Instant.parse( "2015-01-01T12:01:21Z" ), campaign.serverLogEarliestEntry() );
    assertEquals( Instant.parse( "2015-01-01T16:56:45Z" ), campaign.serverLogLatestExit() );
  }

  @AfterClass
  public static void tearDown() {
    campaign.close();
  }

  //--------------------------------------------------------------------------
  //---------------Testing other functionality--------------------------------
  //--------------------------------------------------------------------------

  @Test
  public void testToUTC() {
    assertEquals( "2015-01-01T12:01:21Z", Utils.toUTC( "2015-01-01 12:01:21" ) );
    assertEquals( "word", Utils.toUTC( "word" ) );
    assertEquals( "n/a", Utils.toUTC( "n/a" ) );
  }

  /*
  Wrong type of file inside the zip
  */
  @Test
  public void testBadZip1() {
    Campaign c = new Campaign( new File( "test_data/bad_zip1.zip" ).toPath() );
    assertEquals( false, c.isZipValid() );
  }

  /*
  The csv files exist inside a directory in the zip file
   */
  @Test
  public void testBadZip2() {
    var c = new Campaign( new File( "test_data/bad_zip2.zip" ).toPath() );
    assertEquals( false, c.isZipValid() );
  }

  /*
  We have another directory in the zip file
   */
  @Test
  public void testBadZip3() {
    Campaign c = new Campaign( new File( "test_data/bad_zip3.zip" ).toPath() );
    assertEquals( false, c.isZipValid() );
  }

  /*
  Correct format of the zip but wrong inputs in the csv files
   */
  @Test
  public void testBadZip4() {
    Campaign c = new Campaign( new File( "test_data/bad_zip4.zip" ).toPath() );
    assertEquals( true, c.isZipValid() );
  }

  /*
  Not all the required csv files are present
   */
  @Test
  public void testBadZip5() {
    Campaign c = new Campaign( new File( "test_data/bad_zip5.zip" ).toPath() );
    assertEquals( false, c.isZipValid() );
  }

  @Test
  public void csvBlankLine() {
    Campaign c = new Campaign( new File( "test_data/bad_zip_missing_line.zip" ).toPath() );
    c.load();
    assertEquals( true, c.isZipValid() );
    c.close();
  }

  @Test
  public void csvMissingColumns() {
    Campaign c = new Campaign( new File( "test_data/bad_zip_missing_column.zip" ).toPath() );
    c.load();
    assertEquals( true, c.isZipValid() );
    c.close();
  }

}