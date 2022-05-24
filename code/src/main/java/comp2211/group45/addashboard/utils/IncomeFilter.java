package comp2211.group45.addashboard.utils;

public enum IncomeFilter {
  LOW( "Low" ),
  MEDIUM( "Medium" ),
  HIGH( "High" ),
  ANY( "Any (No filtering)" );

  private String string;

  IncomeFilter( String s ) {
    string = s;
  }

  @Override
  public String toString() {
    return string;
  }

  public String toSQLString() {
    // *INDENT-OFF*
    return switch ( this ) {
      case LOW -> "Income = 0";
      case MEDIUM -> "Income = 1";
      case HIGH -> "Income = 2";
      case ANY -> "TRUE";
    };
    // *INDENT-ON*
  }
}
