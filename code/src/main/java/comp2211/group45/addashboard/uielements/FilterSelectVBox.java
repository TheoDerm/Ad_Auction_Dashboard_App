package comp2211.group45.addashboard.uielements;

import comp2211.group45.addashboard.utils.AgeFilter;
import comp2211.group45.addashboard.utils.ContextFilter;
import comp2211.group45.addashboard.utils.GenderFilter;
import comp2211.group45.addashboard.utils.IncomeFilter;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class FilterSelectVBox extends VBox {
  String typeFace = "System Bold";
  double fontSize = 14.0;
  public ChoiceBox<AgeFilter> ageFilterSel;
  public ChoiceBox<GenderFilter> genderFilterSel;
  public ChoiceBox<IncomeFilter> incomeFilterSel;
  public ChoiceBox<ContextFilter> contextFilterSel;

  public FilterSelectVBox() {
    Text age = new Text( "Age:" );
    age.getStyleClass().add( "textClassBold" );
    age.underlineProperty().set( true );
    getChildren().add( age );

    ageFilterSel = new ChoiceBox<AgeFilter>();
    ageFilterSel.getItems().addAll( AgeFilter.ANY,
                                    AgeFilter.LT_TWENTY_FIVE,
                                    AgeFilter.TWENTY_FIVE_TO_THIRTY_FOUR,
                                    AgeFilter.THIRTY_FIVE_TO_FORTY_FOUR,
                                    AgeFilter.FORTY_FIVE_TO_FIFTY_FOUR,
                                    AgeFilter.GT_FIFTY_FOUR );
    ageFilterSel.setValue( AgeFilter.ANY );
    ageFilterSel.setMinWidth( 160.0 );
    ageFilterSel.getStyleClass().add( "textClass" );
    getChildren().add( ageFilterSel );

    Text gender = new Text( "Gender:" );
    gender.getStyleClass().add( "textClassBold" );
    gender.underlineProperty().set( true );
    getChildren().add( gender );

    genderFilterSel = new ChoiceBox<GenderFilter>();
    genderFilterSel.getItems().addAll( GenderFilter.ANY,
                                       GenderFilter.MALE,
                                       GenderFilter.FEMALE );
    genderFilterSel.setValue( GenderFilter.ANY );
    genderFilterSel.setMinWidth( 160.0 );
    genderFilterSel.getStyleClass().add( "textClass" );
    getChildren().add( genderFilterSel );

    Text income = new Text( "Income:" );
    income.getStyleClass().add( "textClassBold" );
    income.underlineProperty().set( true );
    getChildren().add( income );

    incomeFilterSel = new ChoiceBox<IncomeFilter>();
    incomeFilterSel.getItems().addAll( IncomeFilter.ANY,
                                       IncomeFilter.LOW,
                                       IncomeFilter.MEDIUM,
                                       IncomeFilter.HIGH );
    incomeFilterSel.setValue( IncomeFilter.ANY );
    incomeFilterSel.setMinWidth( 160.0 );
    incomeFilterSel.getStyleClass().add( "textClass" );
    getChildren().add( incomeFilterSel );

    Text context = new Text( "Context:" );
    context.getStyleClass().add( "textClassBold" );
    context.underlineProperty().set( true );
    getChildren().add( context );

    contextFilterSel = new ChoiceBox<ContextFilter>();
    contextFilterSel.getItems().addAll( ContextFilter.ANY,
                                        ContextFilter.BLOG,
                                        ContextFilter.HOBBIES,
                                        ContextFilter.NEWS,
                                        ContextFilter.SHOPPING,
                                        ContextFilter.SOC_MED,
                                        ContextFilter.TRAVEL );
    contextFilterSel.setValue( ContextFilter.ANY );
    contextFilterSel.setMinWidth( 160.0 );
    contextFilterSel.getStyleClass().add( "textClass" );
    getChildren().add( contextFilterSel );

    setSpacing( 6 );
  }

  public AgeFilter getAgeFilter() {
    return ageFilterSel.getValue();
  }

  public GenderFilter getGenderFilter() {
    return genderFilterSel.getValue();
  }

  public IncomeFilter getIncomeFilter() {
    return incomeFilterSel.getValue();
  }

  public ContextFilter getContextFilter() {
    return contextFilterSel.getValue();
  }
}