package gui.viewport.listeners;

import gui.MainWindow;
import gui.viewport.Viewport;

public class ViewportMouseListenersFactory {
	public static ViewportMouseListener getViewportListener(MouseMode mouseMode, Viewport v, MainWindow w) {
		switch (mouseMode) {
		case SELECT_PARTICLE:
			return new ViewportMouseListener(v, w);
		case SELECT_SPRING:
			return new ViewportMouseListener(v, w);
		case ADD_PARTICLE:
			return new ViewportMouseListenerAdd(v, w);
		case ADD_SPRING:
			return new ViewportMouseListenerAdd(v, w);
		case PARTICLE_ACT_FORCE:
			return new ViewportMouseListenerAct(v, w);
		case PARTICLE_ACT_DISPLACE:
			return new ViewportMouseListenerAct(v, w);
		default:
			break;
		}
		return null;
	}
}
