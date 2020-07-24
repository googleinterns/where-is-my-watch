"""Unit test for FileParser Class"""
import unittest
from unittest.mock import patch
from datetime import datetime

from GpsDataAnalyzer.fileparser.fileparser import FileParser
from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

class FileParserTest(unittest.TestCase):
    def setUp(self):
        self.fileparser = FileParser()
        self.xml_gpsdataset = GpsDataSet(gpsmetadata=GpsMetaData(device='salmon',
                                                       identifier='PXDB.200528.004',
                                                       manufacturer='Compal',
                                                       model='Suunto 7',
                                                       startime=datetime.strptime('2020-07-07T18:45:47.005', "%Y-%m-%dT%H:%M:%S.%f"),
                                                       endtime=datetime.strptime('2020-07-07T19:08:01.318', "%Y-%m-%dT%H:%M:%S.%f")),
                                        gpsdatalist=[GpsData(latitude= 37.31013773,
                                                            longitude= -122.0314044,
                                                            altitude= 44.6412353515625,
                                                            speed= 0.0,
                                                            time=datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f")),
                                                    GpsData(latitude= 37.31013774,
                                                            longitude= -122.03140539,
                                                            altitude= 44.76190185546875,
                                                            speed= 0.16,
                                                            time=datetime.strptime('2020-07-07T18:46:37.000', "%Y-%m-%dT%H:%M:%S.%f")),
                                                    GpsData(latitude= 37.31013822,
                                                            longitude= -122.0314067,
                                                            altitude= 44.80401611328125,
                                                            speed= 0.0,
                                                            time=datetime.strptime('2020-07-07T18:46:38.000', "%Y-%m-%dT%H:%M:%S.%f")),
                                                    GpsData(latitude= 37.31013763,
                                                            longitude= -122.03140506,
                                                            altitude= 44.564208984375,
                                                            speed= 0.44,
                                                            time=datetime.strptime('2020-07-07T18:46:39.000', "%Y-%m-%dT%H:%M:%S.%f")),
                                                    GpsData(latitude= 37.31013728,
                                                            longitude= -122.03140187,
                                                            altitude= 44.97833251953125,
                                                            speed= 0.21,
                                                            time=datetime.strptime('2020-07-07T18:46:40.000', "%Y-%m-%dT%H:%M:%S.%f"))])
        self.csv_gpsdataset1 = GpsDataSet(gpsmetadata=GpsMetaData(device='DynamicSimulation',
                                                       identifier=None,
                                                       manufacturer=None,
                                                       model=None,
                                                       startime=datetime(2020, 7, 22, 15, 49, 1, 626803),
                                                       endtime=datetime(2020, 7, 22, 15, 49, 20, 144408)),
                                        gpsdatalist=[GpsData(latitude= 37.31013773,
                                                            longitude= -122.0314044,
                                                            altitude= 44.6412353515625,
                                                            speed= 0.0,
                                                            time=datetime(2020, 7, 22, 15, 49, 1, 626803)),
                                                    GpsData(latitude= 37.31013774,
                                                            longitude= -122.03140539,
                                                            altitude= 44.76190185546875,
                                                            speed= 0.16,
                                                            time=datetime(2020, 7, 22, 15, 49, 1, 726803))])
        self.csv_gpsdataset2 = GpsDataSet(gpsmetadata=GpsMetaData(device='DynamicSimulation',
                                                       identifier=None,
                                                       manufacturer=None,
                                                       model=None,
                                                       startime=datetime(2020, 7, 22, 15, 49, 21, 247269),
                                                       endtime=datetime(2020, 7, 22, 15, 49, 32, 954147)),
                                        gpsdatalist=[GpsData(latitude= 37.31013773,
                                                            longitude= -122.0314044,
                                                            altitude= 44.6412353515625,
                                                            speed= 0.0,
                                                            time=datetime(2020, 7, 22, 15, 49, 21, 247269))])
        self.csv_gpsdataset3 = GpsDataSet(gpsmetadata=GpsMetaData(device='StaticSimulation',
                                                       identifier=None,
                                                       manufacturer=None,
                                                       model=None,
                                                       startime=datetime(2020, 7, 22, 15, 49, 34, 60437),
                                                       endtime=datetime(2020, 7, 22, 15, 49, 47, 631769)),
                                        gpsdatalist=[GpsData(latitude= 27.417747,
                                                            longitude= -112.086086,
                                                            altitude= 0.0,
                                                            speed= 0.0,
                                                            time=datetime(2020, 7, 22, 15, 49, 34, 60437))])
    def test_get_file_type(self):
        xml_file_type = self.fileparser.get_file_type('testfile2.xml')
        self.assertEqual("xml", xml_file_type)
        
        invalid_file_type = self.fileparser.get_file_type('wrongfilename.')
        self.assertEqual('', invalid_file_type)
        
    def test_parse_xml_file_success(self):
        xml_gpsdataset = self.fileparser.parse_file('testfile2.xml')
        self.assertEqual(5, len(xml_gpsdataset.gpsdatalist))
        self.assertEqual(self.xml_gpsdataset, xml_gpsdataset)

    def test_parse_invalid_file_return_none(self):
        none_gpsdataset = self.fileparser.parse_file('wrongfilename.')
        self.assertIsNone(none_gpsdataset)

    @patch('GpsDataAnalyzer.fileparser.utils.calculate_distance')
    @patch('GpsDataAnalyzer.fileparser.utils.cartesian_to_geodetic')
    def test_parse_csv_file_success(self, mock_cartesian_to_geodetic, mock_calculate_distance):
        test_locations = [(37.31013773, -122.0314044, 44.6412353515625),
                          (37.31013774, -122.03140539, 44.76190185546875),
                          (37.31013773, -122.0314044, 44.6412353515625)]
        test_distances = [.016,]
        mock_cartesian_to_geodetic.side_effect = test_locations
        mock_calculate_distance.side_effect = test_distances
        expected = [self.csv_gpsdataset1, self.csv_gpsdataset2, self.csv_gpsdataset3]
        csv_data_set = self.fileparser.parse_csv('GpsDataAnalyzer/test/test_csv.csv')
        self.assertEqual(csv_data_set, expected)

    @patch('GpsDataAnalyzer.fileparser.utils.calculate_distance')
    @patch('GpsDataAnalyzer.fileparser.utils.cartesian_to_geodetic')
    def test_parse_csv_file_success(self, mock_cartesian_to_geodetic, mock_calculate_distance):
        test_locations = [(37.31013773, -122.0314044, 44.6412353515625),
                          (37.31013774, -122.03140539, 44.76190185546875)]
        test_distances = [.016,]
        mock_cartesian_to_geodetic.side_effect = test_locations
        mock_calculate_distance.side_effect = test_distances
        csv_data_set = self.fileparser.parse_csv('GpsDataAnalyzer/test/test_csv _one_sim.csv')
        self.assertEqual(csv_data_set, self.csv_gpsdataset1)

    def tearDown(self):
        self.xml_fileparser = None  
        self.invalid_fileparser = None


if __name__ == "__main__":
    unittest.main()
