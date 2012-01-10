package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import team_rocket.cross_world.commons.data.Crossword;

import com.mongodb.MongoException;

public class CrossWorldCrosswordGenerator implements CrosswordGenerator {

	private WordProvider wordProvider;

	public static void main(String[] args) throws Exception {
		CrossWorldCrosswordGenerator cwcg = new CrossWorldCrosswordGenerator(
				new WordProvider(new WordDictionaryCreator()));
		// cwcg.generateCrossword(3, 5, new int[] { 3, 4, 5, 6, 10, 11, });
		// cwcg.generateCrossword(3, 3, new int[] {});
		// cwcg.generateCrossword(5, 5, new int[] {});
		cwcg.generateCrossword(9, 9, new int[] { 0, 1, 2, 6, 7, 8, 9, 10, 16,
				17, 18, 26, 31, 39, 40, 41, 49, 54, 62, 63, 64, 70, 71, 72, 73,
				74, 78, 79, 80 });
		
	}

	public CrossWorldCrosswordGenerator(WordProvider wordProvider) {
		this.wordProvider = wordProvider;
	}

	public Crossword generateCrossword(int rows, int cols, int[] blanks)
			throws Exception {
		Map<WordIdentifier, WordState> initialState = generateStartingState(
				cols, rows, blanks);
		// for (Entry<WordIdentifier, WordState> wordState :
		// initialState.entrySet()) {
		// System.out.println("Word number: "
		// + wordState.getKey().getWordNumber() + ", Is acrossed: "
		// + wordState.getKey().isAcross() + ", WordLength "
		// + wordState.getValue().getWordLength());
		// for (WordIdentifier crossWordId : wordState.getValue()
		// .getCrossedWords()) {
		// System.out.println("    Is acrossed: " + crossWordId.isAcross()
		// + " Word number: " + crossWordId.getWordNumber());
		// }
		// }
		Map<WordIdentifier, WordState> crossword = generateCrossword(initialState);
		printStuff(crossword);
		return null;
	}

	private void printStuff(Map<WordIdentifier, WordState> state)
			throws UnknownHostException, MongoException {
		for (WordState wordState : state.values()) {
			boolean[] availableWords = wordState.getAvailableWords();
			int i = 0;
			while (!availableWords[i++])
				;
			i--;
		}
	}

	private Map<WordIdentifier, WordState> generateCrossword(
			Map<WordIdentifier, WordState> initialState)
			throws UnknownHostException, MongoException {
		// WordState errorState = getNextState(initialState);
		boolean result = getFinalState(initialState);
		if (!result) {
			System.out.println("Error! Better luck next time.");
			System.exit(1);
		}
		System.out.println("Everything should be alright.");
		return initialState;
	}

	/**
	 * Retrieves the {@link WordState} with {@link WordState#isProcessed} equals
	 * to <code>false</code> with the least possible choices for word or null,
	 * if no such is found.
	 * 
	 * @param wordStates
	 * @return the most constrained {@link WordState}
	 */
	private WordState getMostConstrainedWordState(
			Collection<WordState> wordStates) {
		WordState mostConstrainted = null;
		for (WordState wordState : wordStates) {
			if (!wordState.isProcessed()
					&& (mostConstrainted == null || mostConstrainted
							.getNumberOfChoice() > wordState
							.getNumberOfChoice())) {
				mostConstrainted = wordState;
			}
		}
		return mostConstrainted;
	}

	/**
	 * Returns the best word match as string or null, if none is found.
	 * 
	 * @param wordState
	 * @param state
	 * @return
	 * @throws UnknownHostException
	 * @throws MongoException
	 */
	private int getBestWordMatch(WordState wordState,
			Map<WordIdentifier, WordState> state, boolean[] preconditions)
			throws UnknownHostException, MongoException {
		int wordCount = Math.min(wordState.getNumberOfChoice(), 10);
		boolean[] avalableWords = wordState.getAvailableWords();
		if (preconditions != null) {
			boolean hasAnyWWords = substract(preconditions, avalableWords);
			if (!hasAnyWWords) {
				return -1;
			}
			avalableWords = preconditions;
		}

		Set<Integer> wordsIndexes = getRandomAvailableWords(avalableWords,
				wordCount);
		int maxCrossedCount = -1;
		int maxCrossedCountIndex = -1;
		for (Integer wordIndex : wordsIndexes) {
			String word = wordProvider.getWord(wordState.getWordLength(),
					wordIndex);
			int crossedCount = getAvailableCrossedWordsCount(word,
					wordState.getCrossedWords(), state);
			if (crossedCount > maxCrossedCount) {
				maxCrossedCount = crossedCount;
				maxCrossedCountIndex = wordIndex;
			}
		}

		return maxCrossedCountIndex;
	}

