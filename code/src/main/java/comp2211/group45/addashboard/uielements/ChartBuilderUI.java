package comp2211.group45.addashboard.uielements;

import java.time.Instant;

import comp2211.group45.addashboard.ChartBuilder;
import comp2211.group45.addashboard.Comparison;
import comp2211.group45.addashboard.Controller;
import comp2211.group45.addashboard.campaign.Campaign;
import comp2211.group45.addashboard.utils.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChartBuilderUI extends HBox {

  private Campaign campaign;
  private FilterSelectVBox filters;
  private ChoiceBox<Metric> metricSelect;
  private ChoiceBox<TimeStep> timeStepSelect;
  private DateTimeSelector fromDateTimeSel;
  private DateTimeSelector toDateTimeSel;
  private ChartBuilder chartBuilder;

  public ChartBuilderUI( Campaign c, int i ) {
    VBox metricsBox = new VBox();
    VBox metricsBox2 = new VBox();
    this.campaign = c;
    this.chartBuilder = new ChartBuilder( c );

    Text metric = new Text( "Metric:" );
    metric.getStyleClass().add( "textClassBold" );
    metric.underlineProperty().set( true );
    metricsBox.getChildren().add( metric );

    metricSelect = new ChoiceBox<Metric>();
    metricSelect.getItems().addAll(
      Metric.IMPRESSION_COUNT,
      Metric.CLICK_COUNT,
      Metric.UNIQUE_CLICK_COUNT,
      Metric.BOUNCE_COUNT,
      Metric.CONVERSION_COUNT,
      Metric.TOTAL_COST,
      Metric.CTR,
      Metric.CPA,
      Metric.CPC,
      Metric.CPM,
      Metric.BOUNCE_RATE );
    metricSelect.setValue( Metric.IMPRESSION_COUNT );
    metricSelect.getStyleClass().add( "textClass" );
    metricsBox.getChildren().add( metricSelect );

    this.filters = new FilterSelectVBox();
    metricsBox.getChildren().add( filters );

    Text fromDateTimeText = new Text( "From:" );
    fromDateTimeText.getStyleClass().add( "textClassBold" );
    fromDateTimeText.underlineProperty().set( true );
    metricsBox.getChildren().add( fromDateTimeText );

    fromDateTimeSel = new DateTimeSelector( campaign );
    metricsBox.getChildren().add( fromDateTimeSel );

    Text toDateTimeText = new Text( "To:" );
    toDateTimeText.getStyleClass().add( "textClassBold" );
    toDateTimeText.underlineProperty().set( true );
    metricsBox.getChildren().add( toDateTimeText );

    toDateTimeSel = new DateTimeSelector( campaign );
    metricsBox.getChildren().add( toDateTimeSel );

    Text timeStepSelectText = new Text( "Time Step:" );
    timeStepSelectText.getStyleClass().add( "textClassBold" );
    timeStepSelectText.underlineProperty().set( true );
    metricsBox.getChildren().add( timeStepSelectText );

    timeStepSelect = new ChoiceBox<TimeStep>();
    timeStepSelect.getItems().addAll(
      TimeStep.ONE_MINUTE,
      TimeStep.FIVE_MINUTES,
      TimeStep.THIRTY_MINUTES,
      TimeStep.ONE_HOUR,
      TimeStep.FOUR_HOURS,
      TimeStep.TWELVE_HOURS,
      TimeStep.ONE_DAY,
      TimeStep.TWO_DAYS,
      TimeStep.ONE_WEEK );
    timeStepSelect.setValue( TimeStep.FOUR_HOURS );
    timeStepSelect.getStyleClass().add( "textClass" );
    metricsBox.getChildren().add( timeStepSelect );

    Button updateChart = new Button( "Update Chart" );
    updateChart.getStyleClass().add( "textClass" );
    metricsBox.getChildren().add( updateChart );


    metricsBox.setSpacing( 6 );
    metricsBox2.setSpacing( 1 );

    Text headerText = new Text( "Graph " + i );
    headerText.getStyleClass().add( "textClassBold" );
    headerText.underlineProperty().set( true );
    metricsBox.getChildren().add( headerText );

    Button hide = new Button( i == 1 ? "<<" : ">>" );
    hide.setMinWidth( 60 );
    metricsBox2.getChildren().add( headerText );
    metricsBox2.getChildren().add( hide );
    metricsBox2.getChildren().add( metricsBox );
    hide.getStyleClass().add( "textClassBold" );
    hide.setStyle( "-fx-background-color: transparent" );

    if ( i != 1 ) {
      metricsBox2.getChildren().remove( 2,3 );
    }

    hide.setOnAction( e -> {
      if ( metricsBox2.getChildren().size() == 3 ) {
        metricsBox2.getChildren().remove( 2,3 );
        hide.setText( ">>" );
      } else {
        metricsBox2.getChildren().remove( 1,2 );
        metricsBox2.getChildren().addAll( hide, metricsBox );
        hide.setText( "<<" );
      }
    } );

    updateChart.setOnAction( e -> {
      if ( !( toDateTimeSel.getDateTime().getEpochSecond() > fromDateTimeSel.getDateTime().getEpochSecond() ) ) {
        Alert a = new Alert( Alert.AlertType.ERROR );
        a.setContentText( "Date range invalid" );
        a.showAndWait();
        return;
      }
      if ( getChildren().size() > 1 )
        getChildren().remove( 1, 2 );
      getChildren().add( buildChartFromUI() );
    } );

    getChildren().add( metricsBox2 );

    setSpacing( 3 );
    setPadding( new Insets( 6,6,6,6 ) );
  }

  public ChartBuilderUI( Comparison c ) {
    VBox rootVBox = new VBox();
    rootVBox.setSpacing( 8 );
    rootVBox.setPadding( new Insets( 8 ) );

    for ( var filteredCampaign : c.campaigns() ) {
      Button b = new Button( "Filters for " + filteredCampaign.getName() );
      b.setMaxWidth( 200 );
      b.setWrapText( true );
      b.getStyleClass().add( "textClass" );
      rootVBox.getChildren().add( b );
      b.setOnAction( e -> {
        FilterSelectVBox filters = new FilterSelectVBox();
        filters.ageFilterSel.setValue( filteredCampaign.ageFilter );
        filters.genderFilterSel.setValue( filteredCampaign.genderFilter );
        filters.incomeFilterSel.setValue( filteredCampaign.incomeFilter );
        filters.contextFilterSel.setValue( filteredCampaign.contextFilter );

        if ( filters.getStylesheets().size() > 1 )
          filters.getStylesheets().remove( 0,1 );
        filters.getStylesheets().add( Controller.currentStyleSheet );

        filters.setPadding( new Insets( 10 ) );
        filters.setSpacing( 10 );
        filters.setAlignment( Pos.TOP_CENTER );

        Button update = new Button( "Update" );
        update.getStyleClass().add( "textClass" );
        filters.getChildren().add( update );

        Stage s = new Stage();
        s.initModality( Modality.APPLICATION_MODAL );
        s.setTitle( filteredCampaign.getName() + " filters" );
        s.setScene( new Scene( filters ) );
        s.show();

        update.setOnAction( e1 -> {
          filteredCampaign.ageFilter = filters.getAgeFilter();
          filteredCampaign.incomeFilter = filters.getIncomeFilter();
          filteredCampaign.genderFilter = filters.getGenderFilter();
          filteredCampaign.contextFilter = filters.getContextFilter();
          s.close();
        } );
      } );
    }

    Text startDateText = new Text( "Start Date:" );
    startDateText.getStyleClass().add( "textClassBold" );
    startDateText.underlineProperty().set( true );
    rootVBox.getChildren().add( startDateText );
    DateTimeSelector startDate = new DateTimeSelector( c.campaigns().get( 0 ).campaign );
    rootVBox.getChildren().add( startDate );

    Text endDateText = new Text( "End Date:" );
    endDateText.getStyleClass().add( "textClassBold" );
    endDateText.underlineProperty().set( true );
    rootVBox.getChildren().add( endDateText );
    DateTimeSelector endDate = new DateTimeSelector( c.campaigns().get( 0 ).campaign );
    rootVBox.getChildren().add( endDate );

    Text metricText = new Text( "Metric:" );
    metricText.getStyleClass().add( "textClassBold" );
    metricText.underlineProperty().set( true );
    rootVBox.getChildren().add( metricText );
    metricSelect = new ChoiceBox<Metric>();
    metricSelect.getItems().addAll(
      Metric.IMPRESSION_COUNT,
      Metric.CLICK_COUNT,
      Metric.UNIQUE_CLICK_COUNT,
      Metric.BOUNCE_COUNT,
      Metric.CONVERSION_COUNT,
      Metric.TOTAL_COST,
      Metric.CTR,
      Metric.CPA,
      Metric.CPC,
      Metric.CPM,
      Metric.BOUNCE_RATE );
    metricSelect.setValue( Metric.IMPRESSION_COUNT );
    metricSelect.getStyleClass().add( "textClass" );
    rootVBox.getChildren().add( metricSelect );

    Text timeStepText = new Text( "Time Step:" );
    timeStepText.getStyleClass().add( "textClassBold" );
    timeStepText.underlineProperty().set( true );
    rootVBox.getChildren().add( timeStepText );
    timeStepSelect = new ChoiceBox<TimeStep>();
    timeStepSelect.getItems().addAll(
      TimeStep.ONE_MINUTE,
      TimeStep.FIVE_MINUTES,
      TimeStep.THIRTY_MINUTES,
      TimeStep.ONE_HOUR,
      TimeStep.FOUR_HOURS,
      TimeStep.TWELVE_HOURS,
      TimeStep.ONE_DAY,
      TimeStep.TWO_DAYS,
      TimeStep.ONE_WEEK );
    timeStepSelect.setValue( TimeStep.FOUR_HOURS );
    timeStepSelect.getStyleClass().add( "textClass" );
    rootVBox.getChildren().add( timeStepSelect );

    Button update = new Button( "Update Chart" );
    update.getStyleClass().add( "textClass" );
    rootVBox.getChildren().add( update );

    update.setOnAction( e -> {
      if ( !( startDate.getDateTime().getEpochSecond() < endDate.getDateTime().getEpochSecond() ) ) {
        Alert a = new Alert( Alert.AlertType.ERROR );
        a.setContentText( "Date range invalid" );
        a.showAndWait();
        return;
      }
      if ( getChildren().size() > 1 ) {
        getChildren().remove( 1,2 );
      }
      getChildren().add( c.buildLineChart( metricSelect.getValue(), timeStepSelect.getValue(), startDate.getDateTime(), endDate.getDateTime() ) );
    } );

    getChildren().add( rootVBox );

  }

  public LineChart<String,Number> buildChartFromUI() {
    return chartBuilder.buildLineChart( getMetric(),getTimeStep(),getFromDate(),getToTime(),
                                        getAgeFilter(), getGenderFilter(), getIncomeFilter(),
                                        getContextFilter() );
  }

  public Metric getMetric() {
    return metricSelect.getValue();
  }

  public AgeFilter getAgeFilter() {
    return filters.getAgeFilter();
  }

  public GenderFilter getGenderFilter() {
    return filters.getGenderFilter();
  }

  public IncomeFilter getIncomeFilter() {
    return filters.getIncomeFilter();
  }

  public ContextFilter getContextFilter() {
    return filters.getContextFilter();
  }

  public Instant getFromDate() {
    return fromDateTimeSel.getDateTime();
  }

  public Instant getToTime() {
    return toDateTimeSel.getDateTime();
  }

  public TimeStep getTimeStep() {
    return timeStepSelect.getValue();
  }
}
