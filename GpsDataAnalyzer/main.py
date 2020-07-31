import sys

from fileparser import fileparser
from calculator import deviation_calculator
from visualizer import visualizer

for path in sys.path:
    print (path)

def main(args):
    # Create FileParser
    gps_fileparser = fileparser.FileParser()
    gpsdatasets = []

    # Parse files passed by the command line arguments
    for arg in args:
        print(arg)
        gpsdatasets.append(gps_fileparser.parse_file(arg))

    # Calculate deviation of wear gpsdataset with compared gpsdataset
    calculator = deviation_calculator.DataSetDeviationCalculator(gpsdatasets[0], gpsdatasets[1])

    # Get deviation dataframe and availability
    deviation_dataframe = calculator.get_deviation_dataframe()
    availability = calculator.get_availability()

    #Visualize the deviation
    my_visualizer = visualizer.Visualizer()

    classified_deviation_df = my_visualizer.classify_deviation(deviation_dataframe)

    #time could also be 'Set 1 Timestamp'
    time = classified_deviation_df['Common Timestamp']

    #draw the graphs and save as png files
    for column_name, column_data in classified_deviation_df.iteritems():
        if column_name == 'Confidence':
            my_visualizer.draw_hist_graph(column_data, 'Count', 'Confidence', 'Distance Deviation Confidence', availability)
        elif column_name == 'Deviations':
            my_visualizer.draw_line_graph(time, 'Time', column_data, column_name, 'Distance Deviation')
        elif column_name == 'Altitude Differentials':
            my_visualizer.draw_line_graph(time, 'Time', column_data, column_name, 'Altitude Deviation')
        elif column_name == 'Speed Differentials':
            my_visualizer.draw_line_graph(time, 'Time', column_data, column_name, 'Speed Deviation')


if __name__ == "__main__":
    main(sys.argv[1:])
    