	private boolean substract(boolean[] preconditions, boolean[] availableWords) {
		if (preconditions.length != availableWords.length) {
			throw new IllegalArgumentException("Greda.");
		}
		boolean hasAny = false;
		for (int i = 0; i < preconditions.length; i++) {
			preconditions[i] = !preconditions[i] && availableWords[i];
			hasAny = hasAny || preconditions[i];
		}

		return hasAny;
	}

	/**
	 * Returns the sum of the available crossed word choices if none of them is
	 * 0 and -1 otherwise.
	 * 
	 * @param wordIndex
	 * @param crossedWordEntries
	 * @return
	 * @throws MongoException
	 * @throws UnknownHostException
	 */
	private int getAvailableCrossedWordsCount(String word,
			List<Entry<WordIdentifier, Integer>> crossedWordEntries,
			Map<WordIdentifier, WordState> state) throws UnknownHostException,
			MongoException {
		int totalAvailableWords = 0;

		for (int i = 0; i < crossedWordEntries.size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = crossedWordEntries
					.get(i);
			if (crossedWordEntry == null) {
				continue;
			}
			WordState crossedWordState = state.get(crossedWordEntry.getKey());
			boolean[] crossedState = crossedWordState.getAvailableWords()
					.clone();
			wordProvider.intersect(crossedState,
					crossedWordState.getWordLength(),
					crossedWordEntry.getValue(), word.charAt(i) - 'a');
			int availableWords = 0;
			for (int j = 0; j < crossedState.length; j++) {
				if (crossedState[j]) {
					availableWords++;
				}
			}

			if (availableWords == 0) {
				return -1;
			} else {
				totalAvailableWords += availableWords;
			}
		}

		return totalAvailableWords;
	}

	private Set<Integer> getRandomAvailableWords(boolean[] availableWords,
			int wordCount) {
		List<Integer> availableWordIndexes = new ArrayList<Integer>();
		for (int i = 0; i < availableWords.length; i++) {
			if (availableWords[i]) {
				availableWordIndexes.add(i);
			}
		}

		Set<Integer> chosenWordIndexes = new HashSet<Integer>(wordCount);
		Random generator = new Random();
		int availableWordsCount = availableWordIndexes.size();
		int currentCount = 0;
		while (currentCount < wordCount) {
			int index = generator.nextInt(availableWordsCount);
			if (!chosenWordIndexes.contains(index)) {
				chosenWordIndexes.add(availableWordIndexes.get(index));
				currentCount++;
			}
		}
		return chosenWordIndexes;
	}

