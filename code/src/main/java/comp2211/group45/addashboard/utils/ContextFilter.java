package comp2211.group45.addashboard.utils;

public enum ContextFilter {
  NEWS( "News" ),
  SHOPPING( "Shopping" ),
  SOC_MED( "Social Media" ),
  BLOG( "Blog" ),
  HOBBIES( "Hobbies" ),
  TRAVEL( "Travel" ),
  ANY( "Any (No filtering)" );

  private String string;

  ContextFilter( String s ) {
    string = s;
  }

  @Override
  public String toString() {
    return string;
  }

  public String toSQLString() {
    // *INDENT-OFF*
    return switch ( this ) {
      case NEWS -> "Context = \"News\"";
      case SHOPPING -> "Context = \"Shopping\"";
      case SOC_MED -> "Context = \"Social Media\"";
      case BLOG -> "Context = \"Blog\"";
      case HOBBIES -> "Context = \"Hobbies\"";
      case TRAVEL -> "Context = \"Travel\"";
      case ANY -> "TRUE";
    };
    // *INDENT-ON*
  }


}
