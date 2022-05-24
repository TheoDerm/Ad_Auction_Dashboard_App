package comp2211.group45.addashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.statistics.HistogramDataset;

import comp2211.group45.addashboard.campaign.Campaign;
import comp2211.group45.addashboard.campaign.ServerLog;
import comp2211.group45.addashboard.utils.AgeFilter;
import comp2211.group45.addashboard.utils.ContextFilter;
import comp2211.group45.addashboard.utils.GenderFilter;
import comp2211.group45.addashboard.utils.IncomeFilter;
import comp2211.group45.addashboard.utils.Metric;
import comp2211.group45.addashboard.utils.TimeStep;
import comp2211.group45.addashboard.utils.Utils;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

/**
 * Class to produce a line chart from a given campaign
 */
public class ChartBuilder {
  private static final boolean DEBUG = true;
  public Campaign campaign;
  public ChartBuilder( Campaign campaign ) {
    this.campaign = campaign;
  }

  /**
   * Create a line chart from a given metric over a given time period with a given time step
   * @param metric Metric on the graph (MetricEnum)
   * @param timeStep Time step (TimeStepEnum)
   * @param startTime Start time (Instant)
   * @param stopTime Stop time (Instant)
   * @return LineChart of the data, null if any errors occurred (no exception throwing currently)
   */
  public LineChart<String,Number> buildLineChart( Metric metric, TimeStep timeStep, Instant startTime, Instant stopTime,
      AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    //Time bounds checking:
    if ( startTime.getEpochSecond() >= stopTime.getEpochSecond() )
      return null;
    if ( calcNumDataPoints( timeStep, startTime, stopTime ) > 100 ) {
      d( "More than 100 data points, might take some time on the thicc dataset" );
    }

    CategoryAxis x = new CategoryAxis();
    x.setLabel( "Time" );
    NumberAxis y = new NumberAxis();
    y.setLabel( metric.toString() );

    LineChart<String,Number> chart = new LineChart<String,Number>( x,y );

    var series = getSeries( metric, timeStep, startTime, stopTime, ageFilter, genderFilter, incomeFilter, contextFilter );
    if ( series == null )
      return null;

    chart.setTitle( metric.toString() + " Against Time" );
    chart.getData().add( series );
    chart.setLegendVisible( false );
    chart.setPrefSize( 10000, 10000 );
    chart.getStyleClass().add( "textClass" );
    return chart;
  }

