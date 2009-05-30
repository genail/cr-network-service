/**
 * Copyright (c) 2009, Coral Reef Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of the Coral Reef Project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pl.graniec.coralreef.network.services;

import java.io.NotSerializableException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.ConnectionListener;
import pl.graniec.coralreef.network.server.RemoteClient;
import pl.graniec.coralreef.network.server.Server;
import pl.graniec.coralreef.network.services.packets.ServiceListingPacket;
import pl.graniec.coralreef.network.services.packets.ServiceListingRequestPacket;
import pl.graniec.coralreef.network.services.packets.ServicePacket;

/**
 * The ServiceServer class can provide multiple CoralReef
 * servers as one. If there is any server that implements
 * the cr-network interface then it can be converted to a
 * service.
 * <p>
 * In CoralReef every client-server library that implements
 * from cr-network is very flexible. The only one condition
 * is to stick to the following rule, that 
 * 
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class ServiceServer {

	/** Server implementation */
	private final Server serverImpl;
	/** List of available services */
	private List services = new LinkedList();
	
	/**
	 * Creates a service server that will run on specified
	 * <code>serverImplementation</code>.
	 */
	public ServiceServer(Server serverImplementation) {
			serverImpl = serverImplementation;
			
			serverImpl.addConnectionListener(new ConnectionListener() {
				public void clientConnected(RemoteClient client) {
					handleClientConnected(client);
				}
				
				public void clientDisconnected(RemoteClient client, int reason, String reasonString) {
					handleClientDisconnected(client, reason, reasonString);
				}
			});
	}
	
	private void handleClientConnected(final RemoteClient client) {
		client.addPacketListener(new PacketListener() {
			public void packetReceived(Object data) {
				handlePacketReceived(client, data);
			}
		});
	}
	
	private void handlePacketReceived(RemoteClient sender, Object data) {
		if (!(data instanceof ServicePacket)) {
			return;
		}
		
		if (data instanceof ServiceListingRequestPacket) {
			handleServiceListingRequestPacket(sender, (ServiceListingRequestPacket) data);
		}
	}
	
	/**
	 * Sends back the service listing information.
	 * 
	 * @param data
	 */
	private void handleServiceListingRequestPacket(RemoteClient sender, ServiceListingRequestPacket data) {
		
		try {
			
			int[] ids;
			
			synchronized (services) {
				ids = new int[services.size()];
				
				Service service;
				int counter = 0;
				for (Iterator itor = services.iterator(); itor.hasNext(); ) {
					service = (Service) itor.next();
					ids[counter++] = service.getId();
				}
			}
			
			ServicePacket packet = new ServiceListingPacket(ids);
		
			sender.send(packet);
			
		} catch (NotSerializableException e) {
			// not possible
		} catch (NetworkException e) {
			// ignore
		}
	}

	private void handleClientDisconnected(RemoteClient client, int reason, String reasonString) {
		
	}
	
	/**
	 * Creates a new service that is bound to this server.
	 * <p>
	 * A service should be used as regular {@link Server} implementation for
	 * Coral Reef network applications.
	 * 
	 * @return New service (a Server implementation)
	 */
	public Service newService(int id) {
		final Service service = new Service(this, id);
		
		synchronized (services) {
			services.add(service);
		}
		
		return service;
	}
	
	
	
}
