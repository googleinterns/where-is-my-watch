"""
Usage: xmlparser.py

Parse the xml or csv file and generate a GpsDataSet
"""
import xml.etree.ElementTree as ET

from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsData
from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsMetaData
from GpsDataAnalyzer.gpsdataset.gpsdataset import GpsDataSet

class FileParser:   
    def __init__(self, filename):
        """
        Constructor of fileparser

        Args:
          filename: the filename //Could be replaced by file path
        """
        self.filename = filename

    def get_file_type(self) -> str:
        """
        Get the file type
        """
        # if filename replaced by file path, should use os.path.splittext(filepath)
        file_type = self.filename.split(".")[-1]

        if file_type is None:
            print("File extension is None!")
            return None

        return file_type


    def parse_file(self) -> GpsDataSet:
        """
        Parse the file according to the file type
        """
        file_type = self.get_file_type()
        
        if file_type == 'xml':
            return self.parse_xml()
        elif file_type == 'csv':
            return self.parse_csv()
        else:
            print("Invalid file type!")


    def parse_xml(self) -> GpsDataSet:
        """
        Traverses the xml tree and extract all the needed datafields for analysis

        Args:
          filename: name of the xml file

        Returns:
          a GpsDataSet
        """
    
        with open(self.filename, 'r') as xmlFile:
            xmlTree = ET.parse(xmlFile)
            
        root = xmlTree.getroot()
        """
        Get metadata information
        """
        metadata = root.find('{http://www.topografix.com/GPX/1/1}metadata')
        
        starttime = metadata.find('{http://www.topografix.com/GPX/1/1}time').text
        device = metadata.find('{http://www.topografix.com/GPX/1/1}device').text
        identifier = metadata.find('{http://www.topografix.com/GPX/1/1}id').text
        manufacturer = metadata.find('{http://www.topografix.com/GPX/1/1}manufacturer').text
        model = metadata.find('{http://www.topografix.com/GPX/1/1}model').text


        # Get every track point information
        xml_gpsdatalist = []
        for trkpt in root.find('{http://www.topografix.com/GPX/1/1}trk').find('{http://www.topografix.com/GPX/1/1}trkseg'):
                lat = trkpt.get('lat')
                lon = trkpt.get('lon')
     
                ele = trkpt.find('{http://www.topografix.com/GPX/1/1}ele').text
                speed = trkpt.find('{http://www.topografix.com/GPX/1/1}speed').text
                time = trkpt.find('{http://www.topografix.com/GPX/1/1}time').text

                dataPoint = GpsData(lat, lon, ele, speed, time)
                xml_gpsdatalist.append(dataPoint)

        # Get end time information
        endtime = root.find('{http://www.topografix.com/GPX/1/1}time').text

        # Create the GpsMetaData
        xml_gpsmetadata = GpsMetaData(device, identifier, manufacturer, model, starttime, endtime)

        # Create the GpsDataSet
        xml_gpsdataset = GpsDataSet(xml_gpsmetadata, xml_gpsdatalist)
        
        xmlFile.close()
        return xml_gpsdataset

   #Todo
    def parse_csv(self) -> GpsDataSet:
        return None

