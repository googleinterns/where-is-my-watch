"""
Usage: fileparser.py

Parse the xml or csv file and generate a GpsDataSet
"""
import xml.etree.ElementTree as ET
import csv
from datetime import datetime, timedelta
from datetime import timezone
from geopy.distance import geodesic
import math

from GpsDataAnalyzer import utils
from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet
from GpsDataAnalyzer.mylogger import MyLogger

# Create my logger
FILEPARSER_LOGGER = MyLogger('FileParser')
LOGGER = FILEPARSER_LOGGER.get_logger()

#prefix url in xml file
PREFIX_URL = "{http://www.topografix.com/GPX/1/1}"

class FileParser: 

    def _get_file_type(self, filename) -> str:
        """
        Get the file type
        """
        # if filename replaced by file path, should use os.path.splittext(filepath)
        if filename is None:
            LOGGER.debug('Filename is None.')
            return None
        
        file_type = filename.split(".")[-1]

        if file_type is None:
            LOGGER.debug('File extension is None.')
            return None

        return file_type

    def parse_file(self, filename) -> GpsDataSet:
        """
        Parse the file according to the file type
        """
        file_type = self._get_file_type(filename)
        
        if file_type == 'xml':
            LOGGER.info('Parse xml file ' + filename)
            return self.parse_xml(filename)
        elif file_type == 'csv':
            LOGGER.info('Parse csv file ' + filename)
            return self.parse_csv(filename)
        else:
            LOGGER.debug('Invalid file type. Accepted: xml, csv. Received: ' + file_type)
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
        if timestr[-1] == 'Z':
            timestr = timestr[:-1]

        #Parse time in the format "%Y-%m-%dT%H:%M:%S.%f"
        time = datetime.strptime(timestr, "%Y-%m-%dT%H:%M:%S.%f")

        return time.replace(tzinfo=timezone.utc)


    def parse_xml_metadata(self, root, prefix) -> GpsMetaData:
        """
        Helper function to parse metadata information in xml file
        """
        # Get metadata information
        metadata = root.find(prefix + 'metadata')
        if metadata is None:
            LOGGER.debug('Metadata is None, could not be parsed.')
            return None


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

        trk = root.find(prefix + 'trk')
        if trk is None:
            LOGGER.debug('trk is None, could not parse trkpts.')
            return None

        trkseg = trk.find(prefix + 'trkseg')
        if trkseg is None:
            LOGGER.debug('trkseg is None, could not parse trkpts.')
            return None

        if len(trkseg) == 0:
            LOGGER.debug('trkseg is empty, could not parse trkpts.')
            return None

        # Get the start location
        first_trkpt = trkseg[0]
        prev_location = (first_trkpt.get('lat'), first_trkpt.get('lon'))

        # Get every track point information
        for trkpt in trkseg:
            # Get the latitude and longitude
            lat = float(trkpt.get('lat'))
            lon = float(trkpt.get('lon'))

            # Calculate the distance from previous location, not considering the altitude for now
            cur_location = (lat, lon)
            distance = utils.calculate_distance(cur_location, prev_location)
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


    def parse_csv(self, filename) -> GpsDataSet:
        """
        Reads simulation metadata and data in from csv file and parses it.

        Args:
          filename: name of the csv file to parse

        Returns:
          a list of GpsDataSets
        """
        # reading in data
        with open(filename, 'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=",")
            simulation_list = []
            next_line = next(csvreader)
            # each simulation starts with a blank line in the csv file
            while next_line == []:
                current_simulation = {}
                try:
                    metadata_headers = next(csvreader)
                    metadata_values = next(csvreader)
                    current_simulation["metadata"] = dict(zip(metadata_headers,metadata_values))
                    next_line = next(csvreader)  
                    # start of data for dynamic or end of simulation info for static 
                    data_dicts = None
                    if next_line:
                        data_headers = next_line
                        next_line = next(csvreader)
                        current_simulation["data"] = []
                        while next_line:
                            # append a dictionary like {'time_from_zero': '2.9', 'x': '-2694168.947', 'y': '-4297224.127', 'z': '3854334.722'}
                            current_simulation["data"].append(dict(zip(data_headers,next_line)))
                            next_line = next(csvreader)
                    simulation_list.append(current_simulation)
                except StopIteration:
                    simulation_list.append(current_simulation)
                    break
        gps_data_sets = []
        for simulation in simulation_list:
            # create GpsMetaData object
            device = simulation["metadata"]["simulation_type"]
            start_time = self.parse_time(simulation["metadata"]["start_time"])
            end_time = self.parse_time(simulation["metadata"]["end_time"])
            gps_metadata = GpsMetaData(device, None, None, None, start_time, end_time)

            if device == "DynamicSimulation":
                gps_data_list = self.get_dynamic_simulation_data(simulation)
            else:
                gps_data_list = self.get_static_simulation_data(simulation)

            data_set = GpsDataSet(gps_metadata, gps_data_list)
            gps_data_sets.append(data_set)
        return gps_data_sets

    def get_static_simulation_data(self, simulation):
        """
        Reads simulation dictionary and pulls out info for Static Simulation data:

        Args:
          simulation: a dictionary {"metadata":{....}}

        Returns:
          A list containing the single data point broadcasted
        """
        latitude = float(simulation["metadata"]["latitude"])
        longitude = float(simulation["metadata"]["longitude"])
        altitude = 0.0
        time_stamp = self.parse_time(simulation["metadata"]["start_time"])
        speed = 0.0
        new_point = GpsData(latitude, longitude, altitude, speed, time_stamp)
        return [new_point]

    def get_dynamic_simulation_data(self, simulation):
        """
        Reads simulation dictionary and pulls out info for Static Simulation data:

        Args:
          simulation: a dictionary {"metadata":{....}, "data": [list of data_dicts]}

        Returns:
          A list containing the broadcasted GpsData points in order
        """
        gps_data_list = []
        start_time = self.parse_time(simulation["metadata"]["start_time"])
        for data_point in simulation["data"]:
            x, y, z = float(data_point["x"]), float(data_point["y"]), float(data_point["z"])
            time_from_zero = float(data_point["time_from_zero"])
            lat, lon, alt = utils.cartesian_to_geodetic(x, y, z)
            time_stamp = start_time + timedelta(seconds=time_from_zero)
            # calculate speed for the point
            if gps_data_list:
                previous_point = gps_data_list[-1]
                time_elapsed = (time_stamp-previous_point.time).total_seconds()
                distance = utils.calculate_distance((lat, lon), (previous_point.latitude, previous_point.longitude))
                speed = distance/time_elapsed
            else: 
                speed = 0.0  # first point has speed of zero
            gps_data_list.append(GpsData(lat, lon, alt, speed, time_stamp))
            
        return gps_data_list
