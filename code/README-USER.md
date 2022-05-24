# Ad Dashboard User Guide 

### Running the `jar`
- To run the program, simply enter the following into a command prompt:
```sh
java -jar path/to/auctionboard.jar
```

### Loading a Single Campaign
- To load a campaign, you can either drag and drop the campaign into the window, or select "Load -> Single" in the menu bar
- Alternatively you can just use the shortcut Ctrl-O
- You should automatically be redirected to the home screen once the campaign has loaded

<img src="images/3f4fb00ee01bd7cd8d33e98310d25517cdbca524c0b3009d8a88353be77a3880.png" alt="" width="400" style="border:1px solid black;"/>


### Home Screen (Single)
- The home button on the left will take you to an overview of the campaign
- On the left is a table of the key metrics of the campaign (with no filtering applied)
- On the right is a histogram of the click costs
  - This chart gives a good overview of both the number of clicks and their costs

<img src="images/aca051f18f67e7161f244b28277c59071939c4484f5338ec741efba9e6a49525.png" alt="" width="400" style="border:1px solid black;"/>

### Detailed Metrics Screen
- The info button on the left will open the detailed metrics screen 
- This allows you to calculate metrics based on filters that you define
- Choose any of the four filters from the left that you want to look at, then click "Update Metrics" to update the metrics table on the right
- You can also compare metric values with different filters on them 
- Clicking on the rightmost double-arrow icon will reveal a second metrics interface where you can once again choose the filters you want to see
- This will then create a second table for a side-by-side metric comparison
- Clicking on any of the double-arrow icons again will hide the filter interface if all you want to view are the metrics tables
- To hide the filter selection boxes, click the "<<" button in the top left of each pane; you can then expand them out again to change the filters

<img src="images/dac2949b1d08513a1e8519d0b75a1667dc848da684e70a4d36c0d062c7fdbc2b.png" alt="" width="400" style="border:1px solid black;"/>


### Graph Creator Screen
- Clicking the graph button on the left will open the graph builder screen
- First select the metric to build a graph of
- Then you can filter by the four main filters, as well as by date range
- Finally, you can adjust the time step to increase or reduce the granularity of the graph

- You can also view a second graph if you want to compare filtered metrics
- Clicking on the double-arrow icon below "Graph 2" will reveal a second graph-creator interface where you can create another graph for side-by-side comparisons
- Clicking on any of the double-arrow icons again will hide the creator interface

<img src="images/ccc65d01e6aef0d08ab1f82d628c829d4b2245e208019cb4876a6ac7c78e4eed.png" alt="" width="600" style="border:1px solid black;"/>


### Saving Graphs
- Clicking the save button will allow you to save the current view of the application to a file
- This allows you to save graphs, as well as save any other info you might deem important

<img src="images/4063d9aecbb3eba88fa4b33e0ffd257f4019f3193309aaf7491e5561af7ade73.png" alt="" width="600" style="border:1px solid black;"/>

- The image saved will look like:

<img src="images/image.png" alt="" width="500" style="border:1px solid black;"/>

### Printing Graphs
- Clicking the print button will open a dialog box for the user to print the current view of the application
- Note: this will only work if the user has already connected to a printer

### Loading Multiple Campaigns
- Select "Load -> Comparison" from the menu bar
- Add as many campaigns as you would like to view by continuously clicking the "Add Campaign" button
- Once you have loaded all your campaigns, click on the Home button to view a graph with different lines representing the different campaigns
- You can also go to the graph creator screen to make comparisons for each graph
  - Click on each campaign button and adjust the filters as required
  - Choose the metric graph type and the date range
  - Click "Update Chart" to view the graph with different lines representing the different campaigns

<img src="images/39ad4d70e211e480bc07ddd21317e1a268021469f5825e9d26d5dd9370baceaa.png" alt="" width="500" style="border:1px solid black;"/>

### Other Features
- The top menu bar of the dashboard also allows for a couple of other functionalities
- The settings bar can be clicked to allow the user to change to light/dark mode and to change the sizes of all the text in the dashboard
- The user can also define what they want a bounce to mean. It can be defined as when only a single page is viewed, or depending on how long the page is viewed for.
