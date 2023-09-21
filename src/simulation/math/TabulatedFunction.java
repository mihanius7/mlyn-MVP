package simulation.math;

public abstract class TabulatedFunction {
	protected double stepSize, minX, maxX, param1, param2;
	protected double[] functionTable;
	protected int stepsNumber;

	public TabulatedFunction(double minX, double maxX, double step) {
		this.stepSize = step;
		this.minX = minX;
		this.maxX = maxX;
		stepsNumber = (int) ((maxX - minX) / stepSize);
		functionTable = new double[stepsNumber + 2];
	}

	public abstract void calculateTable();
	
	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
	}

	public double getParam2() {
		return param2;
	}

	public void setParam2(double param2) {
		this.param2 = param2;
	}

	public double getFromTable(double x) {
		if ((x >= minX) && (x <= maxX - minX)) {
			return functionTable[(int) Math.round(x / stepSize)];
		} else
			return -1;
	}

	public double getMaxArgument() {
		return maxX;
	}

	public double getMinArgument() {
		return minX;
	}

	public int getStepsNumber() {
		return stepsNumber;
	}
}
