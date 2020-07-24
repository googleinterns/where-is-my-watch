from dataclasses import dataclass
import datetime

@dataclass
class GpsData:
	"""A GPS Data class"""
	latitude: float
	longitude: float
	altitude: float
	speed: float
	time: datetime


@dataclass
class GpsMetaData:
	"""A Meta Data class includes device/id/manufacturer/model/captureDuration attributes"""
	device: str
	identifier: str
	manufacturer: str
	model: str
	start_time: datetime
	end_time: datetime


@dataclass
class GpsDataSet:
    """A GPS Data Set class includes gpsMeataData and list of GpsData"""
    gps_meta_data: GpsMetaData
    gps_data_list: []


