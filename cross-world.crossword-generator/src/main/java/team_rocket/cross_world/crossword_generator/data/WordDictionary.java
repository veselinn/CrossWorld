package team_rocket.cross_world.crossword_generator.data;

public class WordDictionary {
	private int wordLength;
	/**
	 * A dictionary that stores the information about words with specific length.
	 * The first dimension is for the words length. 
	 * The second dimension is for the letters of English alphabet.
	 * The third is mapping for letter - position and word id.
	 */
	private boolean[][][] dictionary;
	
	public WordDictionary(boolean[][][] dictionary) {
		this.dictionary = dictionary;
		this.wordLength = dictionary.length;
	}
	
	public int getWordLength() {
		return wordLength;
	}
	
	public boolean[] getWordMapping(int position, int letter) {
		return dictionary[position][letter];
	}
}
