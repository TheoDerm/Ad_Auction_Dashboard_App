package comp2211.group45.addashboard.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import javax.xml.bind.DatatypeConverter;

/**
 * A random utilities class that has various useful static methods to be used wherever
 */
public class Utils {

  /**
  * Get a MD5 hash of a string
  * @param thingToHash String to get the hash of
  * @return String containing the hexadecimal representation of the hash
  */
  public static String getMD5Hash( String thingToHash ) {
    try {
      var md = MessageDigest.getInstance( "MD5" );
      md.update( thingToHash.getBytes() );
      return DatatypeConverter.printHexBinary( md.digest() ).toUpperCase();
    } catch ( NoSuchAlgorithmException e ) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get the UTC datetime from the format given in the CSV files
   * @param date String of the datetime in the CSV files
   * @return String of the datetime in UTC format
   */
  public static String toUTC( String date ) {
    String[] split = date.split( " " );
    if ( split.length == 2 ) {
      return split[0] + "T" + split[1] + "Z";
    }
    return date;
  }

  /**
   * Convert a TimeStepEnum to seconds
   * @param e Time step to convert
   * @return long containing the number of seconds
   */
  public static long stepEnumToSeconds( TimeStep e ) {
    switch( e ) {
    case ONE_MINUTE:
      return 60;
    case FIVE_MINUTES:
      return 60*5;
    case THIRTY_MINUTES:
      return 60*30;
    case ONE_HOUR:
      return 60*60;
    case FOUR_HOURS:
      return 60*60*4;
    case TWELVE_HOURS:
      return 60*60*12;
    case ONE_DAY:
      return 60*60*24;
    case TWO_DAYS:
      return 60*60*24*2;
    case ONE_WEEK:
      return 60*60*24*7;
    default:
      return 0;
    }
  }

  /**
   * Convert instant to nice looking date for charts
   * @param i Instant
   * @return String containing nice formatted date
   */
  public static String getPrettyDateTime( Instant i ) {
    String[] dateTime = i.toString().split( "Z" )[0].split( "T" );
    String[] date = dateTime[0].split( "-" );
    String[] time = dateTime[1].split( ":" );
    return date[2] + "/" + date[1] + "/" + date[0].substring( 2 )
           + "\n" + time[0] + ":" + time[1];
  }

}
