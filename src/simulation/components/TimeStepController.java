package simulation.components;

import static simulation.Simulation.interactionProcessor;

import gui.Lang;
import gui.MainWindow;
import gui.Viewport;
import simulation.Simulation;

public class TimeStepController implements OneTimePerStepProcessable {
	private double dt, targetdt, timeScale = 1, lastTimeMillis, measuredTimeScale;
	public static final double INITIAL_STEP_SIZE = 1e-20;
	public static final double TIME_STEP_ALARM_DECREMENT = 0.75;
	public static final float TIME_STEP_CHANGE_COEFFICIENT = 1.1f;
	private long stepsPerSecond = 0;
	private boolean timeStepAlarm = false;
	private static TimeStepMode mode;

	public enum TimeStepMode {
		DYNAMIC, FIXED
	}

	public TimeStepController() {
		mode = TimeStepMode.FIXED;
		dt = INITIAL_STEP_SIZE;
	}

	@Override
	public void process() {
		stepsPerSecond++;
		adjustTimeStep();
		if (timeStepAlarm) {
			correctTimeStepSize();
			timeStepAlarm = false;
		}
	}

	private void adjustTimeStep() {
		if (mode == TimeStepMode.DYNAMIC && Simulation.getInstance().isActive()) {
			targetdt = Simulation.getEvaluationTimeNanos() * 1E-9 * timeScale;
			dt += (targetdt - dt) / 2000;
		}
	}

	public void setTimeStepSize(double newdt) {
		if (newdt > 0) {
			dt = newdt;
			MainWindow.println(String.format(Lang.TIMESTEP + ": %.1e ñ", dt));
		}
	}

	public void multiplyTimeStepSize(double c) {
		if (mode == TimeStepMode.FIXED)
			setTimeStepSize(dt * c);
	}

	private void correctTimeStepSize() {
		double r = interactionProcessor.getTimeStepReserveRatio();
		setTimeStepSize(dt * r * TIME_STEP_ALARM_DECREMENT);
		if (mode == TimeStepMode.DYNAMIC)
			setTimeScale(timeScale * r * TIME_STEP_ALARM_DECREMENT);
		MainWindow.println(String.format(Lang.TIMESTEP_CORRECTION_DONE + " -> ", dt, r));
	}

	public void decreaseTimeStepSize(double coef) {
		if (mode == TimeStepMode.DYNAMIC)
			setTimeScale(timeScale / coef);
		else
			setTimeStepSize(dt / coef);
	}

	public void increaseTimeStepSize(double coef) {
		if (mode == TimeStepMode.DYNAMIC)
			setTimeScale(timeScale * coef);
		else
			setTimeStepSize(dt * coef);
	}

	public double getTimeStepSize() {
		return dt;
	}

	public double defineFd() {
		return 1d / dt;
	}

	public void resetTimeStep() {
		dt = INITIAL_STEP_SIZE;
		stepsPerSecond = 0;
		measuredTimeScale = 1d;
		lastTimeMillis = 0;
	}

	public TimeStepMode getMode() {
		return mode;
	}

	public void setModeAndReset(TimeStepMode newMode) {
		setMode(newMode);
		resetTimeStep();
	}

	private void setMode(TimeStepMode newMode) {
		mode = newMode;
		MainWindow.println(Lang.TIMESTEP_CONTROL_MODE + ": " + mode.toString());
	}

	public double getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(double newTimeScale) {
		if (newTimeScale > 0) {
			if (mode == TimeStepMode.DYNAMIC) {
				timeScale = newTimeScale;
				MainWindow.println(String.format(Lang.TIMESCALE + ": %.1e", timeScale));
			} else if (newTimeScale == 1 && measuredTimeScale > 0)
				multiplyTimeStepSize(1d / measuredTimeScale);
		}
	}

	public long getStepsPerSecond() {
		return stepsPerSecond;
	}

	public void clearStepsPerSecond() {
		stepsPerSecond = 0;
	}

	@Override
	public void setSkipStepsNumber(int skip) {
	}

	public void setTimeStepAlarm() {
		timeStepAlarm = true;
	}

	public boolean isTimeStepAlarm() {
		return timeStepAlarm;
	}

	public void measureTimeScale() {
		if (Simulation.getInstance().isActive()) {
			double timeMillis = Simulation.getTime() * 1000;
			measuredTimeScale = (timeMillis - lastTimeMillis) / Viewport.REFRESH_MESSAGES_INTERVAL;
			lastTimeMillis = timeMillis;
		}
	}

	public double getMeasuredTimeScale() {
		return measuredTimeScale;
	}

	public void switchMode() {
		if (mode == TimeStepMode.DYNAMIC) {
			setMode(TimeStepMode.FIXED);
		} else if (mode == TimeStepMode.FIXED) {
			setMode(TimeStepMode.DYNAMIC);
			setTimeScale(measuredTimeScale);
		}
	}
}
