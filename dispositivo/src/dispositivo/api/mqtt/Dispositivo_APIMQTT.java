package dispositivo.api.mqtt;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import dispositivo.interfaces.Configuracion;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;
import dispositivo.utils.MySimpleLogger;

public class Dispositivo_APIMQTT implements MqttCallback {

	protected MqttClient myClient;
	protected MqttConnectOptions connOpt;
	protected String clientId = null;
	
	protected IDispositivo dispositivo;
	protected String mqttBroker = null;
	
	private String loggerId = null;
	private Integer position = null;  
	
	public static Dispositivo_APIMQTT build(IDispositivo dispositivo, String brokerURL) {
		Dispositivo_APIMQTT api = new Dispositivo_APIMQTT(dispositivo);
		api.setBroker(brokerURL);
		return api;
	}
	
	protected Dispositivo_APIMQTT(IDispositivo dev) {
		this.dispositivo = dev;
		this.loggerId = dev.getId() + "-apiMQTT";
	}
	
	protected void setBroker(String mqttBrokerURL) {
		this.mqttBroker = mqttBrokerURL;
	}
	
	
	@Override
	public void connectionLost(Throwable t) {
		MySimpleLogger.debug(this.loggerId, "Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
				
		JSONObject payload = null;		
		
		try {
			
			System.out.println("-------------------------------------------------");
			System.out.println("| Topic:" + topic);
			System.out.println("| Message: " + payload);
			System.out.println("-------------------------------------------------");
			
			// DO SOME MAGIC HERE!
			
			//
			// Obtenemos el id de la funciÃ³n
			//   Los topics estÃ¡n organizados de la siguiente manera:
			//         $topic_base/dispositivo/funcion/$ID-FUNCION/commamnd
			//   Donde el $topic_base es parametrizable al arrancar el dispositivo
			//   y la $ID-FUNCION es el identificador de la dunciÃ³n
			
			String[] topicNiveles = topic.split("/");
			String roadSegment = topicNiveles[topicNiveles.length-2];
			
			
			if (topic == calculateAlertTopic(roadSegment)) {
				
				String funcionf2 = "f2";
				IFuncion f2 = this.dispositivo.getFuncion(funcionf2);
				
				payload = new JSONObject(message.getPayload());
				String msg = payload.getString("alert");
				f2.encender();
				
			} else if (topic == calculateInfoTopic(roadSegment)) {
				
				payload = new JSONObject(message.getPayload());
				JSONObject msg = payload.getJSONObject("msg");
				String status = payload.getString("status");
				
				position = Integer.parseInt(payload.getString("starting-position")); // o ending-position

				String funcionf1 = "f1";
				String funcionf2 = "f2";
				
				IFuncion f1 = this.dispositivo.getFuncion(funcionf1);
				IFuncion f2 = this.dispositivo.getFuncion(funcionf2);
				
				if (status == "Free_Flow" || status == "Mostly_Free_Flow") {
					f1.apagar();
				}else if (status == "Limited_Manouvers") {
					f1.parpadear();
				}else if (status == "No_Manouvers" || status == "Collapsed"){
					f1.encender();
				}

				f2.apagar();
				
			} else if (topic == calculateTrafficTopic(roadSegment)) {
				
				payload = new JSONObject(message.getPayload());
				JSONObject msg = payload.getJSONObject("msg");
				String role = payload.getString("vehicle-role");
				Integer vehiclePosition = Integer.parseInt(payload.getString("starting-position"));// o ending-position
				
				String funcionf3 = "f3";

				IFuncion f3 = this.dispositivo.getFuncion(funcionf3);
				
				if (role == "Ambulance" || role == "Police" && !position.equals(null)) {
					
					int relativePosition = vehiclePosition - this.position;
					
					if(relativePosition <= 0) {
						f3.apagar();
					}else if (relativePosition < 200) {
						f3.parpadear();
					}else { // relativePosition >= 200
						f3.encender();
					}
					
				} else{
					f3.apagar();
				}
				
			}
			
		} catch (JSONException e) {
			//this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (Exception e) {
			
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
		String clientID = this.dispositivo.getId() + UUID.randomUUID().toString() + ".subscriber";
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		// Connect to Broker
		try {
			MqttDefaultFilePersistence persistence = null;
			try {
				persistence = new MqttDefaultFilePersistence("/tmp");
			} catch (Exception e) {
			}
			if ( persistence != null )
				myClient = new MqttClient(this.mqttBroker, clientID, persistence);
			else
				myClient = new MqttClient(this.mqttBroker, clientID);

			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		MySimpleLogger.info(this.loggerId, "Conectado al broker " + this.mqttBroker);
	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(10000);

			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	protected void subscribe(String myTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(myTopic, subQoS);
			MySimpleLogger.info(this.loggerId, "Suscrito al topic " + myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	protected void unsubscribe(String myTopic) {
		
		// unsubscribe to topic
		try {
			int subQoS = 0;
			myClient.unsubscribe(myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void iniciar() {

		if ( this.myClient == null || !this.myClient.isConnected() )
			this.connect();
		
		if ( this.dispositivo == null )
			return;
		
		for(IFuncion f : this.dispositivo.getFunciones()) {
			this.subscribe(this.calculateInfoTopic("R1s1"));
			this.subscribe(this.calculateTrafficTopic("R1s1"));
			this.subscribe(this.calculateAlertTopic("R1s1"));			
		}
	}
	
	
	public void detener() {
		
		
		// To-Do
		
	}
	
	protected String calculateInfoTopic(String roadSegment) {
		return Configuracion.TOPIC_BASE + roadSegment + "/info";
	}
	
	protected String calculateTrafficTopic(String roadSegment) {
		return Configuracion.TOPIC_BASE + roadSegment + "/traffic";
	}
	
	protected String calculateAlertTopic(String roadSegment) {
		return Configuracion.TOPIC_BASE + roadSegment + "/alerts";
	}
	

}