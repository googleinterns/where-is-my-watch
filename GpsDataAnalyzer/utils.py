"""
Utils for comparisons/conversions of locations and times
"""
from datetime import timedelta
from geopy import distance
import math

# World Geodetic System defined constants
_WGS84_EARTH_RADIUS = 6378137.0
_WGS84_ECCENTRICITY = 0.0818191908426

def calculate_distance(location1, location2):
  """Calculate geodesic distance between two coordinates with ellipsoidal earth model.

  Args:
    location1: tuple of (latitude, longitude) as floats in Decimal Degrees
    location2: tuple of (latitude, longitude) as floats in Decimal Degrees
  Returns:
    A float in meters of the distance between the two points
  """
  return distance.geodesic(location1, location2).meters

def cartesian_to_geodetic(x, y, z):
  """Convert a ECEF cartesian coordinate to a lat/lng/alt geodetic coordinate.

  Produces a geodetic coordinate (lat, lon, alt) from a
  earth-centered, earth-fixed (ECEF) cartesian coordinates from and was adapted
  from c code in bladeGPS:
  https://github.com/osqzss/bladeGPS/blob/master/gpssim.c

  Args:
    x: float, x coordinate
    y: float, y coordinate
    z: float, z coordinate
  Returns:
    A tuple of (latitude, longitude, altitude) with latitude and longitude
    as floats in Decimal Degrees and altitiude as a float in meters
  """
  eps = 1.0 * 10**-3  # convergence criteria
  eccentricity_sq = _WGS84_ECCENTRICITY**2

  norm_vector = math.sqrt(x*x+y*y+z*z)
  if (norm_vector < eps):
    # Invalid ECEF vector
    return (0.0, 0.0, -_WGS84_EARTH_RADIUS)

  rho_sq = x*x + y*y
  dz = eccentricity_sq*z

  while True:
    zdz = z + dz
    nh = math.sqrt(rho_sq + zdz*zdz)
    sin_lat = zdz / nh
    n = _WGS84_EARTH_RADIUS / math.sqrt(1.0-eccentricity_sq*sin_lat*sin_lat)
    dz_new = n*eccentricity_sq*sin_lat

    if (math.fabs(dz-dz_new) < eps):
      break

    dz = dz_new

  latitude = math.degrees(math.atan2(zdz, math.sqrt(rho_sq)))
  longitude= math.degrees(math.atan2(y, x))
  altitude = nh - n

  return (latitude, longitude, altitude)

def round_time(time):
  """
  Round time to nearest second.

  Args:
    time: Datetime object

  Returns:
    roundedD atetime object
  """
  if time.microsecond >= 500000:
    time = time + timedelta(seconds=1)
  time = time.replace(microsecond=0)
  return time
