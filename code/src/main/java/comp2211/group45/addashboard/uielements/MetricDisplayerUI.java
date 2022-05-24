package comp2211.group45.addashboard.uielements;

import comp2211.group45.addashboard.Comparison;
import comp2211.group45.addashboard.campaign.Campaign;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MetricDisplayerUI extends HBox {

  public MetricDisplayerUI( Campaign c, int i ) {
    setSpacing( 10 );
    setPadding( new Insets( 6,6,6,6 ) );

    VBox mainOuter = new VBox();

    VBox main = new VBox();
    main.setAlignment( Pos.TOP_CENTER );
    main.setSpacing( 10 );

    FilterSelectVBox filters = new FilterSelectVBox();

    Button update = new Button( "Update metrics" );
    update.getStyleClass().add( "textClass" );

    main.getChildren().addAll( filters, update );

    getChildren().add( mainOuter );

    Button hide = new Button( i == 1 ? "<<" : ">>" );
    hide.setMinWidth( 60 );
    hide.getStyleClass().add( "textClassBold" );
    hide.setStyle( "-fx-background-color: transparent" );

    mainOuter.getChildren().addAll( hide, main );

    if ( i != 1 ) {
      mainOuter.getChildren().remove( 1,2 );
    }

    hide.setOnAction( e -> {
      if ( mainOuter.getChildren().size() == 2 ) {
        mainOuter.getChildren().remove( 1,2 );
        hide.setText( ">>" );
      } else {
        mainOuter.getChildren().addAll( main );
        hide.setText( "<<" );
      }
    } );

    update.setOnAction( e -> {
      if ( getChildren().size() > 1 )
        getChildren().remove( 1, 2 );
      getChildren().add( new CampaignMetricsTable( c,
                         filters.getAgeFilter(),
                         filters.getGenderFilter(),
                         filters.getIncomeFilter(),
                         filters.getContextFilter() ) );
    } );
  }

  public MetricDisplayerUI( Comparison c ) {
  }
}
