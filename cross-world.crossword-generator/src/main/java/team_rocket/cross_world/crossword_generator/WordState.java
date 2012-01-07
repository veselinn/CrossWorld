package team_rocket.cross_world.crossword_generator;

import java.util.List;

public class WordState {
	private int wordLength;
	private List<WordIdentifier> crossedWords;
	private boolean[] availableWords;
	
	public WordState(int wordLength, List<WordIdentifier> crossedWords, int availableWordsCount) {
		this.wordLength = wordLength;
		this.crossedWords = crossedWords;
		this.availableWords = new boolean[availableWordsCount];
		for(int i = 0; i < availableWordsCount; i++) {
			this.availableWords[i] = true;
		}
	}
	
	public int getWordLength() {
		return wordLength;
	}
	public List<WordIdentifier> getCrossedWords() {
		return crossedWords;
	}
	public boolean[] getAvailableWords() {
		return availableWords;
	}	
}
