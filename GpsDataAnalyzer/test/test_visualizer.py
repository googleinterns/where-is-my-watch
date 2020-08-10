import unittest
from unittest.mock import patch
import pytest
import pandas as pd
from datetime import datetime
from datetime import timezone

from GpsDataAnalyzer.visualizer import visualizer


class VisualizerTest(unittest.TestCase):
    def setUp(self):
        time_list = [datetime(2020, 7, 22, 15, 49, 4, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 5, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 6, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 7, tzinfo=timezone.utc),
                 datetime(2020, 7, 22, 15, 49, 8, tzinfo=timezone.utc)]
        self.deviation_list = [1.0, 5.0, 10.0, 3.0, 15.0]
        self.speed_differentials = [1.0, 0.0, 0.0, 0.0, 0.0]
        self.altitude_differentials = [.3, 0.0, 0.0, 0.0, 0.0]
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
        self.deviations_dataframe = pd.DataFrame({"Common Timestamp": time_list,
                                             "Deviations": self.deviation_list,
                                             "Speed Differentials": self.speed_differentials,
                                             "Altitude Differentials": self.altitude_differentials,
                                             "Set 1 Timestamp": set1_time_list,
                                             "Set 2 Timestamp": set2_time_list})
        
        with patch('os.mkdir') as mock_mkdir:
            self.visualizer = visualizer.Visualizer()

    def test_classify_deviation(self):
        expected_confidence = [3, 3, 2, 3, 1]

        classified_df = self.visualizer.classify_deviation(self.deviations_dataframe)

        for i in range(0,5):
          self.assertEqual(expected_confidence[i], classified_df['Confidence'][i])

    def test_get_min_deviation(self):
        self.assertEqual(1.0, self.visualizer.get_min_deviation(self.deviation_list))
        self.assertEqual(0.0, self.visualizer.get_min_deviation(self.speed_differentials))
        self.assertEqual(0.0, self.visualizer.get_min_deviation(self.altitude_differentials))

    def test_get_max_deviation(self):
        self.assertEqual(15.0, self.visualizer.get_max_deviation(self.deviation_list))
        self.assertEqual(1.0, self.visualizer.get_max_deviation(self.speed_differentials))
        self.assertEqual(0.3, self.visualizer.get_max_deviation(self.altitude_differentials))

    def test_draw_hist_graph(self):
        #Todo
        pass

    def test_draw_line_graph(self):
        #Todo
        pass
    
if __name__ == "__main__":
    unittest.main()
