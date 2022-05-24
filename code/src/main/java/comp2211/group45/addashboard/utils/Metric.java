package comp2211.group45.addashboard.utils;

/**
 * Enum to select a specific metric
 */
public enum Metric {
  IMPRESSION_COUNT( "Number of Impressions" ),
  CLICK_COUNT( "Number of Clicks" ),
  UNIQUE_CLICK_COUNT( "Number of Unique Clicks" ),
  BOUNCE_COUNT( "Number of Bounces" ),
  CONVERSION_COUNT( "Number of Conversions" ),
  TOTAL_COST( "Total Cost" ),
  CTR( "Click Through Rate" ), //Click through rate
  CPA( "Cost Per Acquisition" ), //Cost per acquisition
  CPC( "Cost Per Click" ), //Cost per click
  CPM( "Cost Per 1000 Impressions" ), //Cost per thousand impressions
  BOUNCE_RATE( "Bounce Rate" );

  String string;

  Metric ( String s ) {
    this.string = s;
  }

  @Override
  public String toString() {
    return string;
  }
}
