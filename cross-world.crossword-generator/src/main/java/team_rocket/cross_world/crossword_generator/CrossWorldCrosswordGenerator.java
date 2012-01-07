package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team_rocket.cross_world.commons.data.Crossword;

import com.mongodb.MongoException;

public class CrossWorldCrosswordGenerator implements CrosswordGenerator {
	
	private WordProvider wordProvider;

	public static void main(String[] args) throws Exception {
		CrossWorldCrosswordGenerator cwcg = new CrossWorldCrosswordGenerator(new WordProvider(new WordDictionaryProvider()));
		cwcg.generateCrossword(5, 4, new int[] {1,2,7,12,17,18 });
	}
	
	public CrossWorldCrosswordGenerator(WordProvider wordProvider) {
		this.wordProvider = wordProvider;
	}
	
	public Crossword generateCrossword(int cols, int rows, int[] blanks) throws Exception {
		generateStartingState(cols, rows, blanks);
		return null;
	}

	private Map<WordIdentifier, WordState> generateStartingState(int cols, int rows, int[] blanks) throws UnknownHostException, MongoException {
		int[][] crosswordField  = new int[rows][cols];
		for(int blank: blanks) {
			crosswordField[blank%rows][blank/rows] = -1;
		}
		
		// Fills array with word indexes.
		// Calculates on which index there is a word's start.
		Map<WordIdentifier, WordState> words = new HashMap<WordIdentifier, WordState>();
		int wordIndex = 1;
		List<int[]> acrossWords = new ArrayList<int[]>();
		List<int[]> downWords = new ArrayList<int[]>();
		for(int i = 0 ; i < rows; i++) {
			for(int j = 0 ; j < cols; j++) {
					if (crosswordField[i][j] != -1) {
						crosswordField[i][j] = wordIndex;
					if(i == 0) {
						downWords.add(new int[] {wordIndex, i, j});
					} else if (crosswordField[i-1][j] == -1) {
						downWords.add(new int[] {wordIndex, i, j});
					}
					if(j == 0) {
						acrossWords.add(new int[] {wordIndex, i, j});
					} else if (crosswordField[i][j - 1] == -1) {
						acrossWords.add(new int[] {wordIndex, i, j});
					}
					wordIndex++;
				}
			}
		}
		
		//Adds crossing words for every word.
		//Fills the resulting WordIdentifier - WordState map.
		for(int[] word: acrossWords) {
			int i = word[2];
			List<WordIdentifier> crossingWords = new ArrayList<WordIdentifier>();
			while(i < cols && crosswordField[word[1]][i] != -1 ) {
				int j = word[1];
				while(j >= 0 && crosswordField[j][i] != -1 ) {
					j--;
				}
				crossingWords.add(new WordIdentifier(crosswordField[j+1][i], false));
				i++;
			}
			int wordLength = i - word[2];
			WordIdentifier identifier = new WordIdentifier(word[0], true);
			WordState state = new WordState(wordLength, crossingWords, wordProvider.getWordCount(wordLength));
			words.put(identifier, state);
		}
		
		for(int[] word: downWords) {
			int i = word[1];
			List<WordIdentifier> crossingWords = new ArrayList<WordIdentifier>();
			while(i < rows && crosswordField[i][word[2]] != -1 ) {
				int j = word[2];
				while(j >= 0 && crosswordField[i][j] != -1 ) {
					j--;
				}
				crossingWords.add(new WordIdentifier(crosswordField[i][j+1], true));
				i++;
			}
			int wordLength = i - word[1];
			WordIdentifier identifier = new WordIdentifier(word[0], false);
			WordState state = new WordState(wordLength, crossingWords, wordProvider.getWordCount(wordLength));
			words.put(identifier, state);
		}
		return words;
	}

}
