"""Unit test for FileParser Class"""
import unittest

from GpsDataAnalyzer.fileparser.fileparser import FileParser
from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsDataSet
from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsData
from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsMetaData

class FileParserTest(unittest.TestCase):
    def setUp(self):
        self.xml_fileparser = FileParser('testfile2.xml')
        self.xml_gpsdataset = GpsDataSet(gpsmetadata=GpsMetaData(device='salmon',
                                                       identifier='PXDB.200528.004',
                                                       manufacturer='Compal',
                                                       model='Suunto 7',
                                                       startime='2020-07-07T18:45:47.005Z',
                                                       endtime='2020-07-07T19:08:01.318Z'),
                               gpsdatalist=[GpsData(latitude='37.31013773',
                                                    longitude='-122.0314044',
                                                    altitude='44.6412353515625',
                                                    speed='0.0',
                                                    time='2020-07-07T18:46:36.000Z'),
                                            GpsData(latitude='37.31013774',
                                                    longitude='-122.03140539',
                                                    altitude='44.76190185546875',
                                                    speed='0.16',
                                                    time='2020-07-07T18:46:37.000Z'),
                                            GpsData(latitude='37.31013822',
                                                    longitude='-122.0314067',
                                                    altitude='44.80401611328125',
                                                    speed='0.0',
                                                    time='2020-07-07T18:46:38.000Z'),
                                            GpsData(latitude='37.31013763',
                                                    longitude='-122.03140506',
                                                    altitude='44.564208984375',
                                                    speed='0.44',
                                                    time='2020-07-07T18:46:39.000Z'),
                                            GpsData(latitude='37.31013728',
                                                    longitude='-122.03140187',
                                                    altitude='44.97833251953125',
                                                    speed='0.21',
                                                    time='2020-07-07T18:46:40.000Z')])
        self.invalid_fileparser = FileParser('wrongfilename.')

    def test_get_file_type(self):
        xml_file_type = self.xml_fileparser.get_file_type()
        self.assertEqual("xml", xml_file_type)
        
        invalid_file_type = self.invalid_fileparser.get_file_type()
        self.assertEqual('', invalid_file_type)
        
    def test_parse_xml_file_success(self):
        xml_gpsdataset = self.xml_fileparser.parse_file()
        self.assertEqual(5, len(xml_gpsdataset.gpsdatalist))
        self.assertEqual(self.xml_gpsdataset, xml_gpsdataset)

    def test_parse_invalid_file_return_none(self):
        none_gpsdataset = self.invalid_fileparser.parse_file()
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
