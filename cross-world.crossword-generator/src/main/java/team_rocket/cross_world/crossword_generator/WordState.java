package team_rocket.cross_world.crossword_generator;

import java.util.List;
import java.util.Map.Entry;

public class WordState {
	/**
	 * The length of the word.
	 */
	private int wordLength;
	/**
	 * An array with length the length of the word, that keeps mapping between
	 * crossed word {@link WordIdentifier} and an index denoting the crossed
	 * position in the crossed word. Note that i-th element correspons for the
	 * i-th position in the word,
	 * 
	 */
	private List<Entry<WordIdentifier, Integer>> crossedWords;
	/**
	 * An array with length the number of words with same length as the current.
	 * Every element means is the i-th word possible solution.
	 */
	private boolean[] availableWords;
	/**
	 * Keeps the number of choices.
	 */
	private int numberOfChoice;
	/**
	 * Indicates if the {@link WordState} has been processed, i.e. if a word has
	 * been already chosen.
	 */
	private boolean isProcessed;
	/**
	 * The id of the word state
	 */
	private WordIdentifier id;
	
	public WordState(WordIdentifier id, int wordLength,
			List<Entry<WordIdentifier, Integer>> crossedWords,
			int availableWordsCount) {
		this.id = id;
		this.wordLength = wordLength;
		this.crossedWords = crossedWords;
		this.isProcessed = false;
		this.availableWords = new boolean[availableWordsCount];
		for (int i = 0; i < availableWordsCount; i++) {
			this.availableWords[i] = true;
		}
		this.numberOfChoice = availableWordsCount;
	}

	public void setAvailableWords(boolean[] availableWords) {
		this.availableWords = availableWords;
		countNumberOfChoice();
	}

	public int getNumberOfChoice() {
		return this.numberOfChoice;
	}

	public int getWordLength() {
		return wordLength;
	}

	public List<Entry<WordIdentifier, Integer>> getCrossedWords() {
		return crossedWords;
	}

	public boolean[] getAvailableWords() {
		return availableWords;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	private void countNumberOfChoice() {
		numberOfChoice = 0;
		for (int i = 0; i < availableWords.length; i++) {
			if (availableWords[i]) {
				numberOfChoice++;
			}
		}
	}

	public WordIdentifier getId() {
		return id;
	}
}
