package gui.viewport.listeners;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import elements.point.Particle;
import gui.MainWindow;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;

public class ViewportMouseListenerAct extends ViewportMouseListener{
	
	protected int radiusX;
	protected int radiusY;

	public ViewportMouseListenerAct(Viewport v, MainWindow w) {
		super(v, w);
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
		if (p != null) {
			Simulation.getInstance().content().select(p);
			mainWindow.setFocusTo(p);
			radiusX = CoordinateConverter.toScreenX(p.getX()) - x1;
			radiusY = CoordinateConverter.toScreenY(p.getY()) - y1;
			labelAttachedSprings(p);
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		x2 = arg0.getX();
		y2 = arg0.getY();
		if (viewport.getMouseMode() == MouseMode.PARTICLE_ACT_DISPLACE
				&& Simulation.getInstance().content().getSelectedParticle(0) != null) {
			if (!Simulation.getInstance().isActive()) {
				Simulation.getInstance().content().getSelectedParticle(0)
						.setX(CoordinateConverter.fromScreenX(x2 + radiusX));
				Simulation.getInstance().content().getSelectedParticle(0)
						.setY(CoordinateConverter.fromScreenY(y2 + radiusY));
				if (viewport.useGrid)
					Simulation.getInstance().content().getSelectedParticle(0).snapToGrid(viewport.getGridSize());
				Simulation.getInstance().perfomStep(2, false);
			} else {
				Simulation.getInstance().interactionProcessor.setParticleTargetXY(new Point2D.Double(
						CoordinateConverter.fromScreenX(x2 + radiusX), CoordinateConverter.fromScreenY(y2 + radiusY)));
				Simulation.getInstance().interactionProcessor.setMoveToMouse(true);
			}
		} else if (viewport.getMouseMode() == MouseMode.PARTICLE_ACT_FORCE
				&& Simulation.getInstance().content().getSelectedParticle(0) != null
				&& Simulation.getInstance().isActive()) {
			Simulation.getInstance().interactionProcessor.setAccelerateByMouse(true);
			Simulation.getInstance().interactionProcessor.setParticleTargetXY(
					new Point2D.Double(CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2)));
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		Simulation.getInstance().interactionProcessor.setParticleTargetXY(new Point2D.Double(0, 0));
		Simulation.getInstance().interactionProcessor.setMoveToMouse(false);
		Simulation.getInstance().interactionProcessor.setAccelerateByMouse(false);
		radiusX = 0;
		radiusY = 0;		
		mainWindow.clearSelection();
	}	

}
