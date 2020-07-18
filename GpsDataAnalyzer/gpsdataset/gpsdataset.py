from dataclasses import dataclass

@dataclass
class GpsData:
	"""A GPS Data class"""
	latitude: str
	longitude: str
	altitude: str
	speed: float
	time: str


@dataclass
class GpsMetaData:
	"""A Meta Data class includes device/id/manufacturer/model/captureDuration attributes"""
	device: str
	identifier: str
	manufacturer: str
	model: str
	startime: str
	endtime: str


@dataclass
class GpsDataSet:
    """A GPS Data Set class includes gpsMeataData and list of GpsData"""
    gpsmetadata: GpsMetaData
    gpsdatalist: []


