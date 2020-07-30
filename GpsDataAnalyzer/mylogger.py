import logging 
import logging.config

class MyLogger:
	def __init__(self, TAG):
		# create logger
		self.logger = logging.getLogger(TAG)
		self.logger.setLevel(logging.DEBUG)

		# create console handler and set level to debug
		ch = logging.StreamHandler()
		ch.setLevel(logging.DEBUG)

		# create formatter
		formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

		# add formatter to ch
		ch.setFormatter(formatter)

		# add ch to logger
		self.logger.addHandler(ch)

	def get_logger(self):
		return self.logger
		
