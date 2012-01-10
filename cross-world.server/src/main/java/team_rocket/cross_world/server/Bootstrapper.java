package team_rocket.cross_world.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * An entry point for the CrossWorld.
 *
 */
public class Bootstrapper 
{
    public static void main( String[] args ) throws Exception
    {    	
		Server server = new Server(8080);
			 
		WebAppContext context = new WebAppContext();
		context.setDescriptor("WEB-INF/web.xml");
		context.setResourceBase(".");
		context.setContextPath("/");
		context.setParentLoaderPriority(true);
		
		server.setHandler(context);
		
		server.start();
		server.join();
    }
}
