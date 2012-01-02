package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.List;

import team_rocket.cross_world.commons.data.WordDictionary;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class WordDictionaryProvider {
	private static final String DB_NAME = "CrossWorld";
	private static String DB_COLLECTION_DICTIONARIES = "dictionaries";
	
	private static String FIELD_DICTIONARY_STRUCTURE = "structure";
	private static String FIELD_DICTIONARY_WORD_SIZE = "wordLength";
	
	private static int ALPHABET_SIZE = 26;
	
	private DBCollection mDictionariesCollection;
	
	public WordDictionaryProvider() {
	}
	
	public WordDictionary getWordDictionary(int wordLength) {
		DBObject keyQuery = new BasicDBObject(FIELD_DICTIONARY_STRUCTURE, 1);
		DBObject dictQuery = new BasicDBObject(FIELD_DICTIONARY_WORD_SIZE, wordLength);
		DBObject wordDictionary = mDictionariesCollection.findOne(dictQuery, keyQuery);
		boolean[][][] structure;
		if(wordDictionary != null) {
			structure = convertToBooleanArray(wordDictionary.get(FIELD_DICTIONARY_STRUCTURE));
		} else {
			structure = new boolean[wordLength][ALPHABET_SIZE][0];
		}
		return new WordDictionary(structure);
	}

	private boolean[][][] convertToBooleanArray(Object arrayObject) {
		@SuppressWarnings("unchecked")
		List<List<List<Boolean>>> list = (List<List<List<Boolean>>>)arrayObject;
		int listSize1 = list.size();
		int listSize2 = list.get(0).size();
		int listSize3 = list.get(0).get(0).size();
		boolean [][][] array = new boolean[listSize1][listSize2][listSize3];
		for(int i = 0 ; i < listSize1 ; i++) {
			for(int j = 0 ; j < listSize2 ; j++) {
				for(int k = 0 ; k < listSize3 ; k++) {
					array[i][j][k] = list.get(i).get(j).get(k);
				}
			}
		}
		return array;
	}

	public void initializeDBConnection() throws UnknownHostException, MongoException {
		Mongo mongo = new Mongo();
		DB crossWorld = mongo.getDB(DB_NAME);
		mDictionariesCollection = crossWorld.getCollection(DB_COLLECTION_DICTIONARIES);
	}
}
