metadata {
    definition (name: "WallPanelDriver_MQTT", , namespace: "jorge.martinez", author: "Jorge Martinez"){
        capability "Initialize"
		capability "Sensor"
		capability "MotionSensor"
		capability "IlluminanceMeasurement"
		capability "RelativeHumidityMeasurement"
		capability "TemperatureMeasurement"
		capability "PressureMeasurement"
		capability "Battery"
		capability "SpeechSynthesis"
		//capability "PowerSource"
        attribute "battery", "Number"
        attribute "charging", "bool"
        attribute "acPlugged", "bool"
        attribute "usbPlugged", "bool"
        attribute "facedetected", "bool"
        attribute "illuminance", "Number"
        command "publishMsg", ["String"]
		command "clearCache"
		command "eval", ["String"]  //	JavaScript	{"eval": "alert('Hello World!');"}	Evaluates Javascript in the dashboard
		command "audio", ["String"] //	URL	{"audio": "http://<url>"}	Play the audio specified by the URL immediately
		command "relaunch" //	true	{"relaunch": true}	Relaunches the dashboard from configured launchUrl
		command "reload" //	true	{"reload": true}	Reloads the current page immediately
		command "url" , ["String"]//	URL	{"url": "http://<url>"}	Browse to a new URL immediately
//		command "wake" //	true	{"wake": true, "wakeTime": 180}	Wakes the screen if it is asleep. Option wakeTime (in seconds) is optional, default is 30 sec. (Note: wakeTime cannot be shorter than Androids Display Timeout setting)
		command "wake" //	false	{"wake": false}	Release screen wake (Note: screen will not turn off before Androids Display Timeout finished)
		command "speak", ["String"] //	data	{"speak": "Hello!"}	Uses the devices TTS to speak the message
		command "brightness", ["String"] //	data	{"brightness": 1}	Changes the screens brightness, value 1-255.
//		command "camera", [" String"]//	data	{"camera": true}	Turns on/off camera streaming, requires camera to be enabled.//
		command "volume", ["String"] //	data	{"volume": 100}	Changes the audio volume, value 0-100 (in %. Does not effect TTS volume).
        command "uninstalled"
    }
    preferences {
		section("Device Settings:") 
		{
			input "IP", "String", title:"MQTT Server IP", description: "", required: true, displayDuringSetup: true
			input "Port", "NUMBER", title:"MQTT Server port", description: "", required: true, displayDuringSetup: true
			input "User", "String", title:"MQTT Server User", description: "", required: false, displayDuringSetup: true
			input "Password", "String", title:"MQTT Server Password", description: "", required: false, displayDuringSetup: true
			input "BaseTopic", "String", title:"Base Topic", description: "", required: false, displayDuringSetup: true
		}
    }
}
void installed() {
    log.warn "installed..."
}
// Parse incoming device messages to generate events
void parse(String description) {
	def responce = interfaces.mqtt.parseMessage(description)
    log.debug "parse call"
    log.debug responce.get('topic')
    log.debug responce.get('payload')
    jsonPayload = new groovy.json.JsonSlurper().parseText(responce.get('payload'))
    
//---//******motion******//---//
	if (responce.get('topic') == settings.BaseTopic+"/sensor/motion"){
		log.debug "We got motion report: ${jsonPayload.value}"
        sendEvent(name: "motion", value: jsonPayload.value)
        /*
		if (responce.get('payload').contains("true")){
			log.debug "motion active"
			sendEvent(name: "motion", value: "active")
		}
		if (responce.get('payload').contains("false")){
			log.debug "motion active"
			sendEvent(name: "motion", value: "inactive")
		}*/
	}
//---//******Illuminance******//---//    {"value":83,"unit":"lx","id":"AL3201 Light Sensor"}
	if (responce.get('topic') == settings.BaseTopic+"/sensor/light"){
        log.debug "We got Illuminance report: ${responce.get('payload')}"
		sendEvent(name: "illuminance", value: jsonPayload.value)
        /*
        def data = responce.get('payload')
		def start = data.indexOf('value')+7
		def end = data.indexOf('unit')-2
		log.debug data.substring(start,end)
		sendEvent(name: "illuminance", value: data.substring(start,end))*/
	}
//---//******Battery******//---//       ///{"value":31,"unit":"%","charging":true,"acPlugged":true,"usbPlugged":false} battery 
	if (responce.get('topic') == settings.BaseTopic+"/sensor/battery"){
        sendEvent(name: "battery", value: jsonPayload.value)
        sendEvent(name: "unit", value: jsonPayload.unit)
        sendEvent(name: "charging", value: jsonPayload.charging)
        sendEvent(name: "acPlugged", value: jsonPayload.acPlugged)
        sendEvent(name: "usbPlugged", value: jsonPayload.usbPlugged)
        /*
		log.debug "We got Battery report: ${responce.get('payload')}"
		def data = responce.get('payload')
		def start = data.indexOf('value')+7
		def end = data.indexOf('unit')-2
		sendEvent(name: "battery", value: data.substring(start,end))
		start = data.indexOf('charging')+10
		end = data.indexOf('acPlugged')-2
		sendEvent(name: "charging", value: data.substring(start,end))*/
        
	}
//---//******face******//---//
	if (responce.get('topic') == settings.BaseTopic+"/sensor/face"){
		log.debug "We got face report: ${jsonPayload.value}"
        sendEvent(name: "facedetected", value: jsonPayload.value)
      ////response.json.
        /*
		if (responce.get('payload').contains("true")){
			log.debug "face active"
			sendEvent(name: "FaceDetected", value: "true")
		}
		if (responce.get('payload').contains("false")){
			log.debug "face active"
			sendEvent(name: "FaceDetected", value: "false")
		}*/
	}	
	//---//******State******//---//    /state   ///{"currentUrl":"http:\/\/192.168.86.68\/apps\/api\/49\/dashboard\/434?access_token=e19bf283-5d02-4de8-bebc-945dd16daa10&local=true","screenOn":true,"brightness":77}
	if (responce.get('topic') == settings.BaseTopic+"/state"){
		log.debug "We got State report: ${responce.get('payload')}"
        sendEvent(name: "currentUrl", value: jsonPayload.currentUrl)
        sendEvent(name: "screenOn", value: jsonPayload.screenOn)
        sendEvent(name: "brightness", value: jsonPayload.brightness)
        /*
		def data = responce.get('payload')
		def start = data.indexOf('currentUrl')+13
		def end = data.indexOf('screenOn')-3
		sendEvent(name: "currentUrl", value: data.substring(start,end).replaceAll(' ',''))
		start = data.indexOf('screenOn')+10
		end = data.indexOf('brightness')-2
		sendEvent(name: "screenOn", value: data.substring(start,end))
		start = data.indexOf('brightness')+12
		end = data.length()-1
		sendEvent(name: "brightness", value: data.substring(start,end))*/
	}
//	{"currentUrl":"http:\/\/192.168.86.68\/apps\/api\/49\/dashboard\/434?access_token=e19bf283-5d02-4de8-bebc-945dd16daa10&local=true","screenOn":true,"brightness":77}
}
void publishMsg(String s) {
//	log.warn "/test/hubitat"  {"clearCache": true}
    interfaces.mqtt.publish(settings.BaseTopic+"/command", s)
}
void updated() {
    log.info "updated..."
    initialize()
}
void uninstalled() {
    log.info "disconnecting from mqtt"
    interfaces.mqtt.disconnect()
}
void initialize() {
    try {
		def mqttInt = interfaces.mqtt
        //open connection
		if (!settings.User){
			mqttInt.connect("tcp://${settings.IP}:${settings.Port}", device.deviceNetworkId, null, null)
		}
		if (settings.User){
			mqttInt.connect("tcp://${settings.IP}:${settings.Port}", device.deviceNetworkId, settings.User, settings.Password)
		}
        //give it a chance to start
        pauseExecution(1000)
        log.info "connection established"
        //mqttInt.subscribe("/home/office/dashboard/sensor/#")
		mqttInt.subscribe(settings.BaseTopic+"/sensor/#")
        log.info "subscribe to :${settings.BaseTopic}/sensor/#"
//			mqttInt.subscribe(settings.BaseTopic+"/sensor/motion")
//			mqttInt.subscribe(settings.BaseTopic+"/sensor/light")
//			mqttInt.subscribe(settings.RelativeHumidityMeasurementTopic)
//			mqttInt.subscribe(settings.PressureMeasurementTopic)
//			mqttInt.subscribe(settings.BaseTopic+"/sensor/battery")
//			mqttInt.subscribe(settings.BaseTopic+"/state")
			
//			mqttInt.subscribe(settings.AxisTopic)
		
		
		
		
/*
		if (settings.MotionTopic)
		{
			log.info "Suscribint to ${settings.MotionTopic}"
			mqttInt.subscribe(settings.MotionTopic)
		}
		
		if (settings.IlluminanceTopic)
		{
			log.info "Suscribint to ${settings.IlluminanceTopic}"
			mqttInt.subscribe(settings.IlluminanceTopic)
		}
		
		if (settings.RelativeHumidityMeasurementTopic)
		{
			log.info "Suscribint to ${settings.RelativeHumidityMeasurementTopic}"
			mqttInt.subscribe(settings.RelativeHumidityMeasurementTopic)
		}
		
		if (settings.PressureMeasurementTopic)
		{
			log.info "Suscribint to ${settings.PressureMeasurementTopic}"
			mqttInt.subscribe(settings.PressureMeasurementTopic)
		}
		
		
		if (settings.BatteryTopic)
		{
			log.info "Suscribint to ${settings.BatteryTopic}"
			mqttInt.subscribe(settings.BatteryTopic)
		}
		
		if (settings.StateTopic)
		{
			log.info "Suscribint to ${settings.StateTopic}"
			mqttInt.subscribe(settings.StateTopic)
		}
		
		if (settings.FaceTopic)
		{
			log.info "Suscribint to ${settings.FaceTopic}"
			mqttInt.subscribe(settings.FaceTopic)
		}
		
		if (settings.AxisTopic)
		{
			log.info "Suscribint to ${settings.AxisTopic}"
			mqttInt.subscribe(settings.AxisTopic)
		}
		*/
		
    } catch(e) {
        log.debug "initialize error: ${e.message}"
    }
}
void mqttClientStatus(String message) {
	log.info "Received status message ${message}"
}
void speak(String message){publishMsg("{'speak': '${message}'}")}
//eval	JavaScript	{"eval": "alert('Hello World!');"}	Evaluates Javascript in the dashboard
void eval(String message){    
    log.info "{\"eval\": \"alert(\'${message}\');\"}"
    publishMsg("{\"eval\": \"alert(\'${message}\');\"}")
    }

void wake (){publishMsg("{'wake': true}")}
//void camera (String msg){	publishMsg("{'camera': ${msg}}")}
void url (String URL){publishMsg("{'url': '${URL}'}")}
void relaunch (){publishMsg("{'relaunch': true}")}
void reload (){publishMsg("{'reload': true}")}
//void volume (String value){publishMsg("{'volume': ${value}}")}
void brightness (String value){publishMsg("{'brightness': ${value}}")}
