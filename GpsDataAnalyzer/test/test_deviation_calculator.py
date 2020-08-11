"""Unit test for DataSetDeviationCalculator Class"""
import unittest
from unittest.mock import patch
import pandas as pd

from datetime import datetime
from datetime import timedelta
from datetime import timezone

from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet
from GpsDataAnalyzer.calculator.deviation_calculator import DataSetDeviationCalculator
from GpsDataAnalyzer.calculator import alignment_algorithms

class DeviationCalculatorTest(unittest.TestCase):
	def setUp(self):
		watch_metadata = GpsMetaData(device='sampleDevice',
									 identifier='sampleId',
									 manufacturer='sampleManufacturer',
									 model='sampleModel',
									 start_time=datetime(2020, 7, 7, 18,45,47,5000, tzinfo=timezone.utc),
									 end_time=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc))
		watch_data_list = [GpsData(latitude= 63.18,
                  longitude= -174.13,
                  altitude= 4.61,
									speed= 0.0,
									time=datetime(2020, 7, 22, 15, 49, 4, 0, tzinfo=timezone.utc),
									distance=0.0),
							GpsData(latitude= 63.19,
                  longitude= -174.12,
                  altitude= 4.91,
									speed= 0.16,
									time=datetime(2020, 7, 22, 15, 49, 5, 0, tzinfo=timezone.utc),
									distance=0.09),
							GpsData(latitude= 63.20,
                  longitude= -174.11,
                  altitude= 4.91,
									speed= 0.0,
									time=datetime(2020, 7, 22, 15, 49, 6, 0, tzinfo=timezone.utc),
									distance=0.13),
							GpsData(latitude= 63.21,
                  longitude= -174.10,
                  altitude= 4.91,
									speed= 0.44,
									time=datetime(2020, 7, 22, 15, 49, 7, 0, tzinfo=timezone.utc),
									distance=0.16),
							GpsData(latitude= 63.22,
                  longitude= -174.09,
                  altitude= 4.91,
									speed= 0.21,
									time=datetime(2020, 7, 22, 15, 49, 8, 0, tzinfo=timezone.utc),
									distance=0.28)]
		self.watch_data_set = GpsDataSet(gps_meta_data=watch_metadata,
										 gps_data_list=watch_data_list)

		simulator_metadata = GpsMetaData(device='DynamicSimulation',
										 identifier=None,
										 manufacturer=None,
										 model=None,
										 start_time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc),
										 end_time=datetime(2020, 7, 22, 15, 49, 20, 144408, tzinfo=timezone.utc))
		simulator_data_list = [GpsData(latitude= 63.18,
	                  longitude= -174.13,
	                  altitude= 4.91,
										speed= 1.0,
										time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc)),
								GpsData(latitude= 63.19,
	                  longitude= -174.12,
	                  altitude= 4.91,
										speed= 0.16,
										time=datetime(2020, 7, 22, 15, 49, 2, 626803, tzinfo=timezone.utc)),
								GpsData(latitude= 63.20,
	                  longitude= -174.11,
	                  altitude= 4.91,
										speed= 0.0,
										time=datetime(2020, 7, 22, 15, 49, 3, 626803, tzinfo=timezone.utc)),
								GpsData(latitude= 63.21,
	                  longitude= -174.10,
	                  altitude= 4.91,
										speed= 0.44,
										time=datetime(2020, 7, 22, 15, 49, 4, 626803, tzinfo=timezone.utc)),
								GpsData(latitude= 63.22,
	                  longitude= -174.09,
	                  altitude= 4.91,
										speed= 0.21,
										time=datetime(2020, 7, 22, 15, 49, 5, 626803, tzinfo=timezone.utc))]
		self.simulator_data_set = GpsDataSet(gps_meta_data=simulator_metadata,
											 gps_data_list=simulator_data_list)

		self.test_offset_mapping_1 = {datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc): [self.watch_data_set.gps_data_list[0]],
									  datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc): [self.watch_data_set.gps_data_list[1]],
									  datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc): [self.watch_data_set.gps_data_list[2]],
									  datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc): [self.watch_data_set.gps_data_list[3]],
									  datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc): [self.watch_data_set.gps_data_list[4]]}
		self.test_offset_mapping_2 = {datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[0]],
									  datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[1]],
									  datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[2]],
									  datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[3]],
									  datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[4]]}
	
	def test_init_calculator(self):
		calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)

		self.assertEqual(datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc), calculator.starting_time_1)
		self.assertEqual(datetime(2020, 7, 22, 15, 49, 2, tzinfo=timezone.utc), calculator.starting_time_2)
		self.assertEqual(self.test_offset_mapping_1, calculator.offset_mapping_1)
		self.assertEqual(self.test_offset_mapping_2, calculator.offset_mapping_2)
		self.assertEqual(datetime(2020, 7, 7, 19, 8, 1, 318000,tzinfo=timezone.utc), calculator.ending_time_1)
		self.assertEqual(datetime(2020, 7, 22, 15, 49, 22, 144408, tzinfo=timezone.utc), calculator.ending_time_2)

	def test_init_calculator_no_lineup(self):
		for data_point in self.simulator_data_set.gps_data_list:
			data_point.time = data_point.time + timedelta(seconds=100)
		self.test_offset_mapping_2 = {datetime(2020, 7, 22, 15, 50, 42, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[0]],
									  datetime(2020, 7, 22, 15, 50, 43, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[1]],
									  datetime(2020, 7, 22, 15, 50, 44, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[2]],
									  datetime(2020, 7, 22, 15, 50, 45, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[3]],
									  datetime(2020, 7, 22, 15, 50, 46, tzinfo=timezone.utc): [self.simulator_data_set.gps_data_list[4]]}

		calculator = DataSetDeviationCalculator(self.watch_data_set, self.simulator_data_set)

		self.assertEqual(None, calculator.starting_time_1)
		self.assertEqual(None, calculator.starting_time_2)
		self.assertEqual(self.test_offset_mapping_1, calculator.offset_mapping_1)
		self.assertEqual(self.test_offset_mapping_2, calculator.offset_mapping_2)
		self.assertEqual(datetime(2020, 7, 7, 19, 8, 1, 318000,tzinfo=timezone.utc), calculator.ending_time_1)
		self.assertEqual(datetime(2020, 7, 22, 15, 49, 20, 144408, tzinfo=timezone.utc), calculator.ending_time_2)

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

	def test_alignment_algorithm(self):
		expected_offset = -2

		start_time_1, start_time_2 = alignment_algorithms.find_lineup(self.watch_data_set,
																																	self.simulator_data_set)

		result_offset = (start_time_2-start_time_1).total_seconds()
		self.assertEqual(expected_offset, result_offset)

if __name__ == "__main__":
	unittest.main()
