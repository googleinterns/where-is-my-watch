"""
Usage: fileparser.py

Parse the xml or csv file and generate a GpsDataSet
"""
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta
from datetime import timezone
from geopy.distance import geodesic
import math

from GpsDataAnalyzer.mylogger import MyLogger
from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

import sys
for path in sys.path:
    print (path)

#Create my logger
fileparser_logger = MyLogger('FileParser')
logger = fileparser_logger.get_logger()

#prefix url in xml file
PREFIX_URL = "{http://www.topografix.com/GPX/1/1}"

class FileParser: 

    def _get_file_type(self, filename) -> str:
        """
        Get the file type
        """
        # if filename replaced by file path, should use os.path.splittext(filepath)
        if filename is None:
            logger.debug('Filename is None.')
            return None
        
        file_type = filename.split(".")[-1]

        if file_type is None:
            logger.debug('File extension is None.')
            return None
        
        return file_type
    

    def parse_file(self, filename) -> GpsDataSet:
        """
        Parse the file according to the file type
        """
        file_type = self.get_file_type(filename)
        
        if file_type == 'xml':
            logger.info('Parse xml file ' + filename)
            return self.parse_xml(filename)
        elif file_type == 'csv':
            logger.info('Parse csv file ' + filename)
            return self.parse_csv(filename)
        else:
            logger.debug('Invalid file type. Accepted: xml, csv. Received: ' + file_type)
            return None


    def parse_time(self, timestr) -> datetime:
        """
        Parse the xml timestamp and return a new datetime object

        Args:
          timestr: string of the timestamp extracted from xml file

        Returns:
          A datetime object with timezone
        """
        #Replace timezone Z with '' since python datetime parser could not handler %Z
        timestr = timestr.replace('Z', '', 1)
        
        #Parse time in the format "%Y-%m-%dT%H:%M:%S.%f"
        time = datetime.strptime(timestr, "%Y-%m-%dT%H:%M:%S.%fZ")

        return time.replace(tzinfo=timezone.utc)



    def calculate_distance(self, location1, location2) -> float:
        """
        Calculate 3D distance (including altitude) between two location points with ellipsoidal earth model

        Args:
          location1: tuple of (latitude, longitude) as floats in Decimal Degrees
          location2: tuple of (latitude, longitude) as floats in Decimal Degrees

        Returns:
           A float in meters of the distance between two location points
        """
        return geodesic(location1, location2).meters


    def parse_xml_metadata(self, root, prefix) -> GpsMetaData:
        """
        Helper function to parse metadata information in xml file
        """
        # Get metadata information
        metadata = root.find(prefix + 'metadata')

        # Parse metadata information
        device, identifier, manufacturer, model = "", "", "", ""
        tz_starttime = None

        for data in metadata.iter():
            if data.tag == prefix + 'device':
                device = data.text
            elif data.tag == prefix + 'id':
                identifier = data.text
            elif data.tag == prefix + 'manufacturer':
                manufacturer = data.text
            elif data.tag == prefix + 'model':
                model = data.text
            elif data.tag == prefix + 'time':
                tz_starttime = self.parse_time(data.text)

        # Parse end timestamp
        endtimestr = root.find(prefix + 'time').text
        tz_endtime = self.parse_time(endtimestr)

        return GpsMetaData(device, identifier, manufacturer, model, tz_starttime, tz_endtime)


    def parse_xml_trkpts(self, root, prefix) -> []:
        """
        Helper function to parse trkpts in xml file
        """
        xml_gpsdatalist = []

        # Get the start location
        first_trkpt = root.find(prefix + 'trk').find(prefix + 'trkseg')[0]
        prev_location = (first_trkpt.get('lat'), first_trkpt.get('lon'))

        # Get every track point information
        for trkpt in root.find(prefix + 'trk').find(prefix + 'trkseg'):
            # Get the latitude and longitude
            lat = float(trkpt.get('lat'))
            lon = float(trkpt.get('lon'))

            # Calculate the distance from previous location, not considering the altitude for now
            cur_location = (lat, lon)
            distance = self.calculate_distance(cur_location, prev_location)
            prev_location = cur_location

            # Get the altitude, speed and time 
            alt, speed, tz_time = None, None, None
            for data in trkpt.iter():
                if data.tag == prefix + 'ele':
                    alt = float(data.text)
                elif data.tag == prefix + 'speed':
                    speed = float(data.text)
                elif data.tag == prefix + 'time':
                    tz_time = self.parse_time(data.text)
                        
            dataPoint = GpsData(lat, lon, alt, speed, tz_time, distance)
            xml_gpsdatalist.append(dataPoint)

        return xml_gpsdatalist


    #TODO: TBD(@lynnzl) replace with variable file path
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

        # Create the xml gpsmetadata
        xml_gpsmetadata = self.parse_xml_metadata(root, PREFIX_URL)

        # Create the xml gpsdatalist
        xml_gpsdatalist = self.parse_xml_trkpts(root, PREFIX_URL)

        # Create the GpsDataSet
        return GpsDataSet(xml_gpsmetadata, xml_gpsdatalist)


   #Todo
    def parse_csv(self, filename) -> GpsDataSet:
        return None
