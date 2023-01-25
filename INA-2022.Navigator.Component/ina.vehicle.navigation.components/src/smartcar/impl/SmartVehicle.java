package smartcar.impl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import ina.vehicle.navigation.components.Navigator;
import ina.vehicle.navigation.components.Route;

public class SmartVehicle extends SmartCar
{
	protected String rol;
	protected Navigator navigator;
	protected SmartCar_StepSubscriber stepSubscriber = null;
	protected int speed;
	
	public SmartVehicle(String id, String rol, int speed, int p_inicio, int p_fin)
	{
		super(id);
		
		this.rol = rol;
		this.speed = speed;
		
		this.stepSubscriber = new SmartCar_StepSubscriber(this);
		this.stepSubscriber.connect();
		this.stepSubscriber.subscribe("es/upv/pros/tatami/smartcities/traffic/PTPaterna/step");
		
		
		this.navigator = new Navigator(id + "_navigator");
		

		Route r = new Route();
		r.addRouteFragment("R5s1", p_inicio, p_fin);
		
		this.navigator.setRoute(r);
	}

	public void navigatorMove(long milliseconds)
	{
		this.navigator.move(milliseconds, this.speed);
	}
}