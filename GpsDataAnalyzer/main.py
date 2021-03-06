from datetime import datetime
import itertools
import sys
import os

from GpsDataAnalyzer.fileparser import fileparser
from GpsDataAnalyzer.calculator import deviation_calculator
from GpsDataAnalyzer.visualizer import visualizer
from GpsDataAnalyzer.mylogger import MyLogger


for path in sys.path:
    print (path)

# Create my logger
MAIN_LOGGER = MyLogger('Main')
LOGGER = MAIN_LOGGER.get_logger()

def main(args):
    # Check args amount is correct
    if args is None:
        LOGGER.debug('No file argument, please pass two file arguments.')
        return
    
    if len(args) != 2:
        LOGGER.debug('File arguments number is wrong, please pass exactly two file arguments.')
        return 
    
    # Create FileParser
    LOGGER.debug('Create FileParser')
    gps_fileparser = fileparser.FileParser()
    gpsdatasets = []

    # Parse files passed by the command line arguments
    for arg in args:
        LOGGER.debug("File argument: {}".format(arg))
        parsed_result = gps_fileparser.parse_file(arg)
        datasets = parsed_result if isinstance(parsed_result, list) else [parsed_result]
        gpsdatasets.append(datasets)

    # Get all pairs of datasets between the two files
    dataset_combinations = itertools.product(*gpsdatasets)

    # Visualize the deviation
    LOGGER.debug('Create visualizer')
    my_visualizer = visualizer.Visualizer()

    for dataset_pair in dataset_combinations:

        device_1 = dataset_pair[0].gps_meta_data.device.capitalize()
        device_2 = dataset_pair[1].gps_meta_data.device.capitalize()

        # Move to next dataset pair if one or both datasets are empty
        if not dataset_pair[0].gps_data_list or not dataset_pair[1].gps_data_list:
            empty_datasets = [dataset.gps_meta_data.device for dataset in dataset_pair if not dataset.gps_data_list]
            LOGGER.debug('Gps Data List is empty for %s, cannot calculate deviations' % empty_datasets)
            continue

        # Calculate deviation of wear gpsdataset with compared gpsdataset
        LOGGER.debug('Create calculator')
        calculator = deviation_calculator.DataSetDeviationCalculator(*dataset_pair)

        # Get deviation dataframe and availability
        LOGGER.debug('Get deviation dataframe and availability')
        deviation_dataframe = calculator.get_deviation_dataframe()
        availability = calculator.get_availability()

        # Do not create visualizations for pairs that do not align
        if availability == 0:
            LOGGER.debug('%s and %s do not align.' % (device_1, device_2))
            continue

        classified_deviation_df = my_visualizer.classify_deviation(deviation_dataframe)

        #time could also be 'Set 1 Timestamp'
        time = classified_deviation_df['Common Timestamp']

        #draw the graphs and save as png files
        LOGGER.debug('Start drawing graphs...')
        
        dataset_title = "%s VS %s " % (device_1, device_2) 
        my_visualizer.draw_hist_graph(classified_deviation_df['Confidence'], 'Count', 'Confidence',  dataset_title + 'Distance', availability)
        my_visualizer.draw_line_graph(time, 'Time Duration: ', classified_deviation_df['Distance Deviations'], 'Deviations (Meters)', dataset_title + 'Distance')
        my_visualizer.draw_line_graph(time, 'Time Duration: ', classified_deviation_df['Altitude Deviations'], 'Deviations (Meters)', dataset_title + 'Altitude')
        my_visualizer.draw_line_graph(time, 'Time Duration: ', classified_deviation_df['Speed Deviations'], 'Deviations (Meters)', dataset_title + 'Speed')
        my_visualizer.draw_line_graph(time, 'Time Duration: ', classified_deviation_df['Signal Deviations'], 'Deviations (SNR)', dataset_title + 'Satellites Signal Strength')

        my_visualizer.draw_lines_graph(time, 'Time Duration: ', classified_deviation_df['Set 1 Average Signal'], classified_deviation_df['Set 2 Average Signal'],
            'Average Signal (SNR)', dataset_title + 'Average Satellites Signal', device_1, device_2)

        # save deviation dataframe to csv
        deviation_data_file = "{}Deviation_Data_{}.csv".format(dataset_title, datetime.strftime(datetime.now(), "%Y-%m-%dT%H%M%S"))
        data_file_path = os.path.join(my_visualizer.output_file_folder, deviation_data_file)
        classified_deviation_df.to_csv(data_file_path)

    LOGGER.debug('Finish drawing graphs and save them in visualizer directory.')

if __name__ == "__main__":
    main(sys.argv[1:])
