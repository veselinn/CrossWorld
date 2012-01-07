package team_rocket.cross_world.commons.data;

public class Crossword {
	private int cols;
	private int rows;
	private char[] grid;
	private int[] gridNums;
	private String[] cluesAcross;
	private String[] cluesDown;
	private String[] answersAcross;
	private String[] answersDown;
	
	public int getCols() {
		return cols;
	}
	public void setCols(int cols) {
		this.cols = cols;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public char[] getGrid() {
		return grid;
	}
	public void setGrid(char[] grid) {
		this.grid = grid;
	}
	public int[] getGridNums() {
		return gridNums;
	}
	public void setGridNums(int[] gridNums) {
		this.gridNums = gridNums;
	}
	public String[] getCluesAcross() {
		return cluesAcross;
	}
	public void setCluesAcross(String[] cluesAcross) {
		this.cluesAcross = cluesAcross;
	}
	public String[] getCluesDown() {
		return cluesDown;
	}
	public void setCluesDown(String[] cluesDown) {
		this.cluesDown = cluesDown;
	}
	public String[] getAnswersAcross() {
		return answersAcross;
	}
	public void setAnswersAcross(String[] answersAcross) {
		this.answersAcross = answersAcross;
	}
	public String[] getAnswersDown() {
		return answersDown;
	}
	public void setAnswersDown(String[] answersDown) {
		this.answersDown = answersDown;
	}

}