  /**
   * Get a data series for a specific metric
   */
  protected Series<String, Number> getSeries( Metric metric, TimeStep timeStep, Instant startTime, Instant stopTime,
      AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    XYChart.Series<String,Number> data = new XYChart.Series<String,Number>();

    if ( metric == Metric.IMPRESSION_COUNT ) {
      String queryString = "SELECT COUNT(*) FROM ImpressionLog WHERE DATE BETWEEN \"";

      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number result;

      while( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        result = campaign.impressionLog().numberQuery( queryString +
                 currentTime.toString() + "\" AND \"" +
                 timeAfterStep.toString() + "\" AND " +
                 ageFilter.toSQLString() + " AND " +
                 genderFilter.toSQLString() + " AND " +
                 incomeFilter.toSQLString() + " AND " +
                 contextFilter.toSQLString() );
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.CLICK_COUNT ) {
      //Create temp table from filters:
      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + viewName + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      String queryString = "SELECT COUNT(*) FROM " + viewName + " WHERE Date BETWEEN \"";
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number result;

      while( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        result = campaign.clickLog().numberQuery( queryString +
                 currentTime.toString() + "\" AND \"" +
                 timeAfterStep.toString() + "\"" );
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }

      return data;
    }

    else if ( metric == Metric.CONVERSION_COUNT ) {
      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + viewName + " AS " +
                        "SELECT ServerLog.EntryDate, ServerLog.ID, ServerLog.ExitDate, ServerLog.PagesViewed, ServerLog.Conversion "+
                        "FROM ServerLog INNER JOIN ImpressionLog " +
                        "ON ServerLog.ID = ImpressionLog.ID WHERE " +
                        "ServerLog.Conversion = 1 AND " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      String queryString = "SELECT COUNT(*) FROM " + viewName + " WHERE EntryDate BETWEEN \"";
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number result;

      while( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        result = campaign.clickLog().numberQuery( queryString +
                 currentTime.toString() + "\" AND \"" +
                 timeAfterStep.toString() + "\"" );
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.UNIQUE_CLICK_COUNT ) {
      //Create temp table from filters:
      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + viewName + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      String queryString = "SELECT COUNT(DISTINCT ID) FROM " + viewName + " WHERE Date BETWEEN \"";
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number result;

      while( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        result = campaign.clickLog().numberQuery( queryString +
                 currentTime.toString() + "\" AND \"" +
                 timeAfterStep.toString() + "\"" );
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.TOTAL_COST ) {
      String clickViewName = "V_CLICK_" +  Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + clickViewName + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );
      Instant currentTime = startTime;
      Instant timeAfterStep;
      double clickCost;
      double impressionCost;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        clickCost = campaign.clickLog().numberQuery( "SELECT TOTAL(ClickCost) FROM " + clickViewName + " WHERE Date BETWEEN \"" +
                    currentTime.toString() + "\" AND \"" +
                    timeAfterStep.toString() + "\"" ).doubleValue();
        impressionCost = campaign.impressionLog().numberQuery( "SELECT TOTAL(ImpressionCost) FROM ImpressionLog WHERE Date BETWEEN \""+
                         currentTime.toString() + "\" AND \"" +
                         timeAfterStep.toString() + "\" AND " +
                         ageFilter.toSQLString() + " AND " +
                         genderFilter.toSQLString() + " AND " +
                         incomeFilter.toSQLString() + " AND " +
                         contextFilter.toSQLString() ).doubleValue();
        result = clickCost + impressionCost;
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.CTR ) {
      String clickViewName = "V_CLICK_" +  Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + clickViewName + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Integer clickCount;
      Integer impressionCount;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        clickCount = campaign.clickLog().numberQuery( "SELECT COUNT(*) FROM " + clickViewName + " WHERE Date BETWEEN \"" +
                     currentTime.toString() + "\" AND \"" +
                     timeAfterStep.toString() + "\"" ).intValue();
        impressionCount = campaign.impressionLog().numberQuery( "SELECT COUNT(*) FROM ImpressionLog WHERE Date BETWEEN \""+
                          currentTime.toString() + "\" AND \"" +
                          timeAfterStep.toString() + "\" AND " +
                          ageFilter.toSQLString() + " AND " +
                          genderFilter.toSQLString() + " AND " +
                          incomeFilter.toSQLString() + " AND " +
                          contextFilter.toSQLString() ).intValue();
        result = impressionCount == 0 ? 0 : ( Double.valueOf( clickCount ) / impressionCount ); //Check for divide-by-zero
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.CPA ) {
      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      String query = "CREATE TEMPORARY VIEW " + viewName + " AS " +
                     "SELECT ServerLog.EntryDate, ServerLog.ID, ServerLog.ExitDate, ServerLog.PagesViewed, ServerLog.Conversion " +
                     "FROM ServerLog INNER JOIN ImpressionLog " +
                     "ON ServerLog.ID = ImpressionLog.ID WHERE " +
                     "ServerLog.Conversion = 1 AND " +
                     ageFilter.toSQLString() + " AND " +
                     genderFilter.toSQLString() + " AND " +
                     incomeFilter.toSQLString() + " AND " +
                     contextFilter.toSQLString();
      campaign.dbQuery( query );

      Instant currentTime = startTime;
      Instant timeAfterStep;
      Double impressionCost;
      Integer conversionCount;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        conversionCount = campaign.clickLog().numberQuery( "SELECT COUNT(*) FROM " + viewName + " WHERE EntryDate BETWEEN \""+
                          currentTime.toString() + "\" AND \"" +
                          timeAfterStep.toString() + "\"" ).intValue();
        impressionCost = campaign.impressionLog().numberQuery( "SELECT TOTAL(ImpressionCost) FROM ImpressionLog WHERE Date BETWEEN \"" +
                         currentTime.toString() + "\" AND \"" +
                         timeAfterStep.toString() + "\" AND " +
                         ageFilter.toSQLString() + " AND " +
                         genderFilter.toSQLString() + " AND " +
                         incomeFilter.toSQLString() + " AND " +
                         contextFilter.toSQLString() ).doubleValue();
        result = conversionCount == 0 ? 0 : ( impressionCost / conversionCount ); //Check for divide-by-zero
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.CPC ) {
      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + viewName + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number calculation;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        calculation = campaign.clickLog().numberQuery( "SELECT TOTAL(ClickCost) / COUNT(*) FROM " + viewName + " WHERE Date BETWEEN \"" +
                      currentTime.toString() + "\" AND \"" +
                      timeAfterStep.toString() + "\"" );
        result = calculation == null ? 0 : calculation; //Check for divide-by-zero
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.CPM ) {
      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number calculation;
      Number result;
      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        calculation = campaign.impressionLog().numberQuery( "SELECT (TOTAL(ImpressionCost)/COUNT(*))*1000.0 FROM ImpressionLog WHERE Date BETWEEN \"" +
                      currentTime.toString() + "\" AND \"" +
                      timeAfterStep.toString() + "\" AND " +
                      ageFilter.toSQLString() + " AND " +
                      genderFilter.toSQLString() + " AND " +
                      incomeFilter.toSQLString() + " AND " +
                      contextFilter.toSQLString() );
        result = calculation == null ? 0 : calculation; //Check for divide-by-zero
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.BOUNCE_COUNT ) {
      String bounceQuery = switch ( ServerLog.definitionOfBounce ) {
      case TIME_SPENT -> "strftime(\"%s\", ExitDate) - strftime(\"%s\", EntryDate) < " + Integer.toString( ServerLog.bounceTime ) ;
      case SINGLE_PAGE_VIEW -> "(PagesViewed = 1 OR Conversion = 0)";
      };

      String viewName = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + viewName + " AS " +
                        "SELECT ServerLog.EntryDate, ServerLog.ID, ServerLog.ExitDate, ServerLog.PagesViewed, ServerLog.Conversion " +
                        "FROM ServerLog INNER JOIN ImpressionLog " +
                        "ON ServerLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      Instant currentTime = startTime;
      Instant timeAfterStep;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        result = campaign.clickLog().numberQuery( "SELECT COUNT(*) FROM " + viewName + " WHERE EntryDate BETWEEN \"" +
                 currentTime.toString() + "\" AND \"" +
                 timeAfterStep.toString() + "\" AND " +
                 bounceQuery );
        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    else if ( metric == Metric.BOUNCE_RATE ) {
      String bounceQuery = switch ( ServerLog.definitionOfBounce ) {
      case TIME_SPENT -> "strftime(\"%s\", ExitDate) - strftime(\"%s\", EntryDate) < " + Integer.toString( ServerLog.bounceTime ) ;
      case SINGLE_PAGE_VIEW -> "(PagesViewed = 1 OR Conversion = 0)";
      };

      String serverView = "V_BOUNCE_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + serverView + " AS " +
                        "SELECT ServerLog.EntryDate, ServerLog.ID, ServerLog.ExitDate, ServerLog.PagesViewed, ServerLog.Conversion " +
                        "FROM ServerLog INNER JOIN ImpressionLog " +
                        "ON ServerLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      String clickView = "V_" + Long.toString( System.currentTimeMillis() );
      campaign.dbQuery( "CREATE TEMPORARY VIEW " + clickView + " AS " +
                        "SELECT ClickLog.Date, ClickLog.ID, ClickLog.ClickCost " +
                        "FROM ClickLog INNER JOIN ImpressionLog " +
                        "ON ClickLog.ID = ImpressionLog.ID WHERE " +
                        ageFilter.toSQLString() + " AND " +
                        genderFilter.toSQLString() + " AND " +
                        incomeFilter.toSQLString() + " AND " +
                        contextFilter.toSQLString() );

      Instant currentTime = startTime;
      Instant timeAfterStep;
      Integer bounceCount;
      Integer clickCount;
      Number result;

      while ( true ) {
        if ( currentTime.getEpochSecond() >= stopTime.getEpochSecond() )
          break; //We have reached beyond the end time, so stop
        timeAfterStep = Instant.ofEpochSecond( currentTime.getEpochSecond() + Utils.stepEnumToSeconds( timeStep ) );
        bounceCount = campaign.clickLog().numberQuery( "SELECT COUNT(*) FROM " + serverView + " WHERE EntryDate BETWEEN \"" +
                      currentTime.toString() + "\" AND \"" +
                      timeAfterStep.toString() + "\" AND " +
                      bounceQuery ).intValue();
        clickCount = campaign.clickLog().numberQuery( "SELECT COUNT(*) FROM " + clickView + " WHERE Date BETWEEN \"" +
                     currentTime.toString() + "\" AND \"" +
                     timeAfterStep.toString() + "\"" ).intValue();
        result = ( clickCount == 0 ) ? 0 : bounceCount / ( clickCount / 1.0 );

        data.getData().add( new XYChart.Data<String,Number>(
                              Utils.getPrettyDateTime( currentTime ),
                              result ) ); //Add data to series
        currentTime = timeAfterStep; //Set the current time to the original current time plus the time step
      }
      return data;
    }

    return null;
  }

//-------------------Helper / other working methods-----------------------------------------------------------------------------

  /**
   * Get a histogram of the click costs
   * @return ChartViewer containing histogram
   */
  public ChartViewer getClickCostHistogram() {
    String q = "SELECT ClickCost FROM ClickLog WHERE ClickCost > 0";
    ArrayList<Double> data = new ArrayList<Double>();
    try {
      ResultSet r = campaign.clickLogQuery( q );
      r.next();
      do {
        data.add( r.getDouble( 1 ) );
      } while ( r.next() );
    } catch ( SQLException e ) {
      d( "SQL Error" );
    }
    Double[] dataArray = data.toArray( new Double[data.size()] );
    double[] dataArrayP = Arrays.stream( dataArray ).mapToDouble( Double::doubleValue ).toArray();
    HistogramDataset d = new HistogramDataset();
    d.addSeries( "Click Cost (Pence)", dataArrayP, 100 );
    JFreeChart histogram = ChartFactory.createHistogram( "Histogram of Click Costs (Overview of campaign costs / click counts)", "Click Cost (Pence)", "Frequency", d );
    var view = new ChartViewer( histogram );
    view.setPrefSize( 10000.0, 10000.0 );
    return view;
  }

  /**
   * Calculate the number of data points that will be created in a graph
   * @param timeStep Time step
   * @param startTime Start time
   * @param stopTime Stop time
   * @return
   */
  private long calcNumDataPoints( TimeStep timeStep, Instant startTime, Instant stopTime ) {
    return ( stopTime.getEpochSecond() - startTime.getEpochSecond() ) / Utils.stepEnumToSeconds( timeStep );
  }

  /**
  * Print debug message for controller
  * @param args Thing to print to debug
  */
  public static void d( Object... args  ) {
    if ( DEBUG ) {
      System.out.print( "[\u001B[34mChartBuilder\u001B[0m] " );
      System.out.println( args[0] );
    }
  }
}