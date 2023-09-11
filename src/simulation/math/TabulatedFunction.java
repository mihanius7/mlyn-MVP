package simulation.math;

import gui.ConsoleWindow;

public class TabulatedFunction {
	private double stepSize, minX, maxX, param1, param2;
	private double[] functionTable;
	private int stepsNumber;
	private FunctionType type;

	public enum FunctionType {
		LD, SQRT
	}

	public TabulatedFunction(FunctionType t, double minX, double maxX, double step) {
		this.type = t;
		this.stepSize = step;
		this.minX = minX;
		this.maxX = maxX;
		stepsNumber = (int) ((maxX - minX) / stepSize);
		functionTable = new double[stepsNumber + 2];
	}

	public void calculateTable() {
		double x = minX;
		for (int i = 0; i < functionTable.length - 1; i++) {
			x = minX + i * stepSize;
			if (type == FunctionType.LD)
				functionTable[i] = LD(x, param1, param2);
			else if (type == FunctionType.SQRT)
				functionTable[i] = Math.sqrt(x);
		}
	}

	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
		ConsoleWindow.println(String.format("Табуляваная функцыя, параметр 1 усталяваны: %.3e", this.param1));
	}

	public double getParam2() {
		return param2;
	}

	public void setParam2(double param2) {
		this.param2 = param2;
		ConsoleWindow.println(String.format("Табуляваная функцыя, параметр 2 усталяваны: %.3e", this.param2));
	}

	public FunctionType getType() {
		return type;
	}

	public void setType(FunctionType type) {
		this.type = type;
	}

	private double LD(double r, double a, double d) {
		return 12 * d / a * (Math.pow(a / r, 10) - Math.pow(a / r, 7));
	}

	public double getFromTable(double x) {
		if ((x >= minX) && (x <= maxX - minX)) {
			return functionTable[(int) Math.round(x / stepSize)];
		} else
			return 0;
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
