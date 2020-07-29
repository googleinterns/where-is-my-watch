"""Unit test for FileParser Class"""
import unittest
from unittest.mock import patch

from datetime import datetime
from datetime import timezone

from GpsDataAnalyzer.fileparser import fileparser
from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet


class FileParserTest(unittest.TestCase):
	def setUp(self):
		self.fileparser = fileparser.FileParser()
		self.xml_gpsdataset = GpsDataSet(gps_meta_data=GpsMetaData(device='salmon',
													   identifier='PXDB.200528.004',
													   manufacturer='Compal',
													   model='Suunto 7',
													   start_time=datetime(2020, 7, 7, 18,45,47,5000, tzinfo=timezone.utc),
													   end_time=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc)),
							   gps_data_list=[GpsData(latitude= 37.31013773,
													longitude= -122.0314044,
													altitude= 44.6412353515625,
													speed= 0.0,
													time=datetime(2020,7,7,18,46,36,0,tzinfo=timezone.utc),
													distance=0.0),
											GpsData(latitude= 37.31013774,
													longitude= -122.03140539,
													altitude= 44.76190185546875,
													speed= 0.16,
													time=datetime(2020,7,7,18,46,37,0,tzinfo=timezone.utc),
													distance=0.08776937608839655),
											GpsData(latitude= 37.31013822,
													longitude= -122.0314067,
													altitude= 44.80401611328125,
													speed= 0.0,
													time=datetime(2020,7,7,18,46,38,0,tzinfo=timezone.utc),
													distance=0.12776575084838615),
											GpsData(latitude= 37.31013763,
													longitude= -122.03140506,
													altitude= 44.564208984375,
													speed= 0.44,
													time=datetime(2020,7,7,18,46,39,0,tzinfo=timezone.utc),
													distance=0.15944968362157746),
											GpsData(latitude= 37.31013728,
													longitude= -122.03140187,
													altitude= 44.97833251953125,
													speed= 0.21,
													time=datetime(2020,7,7,18,46,40,0,tzinfo=timezone.utc),
													distance=0.28544519128269824)])
		self.csv_gpsdataset1 = GpsDataSet(gps_meta_data=GpsMetaData(device='DynamicSimulation',
													   identifier=None,
													   manufacturer=None,
													   model=None,
													   start_time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc),
													   end_time=datetime(2020, 7, 22, 15, 49, 20, 144408, tzinfo=timezone.utc)),
										gps_data_list=[GpsData(latitude= 37.31013773,
															longitude= -122.0314044,
															altitude= 44.6412353515625,
															speed= 0.0,
															time=datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc)),
													GpsData(latitude= 37.31013774,
															longitude= -122.03140539,
															altitude= 44.76190185546875,
															speed= 0.16,
															time=datetime(2020, 7, 22, 15, 49, 1, 726803, tzinfo=timezone.utc))])
		self.csv_gpsdataset2 = GpsDataSet(gps_meta_data=GpsMetaData(device='DynamicSimulation',
													   identifier=None,
													   manufacturer=None,
													   model=None,
													   start_time=datetime(2020, 7, 22, 15, 49, 21, 247269, tzinfo=timezone.utc),
													   end_time=datetime(2020, 7, 22, 15, 49, 32, 954147, tzinfo=timezone.utc)),
										gps_data_list=[GpsData(latitude= 37.31013773,
															longitude= -122.0314044,
															altitude= 44.6412353515625,
															speed= 0.0,
															time=datetime(2020, 7, 22, 15, 49, 21, 247269, tzinfo=timezone.utc))])
		self.csv_gpsdataset3 = GpsDataSet(gps_meta_data=GpsMetaData(device='StaticSimulation',
													   identifier=None,
													   manufacturer=None,
													   model=None,
													   start_time=datetime(2020, 7, 22, 15, 49, 34, 60437, tzinfo=timezone.utc),
													   end_time=datetime(2020, 7, 22, 15, 49, 47, 631769, tzinfo=timezone.utc)),
										gps_data_list=[GpsData(latitude= 27.417747,
															longitude= -112.086086,
															altitude= 0.0,
															speed= 0.0,
															time=datetime(2020, 7, 22, 15, 49, 34, 60437, tzinfo=timezone.utc))])

	def test_get_file_type(self):
		xml_file_type = self.fileparser.get_file_type('testfile2.xml')
		self.assertEqual("xml", xml_file_type)
		
		invalid_file_type = self.fileparser.get_file_type('wrongfilename.')
		self.assertEqual('', invalid_file_type)

		none_file_type = self.fileparser.get_file_type(None)
		self.assertIsNone(none_file_type)
		
	def test_parse_xml_file_success(self):
		xml_gpsdataset = self.fileparser.parse_file('GpsDataAnalyzer/test/testfile2.xml')
		self.assertEqual(5, len(xml_gpsdataset.gps_data_list))
		self.assertEqual(self.xml_gpsdataset, xml_gpsdataset)

	def test_parse_invalid_file_return_none(self):
		none_gpsdataset = self.fileparser.parse_file('wrongfilename.')
		self.assertIsNone(none_gpsdataset)

	@patch('GpsDataAnalyzer.utils.calculate_distance')
	@patch('GpsDataAnalyzer.utils.cartesian_to_geodetic')
	def test_parse_csv_file_multiple_simulations(self, mock_cartesian_to_geodetic, mock_calculate_distance):
		test_locations = [(37.31013773, -122.0314044, 44.6412353515625),
						  (37.31013774, -122.03140539, 44.76190185546875),
						  (37.31013773, -122.0314044, 44.6412353515625),
						  (37.31013773, -122.0314044, 44.6412353515625)]
		test_distances = [.016,]
		mock_cartesian_to_geodetic.side_effect = test_locations
		mock_calculate_distance.side_effect = test_distances
		expected = [self.csv_gpsdataset1, self.csv_gpsdataset2, self.csv_gpsdataset3]

		csv_data_set = self.fileparser.parse_csv('GpsDataAnalyzer/test/test_csv.csv')

		self.assertEqual(expected, csv_data_set)

	@patch('GpsDataAnalyzer.utils.calculate_distance')
	@patch('GpsDataAnalyzer.utils.cartesian_to_geodetic')
	def test_parse_csv_file_one_simulation(self, mock_cartesian_to_geodetic, mock_calculate_distance):
		test_locations = [(37.31013773, -122.0314044, 44.6412353515625),
						  (37.31013774, -122.03140539, 44.76190185546875)]
		test_distances = [.016,]
		mock_cartesian_to_geodetic.side_effect = test_locations
		mock_calculate_distance.side_effect = test_distances
		expected = [self.csv_gpsdataset1,]

		csv_data_set = self.fileparser.parse_csv('GpsDataAnalyzer/test/test_csv_one_sim.csv')

		self.assertEqual(expected, csv_data_set)

	def tearDown(self):
		self.xml_fileparser = None  
		self.invalid_fileparser = None


if __name__ == "__main__":
	unittest.main()
