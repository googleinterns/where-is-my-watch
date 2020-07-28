"""Unit test for DataSetDeviationCalculator Class"""
import unittest
from unittest.mock import patch
import pandas as pd

from datetime import datetime
from datetime import timedelta
from datetime import timezone

from datamodel.gpsdataset import GpsData
from datamodel.gpsdataset import GpsMetaData
from datamodel.gpsdataset import GpsDataSet
from calculator.deviation_calculator import DataSetDeviationCalculator


class DeviationCalculatorTest(unittest.TestCase):
  def setUp(self):
    self.watch_data_set = GpsDataSet(gps_meta_data=GpsMetaData(device='salmon',
                                                   identifier='PXDB.200528.004',
                                                   manufacturer='Compal',
                                                   model='Suunto 7',
                                                   start_time=datetime(2020, 7, 7, 18,45,47,5000, tzinfo=timezone.utc),
                                                   end_time=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc)),
                           gps_data_list=[GpsData(latitude= 37.31013773,
                                                longitude= -122.0314044,
                                                altitude= 44.2,
                                                speed= 0.0,
                                                time=datetime(2020, 7, 22, 15, 49, 4, 0, tzinfo=timezone.utc),
                                                distance=0.0),
                                        GpsData(latitude= 37.31013774,
                                                longitude= -122.03140539,
                                                altitude= 44.76190185546875,
                                                speed= 0.16,
                                                time=datetime(2020, 7, 22, 15, 49, 5, 0, tzinfo=timezone.utc),
                                                distance=0.08776937608839655),
                                        GpsData(latitude= 37.31013822,
                                                longitude= -122.0314067,
                                                altitude= 44.80401611328125,
                                                speed= 0.0,
                                                time=datetime(2020, 7, 22, 15, 49, 6, 0, tzinfo=timezone.utc),
                                                distance=0.12776575084838615),
                                        GpsData(latitude= 37.31013763,
                                                longitude= -122.03140506,
                                                altitude= 44.564208984375,
                                                speed= 0.44,
                                                time=datetime(2020, 7, 22, 15, 49, 7, 0, tzinfo=timezone.utc),
                                                distance=0.15944968362157746),
                                        GpsData(latitude= 37.31013728,
                                                longitude= -122.03140187,
                                                altitude= 44.97833251953125,
                                                speed= 0.21,
                                                time=datetime(2020, 7, 22, 15, 49, 8, 0, tzinfo=timezone.utc),
                                                distance=0.28544519128269824)])
    self.simulator_data_set = GpsDataSet(gps_meta_data=GpsMetaData(device='DynamicSimulation',
                                                   identifier=None,
                                                   manufacturer=None,
                                                   model=None,
                                                   start_time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc),
                                                   end_time=datetime(2020, 7, 22, 15, 49, 20, 144408, tzinfo=timezone.utc)),
                                    gps_data_list=[GpsData(latitude= 37.31013773,
                                                        longitude= -122.0314044,
                                                        altitude= 44.5,
                                                        speed= 1.0,
                                                        time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc)),
                                                  GpsData(latitude= 37.31013774,
                                                        longitude= -122.03140539,
                                                        altitude= 44.76190185546875,
                                                        speed= 0.16,
                                                        time=datetime(2020, 7, 22, 15, 49, 2, 626803, tzinfo=timezone.utc)),
                                                GpsData(latitude= 37.31013822,
                                                        longitude= -122.0314067,
                                                        altitude= 44.80401611328125,
                                                        speed= 0.0,
                                                        time=datetime(2020, 7, 22, 15, 49, 3, 626803, tzinfo=timezone.utc)),
                                                GpsData(latitude= 37.31013763,
                                                        longitude= -122.03140506,
                                                        altitude= 44.564208984375,
                                                        speed= 0.44,
                                                        time=datetime(2020, 7, 22, 15, 49, 4, 626803, tzinfo=timezone.utc)),
                                                GpsData(latitude= 37.31013728,
                                                        longitude= -122.03140187,
                                                        altitude= 44.97833251953125,
                                                        speed= 0.21,
                                                        time=datetime(2020, 7, 22, 15, 49, 5, 626803, tzinfo=timezone.utc))])
    self.test_time_point_mapping = {datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc): 
                                {"set1" : self.watch_data_set.gps_data_list[0],
                                 "set2" : self.simulator_data_set.gps_data_list[0]},
                              datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc): 
                                {"set1" : self.watch_data_set.gps_data_list[1],
                                 "set2" : self.simulator_data_set.gps_data_list[1]},
                              datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc): 
                                {"set1" : self.watch_data_set.gps_data_list[2],
                                 "set2" : self.simulator_data_set.gps_data_list[2]},
                              datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc): 
                                {"set1" : self.watch_data_set.gps_data_list[3],
                                 "set2" : self.simulator_data_set.gps_data_list[3]},
                              datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc): 
                                {"set1" : self.watch_data_set.gps_data_list[4],
                                 "set2" : self.simulator_data_set.gps_data_list[4]}}
  def test_init_calculator(self):
    calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)

    self.assertEqual(self.test_time_point_mapping, calculator.offset_time_point_mapping)
    self.assertEqual(datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc), calculator.starting_time_1)
    self.assertEqual(datetime(2020, 7, 22, 15, 49, 2, tzinfo=timezone.utc), calculator.starting_time_2)

  def test_init_calculator_no_lineup(self):
    for data_point in self.simulator_data_set.gps_data_list:
      data_point.time = data_point.time + timedelta(seconds=100)
    self.test_time_point_mapping = {datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc): 
                                    {"set1" : self.watch_data_set.gps_data_list[0]},
                                  datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc): 
                                    {"set1" : self.watch_data_set.gps_data_list[1]},
                                  datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc): 
                                    {"set1" : self.watch_data_set.gps_data_list[2]},
                                  datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc): 
                                    {"set1" : self.watch_data_set.gps_data_list[3]},
                                  datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc): 
                                    {"set1" : self.watch_data_set.gps_data_list[4]},
                                  datetime(2020, 7, 22, 15, 50, 42, tzinfo=timezone.utc): 
                                    {"set2" : self.simulator_data_set.gps_data_list[0]},
                                  datetime(2020, 7, 22, 15, 50, 43, tzinfo=timezone.utc): 
                                    {"set2" : self.simulator_data_set.gps_data_list[1]},
                                  datetime(2020, 7, 22, 15, 50, 44, tzinfo=timezone.utc): 
                                    {"set2" : self.simulator_data_set.gps_data_list[2]},
                                  datetime(2020, 7, 22, 15, 50, 45, tzinfo=timezone.utc): 
                                    {"set2" : self.simulator_data_set.gps_data_list[3]},
                                  datetime(2020, 7, 22, 15, 50, 46, tzinfo=timezone.utc): 
                                    {"set2" : self.simulator_data_set.gps_data_list[4]}}

    calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)

    self.assertEqual(self.test_time_point_mapping, calculator.offset_time_point_mapping)
    self.assertEqual(None, calculator.starting_time_1)
    self.assertEqual(None, calculator.starting_time_2)

  def test_get_deviation_dataframe(self):
    time_list = [datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc)]
    deviation_list = [0.0, 0.0, 0.0, 0.0, 0.0]
    speed_differentials = [1.0, 0.0, 0.0, 0.0, 0.0]
    altitude_differentials = [.3, 0.0, 0.0, 0.0, 0.0]
    set1_time_list = [datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc)]
    set2_time_list = [datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 2, 626803, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 3, 626803, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 4, 626803, tzinfo=timezone.utc),
                      datetime(2020, 7, 22, 15, 49, 5, 626803, tzinfo=timezone.utc)]
    deviations_dataframe = pd.DataFrame({"Common Timestamp": time_list,
                                         "Deviations": deviation_list,
                                         "Speed Differentials": speed_differentials,
                                         "Altitude Differentials": altitude_differentials,
                                         "Set 1 Timestamp": set1_time_list,
                                         "Set 2 Timestamp": set2_time_list})
    calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)

    result = calculator.get_deviation_dataframe()

    pd.testing.assert_frame_equal(deviations_dataframe, result)

  def test_get_deviation_dataframe_no_lineup(self):
    deviations_dataframe = pd.DataFrame({"Common Timestamp": [],
                                         "Deviations": [],
                                         "Speed Differentials": [],
                                         "Altitude Differentials": [],
                                         "Set 1 Timestamp": [],
                                         "Set 2 Timestamp": []})
    for data_point in self.simulator_data_set.gps_data_list:
      data_point.time = data_point.time + timedelta(seconds=100)
    calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)
    
    result = calculator.get_deviation_dataframe()

    pd.testing.assert_frame_equal(deviations_dataframe, result)

if __name__ == "__main__":
    unittest.main()
