package comp2211.group45.addashboard;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import comp2211.group45.addashboard.campaign.Campaign;
import comp2211.group45.addashboard.campaign.ServerLog;
import comp2211.group45.addashboard.uielements.CampaignMetricsTable;
import comp2211.group45.addashboard.uielements.ChartBuilderUI;
import comp2211.group45.addashboard.uielements.MetricDisplayerUI;
import comp2211.group45.addashboard.utils.AgeFilter;
import comp2211.group45.addashboard.utils.BounceDef;
import comp2211.group45.addashboard.utils.ContextFilter;
import comp2211.group45.addashboard.utils.GenderFilter;
import comp2211.group45.addashboard.utils.IncomeFilter;
import comp2211.group45.addashboard.utils.Metric;
import comp2211.group45.addashboard.utils.TimeStep;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class Controller {
  private static final boolean DEBUG = true; //Enable debugging messages (using p())

  @FXML private BorderPane rootBorderPane;            //The main BorderPane of the application
  @FXML private ProgressIndicator loadingIndicator;   //Loading indicator in menu bar
  @FXML private VBox leftButtonBox;                   //Buttons in left portion of app
  private ArrayList<Button> leftButtons = new ArrayList<Button>();
  @FXML private RadioMenuItem bounceSinglePage;       //Menu item for bounce definition
  @FXML private RadioMenuItem bounceTimePeriod;       //Menu item for bounce definition
  @FXML private Spinner<Integer> bounceTimeSpinner;   //Spinner used to select the time period for bounces

  private ChartBuilder chartBuilder;
  private Campaign currentSingleCampaign;
  private ArrayList<Campaign> managedCampaigns = new ArrayList<>();

  public static String currentStyleSheet;

  private boolean isLightMode = true;
  private enum TextSize {
    SMALL,
    MEDIUM,
    LARGE
  }
  private TextSize textSize = TextSize.MEDIUM;

  private boolean isComparison = false;
  public Comparison comparison = null;

  /**
   * Initialise the controller (like a constructor except it has access to the injected @FXML)
   */
  @FXML public void initialize() {
    //Using a fancy forEach with lambda to save a line because why not
    leftButtonBox.getChildren().forEach( ( b ) -> {
      if ( b.getClass().equals( javafx.scene.control.Button.class ) ) {
        ( ( Button ) b ).getTooltip().setShowDelay( Duration.millis( 100 ) );
        leftButtons.add( ( Button ) b );
      }
    } );
    leftButtons.forEach( ( b ) -> b.setDisable( true ) );
    bounceTimeSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory( 10, 600, 30, 10 ) );

    //Manage dragging and dropping files:
    rootBorderPane.setOnDragOver( e-> {
      if ( e.getDragboard().hasFiles() ) {
        /* allow for both copying and moving, whatever user chooses */
        e.acceptTransferModes( TransferMode.COPY_OR_MOVE );
      }
      e.consume();
    } );

    rootBorderPane.setOnDragDropped( e-> {
      Dragboard db = e.getDragboard();
      if ( db.hasFiles() ) {
        List<File> files = db.getFiles();
        File firstFile = files.get( 0 );
        try {
          isComparison = false; //Since we are loading a single campaign, we aren't doing a comparison
          currentSingleCampaign = new Campaign( firstFile.toPath() );
          managedCampaigns.add( currentSingleCampaign );
          if ( !currentSingleCampaign.isZipValid() ) {
            d( "Not loading..." );
            alertMessage( "Zip not valid" );
            return;
          }
          //Run campaign loading on another thread
          Task<Integer> t = new Task<Integer>() {
            @Override
            public Integer call() {
              currentSingleCampaign.load();
              return 0;
            }
          };

          t.setOnSucceeded( ev -> {
            chartBuilder = new ChartBuilder( currentSingleCampaign ); //Instantiate chart maker for the current campaign
            bounceDefnHandler( );
            home();
          } );

          lockUI();
          new Thread( t ).start();

        } catch ( Exception fileError ) {
          alertMessage( "Filesystem error\n" + fileError.getMessage() );
        }
      }
      e.setDropCompleted( false );
      e.consume();
    } );

    lightMode();
  }

  /**
   * Handler for opening a campaign
   */
  @FXML private void openSingle( ) {
    //Create and show fileChooser to select the right file:
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle( "Open Campaign" );
    fileChooser.getExtensionFilters().add( new ExtensionFilter( "Zip files","*.zip" ) );
    fileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ) );
    File openedFile = fileChooser.showOpenDialog( rootBorderPane.getScene().getWindow() );

    if ( openedFile == null ) { //Will happen if the user closes the open dialog
      d( "No file loaded" );
      return;
    }

    Campaign c = new Campaign( openedFile.toPath() );

    if ( !c.isZipValid() ) {
      d( "Not loading..." );
      alertMessage( "Zip not valid" );
      return;
    }

    currentSingleCampaign = c; //Create new campaign handler for the opened file
    managedCampaigns.add( currentSingleCampaign );

    //Run campaign loading on another thread
    Task<Integer> t = new Task<Integer>() {
      @Override
      public Integer call() {
        currentSingleCampaign.load();
        return 0;
      }
    };

    t.setOnSucceeded( e -> {
      isComparison = false; //Since we are loading a single campaign, we aren't doing a comparison
      chartBuilder = new ChartBuilder( currentSingleCampaign ); //Instantiate chart maker for the current campaign
      bounceDefnHandler( );
      home();
    } );

    lockUI();
    new Thread( t ).start();

    Stage s = ( Stage ) rootBorderPane.getScene().getWindow();
    s.setOnCloseRequest( e -> handleClose( null ) );
  }

  /**
   * Home button click handler (currently used for testing)
   */
  @FXML private void home() {
    if ( isComparison == false ) {
      clearMainView();
      lockUI();


      //Create graph on another thread (example of using JavaFX concurrent tasks)
      Task<Node> t = new Task<Node>() {
        @Override
        public Node call() {
          HBox h = new HBox();
          h.setPadding( new Insets( 5.0,5.0,5.0,5.0 ) );
          CampaignMetricsTable v = new CampaignMetricsTable( currentSingleCampaign,
              AgeFilter.ANY,
              GenderFilter.ANY,
              IncomeFilter.ANY,
              ContextFilter.ANY );
          h.getChildren().add( v );
          h.getChildren().add( chartBuilder.getClickCostHistogram() );
          return h;
        }
      };
      //Task finished handler
      t.setOnSucceeded( e -> {
        rootBorderPane.setCenter( t.getValue() );
        unlockUI();
      } );
      //Run task (don't forget to do this)
      new Thread( t ).start();
    } else {
      clearMainView();
      Instant min = Instant.parse( "2070-01-01T00:00:00Z" );
      Instant max = Instant.parse( "1970-01-01T00:00:00Z" );
      for ( var c : comparison.campaigns() ) {
        if ( c.campaign.clickLogStart().getEpochSecond() < min.getEpochSecond() )
          min = c.campaign.clickLogStart();
        if ( c.campaign.impressionLogStart().getEpochSecond() < min.getEpochSecond() )
          min = c.campaign.impressionLogStart();
        if ( c.campaign.serverLogEarliestEntry().getEpochSecond() < min.getEpochSecond() )
          min = c.campaign.serverLogEarliestEntry();
        if ( c.campaign.clickLogEnd().getEpochSecond() > max.getEpochSecond() )
          max = c.campaign.clickLogEnd();
        if ( c.campaign.impressionLogEnd().getEpochSecond() > max.getEpochSecond() )
          max = c.campaign.impressionLogEnd();
        if ( c.campaign.serverLogLatestExit().getEpochSecond() > max.getEpochSecond() )
          max = c.campaign.serverLogLatestExit();

      }
      rootBorderPane.setCenter( comparison.buildLineChart( Metric.CLICK_COUNT, TimeStep.FOUR_HOURS, min, max ) );
    }
  }

  /**
   * Handler for detailed metrics (i) button
   */
  @FXML private void detailedMetrics() {
    if ( isComparison == false ) {
      HBox h = new HBox();

      MetricDisplayerUI m1 = new MetricDisplayerUI( currentSingleCampaign, 1 );
      MetricDisplayerUI m2 = new MetricDisplayerUI( currentSingleCampaign, 2 );

      h.getChildren().addAll( m1,m2 );

      clearMainView();
      rootBorderPane.setCenter( h );
    } else {
      alertMessage( "Not implemented" );
    }
  }

  /**
   * Handler for graphing button
   */
  @FXML private void grapher() {
    if( isComparison == false ) {
      clearMainView();
      HBox v = new HBox();
      ChartBuilderUI chartBuilderUI1 = new ChartBuilderUI( currentSingleCampaign, 1 );
      ChartBuilderUI chartBuilderUI2 = new ChartBuilderUI( currentSingleCampaign, 2 );
      v.getChildren().addAll( chartBuilderUI1, chartBuilderUI2 );
      rootBorderPane.setCenter( v );
    } else {
      clearMainView();
      ChartBuilderUI c = new ChartBuilderUI( comparison );
      rootBorderPane.setCenter( c );
    }
  }

  /**
   * Open comparison of campaigns
   */
  @FXML private void openComparison() {
    clearMainView();

    comparison = new Comparison();

    VBox comparisonOpenRoot = new VBox();
    comparisonOpenRoot.setPadding( new Insets( 10 ) );
    comparisonOpenRoot.setSpacing( 30 );
    comparisonOpenRoot.setAlignment( Pos.CENTER );

    Text titleText = new Text( "Open Multiple Campaigns" );
    titleText.setStyle( "-fx-font-size: 30pt; -fx-fill: -fg-colour; -fx-font-weight: bold;" );
    titleText.setWrappingWidth( 500 );
    titleText.setTextAlignment( TextAlignment.CENTER );

    Button addButton = new Button( "Add Campaign" );
    addButton.setStyle( "-fx-font-size: 14pt; -fx-font-weight: bold;" );

    Text campaignsText = new Text( "" );
    campaignsText.getStyleClass().add( "textClassBold" );
    campaignsText.setTextAlignment( TextAlignment.CENTER );

    comparisonOpenRoot.getChildren().addAll( titleText, addButton, campaignsText );

    addButton.setOnAction( e -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle( "Open Campaign" );
      fileChooser.getExtensionFilters().add( new ExtensionFilter( "Zip files","*.zip" ) );
      fileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ) );
      File openedFile = fileChooser.showOpenDialog( rootBorderPane.getScene().getWindow() );
      if ( openedFile == null ) { //Will happen if the user closes the open dialog
        d( "No file loaded" );
        return;
      }
      Campaign c = new Campaign( openedFile.toPath() );

      if ( !c.isZipValid() ) {
        d( "Not loading..." );
        alertMessage( "Zip not valid" );
        return;
      }

      managedCampaigns.add( c );

      //Run campaign loading on another thread
      Task<Integer> t = new Task<Integer>() {
        @Override
        public Integer call() {
          c.load();
          return 0;
        }
      };

      t.setOnSucceeded( e1 -> {
        comparison.add( c, AgeFilter.ANY, GenderFilter.ANY, IncomeFilter.ANY, ContextFilter.ANY );
        isComparison = true; //Since we are loading a single campaign, we aren't doing a comparison
        campaignsText.setText( campaignsText.getText() + c.zipFilePath().getFileName().toString() + "\n" );
        unlockUI();

        Stage s = ( Stage ) rootBorderPane.getScene().getWindow();
        s.setOnCloseRequest( e2 -> handleClose( null ) );
      } );

      lockUI();
      new Thread( t ).start();
    } );



    rootBorderPane.setCenter( comparisonOpenRoot );

    /*
    VBox root = new VBox();

    HBox h = new HBox();
    h.setAlignment( Pos.CENTER );
    h.setSpacing( 50 );

    Button success = new Button( "Compare Campaigns" );
    success.setVisible( false );

    Campaign c1;
    Campaign c2;

    VBox v1 = new VBox();
    v1.setAlignment( Pos.CENTER );
    v1.setSpacing( 20 );
    Text v1Text = new Text( "Open campaign" );
    v1Text.getStyleClass().add( "textClassBold" );
    v1Text.setWrappingWidth( 150 );
    v1Text.setTextAlignment( TextAlignment.CENTER );
    Button b1 = new Button( "Open" );
    v1.getChildren().addAll( b1, v1Text );

    b1.setOnAction( e -> {
      //Create and show fileChooser to select the right file:
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle( "Open Campaign" );
      fileChooser.getExtensionFilters().add( new ExtensionFilter( "Zip files","*.zip" ) );
      fileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ) );
      File openedFile = fileChooser.showOpenDialog( rootBorderPane.getScene().getWindow() );

      if ( openedFile == null ) { //Will happen if the user closes the open dialog
        d( "No file loaded" );
        return;
      }

      Campaign c = new Campaign( openedFile.toPath() );

      if ( !c.isZipValid() ) {
        d( "Not loading..." );
        alertMessage( "Zip not valid" );
        return;
      }

      currentComparisonFirstCampaign = c; //Create new campaign handler for the opened file
      managedCampaigns.add( currentComparisonFirstCampaign );

      //Run campaign loading on another thread
      Task<Integer> t = new Task<Integer>() {
        @Override
        public Integer call() {
          lockUI();
          currentComparisonFirstCampaign.load();
          return 0;
        }
      };

      t.setOnSucceeded( e1 -> {
        unlockUI();
        if ( currentComparisonFirstCampaign != null && currentComparisonSecondCampaign != null ) {
          isComparison = true;
          success.setVisible( true );
        }
        v1Text.setText( currentComparisonFirstCampaign.zipFilePath().getFileName().toString() );
      } );

      lockUI();
      new Thread( t ).start();
    } );

    VBox v2 = new VBox();
    v2.setAlignment( Pos.CENTER );
    v2.setSpacing( 20 );
    Text v2Text = new Text( "Open campaign" );
    v2Text.getStyleClass().add( "textClassBold" );
    v2Text.setWrappingWidth( 150 );
    v2Text.setTextAlignment( TextAlignment.CENTER );
    Button b2 = new Button( "Open" );
    v2.getChildren().addAll( b2, v2Text );

    b2.setOnAction( e -> {
      //Create and show fileChooser to select the right file:
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle( "Open Campaign" );
      fileChooser.getExtensionFilters().add( new ExtensionFilter( "Zip files","*.zip" ) );
      fileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ) );
      File openedFile = fileChooser.showOpenDialog( rootBorderPane.getScene().getWindow() );

      if ( openedFile == null ) { //Will happen if the user closes the open dialog
        d( "No file loaded" );
        return;
      }

      Campaign c = new Campaign( openedFile.toPath() );

      if ( !c.isZipValid() ) {
        d( "Not loading..." );
        alertMessage( "Zip not valid" );
        return;
      }

      currentComparisonSecondCampaign = c; //Create new campaign handler for the opened file
      managedCampaigns.add( currentComparisonSecondCampaign );

      //Run campaign loading on another thread
      Task<Integer> t = new Task<Integer>() {
        @Override
        public Integer call() {
          lockUI();
          currentComparisonSecondCampaign.load();
          return 0;
        }
      };

      t.setOnSucceeded( e1 -> {
        unlockUI();
        if ( currentComparisonFirstCampaign != null && currentComparisonSecondCampaign != null ) {
          isComparison = true;
          success.setVisible( true );
        }
        v2Text.setText( currentComparisonSecondCampaign.zipFilePath().getFileName().toString() );
      } );

      lockUI();
      new Thread( t ).start();
    } );



    h.getChildren().addAll( v1,v2 );

    Text openText = new Text( "Compare Campaigns" );
    openText.setUnderline( true );
    openText.setStyle( " -fx-font-size: 30pt; -fx-fill: -fg-colour; -fx-font-weight: bold; " );

    root.getChildren().addAll( openText, h, success );
    root.setAlignment( Pos.CENTER );
    root.setPadding( new Insets( 50,0,0,0 ) );
    root.setSpacing( 40.0 );

    rootBorderPane.setCenter( root );
    */
  }

  /**
   * Save button handler saves main view to file
   */
  @FXML private void save() {
    if ( rootBorderPane.getCenter() == null )
      return; //Do nothing if there is nothing in the center
    FileChooser saveDialog = new FileChooser();
    saveDialog.setTitle( "Save Current View" );
    saveDialog.getExtensionFilters().addAll( new ExtensionFilter( "PNG Image", "*.png" ) );
    File file = saveDialog.showSaveDialog( rootBorderPane.getScene().getWindow() );
    //If the user selected a file, then save the main view
    if ( file != null ) {
      //If the user didn't add an extension, we need to add one:
      if ( !( file.toString().lastIndexOf( '.' ) > 0 ) ) {
        file = new File( file.toString() + ".png" );
      }
      WritableImage i = rootBorderPane.getCenter().snapshot( new SnapshotParameters(), null );
      try {
        ImageIO.write( SwingFXUtils.fromFXImage( i, null ), "png", file );
      } catch ( Exception e ) {
        e.printStackTrace();
      }
      d( "Saved view to: " + file.toString() );
    }
  }

  /**
   * Print button handler prints the graphs
   */
  @FXML private void print() {
    if( !isLightMode ) {
      alertMessage( "In order to print the page go to Settings -> Theme -> click 'Light Theme' and try again" );
      return;
    }
    PrinterJob printerJob = PrinterJob.createPrinterJob();
    if ( printerJob == null ) {
      alertMessage( "System printer not available" );
      return;
    }
    if ( ! printerJob.showPrintDialog( null ) ) {
      return;
    }
    WritableImage image = rootBorderPane.getCenter().snapshot( new SnapshotParameters(), null );
    // Scaling image to full page
    Printer printer = printerJob.getPrinter();
    Paper paper = printerJob.getJobSettings().getPageLayout().getPaper();
    PageLayout pageLayout = printer.createPageLayout( paper,PageOrientation.PORTRAIT,Printer.MarginType.DEFAULT );
    double x = pageLayout.getPrintableWidth() / image.getWidth();
    double y = pageLayout.getPrintableHeight() / image.getHeight();
    double scaleDms = Math.min( x, y );

    ImageView printNode = new ImageView( image );
    printNode.getTransforms().add( new Scale( scaleDms, scaleDms ) );

    if ( printerJob.printPage( printNode ) ) {
      printerJob.endJob();
    }
  }

  /**
   * Changes text size to small
   */
  @FXML private void smallText() {
    rootBorderPane.getStylesheets().remove( 0,1 );
    if( isLightMode ) {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode_small.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );

    } else {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode_small.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );

    }

    textSize = TextSize.SMALL;
    d( "Small text" );
  }

  /**
  * Changes text size to medium
  */
  @FXML private void mediumText() {
    rootBorderPane.getStylesheets().remove( 0,1 );
    if( isLightMode ) {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
    } else {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
    }

    textSize = TextSize.MEDIUM;
    d( "Medium text" );
  }

  /**
  * Changes text size to large
  */
  @FXML private void largeText() {
    rootBorderPane.getStylesheets().remove( 0,1 );
    if( isLightMode ) {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode_large.css" ).toString();
      rootBorderPane.getStylesheets().add( getClass().getClassLoader().getResource( "stylesheets/lightMode_large.css" ).toString() );
    } else {
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode_large.css" ).toString();
      rootBorderPane.getStylesheets().add( getClass().getClassLoader().getResource( "stylesheets/darkMode_large.css" ).toString() );
    }

    textSize = TextSize.LARGE;
    d( "Large text" );
  }

  /**
   * Enable light mode (default)
   */
  @FXML private void lightMode() {
    rootBorderPane.getStylesheets().remove( 0, 1 );

    switch ( textSize ) {
    case SMALL:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode_small.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    case MEDIUM:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    case LARGE:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/lightMode_large.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    }

    isLightMode = true;
    d( "Light mode" );
  }

  /**
   * Enable dark mode
   */
  @FXML private void darkMode() {
    rootBorderPane.getStylesheets().remove( 0, 1 );

    switch ( textSize ) {
    case SMALL:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode_small.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    case MEDIUM:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    case LARGE:
      currentStyleSheet = getClass().getClassLoader().getResource( "stylesheets/darkMode_large.css" ).toString();
      rootBorderPane.getStylesheets().add( currentStyleSheet );
      break;
    }

    isLightMode = false;
    d( "Dark mode" );
  }

  /**
  * Handle an update to the bounce definition setting
  */
  @FXML private void bounceDefnHandler() {
    if ( bounceSinglePage.isSelected() ) {
      ServerLog.definitionOfBounce = BounceDef.SINGLE_PAGE_VIEW;
      bounceTimeSpinner.setDisable( true );
      d( "Set bounce to single page view" );
    } else {
      bounceTimeSpinner.setDisable( false );
      ServerLog.definitionOfBounce = BounceDef.TIME_SPENT;
      ServerLog.bounceTime = bounceTimeSpinner.getValue();
      d( "Set bounce to time: " + bounceTimeSpinner.getValue().toString() );
    }
  }

  /**
  * Lock left buttons and show loading indicator
  */
  private void lockUI() {
    loadingIndicator.setVisible( true );
    leftButtons.forEach( ( b ) -> b.setDisable( true ) );
    if ( rootBorderPane.getCenter() != null )
      rootBorderPane.getCenter().setDisable( true );
  }

  /**
   * Unlock left buttons and hide loading indicator
   */
  private void unlockUI() {
    loadingIndicator.setVisible( false );
    leftButtons.forEach( ( b ) -> b.setDisable( false ) );
    if ( rootBorderPane.getCenter() != null )
      rootBorderPane.getCenter().setDisable( false );
    if ( isComparison )
      leftButtons.get( 1 ).setDisable( true );
  }

  /**
   * Send an alert message to the user
   * @param message String to send
   */
  private void alertMessage( String message ) {
    Alert a = new Alert( Alert.AlertType.ERROR );
    a.setContentText( message );
    a.showAndWait();
  }

  /**
  * Print debug message for controller
  * @param args Thing to print to debug
  */
  public static void d( Object... args ) {
    if ( DEBUG ) {
      System.out.print( "[\u001B[34mController\u001B[0m] " );
      System.out.println( args[0] );
    }
  }

  /**
   * Clears the main view
   */
  private void clearMainView() {
    d( "Main view cleared" );
    rootBorderPane.setCenter( null );
  }

  /**
   * Gracefully close all campaigns before exiting
   */
  @FXML private void handleClose( ActionEvent e ) {
    d( "Application exiting" );
    for ( var c : managedCampaigns ) {
      c.close();
    }
    Platform.exit();
  }
}