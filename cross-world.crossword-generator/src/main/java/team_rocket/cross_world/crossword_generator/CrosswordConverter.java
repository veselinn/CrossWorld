package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.Mongo;

import static team_rocket.cross_world.commons.constants.Constants.Mongo.*;
import team_rocket.cross_world.commons.data.Crossword;

public class CrosswordConverter {
	
	private static DBCollection mWordsCollection;
	private static Random random = new Random();
	
	public static Crossword convertCrossword(WordProvider wordProvider, int[] blanks, int rows, 
			int cols, Map<WordIdentifier, WordState> crossword) 
					throws UnknownHostException, MongoException {
		int[][] crosswordField = new int[rows][cols];
		for (int blank : blanks) {
			crosswordField[blank / cols][blank % cols] = -1;
		}
		
		List<int[]> acrossWords = new ArrayList<int[]>();
		List<int[]> downWords = new ArrayList<int[]>();
		
		CrossWorldCrosswordGenerator.createStartingGrid(crosswordField, acrossWords, downWords);
		
		Map<Integer, int[]> acrossWordsPositions =
				createWordsIndexGridPositionsMapping(acrossWords);
		Map<Integer, int[]> downWordsPositions = 
				createWordsIndexGridPositionsMapping(downWords);
		
		int[] gridNums = createGridNumbers(rows, cols, crosswordField);
		
		initializaDB();
		
		char[] grid = new char[rows * cols];
		List<String> cluesAcross = new ArrayList<String>(acrossWordsPositions.size());
		List<String> cluesDown = new ArrayList<String>(downWordsPositions.size());
		for (Map.Entry<WordIdentifier, WordState> entry : crossword.entrySet()) {
			WordState state = entry.getValue();
			WordIdentifier identifier = entry.getKey();
			
			int wordIndex = 0;
			while (!state.getAvailableWords()[wordIndex]) {
				wordIndex++;
			}
			String word = wordProvider.getWord(state.getWordLength(), wordIndex);
			
			int indexInCrossword = identifier.getWordNumber();
			int[] wordPositionInCrossword = (identifier.isAcross() ? acrossWordsPositions : 
				downWordsPositions).get(indexInCrossword);
			int wordPositionInGrid = wordPositionInCrossword[0] * cols + wordPositionInCrossword[1];
			for (int i = 0; i < word.length(); i++) {
				grid[wordPositionInGrid + (identifier.isAcross() ? i : i * cols)] = word.charAt(i);
			}
			
			String[] clues = (String[])mWordsCollection.findOne(new BasicDBObject(FIELD_WORDS_WORD, 
					word)).get(FIELD_WORDS_CLUES);
			String clue = indexInCrossword + ". " + clues[random.nextInt(clues.length)];
			(identifier.isAcross() ? cluesAcross : cluesDown).add(clue);
		}
		
		Crossword resultingCrossword = new Crossword();
		resultingCrossword.setRows(rows);
		resultingCrossword.setCols(cols);
		resultingCrossword.setGridNums(gridNums);
		resultingCrossword.setGrid(grid);
		
		String[] resultingCluesAcross = new String[cluesAcross.size()];
		cluesAcross.toArray(resultingCluesAcross);
		resultingCrossword.setCluesAcross(resultingCluesAcross);
		
		String[] resultingCluesDown = new String[cluesAcross.size()];		
		cluesDown.toArray(resultingCluesDown);
		resultingCrossword.setCluesDown(resultingCluesDown);
		
		return resultingCrossword;
	}

	private static int[] createGridNumbers(int rows, int cols,
			int[][] crosswordField) {
		int[] gridNums = new int[rows * cols];
		for (int i = 0; i < crosswordField.length; i++) {
			for (int j = 0; j < crosswordField[i].length; j++) {
				gridNums[i + j] = crosswordField[i][j] <= 0 ? 0 : crosswordField[i][j];
			}
		}
		return gridNums;
	}

	private static Map<Integer, int[]> createWordsIndexGridPositionsMapping(
			List<int[]> acrossWords) {
		Map<Integer, int[]> acrossWordsPositions = new HashMap<Integer, int[]>();		
		for (Iterator<int[]> iterator = acrossWords.iterator(); iterator.hasNext();) {
			int[] is = (int[]) iterator.next();
			acrossWordsPositions.put(is[0], new int[] {is[1], is[2]});
		}
		return acrossWordsPositions;
	}
	
	private static void initializaDB() throws UnknownHostException, MongoException {
		if (mWordsCollection != null) {
			Mongo mongo = new Mongo();
			DB crossWorld = mongo.getDB(DB_NAME);
			mWordsCollection = crossWorld.getCollection(DB_COLLECTION_WORDS);
		}
	}
}
