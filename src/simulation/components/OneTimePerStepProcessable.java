package simulation.components;

public interface OneTimePerStepProcessable {
	public void process();
	public void setSkipStepsNumber(int skip);
}
