# WallPanel-Hubtat-Driver
Wall panel is android app for smart dashboard is free and work really well whit SharpTool or Hubitat dashboard, this driver with a combination of an MQTT broker will allow 2 way communication with the android table capable of receive sensor information transmitted from the tablet (motion and face detection, light, magnetic field, pressure, humidity) if your table have the sensor build in for motion and face the table will use the camera, this driver also allow control some of function of the table and will send  Google Text-to-Speech command that will be audible on the table.

http://thanksmister.com/wallpanel-android/



what you need?
- Hubitat hub
- MQTT Broker https://mosquitto.org/ will run in almos anything. is a lot of free and pay option online but use at your on risk
- this driver

Installation steps:
- install app on the table and use setting to configure to use your MQTT broker 
- add driver to your hub and set it up on the same MQTT broker
