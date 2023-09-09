package elements.groups;

import java.util.ArrayList;

import elements.force_pair.Spring;
import elements.point_mass.Particle;
import evaluation.MyMath;
import gui.shapes.SpringShape;

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
			dist = MyMath.defineDistanceToLineSegment(s, x, y);
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
