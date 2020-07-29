"""Unit test for FileParser Class"""
import unittest
from datetime import datetime
from datetime import timezone

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
                                                       startime=datetime(2020, 7, 7, 18,45,47,5000, tzinfo=timezone.utc),
                                                       endtime=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc)),
                               gpsdatalist=[GpsData(latitude= 37.31013773,
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

    def test_get_file_type(self):
        xml_file_type = self.fileparser.get_file_type('testfile2.xml')
        self.assertEqual("xml", xml_file_type)
        
        invalid_file_type = self.fileparser.get_file_type('wrongfilename.')
        self.assertEqual('', invalid_file_type)

        none_file_type = self.fileparser.get_file_type(None)
        self.assertIsNone(none_file_type)
        
    def test_parse_xml_file_success(self):
        xml_gpsdataset = self.fileparser.parse_file('testfile2.xml')
        self.assertEqual(5, len(xml_gpsdataset.gpsdatalist))
        self.assertEqual(self.xml_gpsdataset, xml_gpsdataset)
        self.assertEqual(GpsMetaData(device='salmon',
                                                       identifier='PXDB.200528.004',
                                                       manufacturer='Compal',
                                                       model='Suunto 7',
                                                       startime=datetime(2020, 7, 7, 18,45,47,5000, tzinfo=timezone.utc),
                                                       endtime=datetime(2020,7,7,19,8,1,318000,tzinfo=timezone.utc)), xml_gpsdataset.gpsmetadata)

    def test_parse_invalid_file_return_none(self):
        none_gpsdataset = self.fileparser.parse_file('wrongfilename.')
        self.assertIsNone(none_gpsdataset)

    def test_parse_csv_file_success(self):
        #Todo
        pass
    
    def tearDown(self):
        self.xml_fileparser = None  
        self.invalid_fileparser = None


if __name__ == "__main__":
    unittest.main()
