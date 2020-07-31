import unittest

from GpsDataAnalyzer.visualizer.visualizer import Visualizer

class VisualizerTest(unittest.TestCase):
    def setUp(self):
        time_list = [datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc)]
        deviation_list = [1.0, 5.0, 10.0, 3.0, 15.0]
        speed_differentials = [1.0, 0.0, 0.0, 0.0, 0.0]
        altitude_differentials = [.3, 0.0, 0.0, 0.0, 0.0]
        set1_time_list = [datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc)]
        set2_time_list = [datetime(2020, 7, 22, 15, 49, 1, 626803, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 2, 626803, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 3, 626803, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 4, 626803, tzinfo=timezone.utc),
                          datetime(2020, 7, 22, 15, 49, 5, 626803, tzinfo=timezone.utc)]
        deviations_dataframe = pd.DataFrame({"Common Timestamp": time_list,
                                             "Deviations": deviation_list,
                                             "Speed Differentials": speed_differentials,
                                             "Altitude Differentials": altitude_differentials,
                                             "Set 1 Timestamp": set1_time_list,
                                             "Set 2 Timestamp": set2_time_list})


    def test_classify_deviation(self):
        expected_confidence = [3, 3, 2, 3, 1]

        gps_visualizer = Visualizer()
        classified_df = gps_visualizer.classify_deviation(self.deviations_dataframe)

        assertEquals(expected_confidence, classified_df['Confidence'])


    

        

