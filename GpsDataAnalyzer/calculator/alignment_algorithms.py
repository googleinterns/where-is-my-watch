from datetime import datetime, timedelta
import numpy as np
import time

from GpsDataAnalyzer import utils

MEAN_DISTANCE_THRESHOLD = 100

def find_lineup(set1, set2):
    """
    Optimized algorithm to find the approximate time offset between two GPS data sets.

    This algorithm assigns the data set that starts later as primary and the 
    other one as secondary. The offset is then applied to the secondary data
    set. After mapping the timestamps to the corresponding points from each set,
    it finds the range that has the most overlapping timestamps between the two
    sets and then finds the optimal offset using the middle of that range 
    as the starting point.

    Args:
        set1: GpsDataSet object
        set2: GpsDataSet object

    Returns:
        Tuple of two datetimes, the start time for set 1 and for set 2 given the
        calculated offset. If no lineup is found, it will return None.
    """

    # find primary data set (the one with the later start time)
    set1_start_time = set1.gps_data_list[0].time
    set2_start_time = set2.gps_data_list[0].time
    set1_end_time = set1.gps_data_list[-1].time
    set2_end_time = set2.gps_data_list[-1].time 
    if set1_start_time > set2_start_time:
        later_start_time = utils.round_time(set1_start_time)
        primary_set = set1
        secondary_set = set2
    else:
        later_start_time = utils.round_time(set2_start_time)
        primary_set = set2
        secondary_set = set1

    # create dict that maps rounded times to points
    primary_time_points_mapping = create_time_to_points_mapping(primary_set)
    secondary_time_points_mapping = create_time_to_points_mapping(secondary_set)

    # how many offsets to check so, for 50, check offsets (-25,25); should be even
    offset_range_length = 50
    # points to check around each offset, for 100, check points (-50,50); should be even
    point_checking_range_length = 100

    # span length is total length of the span of points to check around the offsets
    span_length = offset_range_length + point_checking_range_length

    range_start = utils.round_time(max(set1_start_time, set2_start_time)) - timedelta(seconds=span_length)
    range_end = utils.round_time(min(set1_end_time, set2_end_time))

    # TODO(ameles): determine if 50 is just a good number or it should be tied to offset size
    range_optimization_size = offset_range_length

    best_range_start = find_best_data_range(primary_time_points_mapping,
                                            secondary_time_points_mapping,
                                            range_optimization_size,
                                            range_start,
                                            range_end)
    print(best_range_start)
    # find best offset starting at the middle of range with most valid points
    range_middle = best_range_start + timedelta(seconds=range_optimization_size//2)

    optimal_offset = find_optimal_offset(primary_time_points_mapping,
                                         secondary_time_points_mapping,
                                         range_middle,
                                         offset_range_length,
                                         point_checking_range_length)
    if optimal_offset is None:
        print("no optimal line-up for these two data sets; check if correct files are being used")
        return (None, None)

    if primary_set == set1:
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
        calculated offset. If no lineup is found, it will return None.
    """
    set1_start_time = set1.gps_data_list[0].time
    set2_start_time = set2.gps_data_list[0].time
    if set1_start_time > set2_start_time:
        later_start_time = utils.round_time(set1_start_time)
        primary_set = set1
        secondary_set = set2
    else:
        later_start_time = utils.round_time(set2_start_time)
        primary_set = set2
        secondary_set = set1

    # create dicts that map rounded times to points
    primary_time_points_mapping = create_time_to_points_mapping(primary_set)
    secondary_time_points_mapping = create_time_to_points_mapping(secondary_set)

    offset_range_length = 200  # how many offsets to check so, for 200, check offsets (-100,100)
    point_checking_range_length = 200  # points to check around each offset, for 200, check points (-100,100)

    # find best offset
    optimal_offset = find_optimal_offset(primary_time_points_mapping,
                                         secondary_time_points_mapping,
                                         later_start_time,
                                         offset_range_length,
                                         point_checking_range_length)
    print(optimal_offset)
    if optimal_offset is None:
        print("no optimal line-up for these two data sets; check if correct files are being used")
        return (None, None)
    if primary_set == set1:
        print("Optimal offset: set 2 is %s seconds from set 1" % optimal_offset)
        return (later_start_time, later_start_time + timedelta(seconds=optimal_offset))
    else:
        print("Optimal offset: set 1 is %s seconds from set 2" % optimal_offset)
        return (later_start_time + timedelta(seconds=optimal_offset), later_start_time)

def find_lineup_naive(set1, set2):
    """
    Initial naive implementation to find lineup between two data sets.

    Finds the point in the secondary data set closest to the start of the
    primary data set, and then uses those starting indices to find the offset
    with the lowest mean distance in the range of points around it index-wise
    in the data set

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

def find_best_data_range(primary_time_points_mapping, secondary_time_points_mapping,
                         range_optimization_size, range_start, range_end):
    """
    Find range of size range_optimization_size with most overlapping points.

    This will return the best range where of the the best range is a range 
    of size range_optimization_size that has the most number of overlapping points
    between the primary and secondary sets that falls between range_start and
    range_end.  

    Args:
        primary_time_points_mapping: Dictionary, {DateTime: [GpsData,], ...}
        secondary_time_points_mapping: Dictionary, {DateTime: [GpsData,], ...}
        range_optimization_size: int, size of optimal range to find
        range_start: Datetime, beginning of range for possible best_range_start timestamp
        range_end: Datetime, end of range for possible best_range_start timestamp

    Returns:
        Datetime, starting timestamp of the range of size 
        range_optimization_size that has the most number of overlapping points
        between the primary and secondary sets.
    """

    # count how many are skipped (not overlapping) in first 50  (offset_range_length) points
    previous_range_skip_count = 0
    for i in range(range_optimization_size):
        time = range_start + timedelta(seconds=i)
        if time not in primary_time_points_mapping or time not in secondary_time_points_mapping:
            previous_range_skip_count += 1

    best_range_start = range_start
    lowest_skip_count = range_optimization_size

    total_seconds = int((range_end-range_start).total_seconds())

    # TODO(ameles): optimize loop so there are not 2n calls to check mappings
    # check for the rest of the ranges how many skipped entries there are
    for start_time in [range_start + timedelta(seconds=x) for x in range(1,total_seconds)]:
        current_range_skip_count = 0

        # use prior value but add or subtract for new point and old point
        current_range_skip_count = previous_range_skip_count

        end_time = start_time + timedelta(seconds=range_optimization_size-1)  # point at end of current range that was just added
        if end_time not in primary_time_points_mapping or end_time not in secondary_time_points_mapping:
            current_range_skip_count += 1

        previous_start = start_time + timedelta(seconds=-1)  # point that was just edged out
        if previous_start not in primary_time_points_mapping or previous_start not in secondary_time_points_mapping:
            current_range_skip_count -= 1

        if current_range_skip_count <= lowest_skip_count:
            lowest_skip_count = current_range_skip_count
            best_range_start = start_time
            if lowest_skip_count == 0:
                break
        previous_range_skip_count = current_range_skip_count

    return best_range_start

def create_time_to_points_mapping(dataset, offset=0):
    """
    Map timestamp to points from the dataset.

    Args:
        dataset: GpsDataSet
        offset: int, seconds of offset to apply to timestamps

    Returns:
        Dictionary that goups dataset points by seconds timestamp in the
        following format: {DateTime: [GpsData,], ...}
    """
    time_points_mapping = {}
    for point in dataset.gps_data_list:
        rounded_time = utils.round_time(point.time) + timedelta(seconds=offset)
        if rounded_time in time_points_mapping:
            time_points_mapping[rounded_time].append(point)
        else:
            time_points_mapping[rounded_time] = [point]
    return time_points_mapping

def find_optimal_offset(primary_set, secondary_set, start_time, 
                        offset_range_length, point_checking_range_length):
    """
    Find optimal offset from offset range to shift the secondary set

    Finds the optimal offset meaning, an offset that is in the given offset
    range to check and that also has overlap between the two sets. If the
    mean distance is over 100m, it returns None, since this indicates sets
    that do not align even if their timestamps can be shifted to align. 

    Args:
        primary_set: dictionary in format {DateTime: [GpsData,], ...}
        secondary_set: dictionary in format {DateTime: [GpsData,], ...}
        start_time: Datetime to start calculation at
        offset_range: int, how many offset values to check i.e. the range of 
                      (-offset_range//2, offset_range//2)
        point_checking_range: int, how many points around the start to use in the deviation
                              mean calculations
        primary_set_index: which set (1 or 2) is the primary (i.e. not being shifted)

    Returns:
        int, the offset in seconds that should be applied to the secondary
        data set if an optimal offset exists,
        None otherwise
    """
    optimal_offset= 0
    optimal_mean_distance = MEAN_DISTANCE_THRESHOLD+1

    for offset in range (-offset_range_length//2, offset_range_length//2):
        distances = []
        skipped_point_count = 0
        for i in range(-point_checking_range_length//2, point_checking_range_length//2):
            # apply offset to secondary set
            primary_time = start_time + timedelta(seconds=i)
            secondary_time = start_time + timedelta(seconds=i+offset)

            if primary_time in primary_set and secondary_time in secondary_set:
                # if more than one point at that time, choose first one
                # TODO(ameles) consider if more complex downsampling needed
                primary_point = primary_set[primary_time][0]
                secondary_point = secondary_set[secondary_time][0]
                location1 = (primary_point.latitude, primary_point.longitude)
                location2 = (secondary_point.latitude, secondary_point.longitude)
                distances.append(utils.calculate_distance(location1, location2))
            else:
                skipped_point_count += 1

        if distances and np.mean(distances) < optimal_mean_distance:
            optimal_mean_distance = np.mean(distances)
            optimal_offset = offset

    print("optimal mean distance: " + str(optimal_mean_distance))
    if optimal_mean_distance is None or optimal_mean_distance > MEAN_DISTANCE_THRESHOLD:
        return None
    return optimal_offset
