package team_rocket.cross_world.crossword_generator;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoException;

import team_rocket.cross_world.commons.data.WordDictionary;

import static team_rocket.cross_world.commons.constants.Constants.ALPHABET_SIZE;

public class WordProvider {
	private WordDictionaryCreator dictionaryCreator;
	private Map<Integer, WordDictionary> wordDictinaries;
	private boolean isInitialized;

	public WordProvider(WordDictionaryCreator dictionaryProvider) {
		this.dictionaryCreator = dictionaryProvider;
		this.wordDictinaries = new HashMap<Integer, WordDictionary>();
		this.isInitialized = false;
	}

	public String getWord(int wordLength, int number) throws UnknownHostException, MongoException {
		char[] wordChars = new char[wordLength]; 
		WordDictionary dictionary = getStoredDictionary(wordLength);
		for(int i = 0; i < wordLength; i++) {
			for(int j = 0; j < ALPHABET_SIZE; j++) {
				boolean[] mapping = dictionary.getWordMapping(i, j);
				if(mapping[number]) {
					wordChars[i] = (char)('a' + j);
					break;
				}
			}
		}
		
		return new String(wordChars);
	}

	public boolean[] intersect(boolean[] availableWord, int wordLength,
			int letterIndex, int letter) throws UnknownHostException, MongoException {
		boolean[] words = getStoredDictionary(wordLength).getWordMapping(
				letterIndex, letter);
		if(words.length != availableWord.length) {
			throw new IllegalArgumentException();
		}
		
		for (int i = 0; i < availableWord.length; i++) {
			availableWord[i] = availableWord[i] && words[i];
 		}
		
		return availableWord;
	}

	public int getWordCount(int wordLength) throws UnknownHostException, MongoException {
		WordDictionary dictionary = getStoredDictionary(wordLength);
		if(dictionary == null) {
			return 0;
		}
		return dictionary.getWordMapping(0, 0).length;
	}

	public void initialize() throws UnknownHostException, MongoException {
		if(!isInitialized) {
			wordDictinaries = dictionaryCreator.getDictionaries();
			isInitialized = true;
		}
	}
	
	private WordDictionary getStoredDictionary(int wordLength) throws UnknownHostException, MongoException {
		initialize();
		return wordDictinaries.get(wordLength);
	}
}
