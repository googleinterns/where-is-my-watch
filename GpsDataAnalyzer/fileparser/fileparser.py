"""
Usage: xmlparser.py

Parse the xml or csv file and generate a GpsDataSet
"""
import xml.etree.ElementTree as ET
import csv
from datetime import datetime, timedelta
from datetime import timezone

from GpsDataAnalyzer.fileparser import utils
from GpsDataAnalyzer.datamodel.gpsdataset import GpsData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsMetaData
from GpsDataAnalyzer.datamodel.gpsdataset import GpsDataSet

import sys
for path in sys.path:
    print (path)

class FileParser:   
    def __init__(self):
        """
        Constructor of fileparser

        Args:
          filename: the filename //Could be replaced by file path
        """

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
                timestr = trkpt.find('{http://www.topografix.com/GPX/1/1}time').text

                #replace the last ':' with an empty string, as python UTC offset format is +HHMM
                # timestr = timestr[::-1].replace(':', '', 1)[::-1]
                # print(timestr)
                # # try:
                # offset = int(timestr[-5:])
                # # except:
                # #     print("Error")
                # delta = timedelta(hours = offset / 100)
                timestr = timestr.replace('Z', '', 1)
                #Convert timestr to a datetime object
                time = datetime.strptime(timestr, "%Y-%m-%dT%H:%M:%S.%f")
                # time -= delta
                time.replace(tzinfo=timezone.utc)
                print(time)

                dataPoint = GpsData(lat, lon, ele, speed, time)
                xml_gpsdatalist.append(dataPoint)

        # Get end time information
        endtimestr = root.find('{http://www.topografix.com/GPX/1/1}time').text
        endtimestr = endtimestr.replace('Z', '', 1)
        endtime = datetime.strptime(endtimestr, "%Y-%m-%dT%H:%M:%S.%f")
        print(endtime)
        
        # Create the GpsMetaData
        xml_gpsmetadata = GpsMetaData(device, identifier, manufacturer, model, starttime, endtime)

        # Create the GpsDataSet
        xml_gpsdataset = GpsDataSet(xml_gpsmetadata, xml_gpsdatalist)
        
        return xml_gpsdataset

    def parse_csv(self) -> GpsDataSet:
        """
        Reads simulation metadata and data in from csv file and parses it.

        Args:
          filename: name of the csv file to parse

        Returns:
          a list of GpsDataSets
        """
        # reading in data
        # TODO(ameles) replace with variable file path
        with open("/home/ameles/Desktop/GPS-simulator/geobeam-dev/simulation_logs/GPSSIM-2020-07-21_15:54:46", 'r') as csvfile:
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
                    if next_line:
                        data_headers = next_line
                        data_dicts = []
                        next_line = next(csvreader)
                        while next_line:
                            # append a dictionary like {'time_from_zero': '2.9', 'x': '-2694168.947', 'y': '-4297224.127', 'z': '3854334.722'}
                            data_dicts.append(dict(zip(data_headers,next_line)))
                            next_line = next(csvreader)
                        current_simulation["data"] = data_dicts
                    simulation_list.append(current_simulation)
                except StopIteration:
                    simulation_list.append(current_simulation)
                    break
        gps_data_sets = []
        for simulation in simulation_list:
            # create GpsMetaData object
            device = simulation["metadata"]["simulation_type"]
            start_time = self.string_to_datetime(simulation["metadata"]["start_time"])
            end_time = self.string_to_datetime(simulation["metadata"]["end_time"])
            gps_metadata = GpsMetaData(device, None, None, None, start_time, end_time)

            if "data" in simulation:
                gps_data_list = self.get_dynamic_simulation_data(simulation)
            else:
                gps_data_list = self.get_static_simulation_data(simulation)

            data_set = GpsDataSet(gps_metadata, gps_data_list)
            gps_data_sets.append(data_set)
        return gps_data_sets

    def get_static_simulation_data(self, simulation):
        latitude = simulation["metadata"]["latitude"]
        longitude = simulation["metadata"]["longitude"]
        altitude = 0
        time_stamp = self.string_to_datetime(simulation["metadata"]["start_time"])
        speed = 0
        new_point = GpsData(latitude, longitude, altitude, speed, time_stamp)
        return [new_point]

    def get_dynamic_simulation_data(self, simulation):
        gps_data_list = []
        start_time = self.string_to_datetime(simulation["metadata"]["start_time"])
        for data_point in simulation["data"]:
            x, y, z = float(data_point["x"]), float(data_point["y"]), float(data_point["z"])
            time_from_zero = float(data_point["time_from_zero"])
            lat, lon, alt = utils.cartesian_to_geodetic(x, y, z)
            time_stamp = start_time + datetime.timedelta(seconds=time_from_zero)
            if gps_data_list:
                previous_point = gps_data_list[-1]
                time_elapsed = (time_stamp-previous_point.time).total_seconds()
                distance = utils.calculate_distance((lat, lon), (previous_point.latitude, previous_point.longitude))
                speed = distance/time_elapsed
            else: speed = 0
            gps_data_list.append(GpsData(lat, lon, alt, speed, time_stamp))
        return gps_data_list

    def string_to_datetime(self, date_string):
        return datetime.datetime.strptime(date_string, '%Y-%m-%dT%H:%M:%S.%f')

