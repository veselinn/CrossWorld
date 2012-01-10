package team_rocket.cross_world.server;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class EchoServlet extends HttpServlet
{   
    /**
	 * 
	 */
	private static final long serialVersionUID = -8515341091066239507L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
		System.out.println(req.getRequestURI());
    }
}