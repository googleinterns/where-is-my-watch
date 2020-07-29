"""Unit test for GpsDataSet Class"""
import unittest
from datetime import datetime

from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

class GpsDataSetTest(unittest.TestCase):

    def setUp(self):
        self.gps_meta_data = GpsMetaData("salmon", "PXDB.200528.004", "Compal", "Suunto 7", datetime.strptime('2020-07-07T18:45:47.005', "%Y-%m-%dT%H:%M:%S.%f"), datetime.strptime('2020-07-07T19:08:01.318', "%Y-%m-%dT%H:%M:%S.%f"))
        self.gpsdata1 = GpsData(37.31013773, -122.0314044, 44.6412353515625, 0.0, datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f"))
        self.gpsdata2 = GpsData(37.31013773, -122.0314044, 44.6412353515625, 0.0, datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f"), 0.0, True)
        self.gps_data_list = []
        self.gps_data_list.append(self.gpsdata1)
        self.gps_data_list.append(self.gpsdata2)
        self.gpsdataset = GpsDataSet(self.gps_meta_data, self.gps_data_list)

    def test_gpsdataset(self):
        self.assertEqual(self.gpsdataset.gps_meta_data, self.gps_meta_data)
        self.assertEqual(self.gpsdataset.gps_data_list, self.gps_data_list)
        self.assertEqual(self.gpsdataset.gps_data_list[0], self.gpsdata1)
        self.assertEqual(self.gpsdataset.gps_data_list[1], self.gpsdata2)
        self.assertEqual(0.0, self.gpsdata1.distance)
        self.assertEqual(0.0, self.gpsdata2.distance)
        self.assertFalse(self.gpsdataset.gps_data_list[0].is_outlier)
        self.assertTrue(self.gpsdataset.gps_data_list[1].is_outlier)
 
    def tearDown(self):
        self.data = None  


if __name__ == "__main__":
    unittest.main()
