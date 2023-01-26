package smartcar.starter;

import smartcar.impl.SmartVehicle;


public class SmartCarStarter {

	public static void main(String[] args) {

		SmartVehicle sc1 = new SmartVehicle("SmartCar001", "Ambulance", 100, 0, 300);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		sc1.changeRoad("R5s1", 100);  // indicamos que el SmartCar está en tal segmento
		//sc1.notifyIncident("accidente"); // Notificar accidente

	}

}
