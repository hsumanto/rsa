package org.vpac.actor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class AkkaInitializer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		ActorCreator.createActorCreator();
	}
}
