"""Unit test for FileParser Class"""
import unittest

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

    def test_parse_csv_file_success(self):
        #Todo
        self.csv_fileparser = FileParser('testfile2.xml')
        self.csv_fileparser.parse_csv()
    
    def tearDown(self):
        self.xml_fileparser = None  
        self.invalid_fileparser = None


if __name__ == "__main__":
    unittest.main()
