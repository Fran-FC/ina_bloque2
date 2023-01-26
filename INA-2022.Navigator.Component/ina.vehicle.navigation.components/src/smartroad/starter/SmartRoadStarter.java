package smartroad.starter;

import java.util.ArrayList;

import ina.vehicle.navigation.components.RoadSegment;
import smartroad.impl.SmartRoad;

public class SmartRoadStarter {

	public static void main(String[] args) {

		RoadSegment rs1 = new RoadSegment("R5s1", "R5", "R5s1", 0, 100, 10, 100);
		RoadSegment rs2 = new RoadSegment("R5s2", "R5", "R5s2", 100, 150, 5, 80);
		ArrayList<RoadSegment> rs = new ArrayList<RoadSegment>();
		rs.add(rs1);
		rs.add(rs2);
		SmartRoad r5s1 = new SmartRoad("R5s1", rs);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
