package team_rocket.cross_world.word_extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;
import com.mongodb.util.JSON;

/**
 * A simple script-like tool for extracting words and clues from the crosswords of NY Times. 
 * The crossword puzzles are in the JSON format used by www.rosswords.info.
 */
public class WordExtractor 
{
	static DBCollection wordsCollection;
	
    public static void main( String[] args ) throws IOException {
    	initMongo();
    	
        File crosswordsDir = new File("../resources/crosswords/nytimes");
        File[] crosswordFiles = crosswordsDir.listFiles();
        
		for (int i = 0; i < crosswordFiles.length; i++) {
			DBObject crossword = (DBObject)JSON.parse(readFile(crosswordFiles[i]));
			DBObject[] words = getWords(crossword);
			for (int j = 0; j < words.length; j++) {
				DBObject modifier = new BasicDBObject("$addToSet", 
						new BasicDBObject("clues", words[j].get("clue")));
				DBObject query = new BasicDBObject("word", words[j].get("word"));
				wordsCollection.update(query, modifier, true, false);
			}
		}
		
		System.out.println("Finished extracting words.");
    }
    
    private static String readFile(File crossword) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(crossword));
		StringBuilder stringFromFile = new StringBuilder();
		String line = "";
		while ((line = reader.readLine()) != null) {
			stringFromFile.append(line);
		}
		
		return stringFromFile.toString();
    }
    
    private static DBObject[] getWords(DBObject crossword) {
    	List<DBObject> words = new ArrayList<DBObject>();
    	BasicDBList answers = new BasicDBList();
    	BasicDBList clues = new BasicDBList();
    	
    	answers.addAll((BasicDBList)((DBObject)crossword.get("answers")).get("across"));
    	answers.addAll((BasicDBList)((DBObject)crossword.get("answers")).get("down"));
    	
    	clues.addAll((BasicDBList)((DBObject)crossword.get("clues")).get("across"));
    	clues.addAll((BasicDBList)((DBObject)crossword.get("clues")).get("down"));
    	
    	for (int i = 0; i < answers.size(); i++) {
    		String answer = ((String)answers.get(i)).toLowerCase();
    		String clue = (String)clues.get(i);
    		clue = clue.replaceFirst("^[0-9]*\\.\\s*", "");
    		if (answer.matches("[a-zA-Z]*")) {
    			BasicDBObject word = new BasicDBObject("word", answer);
    			word.append("clue", clue);
    			words.add(word);    			
    		}
		}
    	
    	DBObject[] wordsArray = new DBObject[words.size()];
    	words.toArray(wordsArray);
    	return wordsArray;
    }
    
    private static void initMongo() throws UnknownHostException, MongoException {
		Mongo mongo = new Mongo();
		wordsCollection = mongo.getDB("CrossWorld").getCollection("words");
		wordsCollection.createIndex(new BasicDBObject("word", 1));
    }
}