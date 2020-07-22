"""Unit test for GpsDataSet Class"""
import unittest
from datetime import datetime

from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

class GpsDataSetTest(unittest.TestCase):

    def setUp(self):
        self.gpsmetadata = GpsMetaData("salmon", "PXDB.200528.004", "Compal", "Suunto 7", datetime.strptime('2020-07-07T18:45:47.005', "%Y-%m-%dT%H:%M:%S.%f"), datetime.strptime('2020-07-07T19:08:01.318', "%Y-%m-%dT%H:%M:%S.%f"))
        gpsdata1 = GpsData(37.31013773, -122.0314044, 44.6412353515625, 0.0, datetime.strptime('2020-07-07T18:46:36.000', "%Y-%m-%dT%H:%M:%S.%f"))
        self.gpsdatalist = []
        self.gpsdatalist.append(gpsdata1)
        self.gpsdataset = GpsDataSet(self.gpsmetadata, self.gpsdatalist)

    def test_gpsdataset(self):
        self.assertEqual(self.gpsdataset.gpsmetadata, self.gpsmetadata)
        self.assertEqual(self.gpsdataset.gpsdatalist, self.gpsdatalist)    
 
    def tearDown(self):
        self.data = None  


if __name__ == "__main__":
    unittest.main()