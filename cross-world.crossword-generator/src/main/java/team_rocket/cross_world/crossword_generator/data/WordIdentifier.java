package team_rocket.cross_world.crossword_generator.data;

public class WordIdentifier {
	private int wordNumber;
	private boolean isAcross;
	
	public WordIdentifier(int wordNumber, boolean isAcross) {
		this.wordNumber = wordNumber;
		this.isAcross = isAcross;
	}
	
	public int getWordNumber() {
		return wordNumber;
	}

	public boolean isAcross() {
		return isAcross;
	}

	@Override
	public int hashCode() {
		if (isAcross) {
			return wordNumber;
		}
		return -wordNumber;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordIdentifier other = (WordIdentifier) obj;
		if (isAcross != other.isAcross)
			return false;
		if (wordNumber != other.wordNumber)
			return false;
		return true;
	}
	
}
