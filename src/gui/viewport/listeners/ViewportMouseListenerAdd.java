package gui.viewport.listeners;

import static simulation.Simulation.getInstance;

import java.awt.event.MouseEvent;

import elements.line.Spring;
import elements.point.Particle;
import gui.MainWindow;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;

public class ViewportMouseListenerAdd extends ViewportMouseListener {

	public ViewportMouseListenerAdd(Viewport v, MainWindow w) {
		super(v, w);
		getInstance().content().setMaxSelectionNumber(1);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		x1 = arg0.getX();
		y1 = arg0.getY();
		x2 = x1;
		y2 = y1;
		Particle p = Simulation.getInstance().content().getParticles().findNearestParticle(
				CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
				CoordinateConverter.fromScreen(10));
		if (viewport.getMouseMode() == MouseMode.ADD_PARTICLE && p == null) {
			Simulation.getInstance().content().getReferenceParticle().setX(CoordinateConverter.fromScreenX(x1));
			Simulation.getInstance().content().getReferenceParticle().setY(CoordinateConverter.fromScreenY(y1));
			Simulation.getInstance();
			if (viewport.useGrid && !Simulation.getInstance().isActive())
				Simulation.getInstance().content().getReferenceParticle().snapToGrid(viewport.getGridSize());
			Simulation.getInstance().content().getReferenceParticle().getShape().setVisible(true);
			mainWindow.clearSelection();
		} else if (viewport.getMouseMode() == MouseMode.ADD_SPRING) {
			if (p != null) {
				Simulation.getInstance().content().select(p);
				mainWindow.setFocusTo(p);
				labelAttachedSprings(p);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		x2 = arg0.getX();
		y2 = arg0.getY();
		if (viewport.getMouseMode() == MouseMode.ADD_PARTICLE
				&& Simulation.getInstance().content().getReferenceParticle().getShape().isVisible()) {
			Simulation.getInstance().content().getReferenceParticle().setX(CoordinateConverter.fromScreenX(x2));
			Simulation.getInstance().content().getReferenceParticle().setY(CoordinateConverter.fromScreenY(y2));
			Simulation.getInstance();
			if (viewport.useGrid && !Simulation.getInstance().isActive())
				Simulation.getInstance().content().getReferenceParticle().snapToGrid(viewport.getGridSize());
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (viewport.getMouseMode() == MouseMode.ADD_PARTICLE
				&& Simulation.getInstance().content().getReferenceParticle().getShape().isVisible()) {
			Simulation.getInstance().content().getReferenceParticle().getShape().setVisible(false);
			Particle refP = Simulation.getInstance().content().getReferenceParticle();
			Particle newP = new Particle(refP.getX(), refP.getY(), refP);
			Simulation.getInstance().add(newP);
			Simulation.getInstance().content().select(newP);
			mainWindow.setFocusTo(newP);
		} else if (viewport.getMouseMode() == MouseMode.ADD_SPRING
				&& Simulation.getInstance().content().getSelectedParticles().size() > 0) {
			Particle p = Simulation.getInstance().content().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2),
					CoordinateConverter.fromScreen(10));
			if (p == null) {
				Particle p2 = new Particle(CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2), 0,
						0, Simulation.getInstance().content().getReferenceParticle().getMass(),
						Simulation.getInstance().content().getReferenceParticle().getRadius());
				Simulation.getInstance().add(p2);
				p2.setX(CoordinateConverter.fromScreenX(x2));
				p2.setY(CoordinateConverter.fromScreenY(y2));
				if (viewport.useGrid)
					p2.snapToGrid(viewport.getGridSize());
				p = p2;
			}
			if (Simulation.getInstance().content().getSelectedParticle(0) != p) {
				Spring s = new Spring(Simulation.getInstance().content().getSelectedParticle(0), p, 5E4, 0);
				s.setDampingRatio(0.15d);
				s.setResonantFrequency(50d);
				Simulation.getInstance().add(s);
			}
			mainWindow.clearSelection();
		}
	}
}
