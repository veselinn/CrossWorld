package team_rocket.cross_world.crossword_generator;

import team_rocket.cross_world.commons.data.Crossword;

public interface CrosswordGenerator {
	public Crossword generateCrossword(int cols, int rows, int[] blanks) throws Exception;
}
