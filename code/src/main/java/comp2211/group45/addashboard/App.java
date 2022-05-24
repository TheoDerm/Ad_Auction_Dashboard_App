package comp2211.group45.addashboard;


import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class App extends Application {

  /**
   * True entry point of the program,
   * sets up the environment and launches the GUI.
   * @param args the command-line arguments to the program.
   */
  public static void main( String[] args ) {
    launch( args );
  }

  /**
   * Logic pertaining to the launching of the graphical environment
   * @param stage the {@link Stage} on which the GUI is created (handled by JavaFX)
   */
  @Override
  public void start( Stage stage ) throws Exception {
    stage.setTitle( "Ad Dashboard" );
    stage.setMinWidth( 900 );
    stage.setMinHeight( 700 );

    FXMLLoader loader = new FXMLLoader();
    // loader.setLocation( new File( "GUI.fxml" ).toURI().toURL() );
    loader.setLocation( getClass().getClassLoader().getResource( "GUI.fxml" ) );
    Parent root = loader.load();
    Scene scene = new Scene( root );
    stage.setScene( scene );
    stage.show();
  }
}
