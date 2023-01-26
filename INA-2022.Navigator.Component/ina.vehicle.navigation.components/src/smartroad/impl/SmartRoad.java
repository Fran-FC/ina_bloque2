package smartroad.impl;

import java.util.ArrayList;

import ina.vehicle.navigation.components.RoadSegment;

public class SmartRoad {
	
	protected SmartRoad_InicidentNotifier notifier = null;
	protected SmartRoad_RoadIncidentsSubscriber subscriber  = null;
	protected String id = null;
//	protected ArrayList<RoadSegment> roadSegments = null;
	
	public SmartRoad(String id) {
		this.setId(id);
		
//		this.roadSegments = rs;
		
		this.subscriber = new SmartRoad_RoadIncidentsSubscriber(this);
		this.subscriber.connect();
		this.subscriber.subscribe("es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + id + "/alerts");
		
		notifier = new SmartRoad_InicidentNotifier(this);
		notifier.connect();
	}

	 public String getId() {
		return id;
	}
	 
	 public void setId(String id) {
		this.id = id;
	}
	 
	 public void notify(String message) {
		 this.notifier.notify(message);
	 }
	
}
