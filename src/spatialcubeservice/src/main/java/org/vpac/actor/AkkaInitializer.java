package org.vpac.actor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;

public class AkkaInitializer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Runnable myRunnable = new Runnable() {
          public void run() {
            ActorCreator.createActorCreator();
          }
        };

        new Thread(myRunnable).start();
	}
}
