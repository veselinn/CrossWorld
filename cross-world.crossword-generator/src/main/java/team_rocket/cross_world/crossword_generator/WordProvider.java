package team_rocket.cross_world.crossword_generator;

import java.util.Map;

import team_rocket.word_dictionary.WordDictionary;

public class WordProvider {

	private WordDictionaryProvider dictionaryProvider;
	private Map<Integer, WordDictionary> wordDictinaries;

	private static int ALPHABET_SIZE = 26;
	
	public WordProvider(WordDictionaryProvider dictionaryProvider) {
		this.dictionaryProvider = dictionaryProvider;
	}

	public String getWord(int wordLength, int number) {
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

	public void intersect(boolean[] availableWord, int wordLength,
			int letterIndex, int letter) {
		boolean[] words = getStoredDictionary(wordLength).getWordMapping(
				letterIndex, letter);
		if(words.length != availableWord.length) {
			throw new IllegalArgumentException();
		}
		
		for (int i = 0; i < availableWord.length; i++) {
			availableWord[i] = availableWord[i] && words[i];
 		}
	}

	public int getWordCount(int wordLength) {
		return getStoredDictionary(wordLength).getWordMapping(0, 0).length;
	}

	private WordDictionary getStoredDictionary(int wordLength) {
		WordDictionary dictionary = wordDictinaries.get(wordLength);
		if (dictionary == null) {
			dictionary = dictionaryProvider.getWordDictionary(wordLength);
			wordDictinaries.put(wordLength, dictionary);
		}
		return dictionary;
	}
}
