package elements.group;

import java.util.ArrayList;

import elements.line.Spring;
import elements.point.Particle;
import gui.shapes.SpringShape;
import simulation.math.Functions;

public class SpringGroup extends ArrayList<Spring> {

	private static final long serialVersionUID = 2346793353589209048L;

	public Spring findNearestSpring(double x, double y, double maxDistance) {
		double dist, margin;
		Spring nearest = null;
		for (Spring s : this) {
			if (s.isLine())
				margin = maxDistance;
			else
				margin = SpringShape.SPRING_ZIGZAG_AMPLITUDE + s.getVisibleWidth() / 2;
			dist = Functions.defineDistanceToLineSegment(s, x, y);
			if (dist < Math.max(s.getVisibleWidth() / 2, margin)) {
				nearest = s;
			}
		}
		return nearest;
	}
	
	public SpringGroup findAttachedSprings(Particle p) {
		SpringGroup returnList = new SpringGroup();
		for (int i = 0; i < this.size(); i++) {
			Spring s = this.get(i);
			if (s != null) {
				if (s.isHasParticle(p))
					returnList.add(s);
			}
		}
		return returnList;
	}
	
}
