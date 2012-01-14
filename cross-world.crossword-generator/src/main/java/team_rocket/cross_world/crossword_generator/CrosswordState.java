package team_rocket.cross_world.crossword_generator;

public class CrosswordState {
	public boolean[] oldAvailableWords;
	public boolean[][] oldCrossedWordStates;
	public String word;
	public int wordIndex;
	public WordState wordState;
}
