# Where is my watch

This project contains tools to evaluate GPS performance of Wear OS Watch

# GpsDataCapturer
The GpsDataCapturer is an app works on both watch and phone with following features: 
  * Capture GPS data every second from watch and phone 
  * Generate a xml file with captured GPS data
  * Update GPS data on UI 

The source code is supplied as an Android Studio project that can be built and run with [Android Studio](https://developer.android.com/studio)
  
# GpsDataAnalyzer 
The GpsDataAnalyzer is a tool help us to analyze the GPS data files: 
  * Parse xml file and csv file generated from GpsDataCapturer/[Geobeam Simulator](https://github.com/googleinterns/geobeam)
  * Compute differentials of the two GPS datasets
  * Visualize the diferentials

To run this program, you need to install [python3.8 version](https://www.python.org/downloads/) 

# Copyright Notice
Copyright 2020 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

