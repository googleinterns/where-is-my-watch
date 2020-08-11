from datetime import datetime
from datetime import timezone
import unittest

from GpsDataAnalyzer import utils

class CoordinateConversionTest(unittest.TestCase):

  def coordinate_assertions(self, geodetic_coordinate, ecef_coordinate):
    lat, lon, alt = geodetic_coordinate
    x, y, z = ecef_coordinate

    result = utils.cartesian_to_geodetic(x, y, z)

    self.assertAlmostEqual(result[0], lat, places=5)
    self.assertAlmostEqual(result[1], lon, places=5)
    self.assertAlmostEqual(result[2], alt, places=3)

  def test_cartesian_to_geodetic_mountainview(self):
    geodetic_mountainview = (37.4178134, -122.086011, 3.45)
    ecef_mountainview = (-2694180.667, -4297222.330, 3854325.576)

    self.coordinate_assertions(geodetic_mountainview, ecef_mountainview)

  def test_cartesian_to_geodetic_negative_altitude(self):
    geodetic_mountainview_negative_altitude = (37.4211366, -122.0936967, -10.000)
    ecef_mountainview_negative_altitude = (-2694632.326, -4296661.975, 3854610.329)

    self.coordinate_assertions(geodetic_mountainview_negative_altitude, ecef_mountainview_negative_altitude)

  def test_cartesian_to_geodetic_shanghai(self):
    geodetic_shanghai = (31.230441, 121.467685, 4.5)
    ecef_shanghai = (-2849585.509, 4655993.331, 3287769.376)

    self.coordinate_assertions(geodetic_shanghai, ecef_shanghai)


class CalculateDistanceTest(unittest.TestCase):

  def test_calculate_zero_distance(self):
    coordinate_1 = (37.1111, -122.1124)
    coordinate_2 = (37.1111, -122.1124)

    result = utils.calculate_distance(coordinate_1, coordinate_2)

    self.assertAlmostEqual(result, 0.0, places=8)

  def test_calculate_distance(self):
    coordinate_1 = (37.1111, -122.1124)
    coordinate_2 = (37.1112, -122.1125)

    result = utils.calculate_distance(coordinate_1, coordinate_2)

    self.assertAlmostEqual(result, 14.2184735, places=3)

class RoundTimeTest(unittest.TestCase):

  def test_round_time_rounds(self):
    unrounded_time = datetime(2020,7,7,18,46,36,883732,tzinfo=timezone.utc)
    rounded_time = datetime(2020,7,7,18,46,37,tzinfo=timezone.utc)

    result = utils.round_time(unrounded_time)

    self.assertEqual(rounded_time, result)

  def test_round_time_rounds_up(self):
    unrounded_time = datetime(2020,7,7,18,46,36,500000,tzinfo=timezone.utc)
    rounded_time = datetime(2020,7,7,18,46,37,tzinfo=timezone.utc)

    result = utils.round_time(unrounded_time)

    self.assertEqual(rounded_time, result)

  def test_round_time_rounds_down(self):
    unrounded_time = datetime(2020,7,7,18,46,36,499999,tzinfo=timezone.utc)
    rounded_time = datetime(2020,7,7,18,46,36,tzinfo=timezone.utc)

    result = utils.round_time(unrounded_time)

    self.assertEqual(rounded_time, result)

if __name__ == '__main__':
    unittest.main()
