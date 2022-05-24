package comp2211.group45.addashboard.uielements;

import static java.lang.String.*;

import comp2211.group45.addashboard.campaign.Campaign;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import comp2211.group45.addashboard.utils.*;

public class CampaignMetricsTable extends TableView<CampaignMetricsTable.Pair> {

  public static class Pair {
    private SimpleStringProperty left;
    private SimpleStringProperty right;

    public Pair( String leftStr, String rightStr ) {
      left = new SimpleStringProperty( leftStr );
      right = new SimpleStringProperty( rightStr );
    }

    public String getLeft() {
      return left.get();
    }

    public String getRight() {
      return right.get();
    }

    public void setLeft( String text ) {
      left.set( text );
    }

    public void setRight( String text ) {
      right.set( text );
    }
  }

  private ObservableList<Pair> data;

  public CampaignMetricsTable( Campaign c, AgeFilter ageFilter, GenderFilter genderFilter, IncomeFilter incomeFilter, ContextFilter contextFilter ) {
    setEditable( false );

    var impression = new Pair( Metric.IMPRESSION_COUNT.toString(), format( "%,d", c.impressionCount( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var click = new Pair( Metric.CLICK_COUNT.toString(), format( "%,d", c.clickCount( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var unique = new Pair( Metric.UNIQUE_CLICK_COUNT.toString(), format( "%,d", c.uniqueClickCount( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var bounce = new Pair( Metric.BOUNCE_COUNT.toString(), format( "%,d", c.bounceCount( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var conversion = new Pair( Metric.CONVERSION_COUNT.toString(), format( "%,d", c.conversionCount( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var total = new Pair( Metric.TOTAL_COST.toString(), format( "£%.2f", c.totalCost( ageFilter, genderFilter, incomeFilter, contextFilter ) / 100 ) );
    var ctr = new Pair( Metric.CTR.toString(), format( "%.3f", c.ctr( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var cpa = new Pair( Metric.CPA.toString(), format( "%.3fp", c.cpa( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var cpc = new Pair( Metric.CPC.toString(), format( "%.3fp", c.costPerClick( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var cpm = new Pair( Metric.CPM.toString(), format( "%.3fp", c.cpm( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );
    var bounceRate = new Pair( Metric.BOUNCE_RATE.toString(), format( "%.3f", c.bounceRate( ageFilter, genderFilter, incomeFilter, contextFilter ) ) );

    data = FXCollections.observableArrayList(
             impression, click, unique, bounce, conversion,
             total, ctr, cpa, cpc, cpm, bounceRate
           );

    var columns = getColumns();

    var keyColumn = new TableColumn<Pair,String>( "Metric" );
    keyColumn.setCellValueFactory(
      new PropertyValueFactory<Pair, String>( "left" ) );
    keyColumn.getStyleClass().add( "textClassBold" );

    var valueColumn = new TableColumn<Pair,String>( "Value" );
    valueColumn.setCellValueFactory(
      new PropertyValueFactory<Pair, String>( "right" ) );
    valueColumn.getStyleClass().add( "textClass" );

    setItems( data );
    columns.add( keyColumn );
    columns.add( valueColumn );
    setPrefWidth( 10000.0 );
  }
}
