from datetime import datetime, timedelta
import numpy as np
import time
import pprint

import utils

def find_lineup(set1, set2):
  """
  Optimized algorithm to find the approximate offset between two GPS data sets.

  This algorithm first identifies a primary data set and a secondary one based
  on which starts later. The offset is then applied to the secondary data set.
  After mapping the timestamps to the corresponding points from each set, it
  finds the range that has the least amount of invalid timestamps (has points
  from only one set or neither) and then finds the optimal offset using the
  middle of that range as the starting point.

  Args:
    set1: GpsDataSet object
    set2: GpsDataSet object

  Returns:
    Tuple of two datetimes, the start time for set 1 and for set 2 given the
    calculated offset.
  """

  # find primary data set (the one with the later start time)
  set1_start_time = set1.gps_data_list[0].time
  set2_start_time = set2.gps_data_list[0].time
  set1_end_time = set1.gps_data_list[-1].time
  set2_end_time = set2.gps_data_list[-1].time 
  if set1_start_time > set2_start_time:
    later_start_time = utils.round_time(set1_start_time)
    primary_set_index = 0
  else:
    later_start_time = utils.round_time(set2_start_time)
    primary_set_index = 1

  # create dict that maps rounded times to points
  time_point_mapping = create_time_to_point_mapping(set1, set2)

  span_length = 50
  range_start = utils.round_time(min(set1_start_time, set2_start_time))
  range_end = utils.round_time(min(set1_end_time, set2_end_time)) + timedelta(seconds=span_length)
  total_seconds = int((range_end-range_start).total_seconds())

  best_range_start = range_start
  lowest_skip_count = span_length
  previous_range_skip_count = None

  # check each time for how many skipped/invalid entries there are
  for start_time in [range_start + timedelta(seconds=x) for x in range(total_seconds)]:
    current_range_skip_count = 0

    # first loop through, count how many are invalid in first 50 points
    if previous_range_skip_count is None:
      for i in range(span_length):
        time = start_time + timedelta(seconds=i)
        if time not in time_point_mapping or len(time_point_mapping[time]) < 2:
          current_range_skip_count += 1

    # all other times through loop, use prior value but add or subtract for new point and old point
    else:
      current_range_skip_count = previous_range_skip_count

      end_time = start_time + timedelta(seconds=span_length-1)  # point at end of current range that was just added
      if end_time not in time_point_mapping or len(time_point_mapping[end_time]) < 2:
        current_range_skip_count += 1

      previous_start = start_time + timedelta(seconds=-1)  # point that was just edged out
      if previous_start not in time_point_mapping or len(time_point_mapping[previous_start]) < 2:
        current_range_skip_count -= 1

    if current_range_skip_count < lowest_skip_count:
      lowest_skip_count = current_range_skip_count
      best_range_start = start_time
    previous_range_skip_count = current_range_skip_count

  # find best offset starting at the middle of range with most valid points
  range_middle = best_range_start + timedelta(seconds=span_length//2)
  optimal_offset = find_optimal_offset(time_point_mapping, range_middle, span_length, 100, primary_set_index)
  print(optimal_offset)
  if optimal_offset is None:
    print("no optimal line-up for these two data sets; check if correct files are being used")
    return (None, None)

  if primary_set_index == 0:
    print("Optimal offset: set 2 is %s seconds from set 1" % optimal_offset)
    return (later_start_time, later_start_time + timedelta(seconds=optimal_offset))
  else:
    print("Optimal offset: set 1 is %s seconds from set 2" % optimal_offset)
    return (later_start_time + timedelta(seconds=optimal_offset), later_start_time)

def find_lineup_no_optimization(set1, set2):
  """
  Find the approximate offset between two GPS data sets without range start optimization.

  This algorithm first identifies a primary data set and a secondary one based
  on which starts later. The offset is then applied to the secondary data set.
  After mapping the timestamps to the corresponding points from each set,
  it finds the optimal offset using the later start time of the two sets as
  the starting point. Requires checking of more values in the set to obtain
  an accurate offset, compared to the optimized version.

  Args:
    set1: GpsDataSet object
    set2: GpsDataSet object

  Returns:
    Tuple of two datetimes, the start time for set 1 and for set 2 given the
    calculated offset.
  """
  set1_start_time = set1.gps_data_list[0].time
  set2_start_time = set2.gps_data_list[0].time
  if set1_start_time > set2_start_time:
    later_start_time = utils.round_time(set1_start_time)
    primary_set_index = 0
  else:
    later_start_time = utils.round_time(set2_start_time)
    primary_set_index = 1

  # create dict that maps rounded times to points
  time_point_mapping = create_time_to_point_mapping(set1, set2)

  # find best offset
  #offset: (-100,100), check points: (-100, 100)
  optimal_offset = find_optimal_offset(time_point_mapping, later_start_time, 200, 200, primary_set_index)
  print(optimal_offset)
  if optimal_offset is None:
    print("no optimal line-up for these two data sets; check if correct files are being used")
    return (None, None)
  if primary_set_index == 0:
    print("Optimal offset: set 2 is %s seconds from set 1" % optimal_offset)
    return (later_start_time, later_start_time + timedelta(seconds=optimal_offset))
  else:
    print("Optimal offset: set 1 is %s seconds from set 2" % optimal_offset)
    return (later_start_time + timedelta(seconds=optimal_offset), later_start_time)

def find_lineup_naive(set1, set2):
  """
  Initial naive implementation to find lineup between two data sets.

  This algorithm first identifies a primary data set and a secondary one based
  on which starts later. The offset is then applied to the secondary data set.
  After mapping the timestamps to the corresponding points from each set, it
  finds the range that has the least amount of invalid timestamps (has points
  from only one set or neither) and then finds the optimal offset using the
  middle of that range as the starting point.

  Args:
    set1: GpsDataSet object
    set2: GpsDataSet object

  Returns:
    The indexes of the starting points in each data set
  """
  sets = [set1, set2]
  starting_indexes = [0,0]
  set1_start_time = set1.gps_data_list[0].time
  set2_start_time = set2.gps_data_list[0].time
  if set1_start_time > set2_start_time:
    later_start_time = set1_start_time
    primary_set_index = 0
    secondary_set_index = 1
  else:
    later_start_time = set2_start_time
    primary_set_index = 1
    secondary_set_index = 0

  smallest_time_difference = None
  for i,data_point in enumerate(sets[secondary_set_index].gps_data_list):
    time_difference = abs((data_point.time - later_start_time).total_seconds())
    if smallest_time_difference is None or time_difference < smallest_time_difference:
      smallest_time_difference = time_difference
      starting_indexes[secondary_set_index] = i
  best_index = 0
  best_mean_distance = None
  for offset in range(-100,100):
    distances = []
    moving_index = starting_indexes[secondary_set_index] + offset
    if moving_index >= 0:
      primary_points = sets[primary_set_index].gps_data_list
      secondary_points = sets[secondary_set_index].gps_data_list[moving_index:]
      for primary_point,secondary_point in zip(primary_points, secondary_points):
        location1 = (primary_point.latitude, primary_point.longitude)
        location2 = (secondary_point.latitude, secondary_point.longitude)
        distances.append(utils.calculate_distance(location1, location2))
    if distances and (best_mean_distance == None or np.mean(distances) < best_mean_distance):
      best_mean_distance = np.mean(distances)
      best_index = moving_index
  print("optimal mean distance: " + str(best_mean_distance))
  starting_indexes[secondary_set_index] = best_index
  return starting_indexes

def create_time_to_point_mapping(set1, set2):
  """
  Map common timestamp to points from both sets.

  Args:
    set1: GpsDataSet object
    set2: GpsDataSet object

  Returns:
    Dictionary that maps datetime timestamps to the points that fall at that
    time in the following format:
    {DateTime: {"set1": GpsData, "set2": GpsData}, ...}
  """
  time_point_mapping = {}
  for point in set1.gps_data_list:
    rounded_time = utils.round_time(point.time)
    try:
      time_point_mapping[rounded_time]["set1"] = point
    except KeyError:
      time_point_mapping[rounded_time] = {"set1":point}
  for point in set2.gps_data_list:
    rounded_time = utils.round_time(point.time)
    try:
      time_point_mapping[rounded_time]["set2"] = point
    except KeyError:
      time_point_mapping[rounded_time] = {"set2":point}
  return time_point_mapping

def find_optimal_offset(time_point_mapping, start_time, offset_range, point_checking_range, primary_set_index):
  """
  Map common timestamp to points from both sets.

  Args:
    time_point_mapping: dictionary in format {DateTime: {"set1": GpsData, "set2": GpsData}, ...}
    start_time: Datetime to start calculation at
    offset_range: int, how many offset values to check i.e. the range of (-offset_range//2, offset_range//2)
    point_checking_range: int, how many points around the start to use in the deviation
    mean calculations
    primary_set_index: which set (1 or 2) is the primary (i.e. not being shifted)

  Returns:
    int, the offset in seconds that should be applied to the secondary
    data set if a decent offset exists,
    None otherwise
  """
  optimal_offset= 0
  optimal_mean_distance = None

  for offset in range (-offset_range//2, offset_range//2):
    distances = []
    skipped_point_count = 0
    for i in range(-point_checking_range//2, point_checking_range//2):

      # apply offset to secondary set
      if primary_set_index == 0:
        time1 = start_time + timedelta(seconds=i)
        time2 = start_time + timedelta(seconds=i+offset)
      else:
        time1 = start_time + timedelta(seconds=i+offset)
        time2 = start_time + timedelta(seconds=i)

      try:
        point1 = time_point_mapping[time1]["set1"]
        point2 = time_point_mapping[time2]["set2"]
        location1 = (point1.latitude, point1.longitude)
        location2 = (point2.latitude, point2.longitude)
        distances.append(utils.calculate_distance(location1, location2))
      except KeyError:
        skipped_point_count += 1
        continue

    if (distances and skipped_point_count <= point_checking_range-5
       and (optimal_mean_distance == None or np.mean(distances) < optimal_mean_distance)):
      optimal_mean_distance = np.mean(distances)
      optimal_offset = offset

  print("optimal mean distance: " + str(optimal_mean_distance))
  if optimal_mean_distance is None or optimal_mean_distance > 100:
    return None
  return optimal_offset

def print_reponse(response):
  pp = pprint.PrettyPrinter(depth=6)
  pp.pprint(response)
