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
	protected String current_action;
	
	public SmartVehicle(String id, String rol, int speed, int p_inicio, int p_fin)
	{
		super(id);
		
		this.rol = rol;
		this.speed = speed;
		this.current_action = "VEHICLE_IN";
		
		this.stepSubscriber = new SmartCar_StepSubscriber(this);
		this.stepSubscriber.connect();
		this.stepSubscriber.subscribe("es/upv/pros/tatami/smartcities/traffic/PTPaterna/step");
		
		
		this.navigator = new Navigator(id + "_navigator");
		

		Route r = new Route();
		r.addRouteFragment("R5s1", p_inicio, p_fin);
		
		this.navigator.setRoute(r);
	}
	
	public int getSpeed()
	{
		if(this.rol.equals("Ambulance") || this.rol.equals("Police"))
			return this.speed;
//		• la velocidad máxima (actual) del road-segment
//		• si existe una señal de tráfico de tipo speed-limit que afecta a la posición en la
//		que estamos TODO
//		• la velocidad de crucero establecida
		int maxSpeedSegment = this.navigator.getCurrentRoadSegment().getCurrentMaxSpeed();
//		int speedLimitSignal
		
		return maxSpeedSegment < this.speed ? maxSpeedSegment : this.speed;
	}

	@Override
	public void changeRoad(String road, int km) {
		this.getCurrentPlace().setRoad(road);
		this.getCurrentPlace().setKm(km);
		
		this.current_action = "VEHICLE_OUT";
	}
	
	public void setCurrentAction(String action)
	{
		this.current_action = action;
	}
	
	public String getCurrentAction()
	{
		return this.current_action;
	}
	
	public String getSmartVehicleRole()
	{
		return this.rol;
	}
	
	public void navigatorMove(long milliseconds)
	{
		this.navigator.move(milliseconds, this.getSpeed());
	}
	
	public int getCurrentPosition()
	{
		return this.navigator.getCurrentPosition().getPosition();
	}
	
	public void notifyIncident(String incidentType) {
		if ( this.notifier == null )
			return;
		
		this.notifier.alert(this.getSmartCarID(), incidentType, this.getCurrentPlace(), this.getCurrentPosition());
	}
}