	private WordState getNextState(Map<WordIdentifier, WordState> state)
			throws UnknownHostException, MongoException {

		System.out.println();
		WordState wordState = getMostConstrainedWordState(state.values());

		if (wordState == null) {
			return null;
		}

		System.out.println("Processing " + wordState.getId().getWordNumber()
				+ ", " + (wordState.getId().isAcross() ? "across" : "down"));

		// get best word match (intelligent instantiation)
		int bestWordIndex = getBestWordMatch(wordState, state, null);
		if (bestWordIndex == -1) {
			return wordState;
		}
		String word = wordProvider.getWord(wordState.getWordLength(),
				bestWordIndex);
		System.out.println("Chose : " + word);
		// modify state
		boolean[] availableWords = wordState.getAvailableWords();
		boolean[] oldAvailableWords = availableWords.clone();
		for (int i = 0; i < availableWords.length; i++) {
			if (i != bestWordIndex) {
				availableWords[i] = false;
			}
		}
		wordState.setAvailableWords(availableWords);
		wordState.setProcessed(true);

		// change crossed states
		boolean[][] oldCrossedWordStates = new boolean[wordState
				.getWordLength()][wordState.getAvailableWords().length];
		for (int i = 0; i < wordState.getCrossedWords().size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = wordState
					.getCrossedWords().get(i);
			if (crossedWordEntry == null) {
				continue;
			}// TODO if is processed dont do that
			WordState crossedWordState = state.get(crossedWordEntry.getKey());
			oldCrossedWordStates[i] = crossedWordState.getAvailableWords()
					.clone();
			boolean[] crossedState = crossedWordState.getAvailableWords();
			wordProvider.intersect(crossedState,
					crossedWordState.getWordLength(),
					crossedWordEntry.getValue(), word.charAt(i) - 'a');
			crossedWordState.setAvailableWords(crossedState);
			System.out.println("Choise for crossed word "
					+ crossedWordState.getId().getWordNumber() + ", "
					+ (crossedWordState.getId().isAcross() ? "across" : "down")
					+ ", " + (crossedWordState.isProcessed() ? "" : "not ")
					+ "processed" + " - "
					+ crossedWordState.getNumberOfChoice());
		}

		// handle backtrack
		WordState nextState = getNextState(state);
		while (nextState != null) {
			System.out.println("Backtracking.");
			availableWords = null;
			// revert state
			wordState.setAvailableWords(oldAvailableWords);
			wordState.setProcessed(false);
			int i = 0;
			for (Entry<WordIdentifier, Integer> crossedWordEntry : wordState
					.getCrossedWords()) {
				if (crossedWordEntry == null) {
					continue;
				}
				state.get(crossedWordEntry.getKey()).setAvailableWords(
						oldCrossedWordStates[i++]);
			}
			// continue

			boolean isCrossed = false;
			for (Entry<WordIdentifier, Integer> crossedWordEntry : wordState
					.getCrossedWords()) {
				if (crossedWordEntry == null) {
					continue;
				}
				if (crossedWordEntry.getKey().equals(nextState.getId())) {
					nextState = getNextState(state);
					isCrossed = true;
					break;
				}
			}
			if (!isCrossed) {
				return nextState;
			}
		}
		return null;
	}

	private boolean getFinalState(Map<WordIdentifier, WordState> wordStateMap)
			throws UnknownHostException, MongoException {
		String word = null;
		WordState wordState = null;
		Deque<CrosswordState> crosswordStates = new LinkedList<CrosswordState>();
		boolean[] preconditions = null;
		do {
			System.out.println();
			if (wordState == null) {
				wordState = getMostConstrainedWordState(wordStateMap.values());
			}
			if (wordState == null) {
				return true;
			}

			System.out.println("Processing "
					+ wordState.getId().getWordNumber() + ", "
					+ (wordState.getId().isAcross() ? "across" : "down"));
			// get best word match (intelligent instantiation)

			int bestWordIndex = getBestWordMatch(wordState, wordStateMap,
					preconditions);
			preconditions = null;
			if (bestWordIndex != -1) {
				word = wordProvider.getWord(wordState.getWordLength(),
						bestWordIndex);
				CrosswordState crosswordState = getCurrentState(wordStateMap,
						wordState);
				changeCurrentState(wordStateMap, wordState, bestWordIndex, word);
				crosswordStates.addLast(crosswordState);
				wordState = null;
			} else {
				CrosswordState crosswordState;
				List<CrosswordState> backtrackedStates = new LinkedList<CrosswordState>();
				do {
					System.out.println("Backtracking");
					crosswordState = crosswordStates.removeLast();
					backtrackedStates.add(crosswordState);
					revertState(wordStateMap, crosswordState);
				} while (getCrossedIndex(wordState, crosswordState.wordState) != -1);
				preconditions = getPreconditions(backtrackedStates, word);
				wordState = crosswordState.wordState;
			}
		} while (!crosswordStates.isEmpty());
		return false;
	}

