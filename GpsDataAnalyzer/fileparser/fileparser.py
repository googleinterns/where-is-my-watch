"""
Usage: xmlparser.py

Parse the xml or csv file and generate a GpsDataSet
"""
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta
from datetime import timezone

from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

import sys
for path in sys.path:
    print (path)

class FileParser:   

    def get_file_type(self, filename) -> str:
        """
        Get the file type
        """
        # if filename replaced by file path, should use os.path.splittext(filepath)
        file_type = filename.split(".")[-1]

        if file_type is None:
            print("File extension is None!")
            return None

        return file_type


    def parse_file(self, filename) -> GpsDataSet:
        """
        Parse the file according to the file type

        Args:
          filename: name of the file

        Returns:
          a GpsDataSet
        """
        file_type = self.get_file_type(filename)
        
        if file_type == 'xml':
            return self.parse_xml(filename)
        elif file_type == 'csv':
            return self.parse_csv(filename)
        else:
            print("Invalid file type!")
            return None


    def parse_xml(self, filename) -> GpsDataSet:
        """
        Traverses the xml tree and extract all the needed datafields for analysis

        Args:
          filename: name of the xml file

        Returns:
          a GpsDataSet
        """
    
        with open(filename, 'r') as xmlFile:
            xmlTree = ET.parse(xmlFile)
            
        root = xmlTree.getroot()

        #Get metadata information
        metadata = root.find('{http://www.topografix.com/GPX/1/1}metadata')
        
        #Create the starttime datetime object
        starttimestr = metadata.find('{http://www.topografix.com/GPX/1/1}time').text
        starttimestr = starttimestr.replace('Z', '', 1)
        starttime = datetime.strptime(starttimestr, "%Y-%m-%dT%H:%M:%S.%f")
        print(starttime)

        device = metadata.find('{http://www.topografix.com/GPX/1/1}device').text
        identifier = metadata.find('{http://www.topografix.com/GPX/1/1}id').text
        manufacturer = metadata.find('{http://www.topografix.com/GPX/1/1}manufacturer').text
        model = metadata.find('{http://www.topografix.com/GPX/1/1}model').text


        # Get every track point information
        xml_gpsdatalist = []
        for trkpt in root.find('{http://www.topografix.com/GPX/1/1}trk').find('{http://www.topografix.com/GPX/1/1}trkseg'):
                lat = float(trkpt.get('lat'))
                lon = float(trkpt.get('lon'))
     
                ele = float(trkpt.find('{http://www.topografix.com/GPX/1/1}ele').text)
                speed = float(trkpt.find('{http://www.topografix.com/GPX/1/1}speed').text)

                #Get the time string
                timestr = trkpt.find('{http://www.topografix.com/GPX/1/1}time').text
                timestr = timestr.replace('Z', '', 1)
                #Convert timestr to a datetime object
                time = datetime.strptime(timestr, "%Y-%m-%dT%H:%M:%S.%f")
                print(time)

                dataPoint = GpsData(lat, lon, ele, speed, time)
                xml_gpsdatalist.append(dataPoint)

        # Get end time string and convert to a datetime object
        endtimestr = root.find('{http://www.topografix.com/GPX/1/1}time').text
        endtimestr = endtimestr.replace('Z', '', 1)
        endtime = datetime.strptime(endtimestr, "%Y-%m-%dT%H:%M:%S.%f")
        print(endtime)
        
        # Create the GpsMetaData
        xml_gpsmetadata = GpsMetaData(device, identifier, manufacturer, model, starttime, endtime)

        # Create the GpsDataSet
        xml_gpsdataset = GpsDataSet(xml_gpsmetadata, xml_gpsdatalist)
        
        return xml_gpsdataset

   #Todo
    def parse_csv(self, filename) -> GpsDataSet:
        return None

