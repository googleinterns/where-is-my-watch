# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Handles Calculations on pairs of GPS data sets.

Usage:
    gps_fileparser = FileParser()
    phone_data_set = gps_fileparser.parse_file("<file_path>/2020-07-21T19:48:44.697Z.xml")
    simulator_data_set =  gps_fileparser.parse_file("<file_path>/GPSSIM-2020-07-21_19:49:31.csv")
    downsampled_list = simulator_data_set[0].gps_data_list[::10]
    simulator_data_set[0].gps_data_list = downsampled_list
    calculator = DataSetDeviationCalculator(phone_data_set, simulator_data_set[0])
    devation_dataframe = calculator.get_deviation_dataframe()
"""
from datetime import datetime, timedelta
from datetime import timezone
import time

import numpy as np
import pandas as pd

from GpsDataAnalyzer import utils
from GpsDataAnalyzer.fileparser.fileparser import FileParser
from GpsDataAnalyzer.calculator import alignment_algorithms


class DataSetDeviationCalculator:
    """An object for Calculating Deviations on two data sets.

    Attributes:
        data_set_1: GpsDataSet
        data_set_2: GpsDataSet
        starting_time_1: Datetime, offset included start time for 1st set
        starting_time_2: Datetime, offset included start time for 2nd set
        ending_time_1: Datetime, offset included end time for 1st set
        ending_time_2: Datetime, offset included end time for 2nd set
        offset_mapping_1: Dictionary, {DateTime: [GpsData, ], ...}
        offset_mapping_2: Dictionary, {DateTime: [GpsData, ], ...}
        deviations_dataframe: Pandas Dataframe that holds values after calculation
    """
    def __init__(self, data_set_1, data_set_2):
        self.data_set_1 = data_set_1
        self.data_set_2 = data_set_2
        self.starting_time_1 = None
        self.starting_time_2 = None
        self.ending_time_1 = None
        self.ending_time_2 = None
        self.offset_mapping_1= {}
        self.offset_mapping_2 = {}
        self.deviations_dataframe = None
        self.availability = None

        start = time.perf_counter()
        print("Optimized lineup implementation:")
        self.starting_time_1, self.starting_time_2 = alignment_algorithms.find_lineup(self.data_set_1,
                                                                                      self.data_set_2)
        end = time.perf_counter()
        print(f"Lined up data in {end - start:0.4f} seconds")
        print("start time 1: " + str(self.starting_time_1))
        print("start time 2: " + str(self.starting_time_2))
        print("\n")

        self.ending_time_1 = self.data_set_1.gps_meta_data.end_time
        self.ending_time_2 = self.data_set_2.gps_meta_data.end_time
        if not self.starting_time_1 and not self.starting_time_2:
            self.offset_mapping_1 = alignment_algorithms.create_time_to_points_mapping(self.data_set_1, 0)
            self.offset_mapping_2 = alignment_algorithms.create_time_to_points_mapping(self.data_set_2, 0)
        elif self.data_set_1.gps_data_list[0].time > self.data_set_2.gps_data_list[0].time:
            offset = (self.starting_time_1-self.starting_time_2).total_seconds()
            self.offset_mapping_1 = alignment_algorithms.create_time_to_points_mapping(self.data_set_1, 0)
            self.offset_mapping_2 = alignment_algorithms.create_time_to_points_mapping(self.data_set_2, offset)
            self.ending_time_2 = self.ending_time_2 + timedelta(seconds=offset)
        else:
            offset = (self.starting_time_2-self.starting_time_1).total_seconds()
            self.offset_mapping_1 = alignment_algorithms.create_time_to_points_mapping(self.data_set_1, offset)
            self.offset_mapping_2 = alignment_algorithms.create_time_to_points_mapping(self.data_set_2, 0)
            self.ending_time_1 = self.ending_time_1 + timedelta(seconds=offset)

    def get_deviation_dataframe(self):
        """
        Extracts and returns deviation for each valid timestamp & other information.

        Returns:
            A pandas dataframe including the shared timestamp with the offset included,
            the deviations of lat/lon, the difference in speed, the difference in
            altitude, and the original timestamps for each set
        """
        if self.deviations_dataframe is not None: 
            return self.deviations_dataframe

        time_list, distance_deviation_list, speed_deviation_list, altitude_deviation_list= [], [], [], []
        set1_time_list, set2_time_list = [], []
        set1_average_signal_list, set2_average_signal_list, signal_deviation_list = [], [], []

        for timestamp in self.offset_mapping_1:
            if timestamp in self.offset_mapping_2:
                time_list.append(timestamp)

                # Get the mapping pair of data points in each dataset
                point1 = self.offset_mapping_1[timestamp][0]
                point2 = self.offset_mapping_2[timestamp][0]

                # Calculate the distance deviation
                location1 = (point1.latitude, point1.longitude)
                location2 = (point2.latitude, point2.longitude)
                distance_deviation_list.append(utils.calculate_distance(location1, location2))

                # Calculate the speed differentials
                speed_deviation_list.append(point2.speed - point1.speed)

                # Calculate the altitude differentials
                if point1.altitude is None or point2.altitude is None:
                    altitude_deviation_list.append(None)
                else:
                    altitude_deviation_list.append(point2.altitude - point1.altitude)

                # Append the original timestamp in each dataset
                set1_time_list.append(point1.time)
                set2_time_list.append(point2.time)

                # Append the average signal if have
                set1_average_signal_list.append(point1.average_signal)
                set2_average_signal_list.append(point2.average_signal)
                signal_deviation_list.append(point2.average_signal - point1.average_signal)


        self.deviations_dataframe = pd.DataFrame({"Common Timestamp": time_list,
                                                  "Distance Deviations": distance_deviation_list,
                                                  "Speed Deviations": speed_deviation_list,
                                                  "Altitude Deviations": altitude_deviation_list,
                                                  "Set 1 Timestamp": set1_time_list,
                                                  "Set 2 Timestamp": set2_time_list,
                                                  "Set 1 Average Signal": set1_average_signal_list,
                                                  "Set 2 Average Signal": set2_average_signal_list,
                                                  "Signal Deviations": signal_deviation_list})
        return self.deviations_dataframe


    def get_availability(self):
        """
        Calculate the availability of wear captured gps data

        Returns:
            Percentile of wear captured gps data by compared gpsdataset
        """
        if self.availability:
            return self.availability
        if not self.starting_time_1 and not self.starting_time_2:
            return 0
        total_timestamps = 0
        available_timestamps = 0

        start_time = utils.round_time(max(self.starting_time_1, self.starting_time_2))
        end_time = utils.round_time(min(self.ending_time_1, self.ending_time_2))
        total_seconds = int((end_time-start_time).total_seconds())

        for timestamp in [start_time + timedelta(seconds=x) for x in range(total_seconds)]:
            if timestamp in self.offset_mapping_1 and timestamp in self.offset_mapping_2:
                available_timestamps += 1

        return round(available_timestamps / total_seconds, 4)*100

