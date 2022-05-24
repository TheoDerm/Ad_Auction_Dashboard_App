package comp2211.group45.addashboard.uielements;

import java.time.Instant;
import java.time.LocalDate;

import comp2211.group45.addashboard.campaign.Campaign;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Date;

public class DateTimeSelector extends VBox {

  private Instant startDate;
  private Instant endDate;

  private DatePicker datePicker;
  private Spinner<Integer> hourSpinner;
  private Spinner<Integer> minuteSpinner;
  private Spinner<Integer> secondSpinner;

  public DateTimeSelector( Campaign c ) {
    startDate = minOf( c.serverLogEarliestEntry(),c.clickLogStart(), c.impressionLogStart() );
    endDate = maxOf( c.serverLogEarliestEntry(),c.clickLogStart(), c.impressionLogStart() );

    datePicker = new DatePicker();
    datePicker.setValue( LocalDate.parse( getStartDate().toString().split( "T" )[0] ) );
    datePicker.getStyleClass().add( "textClass" );
    getChildren().add( datePicker );

    HBox time = new HBox();
    hourSpinner = new Spinner<Integer>();
    hourSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory( 0, 23, 12, 1 ) );
    hourSpinner.setPrefWidth( 87 );
    hourSpinner.setMinWidth( 87 );
    hourSpinner.setEditable( true );
    hourSpinner.getStyleClass().add( "textClass" );
    Text sep1 = new Text( ":" );
    sep1.getStyleClass().add( "textClassBold" );
    minuteSpinner = new Spinner<Integer>();
    minuteSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory( 0, 59, 0, 1 ) );
    minuteSpinner.setPrefWidth( 87 );
    minuteSpinner.setMinWidth( 87 );
    minuteSpinner.setEditable( true );
    minuteSpinner.getStyleClass().add( "textClass" );
    Text sep2 = new Text( ":" );
    sep2.getStyleClass().add( "textClassBold" );
    secondSpinner = new Spinner<Integer>();
    secondSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory( 0, 59, 0, 1 ) );
    secondSpinner.setPrefWidth( 87 );
    secondSpinner.setMinWidth( 87 );
    secondSpinner.getStyleClass().add( "textClass" );
    secondSpinner.setEditable( true );

    time.getChildren().addAll( hourSpinner, sep1, minuteSpinner, sep2, secondSpinner );

    getChildren().add( time );

    setSpacing( 6 );
  }

  public Instant getDateTime() {
    String toReturn = datePicker.getValue().toString() + "T" +
                      String.format( "%02d", hourSpinner.getValue() ) + ":" +
                      String.format( "%02d", minuteSpinner.getValue() ) + ":" +
                      String.format( "%02d", secondSpinner.getValue() ) + "Z";
    return Instant.parse( toReturn );
  }

  private Instant minOf( Instant i, Instant j, Instant k ) {
    return new Date( Math.min( Math.min( i.getEpochSecond(), j.getEpochSecond() ), k.getEpochSecond() ) * 1000 ).toInstant();
  }

  private Instant maxOf( Instant i, Instant j, Instant k ) {
    return new Date( Math.max( Math.max( i.getEpochSecond(), j.getEpochSecond() ), k.getEpochSecond() ) * 1000 ).toInstant();
  }

  public Instant getStartDate() {
    return startDate;
  }

  public Instant getEndDate() {
    return endDate;
  }

}