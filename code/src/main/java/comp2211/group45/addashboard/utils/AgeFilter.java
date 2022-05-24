package comp2211.group45.addashboard.utils;

public enum AgeFilter {
  LT_TWENTY_FIVE( "<25" ),
  TWENTY_FIVE_TO_THIRTY_FOUR( "25-34" ),
  THIRTY_FIVE_TO_FORTY_FOUR( "35-44" ),
  FORTY_FIVE_TO_FIFTY_FOUR( "45-54" ),
  GT_FIFTY_FOUR( ">54" ),
  ANY( "Any (No filtering)" );

  private String string;

  AgeFilter( String s ) {
    string = s;
  }

  @Override
  public final String toString() {
    return string;
  }

  public String toSQLString() {
    // *INDENT-OFF*
    return switch( this ) {
      case LT_TWENTY_FIVE -> "AgeRange = \"<25\"";
      case TWENTY_FIVE_TO_THIRTY_FOUR -> "AgeRange = \"25-34\"";
      case THIRTY_FIVE_TO_FORTY_FOUR -> "AgeRange = \"35-44\"";
      case FORTY_FIVE_TO_FIFTY_FOUR -> "AgeRange = \"45-54\"";
      case GT_FIFTY_FOUR -> "AgeRange = \">54\"";
      case ANY -> "TRUE";
    };
    // *INDENT-ON*
  }
}
