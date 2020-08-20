"""
Usage: visualizer.py

Visualize the classified data as histogram and line graph with min/max/mean/std/availability information
"""
import matplotlib.pyplot as plt
import matplotlib.dates as dates
import numpy as np
import pandas as pd
import os

from datetime import datetime
from datetime import timedelta
from datetime import timezone

"""
Deviation distribution zone
"""
HIGH_CONFIDENCE_THRESHOLD = 5
LOW_CONFIDENCE_THRESHOLD = 10

class Visualizer:
    """
    Classify the deviation of distance and visualize the deviations of distance/speed/altitude
    """
    def __init__(self):
        current_directory = os.path.dirname(__file__)
        current_time = datetime.strftime(datetime.now(), "%Y-%m-%dT%H%M%S")
        self.output_file_folder = os.path.join(current_directory, current_time)
        os.mkdir(self.output_file_folder)

    def get_min_deviation(self, data):
        """
        Get the min value of deviation

        Args:
           data: the deviation data
        """
        return min(deviation for deviation in data)

    def get_max_deviation(self, data):
        """
        Get the max value of deviation

        Args:
           data: the deviation data
        """
        return max(deviation for deviation in data)

    def classify_deviation(self, deviation_dataframe):
        """
        Classify the deviation of distance according to its absolute value, and mark the data confidence (1, 2, 3). 
        Higher score means higher confidence and accuracy.

        Args:
          deviation_dataframe: a dataframe containing time and deviation of distance/speed/altitude 

        Returns:
          A dataframe after distance deviation classified with confidence
        """
        deviation_list = deviation_dataframe["Deviations"]
        confidence = []

        for deviation in deviation_list:
            abs_deviation = abs(deviation)
            if abs_deviation <= HIGH_CONFIDENCE_THRESHOLD:
                confidence.append(3)
            elif abs_deviation <= LOW_CONFIDENCE_THRESHOLD:
                confidence.append(2)
            else:
                confidence.append(1)

        deviation_dataframe["Confidence"] = confidence 

        return deviation_dataframe


    def draw_hist_graph(self, data, x_label, y_label, title, availability):
        """
        Draw the histogram graph and save it as a png file

        Args:
          data: data on y axis
          x_label: label for x axis
          y_label: label for y axis
          title: title for the graph
          availability: percentile of captured datapoints
        """
        # Plot the data
        fig = plt.figure(figsize=(20,10))
        hist_label = "Availability: {}%".format(availability)
        plt.hist(data, align='mid', bins=[0.5,1.5,2.5,3.5], rwidth=0.8, label=hist_label, orientation="horizontal", color='cornflowerblue')
        
        # Set the title and labels    
        plt.legend(loc="upper left")
        plt.xlabel(x_label, fontsize=10)
        plt.ylabel(y_label, fontsize=10)
        plt.title(title, fontsize=12)
        plt.yticks(range(0,5))

        # Save the graph as a png picture
        my_file = "{}_Deviation_Confidence_{}.png".format(title, datetime.strftime(datetime.now(), "%Y-%m-%dT%H%M%S"))
        fig.savefig(os.path.join(self.output_file_folder, my_file)) 


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
        # Get the absolute mean of deviation, stadard deviation, min and max deviation
        abs_mean_deviation = round(np.mean(y_data),3)
        std_deviation = round(np.std(y_data),3)
        min_deviation = round(self.get_min_deviation(y_data), 3)
        max_deviation = round(self.get_max_deviation(y_data), 3)

        # Get the time duration 
        time_duration = x_data[len(x_data)-1] - x_data[0]

        # Set the line_label and x_label
        line_label = "Mean: {}\nSTD: {}\nMin: {}\nMax: {}".format(abs_mean_deviation, std_deviation, min_deviation, max_deviation)
        x_label += str(time_duration)

        # Plot the data
        fig = plt.figure(figsize=(20,10))
        ax = plt.subplot()
        ax.plot(x_data, y_data, color='cornflowerblue', label= line_label)

        # Format the time on x axis '%H:%M:%S'
        ax.xaxis.set_major_formatter(dates.DateFormatter('%H:%M:%S'))

        # Set the title and labels
        plt.legend(loc="upper left")
        plt.title(title +" Deviation", fontsize = 12)
        plt.xlabel(x_label, fontsize = 10)
        plt.ylabel(y_label, fontsize = 10)

        # Save the graph as a png picture
        my_file = "{}_Deviation_{}.png".format(title, datetime.strftime(datetime.now(), "%Y-%m-%dT%H%M%S"))
        fig.savefig(os.path.join(self.output_file_folder, my_file))


    def draw_lines_graph(self, x_data, x_label, y1_data, y2_data, y_label, title, label_1, label_2):
         # Get the time duration 
        time_duration = x_data[len(x_data)-1] - x_data[0]
        x_label += str(time_duration)

         # Plot the data
        fig = plt.figure(figsize=(20,10))
        ax = plt.subplot()
        ax.plot(x_data, y1_data, color='cornflowerblue', label=label_1)
        ax.plot(x_data, y2_data, color='forestgreen', label=label_2)

        # Format the time on x axis '%H:%M:%S'
        ax.xaxis.set_major_formatter(dates.DateFormatter('%H:%M:%S'))

        # Set the title and labels
        plt.legend(loc="upper left")
        plt.title(title, fontsize = 12)
        plt.xlabel(x_label, fontsize = 10)
        plt.ylabel(y_label, fontsize = 10)

        # Save the graph as a png picture
        my_file = "{}_Deviation_{}.png".format(title, datetime.strftime(datetime.now(), "%Y-%m-%dT%H%M%S"))
        fig.savefig(os.path.join(self.output_file_folder, my_file))

