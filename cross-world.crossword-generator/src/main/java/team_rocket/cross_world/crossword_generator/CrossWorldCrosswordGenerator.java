package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import team_rocket.cross_world.commons.data.Crossword;

import com.mongodb.MongoException;

public class CrossWorldCrosswordGenerator implements CrosswordGenerator {

	// private static Logger logger = Logger
	// .getLogger(CrossWorldCrosswordGenerator.class);

	private static final int MIN_WORD_LENGTH = 2;
	private static final int MAX_TRIES_PER_WORDSTATE = 7;
	private static final int INTEGELLIGENT_INSTANTIATION_WORD_COUNT = 10;
	
	private WordProvider wordProvider;

	public static void main(String[] args) throws Exception {
		// BasicConfigurator.configure();
		// logger.addAppender(new FileAppender(new SimpleLayout(),
		// "/crossword-generator.log"));
		// logger.addAppender(new ConsoleAppender(new SimpleLayout()));

		CrossWorldCrosswordGenerator cwcg = new CrossWorldCrosswordGenerator(
				new WordProvider(new WordDictionaryCreator()));
		// cwcg.generateCrossword(3, 5, new int[] { 3, 4, 5, 6, 10, 11, });
		// cwcg.generateCrossword(3, 3, new int[] {});
//		cwcg.generateCrossword(5, 5, new int[] {});
		cwcg.generateCrossword(7, 7, new int[] {});
//		cwcg.generateCrossword(9, 9, new int[] { 0, 1, 2, 6, 7, 8, 9, 10, 16,
//				17, 18, 26, 31, 39, 40, 41, 49, 54, 62, 63, 64, 70, 71, 72, 73,
//				74, 78, 79, 80 });
//		cwcg.generateCrossword(13, 13, new int[] {
//				4, 17, 30, 134, 147, 160, 8, 21, 34, 138, 151, 164,
//				52, 53, 54, 62, 63, 64, 104, 105, 106, 114, 115, 116,
//				45,
//				58,
//				70, 71, 72,
//				81, 82, 83, 84, 85, 86, 87, 
//				96, 97, 98,
//				110,
//				123
//		});
//		cwcg.generateCrossword(13, 13, new int[] {
//				4, 17, 30, 134, 147, 160, 8, 21, 34, 138, 151, 164,
//				52, 53, 54, 62, 63, 64, 104, 105, 106, 114, 115, 116,
//				58,
//				70, 71, 72,
//				82, 83, 84, 85, 86, 
//				96, 97, 98,
//				110
//		});

	}

	public CrossWorldCrosswordGenerator(WordProvider wordProvider) {
		this.wordProvider = wordProvider;
	}

	public Crossword generateCrossword(int rows, int cols, int[] blanks)
			throws Exception {
		Map<WordIdentifier, WordState> initialState = generateStartingState(
				cols, rows, blanks);
		Map<WordIdentifier, WordState> crossword = generateCrossword(initialState);
		printState(crossword);
		return CrosswordConverter.convertCrossword(wordProvider, blanks, rows, cols, crossword);
	}

