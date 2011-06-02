/*
 * Copyright (c) 2011 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.testbed.test;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Segment;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.testbed.ContactCounter;
import org.dyn4j.testbed.Entity;
import org.dyn4j.testbed.GLHelper;
import org.dyn4j.testbed.Test;
import org.dyn4j.testbed.input.Input;
import org.dyn4j.testbed.input.Keyboard;
import org.dyn4j.testbed.input.Mouse;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Tests the performance of the {@link World}'s raycast methods.
 * @author William Bittle
 * @version 3.0.0
 * @since 2.0.0
 */
public class RaycastPerformance extends Test {
	/** The render radius of the points */
	private static final double r = 0.01;
	
	/** The ray for the raycast test */
	private Ray ray;
	
	/** The ray length; initially zero for an infinite length */
	private double length = 0.0;
	
	/** Whether the ray is infinite length or not */
	private boolean infinite = true;
	
	/** Whether to get all results or just the closest */
	private boolean all = true;
	
	/** The glut object to render text */
	private GLUT glut = new GLUT();
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#getName()
	 */
	@Override
	public String getName() {
		return "Raycast Performance";
	}
	
	/* (non-Javadoc)
	 * @see test.Test#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Tests the raycast methods of the World class." +
			   "\n\nOne of the Cicles in this test should not be" +
			   "raycast because of the custom raycast listener that" +
			   "ignores it.";
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.Test#initialize()
	 */
	@Override
	public void initialize() {
		// call the super method
		super.initialize();
		
		// set the camera position and zoom
		this.home();
		
		// create the ray
		Vector2 s = new Vector2();
		Vector2 d = new Vector2(Math.sqrt(3) * 0.5, 0.5);
		this.ray = new Ray(s, d);
		
		// create the world
		this.world = new World();
		
		// setup the contact counter
		ContactCounter cc = new ContactCounter();
		this.world.setContactListener(cc);
		this.world.setStepListener(cc);
		
		// turn off gravity
		this.world.setGravity(new Vector2());
		
		// setup the bodies in the world
		this.setup();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.Test#setup()
	 */
	@Override
	protected void setup() {
		for (int i = 0; i < 1000; i++ ) {
			double x = Math.random() * 5.0 - Math.random() * 5.0;
			double y = Math.random() * 5.0 - Math.random() * 5.0;
			int s = (int)Math.floor(Math.random() * 4.0);
			
			Entity e = new Entity(0.5f);
			if (s == 0) {
				// create a circle
				Circle c = Geometry.createCircle(Math.random() * 0.2 + 0.1);
				e.addFixture(c);
			} else if (s == 1) {
				// create a box
				Rectangle r = Geometry.createRectangle(Math.random() * 0.2 + 0.1, Math.random() * 0.2 + 0.1);
				e.addFixture(r);
			} else if (s == 2) {
				// create a triangle
				Triangle t = Geometry.createEquilateralTriangle(Math.random() * 0.2 + 0.1);
				e.addFixture(t);
			} else if (s == 3) {
				// create a segment
				Segment se = Geometry.createHorizontalSegment(Math.random() * 0.2 + 0.1);
				e.addFixture(se);
			} else {
				System.out.println("no");
			}
			
			e.setMass(Mass.Type.INFINITE);
			e.translate(x, y);
			this.world.add(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#renderBefore(javax.media.opengl.GL2)
	 */
	@Override
	protected void renderBefore(GL2 gl) {
		// render the axes
		this.renderAxes(gl, new float[] { 0.3f, 0.3f, 0.3f, 1.0f }, 
				1.0, 0.25, new float[] { 0.3f, 0.3f, 0.3f, 1.0f }, 
				0.1, 0.125, new float[] { 0.5f, 0.5f, 0.5f, 1.0f });
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#renderAfter(javax.media.opengl.GL2)
	 */
	@Override
	protected void renderAfter(GL2 gl) {
		// create the list for the results
		List<RaycastResult> results = new ArrayList<RaycastResult>();
		
		// render the ray
		gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
		this.renderRay(gl, this.ray, this.length);
		
		gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		// perform a raycast
		if (this.world.raycast(this.ray, this.length, false, this.all, results)) {
			// get the number of results
			int size = results.size();
			// show the number of results
			gl.glPushMatrix();
			gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			gl.glLoadIdentity();
			gl.glRasterPos2d(-this.size.width / 2.0 + 5.0, this.size.height / 2.0 - 15.0);
			this.glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Results: " + size);
			gl.glPopMatrix();
			
			gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			
			// check the size
			if (size > 1) {
				// then lets sort
				Collections.sort(results);
				
				// there is no real reason to do this here but real applications
				// can use this to order the results so that logic can be performed
				// on each body as the distance increase (a bullet passing through
				// a number of bodies may slow down and damage each body less and
				// less for example)
			}
			// loop over the results
			for (int i = 0; i < size; i++) {
				// should always contain just one result
				RaycastResult result = results.get(i);
				org.dyn4j.collision.narrowphase.Raycast raycast = result.getRaycast();
				
				// draw the normal and point
				Vector2 point = raycast.getPoint();
				Vector2 normal = raycast.getNormal();
				
				GLHelper.fillRectangle(gl, point.x, point.y, r, r);
				
				gl.glBegin(GL.GL_LINES);
					gl.glVertex2d(point.x, point.y);
					gl.glVertex2d(point.x + normal.x, point.y + normal.y);
				gl.glEnd();
			}
		}
	}
	
	/**
	 * Renders the given ray to the given graphics object.
	 * @param gl the OpenGL graphics context
	 * @param ray the ray to render
	 * @param length the ray length; 0 for infinite length
	 * @since 2.0.0
	 */
	protected void renderRay(GL2 gl, Ray ray, double length) {
		// get the ray attributes (world coordinates)
		Vector2 s = ray.getStart();
		Vector2 d = ray.getDirection();
		
		double l = length > 0.0 ? length : 10000.0;
		
		// draw the line from the start to the end, along d, l distance
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(s.x, s.y);
			gl.glVertex2d(s.x + d.x * l, s.y + d.y * l);
		gl.glEnd();
	}
	
	/**
	 * Renders the x and y axis with minor and major ticks.
	 * @param gl the OpenGL graphics context
	 * @param lineColor the color of the axes; RGBA
	 * @param majorTickScale the major tick scale in meters
	 * @param majorTickWidth the major tick width in pixels
	 * @param majorTickColor the major tick color; RGBA
	 * @param minorTickScale the minor tick scale in meters
	 * @param minorTickWidth the minor tick width in pixels
	 * @param minorTickColor the minor tick color; RGBA
	 */
	protected void renderAxes(GL2 gl, float[] lineColor,
			double majorTickScale, double majorTickWidth, float[] majorTickColor,
			double minorTickScale, double minorTickWidth, float[] minorTickColor) {
		// set the line color
		gl.glColor4fv(lineColor, 0);
		
		// get the current width and height
		double width = this.size.width;
		double height = this.size.height;
		
		// render the y axis
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(0.0,  height / 2.0 - this.offset.y);
			gl.glVertex2d(0.0, -height / 2.0 + this.offset.y);
			
			gl.glVertex2d( width / 2.0 - this.offset.x, 0.0);
			gl.glVertex2d(-width / 2.0 + this.offset.x, 0.0);
		gl.glEnd();
		
		// compute the major tick offset
		double mao = majorTickWidth / 2.0;
		// compute the minor tick offset
		double mio = minorTickWidth / 2.0;
		
		// render the y tick marks
		// compute the number of major ticks on the y axis
		int yMajorTicks= (int) Math.ceil(height / 2.0 / majorTickScale) + 1;
		// compute the y axis offset
		int yoffset = -(int) Math.floor(this.offset.y / majorTickScale);
		
		gl.glBegin(GL.GL_LINES);
		for (int i = (-yMajorTicks + yoffset); i < (yMajorTicks + yoffset); i++) {
			// set the color
			gl.glColor4fv(majorTickColor, 0);
			// compute the major tick y
			double yma = majorTickScale * i;
			// skip drawing the major tick at zero
			
			if (i != 0) {
				// draw the +y ticks
				gl.glVertex2d(-mao, yma);
				gl.glVertex2d( mao, yma);
			}
			
			// render the minor y tick marks
			// set the color
			gl.glColor4fv(minorTickColor, 0);
			// compute the number of minor ticks
			int minorTicks = (int) Math.ceil(majorTickScale / minorTickScale);
			for (int j = 1; j < minorTicks; j++) {
				// compute the major tick y
				double ymi = majorTickScale * i - minorTickScale * j;
				// draw the +y ticks
				gl.glVertex2d(-mio, ymi);
				gl.glVertex2d( mio, ymi);
			}
		}
		
		// render the x tick marks
		// compute the number of major ticks on the x axis
		int xMajorTicks= (int) Math.ceil(width / 2.0 / majorTickScale) + 1;
		// compute the x axis offset
		int xoffset = -(int) Math.floor(this.offset.x / majorTickScale);
		for (int i = (-xMajorTicks + xoffset); i < (xMajorTicks + xoffset); i++) {
			// set the color
			gl.glColor4fv(majorTickColor, 0);
			// compute the major tick x
			double xma = majorTickScale * i;
			// skip drawing the major tick at zero
			if (i != 0) {
				// draw the major ticks
				gl.glVertex2d(xma,  mao);
				gl.glVertex2d(xma, -mao);
			}
			
			// render the minor x tick marks
			// set the color
			gl.glColor4fv(minorTickColor, 0);
			// compute the number of minor ticks
			int minorTicks = (int) Math.ceil(majorTickScale / minorTickScale);
			for (int j = 1; j < minorTicks; j++) {
				// compute the major tick x
				double xmi = majorTickScale * i - minorTickScale * j;
				// draw the minor ticks
				gl.glVertex2d(xmi,  mio);
				gl.glVertex2d(xmi, -mio);
			}
		}
		gl.glEnd();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#getControls()
	 */
	@Override
	public String[][] getControls() {
		return new String[][] {
				{"Change Angle", "Decrease/Increase the angle from the positive x-axis by 2 degrees.", "<html><span style='color: blue;'>d</span> / <span style='color: blue;'>D</span></html>"},
				{"Change Length", "Decrease/Increase the length of the ray by 0.25m.", "<html><span style='color: blue;'>l</span> / <span style='color: blue;'>L</span></html>"},
				{"Toggle Infinite", "Makes the ray's length infinite.", "<html><span style='color: blue;'>i</span></html>"},
				{"Toggle All", "Toggles between all results or the closest result.", "<html><span style='color: blue;'>a</span></html>"}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#initializeInput(org.codezealot.game.input.Keyboard, org.codezealot.game.input.Mouse)
	 */
	@Override
	public void initializeInput(Keyboard keyboard, Mouse mouse) {
		super.initializeInput(keyboard, mouse);
		
		// shift is already setup by the testbed
		
		// setup the a and l
		
		keyboard.add(new Input(KeyEvent.VK_D, Input.Hold.NO_HOLD));
		keyboard.add(new Input(KeyEvent.VK_L, Input.Hold.NO_HOLD));
		keyboard.add(new Input(KeyEvent.VK_I, Input.Hold.NO_HOLD));
		keyboard.add(new Input(KeyEvent.VK_A, Input.Hold.NO_HOLD));
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.testbed.Test#poll(org.codezealot.game.input.Keyboard, org.codezealot.game.input.Mouse)
	 */
	@Override
	public void poll(Keyboard keyboard, Mouse mouse) {
		super.poll(keyboard, mouse);
		
		// look for the a key
		if (keyboard.isPressed(KeyEvent.VK_D)) {
			// look for the shift key
			if (keyboard.isPressed(KeyEvent.VK_SHIFT)) {
				this.ray.getDirection().rotate(Math.toRadians(2.0));
			} else {
				this.ray.getDirection().rotate(Math.toRadians(-2.0));
			}
		}
		
		// look for the l key
		if (keyboard.isPressed(KeyEvent.VK_L)) {
			// is it currently infinite?
			if (this.infinite) {
				this.infinite = false;
				// give the ray an initial length
				this.length = 0.25;
			}
			// look for the shift key
			if (keyboard.isPressed(KeyEvent.VK_SHIFT)) {
				this.length += 0.25;
			} else {
				if (this.length != 0.25) {
					this.length -= 0.25;
				}
			}
		}
		
		// look for the i key
		if (keyboard.isPressed(KeyEvent.VK_I)) {
			this.infinite = true;
			this.length = 0.0;
		}
		
		// look for the a key
		if (keyboard.isPressed(KeyEvent.VK_A)) {
			this.all = !this.all;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.Test#home()
	 */
	@Override
	public void home() {
		// set the scale
		this.scale = 64.0;
		// set the offset
		this.offset.set(0.0, 0.0);
	}
}