	private boolean[] getPreconditions(List<CrosswordState> backtrackedStates,
			String word) throws UnknownHostException, MongoException {
		CrosswordState lastState = backtrackedStates.remove(backtrackedStates
				.size() - 1);
		boolean[] preconditions = new boolean[lastState.wordState
				.getAvailableWords().length];
		for (int i = 0; i < preconditions.length; i++) {
			preconditions[i] = true;
		}
		for (CrosswordState crosswordState : backtrackedStates) {
			int index = getCrossedIndex(lastState.wordState,
					crosswordState.wordState);
			if (index != -1) {
				wordProvider.intersect(preconditions, word.length(), index,
						word.charAt(index) - 'a');
			}
		}
		return preconditions;
	}

	private int getCrossedIndex(WordState wordState, WordState otherWordState) {
		int i = 0;
		for (Entry<WordIdentifier, Integer> crossedWordEntry : wordState
				.getCrossedWords()) {
			if (crossedWordEntry == null) {
				continue;
			}
			if (crossedWordEntry.getKey().equals(otherWordState.getId())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private void revertState(Map<WordIdentifier, WordState> wordStateMap,
			CrosswordState crosswordState) {
		WordState wordState = crosswordState.wordState;
		wordState.setAvailableWords(crosswordState.oldAvailableWords);
		wordState.setProcessed(false);
		int i = 0;
		for (Entry<WordIdentifier, Integer> crossedWordEntry : wordState
				.getCrossedWords()) {
			if (crossedWordEntry == null) {
				continue;
			}
			wordStateMap.get(crossedWordEntry.getKey()).setAvailableWords(
					crosswordState.oldCrossedWordStates[i++]);
		}

	}

	private void changeCurrentState(
			Map<WordIdentifier, WordState> wordStateMap, WordState wordState,
			int bestWordIndex, String word) throws UnknownHostException,
			MongoException {
		System.out.println("Chose : " + word);

		boolean[] availableWords = wordState.getAvailableWords();
		for (int i = 0; i < availableWords.length; i++) {
			if (i != bestWordIndex) {
				availableWords[i] = false;
			}
		}
		wordState.setAvailableWords(availableWords);
		wordState.setProcessed(true);

		// change crossed states
		for (int i = 0; i < wordState.getCrossedWords().size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = wordState
					.getCrossedWords().get(i);
			if (crossedWordEntry == null) {
				continue;
			}// TODO if is processed dont do that
			WordState crossedWordState = wordStateMap.get(crossedWordEntry
					.getKey());
			boolean[] crossedState = crossedWordState.getAvailableWords();
			wordProvider.intersect(crossedState,
					crossedWordState.getWordLength(),
					crossedWordEntry.getValue(), word.charAt(i) - 'a');
			crossedWordState.setAvailableWords(crossedState);
			System.out.println("Choise for crossed word "
					+ crossedWordState.getId().getWordNumber() + ", "
					+ (crossedWordState.getId().isAcross() ? "across" : "down")
					+ ", " + (crossedWordState.isProcessed() ? "" : "not ")
					+ "processed" + " - "
					+ crossedWordState.getNumberOfChoice());
		}
	}

	private CrosswordState getCurrentState(
			Map<WordIdentifier, WordState> wordStateMap, WordState wordState) {
		boolean[] oldAvailableWords = wordState.getAvailableWords().clone();
		boolean[][] oldCrossedWordStates = new boolean[wordState
				.getWordLength()][wordState.getAvailableWords().length];
		for (int i = 0; i < wordState.getCrossedWords().size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = wordState
					.getCrossedWords().get(i);
			if (crossedWordEntry == null) {
				continue;
			}// TODO if is processed dont do that
			WordState crossedWordState = wordStateMap.get(crossedWordEntry
					.getKey());
			oldCrossedWordStates[i] = crossedWordState.getAvailableWords()
					.clone();
		}

		CrosswordState crosswordState = new CrosswordState();
		crosswordState.oldAvailableWords = oldAvailableWords;
		crosswordState.oldCrossedWordStates = oldCrossedWordStates;
		crosswordState.wordState = wordState;
		return crosswordState;
	}

	private Map<WordIdentifier, WordState> generateStartingState(int cols,
			int rows, int[] blanks) throws UnknownHostException, MongoException {
		int[][] crosswordField = new int[rows][cols];
		for (int blank : blanks) {
			crosswordField[blank / cols][blank % cols] = -1;
		}

		printCrossword(cols, rows, crosswordField);

		// Fills array with word indexes.
		// Calculates on which index there is a word's start.
		int wordIndex = 1;
		// gridnum, row, col
		List<int[]> acrossWords = new ArrayList<int[]>();
		List<int[]> downWords = new ArrayList<int[]>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (crosswordField[i][j] != -1) {
					boolean isWordStart = false;
					if ((i == 0) || (crosswordField[i - 1][j] == -1)) {
						int k = 0;
						while ((k + i) < rows && crosswordField[i + k][j] != -1) {
							k++;
						}
						if (k > 2) {
							downWords.add(new int[] { wordIndex, i, j });
							isWordStart = true;
						}
					}

					if ((j == 0) || (crosswordField[i][j - 1] == -1)) {
						int k = 0;
						while ((j + k) < cols && crosswordField[i][j + k] != -1) {
							k++;
						}
						if (k > 2) {
							acrossWords.add(new int[] { wordIndex, i, j });
							isWordStart = true;
						}
					}

					if (isWordStart) {
						crosswordField[i][j] = wordIndex;
						wordIndex++;
					}
				}
			}
		}

		printCrossword(cols, rows, crosswordField);

		// Adds crossing words for every word.
		// Fills the resulting WordIdentifier - WordState map.
		Map<WordIdentifier, WordState> words = new HashMap<WordIdentifier, WordState>();
		for (int[] word : acrossWords) {
			int i = word[2];
			List<Entry<WordIdentifier, Integer>> crossingWords = new ArrayList<Entry<WordIdentifier, Integer>>();
			while (i < cols && crosswordField[word[1]][i] != -1) {
				int j = word[1];
				while (j >= 0 && crosswordField[j][i] != -1) {
					j--;
				}
				if (crosswordField[j + 1][i] > 0) {
					boolean wordExists = false;
					for (int[] downWord : downWords) {
						if (downWord[0] == crosswordField[j + 1][i]) {
							wordExists = true;
							break;
						}
					}
					if (wordExists) {
						crossingWords
								.add(new SimpleImmutableEntry<WordIdentifier, Integer>(
										new WordIdentifier(
												crosswordField[j + 1][i], false),
										word[1] - j - 1));
					} else {
						crossingWords.add(null);
					}

				} else {
					crossingWords.add(null);
				}
				i++;
			}
			int wordLength = i - word[2];
			WordIdentifier identifier = new WordIdentifier(word[0], true);
			WordState state = new WordState(identifier, wordLength,
					crossingWords, wordProvider.getWordCount(wordLength));
			words.put(identifier, state);
		}

		for (int[] word : downWords) {
			int i = word[1];
			List<Entry<WordIdentifier, Integer>> crossingWords = new ArrayList<Entry<WordIdentifier, Integer>>();
			while (i < rows && crosswordField[i][word[2]] != -1) {
				int j = word[2];
				while (j >= 0 && crosswordField[i][j] != -1) {
					j--;
				}
				if (crosswordField[i][j + 1] > 0) {
					boolean wordExists = false;
					for (int[] acrossWord : acrossWords) {
						if (acrossWord[0] == crosswordField[i][j + 1]) {
							wordExists = true;
							break;
						}
					}
					if (wordExists) {
						crossingWords
								.add(new SimpleImmutableEntry<WordIdentifier, Integer>(
										new WordIdentifier(
												crosswordField[i][j + 1], true),
										word[2] - j - 1));
					} else {
						crossingWords.add(null);
					}
				}
				i++;
			}
			int wordLength = i - word[1];
			WordIdentifier identifier = new WordIdentifier(word[0], false);
			WordState state = new WordState(identifier, wordLength,
					crossingWords, wordProvider.getWordCount(wordLength));
			words.put(identifier, state);
		}
		return words;
	}

	private void printCrossword(int cols, int rows, int[][] crosswordField) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.out.print(crosswordField[i][j] + " ");
			}
			System.out.println();
		}
	}

}
