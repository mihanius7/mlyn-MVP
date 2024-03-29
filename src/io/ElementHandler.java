package io;

import java.awt.Color;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import elements.line.Spring;
import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Boundaries;
import simulation.Simulation;

public class ElementHandler implements ContentHandler {

	private Particle p;
	private Spring s;
	private String colorString;
	private double x, y, vx, vy, m, r, q, frict, stict, maxStress;
	private boolean movableX, movableY, collidableP, collidableS, asLine;
	private int i, j, colorIndex;
	private double l0, k, c;

	@Override
	public void startDocument() throws SAXException {
		ConsoleWindow.println(GUIStrings.FILE_LOADING_STARTET);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs)
			throws SAXException, NumberFormatException {
		if (qName.equalsIgnoreCase("particle")) {
			x = Double.parseDouble(attrs.getValue("x"));
			y = Double.parseDouble(attrs.getValue("y"));
			vx = Double.parseDouble(attrs.getValue("vx"));
			vy = Double.parseDouble(attrs.getValue("vy"));
			m = Double.parseDouble(attrs.getValue("m"));
			r = Double.parseDouble(attrs.getValue("r"));
			q = Double.parseDouble(attrs.getValue("q"));
			Double.parseDouble(attrs.getValue("elast"));
			frict = Double.parseDouble(attrs.getValue("frict"));
			String s = attrs.getValue("stict");
			if (s != null)
				stict = Double.parseDouble(attrs.getValue("stict"));
			else
				stict = 0;
			p = new Particle(x, y, m, q, vx, vy, r, ParticleShape.PARTICLE_DEFAULT);
			movableX = Boolean.parseBoolean(attrs.getValue("movableX"));
			movableY = Boolean.parseBoolean(attrs.getValue("movableY"));
			collidableP = Boolean.parseBoolean(attrs.getValue("collidable"));
			p.setMovableX(movableX);
			p.setMovableY(movableY);
			p.setCanCollide(collidableP);
			p.setFrictionForce(frict);
			p.setStictionForce(stict);
			colorString = attrs.getValue("color");
			if (colorString != null) {
				colorIndex = Integer.parseInt(colorString);
				p.getShape().setColor(new Color(colorIndex));
			}
		} else if (qName.equalsIgnoreCase("spring")) {
			String str;
			i = Integer.parseInt(attrs.getValue("p1"));
			j = Integer.parseInt(attrs.getValue("p2"));
			k = Double.parseDouble(attrs.getValue("k"));
			c = Double.parseDouble(attrs.getValue("c"));
			l0 = Double.parseDouble(attrs.getValue("l0"));
			collidableS = Boolean.parseBoolean(attrs.getValue("collidable"));
			str = attrs.getValue("gap_value");
			str = attrs.getValue("max_stress");
			if (str != null)
				maxStress = Double.parseDouble(str);
			else
				maxStress = Double.MAX_VALUE;
			asLine = Boolean.parseBoolean(attrs.getValue("as_line"));
			s = new Spring(i, j, l0, k, c, 0);
			s.setCanCollide(collidableS);
			s.setBreakUpTension(maxStress);
			s.setIsLine(asLine);
		} else if (qName.equalsIgnoreCase("bspring")) {
			i = Integer.parseInt(attrs.getValue("s1"));
			j = Integer.parseInt(attrs.getValue("s2"));
			k = Double.parseDouble(attrs.getValue("ak"));
			c = Double.parseDouble(attrs.getValue("ac"));
			l0 = Double.parseDouble(attrs.getValue("a0"));
		} else if (qName.equalsIgnoreCase("boundaries")) {
			Boundaries b = Simulation.getInstance().content().getBoundaries();
			b.setUseLeft(Boolean.parseBoolean(attrs.getValue("use_left")));
			b.setUseRight(Boolean.parseBoolean(attrs.getValue("use_right")));
			b.setUseUpper(Boolean.parseBoolean(attrs.getValue("use_upper")));
			b.setUseBottom(Boolean.parseBoolean(attrs.getValue("use_bottom")));
			b.setBounds(Double.parseDouble(attrs.getValue("left")), Double.parseDouble(attrs.getValue("right")),
					Double.parseDouble(attrs.getValue("upper")), Double.parseDouble(attrs.getValue("bottom")));
		} else if (qName.equalsIgnoreCase("elements")) {
			Simulation.getInstance().interactionProcessor
					.setUseFriction(Boolean.parseBoolean(attrs.getValue("friction_forces")));
			Simulation.getInstance().interactionProcessor
					.setUsePPCollisions(Boolean.parseBoolean(attrs.getValue("collisions")));
			Simulation.getInstance().interactionProcessor
					.setUseExternalForces(Boolean.parseBoolean(attrs.getValue("external_forces")));
			Simulation.getInstance().interactionProcessor
					.setUseInterparticleForces(Boolean.parseBoolean(attrs.getValue("interparticle_forces")));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("particle") && p != null) {
			Simulation.getInstance().add(p);
			p = null;
		}
		if (qName.equals("spring") && s != null) {
			Simulation.getInstance().add(s);
			s = null;
		}
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endDocument() throws SAXException {
		ConsoleWindow.println(GUIStrings.FILE_LOADING_FINISHED);
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String arg0, String arg1) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
		// TODO Auto-generated method stub

	}

}
