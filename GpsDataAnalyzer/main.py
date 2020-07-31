import sys

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
        LOGGER.debug('File argument: ' + arg)
        gpsdatasets.append(gps_fileparser.parse_file(arg))

    # Calculate deviation of wear gpsdataset with compared gpsdataset
    LOGGER.debug('Create calculator')
    calculator = deviation_calculator.DataSetDeviationCalculator(gpsdatasets[0], gpsdatasets[1])

    # Get deviation dataframe and availability
    LOGGER.debug('Get deviation dataframe and availability')
    deviation_dataframe = calculator.get_deviation_dataframe()
    availability = calculator.get_availability()

    #Visualize the deviation
    LOGGER.debug('Create visualizer')
    my_visualizer = visualizer.Visualizer()

    classified_deviation_df = my_visualizer.classify_deviation(deviation_dataframe)

    #time could also be 'Set 1 Timestamp'
    time = classified_deviation_df['Common Timestamp']

    #draw the graphs and save as png files
    LOGGER.debug('Start drawing graphs...')
    for column_name, column_data in classified_deviation_df.iteritems():
        if column_name == 'Confidence':
            my_visualizer.draw_hist_graph(column_data, 'Count', column_name, 'Distance Deviation Confidence', availability)
        elif column_name == 'Deviations':
            my_visualizer.draw_line_graph(time, 'Time Duration: ', column_data, column_name + ' (Meters)', 'Distance Deviation')
        elif column_name == 'Altitude Differentials':
            my_visualizer.draw_line_graph(time, 'Time Duration: ', column_data, column_name + ' (Meters)', 'Altitude Deviation')
        elif column_name == 'Speed Differentials':
            my_visualizer.draw_line_graph(time, 'Time Duration: ', column_data, column_name + ' (Meters/Second)', 'Speed Deviation')
    LOGGER.debug('Finish drawing graphs and save them in visualizer directory.')

if __name__ == "__main__":
    main(sys.argv[1:])
