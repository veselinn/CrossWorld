package team_rocket.cross_world.server;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.codehaus.jackson.map.ObjectMapper;

import team_rocket.cross_world.commons.data.Crossword;
import team_rocket.cross_world.crossword_generator.CrossWorldCrosswordGenerator;
import team_rocket.cross_world.crossword_generator.CrosswordGenerator;
import team_rocket.cross_world.crossword_generator.WordDictionaryCreator;
import team_rocket.cross_world.crossword_generator.WordProvider;

/**
 * A servlet responsible for serving crosswords.
 */
public class CrosswordServlet extends HttpServlet
{   
	private static final long serialVersionUID = -8515341091066239507L;
	CrosswordGenerator crosswordGenerator;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		crosswordGenerator = new CrossWorldCrosswordGenerator(
				new WordProvider(new WordDictionaryCreator()));
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
		Crossword crossworld;
		try {
			crossworld = crosswordGenerator.generateCrossword(cols, rows, blankCells);
			resp.addHeader("Content-Type", "application/json");
			ObjectMapper mapper = new ObjectMapper();
			Writer writer = resp.getWriter();
			writer.write(mapper.writeValueAsString(crossworld));
			
			resp.setStatus(200);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(500);
		}    	
    }
}