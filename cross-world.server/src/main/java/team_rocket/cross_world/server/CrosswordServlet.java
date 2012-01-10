package team_rocket.cross_world.server;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.codehaus.jackson.map.ObjectMapper;

import team_rocket.cross_world.commons.data.Crossword;

/**
 * A servlet responsible for serving crosswords.
 */
public class CrosswordServlet extends HttpServlet
{   
	private static final long serialVersionUID = -8515341091066239507L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
		String[] blankCellsParams = req.getParameterValues("blankCells[]");
		int[] blankCells = new int[blankCellsParams.length];
		for (int i = 0; i < blankCellsParams.length; i++) {
			blankCells[i] = Integer.parseInt(blankCellsParams[i]);
		}
		
		//Stub data. To be replaced with a call to a crossword generating service.
    	Crossword crossworld = new Crossword();
    	crossworld.setCols(3);
    	crossworld.setRows(3);
    	crossworld.setGridNums(new int[] {1, 2, 3, 4, 0, 5, 6, 7, 8});
    	crossworld.setGrid(new char[] {'T', 'O', 'E', 'A', '.', 'A', 'C', 'A', 'R'});
    	
    	crossworld.setAnswersAcross(new String[] {"TOE", "CAR"});
    	crossworld.setAnswersDown(new String[] {"TAC", "EAR"});
    	
    	crossworld.setCluesAcross(new String[] {"1. A finger.", "6. Automobile."});
    	crossworld.setCluesDown(new String[] {"1. Tic ____.", "3. Part of head."});
    	
    	resp.addHeader("Content-Type", "application/json");
    	ObjectMapper mapper = new ObjectMapper();
    	Writer writer = resp.getWriter();
    	writer.write(mapper.writeValueAsString(crossworld));
    	
    	resp.setStatus(200);
    }
}