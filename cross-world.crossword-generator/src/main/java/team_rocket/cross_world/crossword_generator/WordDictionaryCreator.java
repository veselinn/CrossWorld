package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import team_rocket.cross_world.commons.data.WordDictionary;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import static team_rocket.cross_world.commons.constants.Constants.Mongo.*;
import static team_rocket.cross_world.commons.constants.Constants.ALPHABET_SIZE;

public class WordDictionaryCreator {
	private DBCollection mWordsCollection;

	public static void main(String[] args) throws UnknownHostException,
			MongoException {
		WordDictionaryCreator wdc = new WordDictionaryCreator();
		wdc.initializeDBConnection();
		Map<Integer, WordDictionary> dictionaries = wdc.getDictionaries();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 26; j++) {
				if(dictionaries.get(4).getWordMapping(i, j)[123] == true) {
					char currentCh = (char) ('a' + j);
					System.out.println("letter " + currentCh);
				}
			}
		}

		System.out.println(dictionaries.get(4).getWordMapping(0, 0).length);
	}

	public Map<Integer, WordDictionary> getDictionaries() {
		Map<Integer, WordDictionary> dictionaries = new HashMap<Integer, WordDictionary>();
		int allWordsCount = mWordsCollection.find().count();
		int currentWordsCount = 0;
		int wordsLength = 1;
		while (currentWordsCount < allWordsCount) {
			WordDictionary dictionary = getWordDictionary(wordsLength);
			int wordsCount = dictionary.getWordMapping(0, 0).length;
			currentWordsCount += wordsCount;
			if (wordsCount > 0) {
				dictionaries.put(wordsLength, dictionary);
			}
			wordsLength++;
		}
		return dictionaries;
	}

	public WordDictionary getWordDictionary(int wordLength) {
		List<DBObject> words = getWordsByLength(wordLength);
		String[] wordStrings = getWordStrings(words);
		boolean[][][] dictionaryStructure = createDictionaryStructure(
				wordStrings, wordLength);
		return new WordDictionary(dictionaryStructure);
	}

	private String[] getWordStrings(List<DBObject> words) {
		String[] wordString = new String[words.size()];
		for (int i = 0; i < words.size(); i++) {
			DBObject word = words.get(i);
			wordString[i] = (String) word.get(FIELD_WORDS_WORD);
		}
		return wordString;
	}

	private List<DBObject> getWordsByLength(int wordLength) {
		DBObject wordQuery = new BasicDBObject(FIELD_WORDS_WORD,
				Pattern.compile("^.{" + wordLength + "}$"));
		DBObject keysQuery = new BasicDBObject(FIELD_WORDS_WORD, 1);
		return mWordsCollection.find(wordQuery, keysQuery).toArray();
	}

	private boolean[][][] createDictionaryStructure(String[] words,
			int wordLength) {
		boolean[][][] dictionary = new boolean[wordLength][ALPHABET_SIZE][words.length];
		for (int i = 0; i < words.length; i++) {
			for (int j = 0; j < wordLength; j++) {
				int letterIndex = getAlphabetIndex(words[i].charAt(j));
				dictionary[j][letterIndex][i] = true;
			}
		}
		return dictionary;
	}

	private int getAlphabetIndex(char charAt) {
		return charAt - 'a';
	}

	private void initializeDBConnection() throws UnknownHostException,
			MongoException {
		Mongo mongo = new Mongo();
		DB crossWorld = mongo.getDB(DB_NAME);
		mWordsCollection = crossWorld.getCollection(DB_COLLECTION_WORDS);
	}
}
