package comp2211.group45.addashboard.utils;

public enum GenderFilter {
  MALE( "Male" ),
  FEMALE( "Female" ),
  ANY( "Any (No filtering)" );

  private String string;

  GenderFilter( String s ) {
    string = s;
  }

  @Override
  public String toString() {
    return string;
  }

  public String toSQLString() {
    // *INDENT-OFF*
    return switch ( this ) {
      case MALE -> "Gender = \"Male\"";
      case FEMALE -> "Gender = \"Female\"";
      case ANY -> "TRUE";
    };
    // *INDENT-ON*
  }

}
