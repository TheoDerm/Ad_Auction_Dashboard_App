package comp2211.group45.addashboard.utils;

public enum TimeStep {
  ONE_MINUTE( "One Minute" ),
  FIVE_MINUTES( "Five Minutes" ),
  THIRTY_MINUTES( "Thirty Minutes" ),
  ONE_HOUR( "One Hour" ),
  FOUR_HOURS( "Four Hours" ),
  TWELVE_HOURS( "Twelve Hours" ),
  ONE_DAY( "One Day" ),
  TWO_DAYS( "Two Days" ),
  ONE_WEEK( "One Week" );

  String string;

  TimeStep ( String s ) {
    this.string = s;
  }

  @Override
  public String toString() {
    return string;
  }
}
