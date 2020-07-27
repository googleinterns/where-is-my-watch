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

from fileparser import utils
from fileparser.fileparser import FileParser
import alignment_algorithms


class DataSetDeviationCalculator:
  """An object for Calculating Deviations on two data sets.

  Attributes:
    data_set_1: GpsDataSet
    data_set_2: GpsDataSet
    starting_time_1: Datetime, offset included start time for 1st set
    starting_time_2: Datetime, offset included start time for 2nd set
    offset_time_point_mapping: Dictionary, {DateTime: {"set1": GpsData, "set2": GpsData}, ...}
    deviations_dataframe: Pandas Dataframe that holds values after calculation
  """
  def __init__(self, data_set_1, data_set_2):
    self.data_set_1 = data_set_1
    self.data_set_2 = data_set_2
    self.starting_time_1 = None
    self.starting_time_2 = None
    self.offset_time_point_mapping = {}
    self.deviations_dataframe = None

    # comparing both algorithms for deciding offset
    start = time.perf_counter()
    print("Unoptimized lineup implementation:")
    self.starting_time_1, self.starting_time_2 = alignment_algorithms.find_lineup_no_optimization(self.data_set_1,
                                                                    self.data_set_2)
    end = time.perf_counter()
    print(f"Lined up data in {end - start:0.4f} seconds")
    print("start time 1: " + str(self.starting_time_1))
    print("start time 2: " + str(self.starting_time_2))
    print("\n")

    start = time.perf_counter()
    print("Optimized lineup implementation:")
    self.starting_time_1, self.starting_time_2 = alignment_algorithms.find_lineup(self.data_set_1,
                                                                                  self.data_set_2)
    end = time.perf_counter()
    print(f"Lined up data in {end - start:0.4f} seconds")
    print("start time 1: " + str(self.starting_time_1))
    print("start time 2: " + str(self.starting_time_2))
    print("\n")

    if self.data_set_1.gps_data_list[0].time > self.data_set_2.gps_data_list[0].time:
      offset = (self.starting_time_2-self.starting_time_1).total_seconds()
      self.offset_time_point_mapping = self.create_time_to_point_mapping(self.data_set_1, self.data_set_2, offset, 0)
    else:
      offset = (self.starting_time_1-self.starting_time_2).total_seconds()
      self.offset_time_point_mapping = self.create_time_to_point_mapping(self.data_set_1, self.data_set_2, 0, offset)

  def get_deviation_dataframe(self):
    if self.deviations_dataframe: 
      return self.deviations_dataframe
    else:
      time_list, deviation_list, speed_differentials, altitude_differentials = [], [], [], []
      set1_time_list, set2_time_list = [], []
      time_point_mapping = self.offset_time_point_mapping

      for timestamp in time_point_mapping:
        entry = time_point_mapping[timestamp]
        if len(entry) == 2:
          time_list.append(timestamp)

          point1 = time_point_mapping[timestamp]["set1"]
          point2 = time_point_mapping[timestamp]["set2"]
          location1 = (point1.latitude, point1.longitude)
          location2 = (point2.latitude, point2.longitude)
          deviation_list.append(utils.calculate_distance(location1, location2))

          speed_differentials.append(point2.speed - point1.speed)
          altitude_differentials.append(point2.altitude - point1.altitude)
          set1_time_list.append(point1.time)
          set2_time_list.append(point2.time)

      self.deviations_dataframe = pd.DataFrame({"Common Timestamp": time_list,
                                                "Deviations": deviation_list,
                                                "Speed Differentials": speed_differentials,
                                                "Altitude Differentials": altitude_differentials,
                                                "Set 1 Timestamp": set1_time_list,
                                                "Set 2 Timestamp": set2_time_list})
      return self.deviations_dataframe


  def create_time_to_point_mapping(self, set1, set2, offset1, offset2):
    time_point_mapping = {}
    for point in set1.gps_data_list:
      rounded_time = round_time(point.time) + timedelta(seconds=offset1)
      try:
        time_point_mapping[rounded_time]["set1"] = point
      except KeyError:
        time_point_mapping[rounded_time] = {"set1":point}
    for point in set2.gps_data_list:
      rounded_time = round_time(point.time) + timedelta(seconds=offset2)
      try:
        time_point_mapping[rounded_time]["set2"] = point
      except KeyError:
        time_point_mapping[rounded_time] = {"set2":point}
    return time_point_mapping

def round_time(time):
  if time.microsecond >= 500000:
    time = time + timedelta(seconds=1)
  time = time.replace(microsecond=0)
  return time


