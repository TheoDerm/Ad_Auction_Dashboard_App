package comp2211.group45.addashboard;

import comp2211.group45.addashboard.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;

import java.time.Instant;

import comp2211.group45.addashboard.campaign.Campaign;

public class Comparison {
  public class FilteredCampaign extends ChartBuilder {
    public AgeFilter ageFilter;
    public GenderFilter genderFilter;
    public IncomeFilter incomeFilter;
    public ContextFilter contextFilter;

    public FilteredCampaign( Campaign c, AgeFilter af, GenderFilter gf, IncomeFilter i, ContextFilter cf ) {
      super( c );
      this.ageFilter = af;
      this.genderFilter = gf;
      this.incomeFilter = i;
      this.contextFilter = cf;
    }

    public LineChart<String,Number> buildLineChart( Metric metric, TimeStep timeStep, Instant startTime, Instant stopTime ) {
      return super.buildLineChart( metric, timeStep, startTime, stopTime, ageFilter, genderFilter, incomeFilter, contextFilter );
    }

    protected Series<String, Number> getSeries( Metric metric, TimeStep timeStep, Instant startTime, Instant stopTime ) {
      return super.getSeries( metric, timeStep, startTime, stopTime, ageFilter, genderFilter, incomeFilter, contextFilter );
    }

    public String getName() {
      return campaign.name;
    }
  }

  private ObservableList<FilteredCampaign> campaigns = FXCollections.observableArrayList();

  public ObservableList<FilteredCampaign> campaigns() {
    return campaigns;
  }

  public void add( Campaign camp, AgeFilter a, GenderFilter g, IncomeFilter i, ContextFilter c ) {
    campaigns.add( new FilteredCampaign( camp, a, g, i, c ) ) ;
  }

  public LineChart<String,Number> buildLineChart( Metric metric, TimeStep timeStep, Instant startTime, Instant stopTime ) {
    if ( startTime.getEpochSecond() >= stopTime.getEpochSecond() )
      return null;

    CategoryAxis x = new CategoryAxis();
    x.setLabel( "Time" );

    NumberAxis y = new NumberAxis();
    y.setLabel( metric.toString() );

    LineChart<String,Number> chart = new LineChart<String,Number>( x,y );

    chart.setTitle( metric.toString() + " Against Time" );
    chart.setLegendVisible( true );
    chart.setPrefSize( 10000, 10000 );
    chart.setTitleSide( Side.BOTTOM );
    chart.getStyleClass().add( "textClass" );

    for ( var campaign : campaigns ) {
      var s = campaign.getSeries( metric, timeStep, startTime, stopTime );
      s.setName( campaign.getName() );
      chart.getData().add( s );
    }
    return chart;
  }
}
