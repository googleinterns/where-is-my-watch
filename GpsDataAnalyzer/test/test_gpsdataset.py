"""Unit test for GpsDataSet Class"""
import unittest
from datetime import datetime
from datetime import timezone

from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

class GpsDataSetTest(unittest.TestCase):

    def setUp(self):
        self.gps_meta_data = GpsMetaData(device='sampleDevice',
                                                         identifier='sampleId',
                                                         manufacturer='sampleManufacturer',
                                                         model='sampleModel',
                                                         start_time=datetime(2020,7,7,18,45,47,5000,tzinfo=timezone.utc),
                                                         end_time=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc))
        self.gpsdata1 = GpsData(63.17964, -174.12954, 4.91, 0.0, datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f"))
        self.gpsdata2 = GpsData(63.17965, -174.12955, 4.91, 0.0, datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f"), 0.0, 8, 35.026, 32.124, 29.458, 27.123, 30.93275)
        self.gps_data_list = []
        self.gps_data_list.append(self.gpsdata1)
        self.gps_data_list.append(self.gpsdata2)
        self.gpsdataset = GpsDataSet(self.gps_meta_data, self.gps_data_list)

    def test_gpsdataset(self):
        self.assertEqual(self.gpsdataset.gps_meta_data, self.gps_meta_data)
        self.assertEqual(self.gpsdataset.gps_data_list, self.gps_data_list)

        self.assertEqual(self.gpsdataset.gps_data_list[0], self.gpsdata1)
        self.assertEqual(0.0, self.gpsdata1.distance)
        self.assertEqual(0, self.gpsdata1.satellites)
        self.assertEqual(0.0, self.gpsdata1.first_signal)
        self.assertEqual(0.0, self.gpsdata1.second_signal)
        self.assertEqual(0.0, self.gpsdata1.third_signal)
        self.assertEqual(0.0, self.gpsdata1.forth_signal)
        self.assertEqual(0.0, self.gpsdata1.average_signal)


        self.assertEqual(self.gpsdataset.gps_data_list[1], self.gpsdata2)
        self.assertEqual(0.0, self.gpsdata2.distance)
        self.assertEqual(8, self.gpsdata2.satellites)
        self.assertEqual(35.026, self.gpsdata2.first_signal)
        self.assertEqual(32.124, self.gpsdata2.second_signal)
        self.assertEqual(29.458, self.gpsdata2.third_signal)
        self.assertEqual(27.123, self.gpsdata2.forth_signal)
        self.assertEqual(30.93275, self.gpsdata2.average_signal)
        

 
    def tearDown(self):
        self.data = None  


if __name__ == "__main__":
    unittest.main()
