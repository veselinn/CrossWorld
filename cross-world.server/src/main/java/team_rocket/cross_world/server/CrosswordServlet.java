package team_rocket.cross_world.server;

import java.io.*;
import java.net.UnknownHostException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.codehaus.jackson.map.ObjectMapper;

import com.mongodb.MongoException;

import team_rocket.cross_world.commons.data.Crossword;
import team_rocket.cross_world.crossword_generator.algorithm.CrossWorldCrosswordGenerator;
import team_rocket.cross_world.crossword_generator.algorithm.CrosswordGenerator;
import team_rocket.cross_world.crossword_generator.util.WordDictionaryCreator;
import team_rocket.cross_world.crossword_generator.util.WordProvider;

/**
 * A servlet responsible for serving crosswords.
 */
public class CrosswordServlet extends HttpServlet
{   
	private static final long serialVersionUID = -8515341091066239507L;
	CrosswordGenerator crosswordGenerator;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		WordProvider wordProvider = new WordProvider(new WordDictionaryCreator());
			try {
				wordProvider.initialize();
			} catch (UnknownHostException e) {
				throw new ServletException("Failed to initialize word provider.");
			} catch (MongoException e) {
				throw new ServletException("Failed to initialize word provider.");
			}
		
		crosswordGenerator = new CrossWorldCrosswordGenerator(wordProvider);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
		String[] blankCellsParams = req.getParameterValues("blankCells[]");
		int[] blankCells = new int[blankCellsParams.length];
		for (int i = 0; i < blankCellsParams.length; i++) {
			blankCells[i] = Integer.parseInt(blankCellsParams[i]);
		}
		
		int rows = Integer.parseInt(req.getParameter("rows"));
		int cols = Integer.parseInt(req.getParameter("cols"));
		Writer writer = resp.getWriter();
		Crossword crossword;
		try {
			crossword = crosswordGenerator.generateCrossword(cols, rows, blankCells);
			if (crossword != null) {
				resp.addHeader("Content-Type", "application/json");
				ObjectMapper mapper = new ObjectMapper();
				writer.write(mapper.writeValueAsString(crossword));				
				resp.setStatus(200);
			} else {
				writer.write("Failed to generate crossword.");
				resp.setStatus(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			writer.write("Error while generating crossword.");
			resp.setStatus(500);
		}    	
    }
}