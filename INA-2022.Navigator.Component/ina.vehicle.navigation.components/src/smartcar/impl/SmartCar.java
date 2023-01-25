package smartcar.impl;

import ina.vehicle.navigation.components.Navigator;
import ina.vehicle.navigation.components.RoadPoint;
import ina.vehicle.navigation.components.RoadSegment;
import ina.vehicle.navigation.components.Route;
import ina.vehicle.navigation.components.Route.RouteFragment;

public class SmartCar {

	protected String smartCarID = null;
	protected RoadPlace rp = null;	// simula la ubicación actual del vehículo
	protected SmartCar_RoadInfoSubscriber roadInfoSubscriber = null;
	
	protected SmartCar_InicidentNotifier notifier = null;
	
	
	
	public SmartCar(String id) {
		this.setSmartCarID(id);
		
		this.rp = new RoadPlace("R5s1", 10);
		
		this.roadInfoSubscriber = new SmartCar_RoadInfoSubscriber(this);
		this.roadInfoSubscriber.connect();
		this.roadInfoSubscriber.subscribe("es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/R5s1/info");
		
		this.notifier = new SmartCar_InicidentNotifier(id);
		this.notifier.connect();
	}
	
	
	public void setSmartCarID(String smartCarID) {
		this.smartCarID = smartCarID;
	}
	
	public String getSmartCarID() {
		return smartCarID;
	}

	public RoadPlace getCurrentPlace() {
		return rp;
	}

	public void changeKm(int km) {
		this.getCurrentPlace().setKm(km);
	}
	
	public void changeRoad(String road, int km) {
		this.getCurrentPlace().setRoad(road);
		this.getCurrentPlace().setKm(km);
	}
	
	public void notifyIncident(String incidentType) {
		if ( this.notifier == null )
			return;
		
		this.notifier.alert(this.getSmartCarID(), incidentType, this.getCurrentPlace());
		
	}

}


