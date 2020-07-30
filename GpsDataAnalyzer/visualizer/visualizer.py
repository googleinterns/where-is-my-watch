"""
Usage: visualizer.py

Parse the xml or csv file and generate a GpsDataSet
"""
import matplotlib.pyplot as plt
import matplotlib.dates as dates
import numpy as np
import seaborn as sns
import pandas as pd

from datetime import datetime
from datetime import timedelta
from datetime import timezone

from GpsDataAnalyzer.datamodel.deviationresult import Deviation

class Visualizer:
	"""
	Classify the deivation of distance and visualize the deviations of distance/speed/altitude
	"""
	def _classify_deviation(self, deviation_dataframe):
		"""
		Classify the deviation of distance according to its absolute value, and mark the data confidence (1, 2, 3). 
		Higher score means higher confidence and acurancy.

		Args:
		  deviation_dataframe: a dataframe constaining time and deviation of distance/speed/altitude 

		Returns:
		  A dataframe with confidence
		"""
		deviation_list = deviation_dataframe["Deviation"]
        confidence = []

        for deviation in deviation_list:
            abs_deviation = abs(deviation)
            if abs_deviation <= 5:
                confidence.append(3)
            elif abs_deviation > 5 and abs_deviation <= 10:
                confidence.append(2)
            else:
                confidence.append(1)

        deviation_dataframe["Confidence"] = confidence 

        return deviation_dataframe


    def draw_hist_graph(self, data, x_label, y_label, title):
    	"""
    	Draw the histogram graph and save it as a png file

    	Args:
    	  data: data on y axis
    	  x_label: label for x axis
    	  y_label: label for y axis
    	  title: title for the graph
    	"""
        fig = plt.figure(figsize=(20,10))
        plt.hist(data, align='mid', bins=[0.5,1.5,2.5,3.5], rwidth=0.8, label="Count", orientation="horizontal")
            
        plt.legend(loc="lower right")
        plt.xlabel(x_label)
        plt.ylabel(y_label)
        plt.title(title);

        plt.yticks(range(0,5))
        plt.xticks(fontsize = 8)
        plt.yticks(fontsize = 8)
        fig.savefig(title + "_" + datetime.strftime(datetime.now(), "%Y-%m-%dT%H:%M:%S") + ".png") 


    def draw_line_graph(self, x_data, x_label, y_data, y_label, title):
    	"""
    	Draw the line graph and save it as a png file

    	Args:
    	  x_data: data on x axis
    	  x_label: label for x axis
    	  y_data: data on y axis
    	  y_label: label for y axis
    	  title: title for the graph
    	"""
    	# Get the absolute mean of deviation and stadard deviation
    	abs_mean_deviation = round(np.mean(x_data),3)
    	std_deviation = round(np.std(x_data),3)

    	# Create the absolute mean of deviation and stadard deviation label
    	line_label = 'Mean: '+str(distance_mean_deviation) + '\n' +'STD: ' + str(distance_std_deviation)

    	# Plot the data
    	fig = plt.figure(figsize=(20,10))
    	ax = plt.subplot()
        ax.plot(x_data, y_data, label= line_label)

        # Format the time on x axis '%H:%M:%S'
        ax.xaxis.set_major_formatter(dates.DateFormatter('%H:%M:%S'))

        # Set the title and labels
        plt.title(title, fontsize = 12)
        plt.xlabel(x_label, fontsize = 10)
        plt.ylabel(y_label, fontsize = 10)

        # Save the graph as a png picture
        fig.savefig(title + "_" + datetime.strftime(datetime.now(), "%Y-%m-%dT%H:%M:%S") + ".png")

