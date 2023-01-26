package smartcar.impl;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartCar_StepSubscriber implements MqttCallback {

	MqttClient myClient;
	MqttConnectOptions connOpt;

	static final String BROKER_URL = "tcp://tambori.dsic.upv.es:10083";
//	static final String M2MIO_USERNAME = "<m2m.io username>";
//	static final String M2MIO_PASSWORD_MD5 = "<m2m.io password (MD5 sum of password)>";

	SmartVehicle smartvehicle;
	protected Integer previousTimestamp;
	
	public SmartCar_StepSubscriber(SmartVehicle smartvehicle) {
		this.smartvehicle = smartvehicle;
		this.previousTimestamp = 0;
	}
	
	protected void _debug(String message) {
		System.out.println("(SmartCar: " + this.smartvehicle.getSmartCarID() + ") " + message);
	}

	
	@Override
	public void connectionLost(Throwable t) {
		this._debug("Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		String payload = new String(message.getPayload());
		
		JSONObject jsonPayload = new JSONObject(payload);
		// DO SOME MAGIC HERE!
		String actualTimeStamp = jsonPayload.getString("timestamp");
		
		int time = Integer.parseInt(actualTimeStamp) - this.previousTimestamp;
		
		if(previousTimestamp != 0)
			smartvehicle.navigatorMove(time);
	
		previousTimestamp = Integer.parseInt(actualTimeStamp);

		this.publishTraffic(smartvehicle.getCurrentPlace().getRoad(), 
				smartvehicle.getCurrentPosition(), 
				smartvehicle.getCurrentAction());
		
		if (smartvehicle.getCurrentAction().equals("VEHICLE_OUT"))
		{
			smartvehicle.setCurrentAction("VEHICLE_IN");
			this.publishTraffic(smartvehicle.getCurrentPlace().getRoad(), 
					smartvehicle.getCurrentPosition(), 
					smartvehicle.getCurrentAction());
		}
	}
	
	public void publishTraffic(String roadSegment, int position, String action) throws JSONException
	{
		String topic = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + roadSegment + "/traffic";
		JSONObject msg1 =new JSONObject();
		JSONObject msg2 = new JSONObject();
		
		Random random = new Random();
		
		msg2.put("action", action);
		msg2.put("vehicle-role", smartvehicle.getSmartVehicleRole());
		msg2.put("vehicle-id", smartvehicle.getSmartCarID());
		msg2.put("road-segment", roadSegment);
		msg2.put("position", position);
		
		msg1.put("msg", msg2);
		msg1.put("id", "MSG_"+ random.ints(1, 1, 999999990));
		msg1.put("timestamp", System.currentTimeMillis());
		msg1.put("type", "TRAFFIC");
		
		MqttMessage messagePublish = new MqttMessage();
		messagePublish.setPayload(msg1.toString().getBytes());

		try {
			myClient.publish(topic, messagePublish);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * runClient
	 * The main functionality of this simple example.
	 * Create a MQTT client, connect to broker, pub/sub, disconnect.
	 * 
	 */
	public void connect() {
		// setup MQTT Client
		String clientID = this.smartvehicle.getSmartCarID() + ".subscriber";
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, clientID);
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		this._debug("Subscriber Connected to " + BROKER_URL);

	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(120000);

			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	
	public void subscribe(String myTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(myTopic, subQoS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public void unsubscribe(String myTopic) {
		
		// unsubscribe to topic
		try {
			int subQoS = 0;
			myClient.unsubscribe(myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
