package team_rocket.cross_world.dictionary_manipulator;

import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

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
	private DBCollection mDictionariesCollection;
	
	public static void main(String[] args) throws UnknownHostException, MongoException {
		WordDictionaryCreator wdc = new WordDictionaryCreator();
		wdc.initializeDBConnection();
		wdc.initializeDictionaries();
	}
	
	public void initializeDictionaries() {
		mDictionariesCollection.remove(new BasicDBObject());
		int allWordsCount = mWordsCollection.find().count();
		int currentWordsCount = 0;
		int wordsLength = 1;
		while(currentWordsCount < allWordsCount) {
			int wordsCount = addWordDictionary(wordsLength++);
			currentWordsCount += wordsCount;
		}
	}
	
	public int addWordDictionary(int wordLength) {
		List<DBObject> words = getWordsByLength(wordLength);
		int wordsCount = words.size();
		String[] wordString = new String[wordsCount];
		for(int i = 0; i < wordsCount; i++){
			DBObject word = words.get(i); 
			wordString[i] = (String) word.get(FIELD_WORDS_WORD);
		}
		boolean[][][] dictionaryStructure = createDictionaryStructure(wordString);
		DBObject dbWordDictionary = new BasicDBObject();
		dbWordDictionary.put(FIELD_DICTIONARY_WORD_SIZE, wordLength);
		dbWordDictionary.put(FIELD_DICTIONARY_STRUCTURE, dictionaryStructure);
		mDictionariesCollection.save(dbWordDictionary);
		return wordsCount; 
	}
	
	private List<DBObject> getWordsByLength(int wordLength) {
		DBObject wordQuery = new BasicDBObject(FIELD_WORDS_WORD, Pattern.compile("^.{" + wordLength + "}$"));
		DBObject keysQuery = new BasicDBObject(FIELD_WORDS_WORD, 1);
		return mWordsCollection.find(wordQuery, keysQuery).toArray();
	}

	private boolean[][][] createDictionaryStructure(String[] words) {
		int wordLength = words.length != 0 ? words[0].length() : 0;
		boolean[][][] dictionary = new boolean[wordLength][ALPHABET_SIZE][words.length];
		for(int i = 0; i < words.length; i++){
			for(int j = 0; j < wordLength; j++){
				int letterIndex = getAlphabetIndex(words[i].charAt(j));
				dictionary[j][letterIndex][i] = true;
			}
		}
		return dictionary;
	}
	
	private int getAlphabetIndex(char charAt) {
		return charAt - 'a';
	}

	private void initializeDBConnection() throws UnknownHostException, MongoException {
		Mongo mongo = new Mongo();
		DB crossWorld = mongo.getDB(DB_NAME);
		mWordsCollection = crossWorld.getCollection(DB_COLLECTION_WORDS);
		mDictionariesCollection = crossWorld.getCollection(DB_COLLECTION_DICTIONARIES);
	}
}
