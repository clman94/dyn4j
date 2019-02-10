package test;

import org.dyn4j.dynamics.*;
import org.dyn4j.geometry.*;

public class Main {
	public static void main(String[] args)  
	{
		World world = new World();
		
		{
			Body body = new Body();
			BodyFixture fixture = new BodyFixture(new Rectangle(1, 1));
			fixture.setShape(new Rectangle(10, 10));
			body.addFixture(fixture);
			body.setMass(MassType.NORMAL);
			world.addBody(body);
		}
		
		{
			Body body = new Body();
			BodyFixture fixture = new BodyFixture(new Rectangle(1, 1));
			fixture.setShape(new Rectangle(10, 10));
			body.addFixture(fixture);
			world.addBody(body);
		}
		
		for(;;)
		{
			world.updatev(1.f / 10.f);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
