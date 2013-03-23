package org.network.stcp.server;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 10:54 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ConnectionHandler  {

	void handle(byte [] data);
}