	private void printState(Map<WordIdentifier, WordState> state)
			throws UnknownHostException, MongoException {
		for (Entry<WordIdentifier, WordState> wordEntry : state.entrySet()) {
			int chosenWordIndex = 0;
			for (int i = 0; i < wordEntry.getValue().getAvailableWords().length; i++) {
				if (wordEntry.getValue().getAvailableWords()[i]) {
					chosenWordIndex = i;
					break;
				}
			}

			System.out.println("Word number: "
					+ wordEntry.getKey().getWordNumber()
					+ ", Is acrossed: "
					+ wordEntry.getKey().isAcross()
					+ ", Word: "
					+ wordProvider.getWord(
							wordEntry.getValue().getWordLength(),
							chosenWordIndex));
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
	 * Returns the best word match as int or -1, if none is found.
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
		
		int wordCount = Math.min(wordState.getNumberOfChoice(), INTEGELLIGENT_INSTANTIATION_WORD_COUNT);
		boolean[] avalableWords = wordState.getAvailableWords();
		if (preconditions != null) {
			boolean hasAnyWords = substract(preconditions, avalableWords);
			if (!hasAnyWords) {
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
		wordCount = Math.min(wordCount, availableWordsCount);
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
	
	/**
	 * Creates a starting grid and lists for the starting positions of the words. 
	 * <code>crosswordField</code> must contain 0 in every cell that is considered free and -1 for 
	 * every unavailable("blank") cell.
	 * Modifies <code>crosswordField</code> to contain the starting grid, <code>acrossWords</code> and 
	 * <code>downWords</code> to contain the start index and position of the across words and down 
	 * words. 
	 * 
	 * @param crosswordField
	 * @param acrossWords
	 * @param downWords
	 */
	public static void createStartingGrid(int[][] crosswordField, List<int[]> acrossWords,
			List<int[]> downWords) {
		int wordIndex = 1;
		int rows = crosswordField.length;
		int cols = crosswordField[0].length;
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (crosswordField[i][j] != -1) {
					boolean isWordStart = false;
					if ((i == 0) || (crosswordField[i - 1][j] == -1)) {
						int k = 0;
						while ((k + i) < rows && crosswordField[i + k][j] != -1) {
							k++;
						}
						if (k > MIN_WORD_LENGTH) {
							downWords.add(new int[] { wordIndex, i, j });
							isWordStart = true;
						}
					}

					if ((j == 0) || (crosswordField[i][j - 1] == -1)) {
						int k = 0;
						while ((j + k) < cols && crosswordField[i][j + k] != -1) {
							k++;
						}
						if (k > MIN_WORD_LENGTH) {
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
	}
	
	private boolean getFinalState(Map<WordIdentifier, WordState> wordStateMap)
			throws UnknownHostException, MongoException {
		WordState wordState = null;
		Map<WordState, List<Integer>> backtracksPerWordstate = new HashMap<WordState, List<Integer>>();
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

			System.out.println("Processing " + getIdentifierString(wordState));
			// get best word match (intelligent instantiation)

			int bestWordIndex = getBestWordMatch(wordState, wordStateMap,
					preconditions);
			preconditions = null;
			if (bestWordIndex != -1) {
				String word = wordProvider.getWord(wordState.getWordLength(),
						bestWordIndex);
				CrosswordState crosswordState = getCurrentState(wordStateMap,
						wordState, word, bestWordIndex);
				crosswordStates.addLast(crosswordState);

				changeCurrentState(wordStateMap, wordState, bestWordIndex, word);
				wordState = null;
			} else {
				backtracksPerWordstate
						.put(wordState, new LinkedList<Integer>());
				CrosswordState crosswordState;
				List<Integer> backtracksTries;
				List<CrosswordState> backtrackedStates = new LinkedList<CrosswordState>();
				do {
					System.out.println("Backtracking");
					if(crosswordStates.isEmpty()) {
						return false;
					}
					crosswordState = crosswordStates.removeLast();
					
					backtrackedStates.add(crosswordState);
					revertState(wordStateMap, crosswordState);
					backtracksTries = backtracksPerWordstate
							.put(crosswordState.wordState,
									new LinkedList<Integer>());
				} while (getCrossedIndex(wordState, crosswordState.wordState) == -1);

				if (backtracksTries == null) {
					backtracksTries = new LinkedList<Integer>();
				}

				boolean calculatePreconditions = true;
				while (backtracksTries.size() == MAX_TRIES_PER_WORDSTATE) {
					calculatePreconditions = false;
					backtracksPerWordstate.put(crosswordState.wordState,
							new LinkedList<Integer>());
					if(crosswordStates.isEmpty()) {
						return false;
					}
					crosswordState = crosswordStates.removeLast();
					revertState(wordStateMap, crosswordState);
					backtracksTries = backtracksPerWordstate
							.get(crosswordState.wordState);
					if (backtracksTries == null) {
						backtracksTries = new LinkedList<Integer>();
					}
					System.out
							.println("Backtrack count exceeded. Backtracking to "
									+ getIdentifierString(crosswordState.wordState)
									+ " - number of backtracks: "
									+ backtracksTries);
				}
				backtracksTries.add(crosswordState.wordIndex);
				backtracksPerWordstate.put(crosswordState.wordState,
						backtracksTries);
				// backtracksPerWordstate.put(crosswordState.wordState,
				// backtracksTries + 1);
				if (calculatePreconditions) {
					preconditions = getPreconditions(backtrackedStates,
							wordState);
				} else {
					preconditions = new boolean[crosswordState.wordState
							.getAvailableWords().length];
				}
				for (int i = 0; i < backtracksTries.size(); i++) {
					preconditions[backtracksTries.get(i)] = true;
				}
				wordState = crosswordState.wordState;
			}
		} while (true);
		// return false;
	}

	private String getIdentifierString(WordState wordState) {
		return wordState.getId().getWordNumber() + ", "
				+ (wordState.getId().isAcross() ? "across" : "down");
	}

	private boolean[] getPreconditions(List<CrosswordState> backtrackedStates,
			WordState brokenWordState) throws UnknownHostException,
			MongoException {
		System.out.println("Calculating preconditions.");
		CrosswordState lastState = backtrackedStates.remove(backtrackedStates
				.size() - 1);
		String word = lastState.word;

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
		int brokenWordIndex = getCrossedIndex(lastState.wordState,
				brokenWordState);
		wordProvider.intersect(preconditions, word.length(), brokenWordIndex,
				word.charAt(brokenWordIndex) - 'a');
		return preconditions;
	}

	private int getCrossedIndex(WordState wordState, WordState otherWordState) {
		int i = 0;

		System.out.println("Checking if " + wordState.getId().getWordNumber()
				+ ", " + (wordState.getId().isAcross() ? "across" : "down")
				+ " and " + otherWordState.getId().getWordNumber() + ", "
				+ (otherWordState.getId().isAcross() ? "across" : "down")
				+ " are crossing");

		for (Entry<WordIdentifier, Integer> crossedWordEntry : wordState
				.getCrossedWords()) {
			if (crossedWordEntry == null) {
				continue;
			}
			if (crossedWordEntry.getKey().equals(otherWordState.getId())) {
				System.out.println("Crossed.");
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
		wordState.setWord(null);
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
		wordState.setWord(word);

		// change crossed states
		for (int i = 0; i < wordState.getCrossedWords().size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = wordState
					.getCrossedWords().get(i);
			if (crossedWordEntry == null) {
				continue;
			}
			WordState crossedWordState = wordStateMap.get(crossedWordEntry
					.getKey());
//			if(crossedWordState.isProcessed()) {
//				continue;
//			}
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
			Map<WordIdentifier, WordState> wordStateMap, WordState wordState,
			String word, int wordIndex) {
		boolean[] oldAvailableWords = wordState.getAvailableWords().clone();
		boolean[][] oldCrossedWordStates = new boolean[wordState
				.getWordLength()][wordState.getAvailableWords().length];
		for (int i = 0; i < wordState.getCrossedWords().size(); i++) {
			Entry<WordIdentifier, Integer> crossedWordEntry = wordState
					.getCrossedWords().get(i);
			if (crossedWordEntry == null) {
				continue;
			}
			WordState crossedWordState = wordStateMap.get(crossedWordEntry
					.getKey());
			oldCrossedWordStates[i] = crossedWordState.getAvailableWords()
					.clone();
		}

		CrosswordState crosswordState = new CrosswordState();
		crosswordState.oldAvailableWords = oldAvailableWords;
		crosswordState.oldCrossedWordStates = oldCrossedWordStates;
		crosswordState.wordState = wordState;
		crosswordState.word = word;
		crosswordState.wordIndex = wordIndex;
		return crosswordState;
	}

	private Map<WordIdentifier, WordState> generateStartingState(int cols,
			int rows, int[] blanks) throws UnknownHostException, MongoException {
		int[][] crosswordField = new int[rows][cols];
		for (int blank : blanks) {
			crosswordField[blank / cols][blank % cols] = -1;
		}

		printCrossword(cols, rows, crosswordField);

		List<int[]> acrossWords = new ArrayList<int[]>();
		List<int[]> downWords = new ArrayList<int[]>();
		createStartingGrid(crosswordField, acrossWords, downWords);
		
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
				// logger.info(crosswordField[i][j] + " ");
				System.out.print((crosswordField[i][j] >= 0 ? " " : "")
						+ (crosswordField[i][j] < 10 ? " " : "")
						+ crosswordField[i][j] + " ");
			}
			// logger.info("\n");
			System.out.println();
		}
		// logger.info("\n");
		System.out.println();
	}

}
