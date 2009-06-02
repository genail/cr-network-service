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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import pl.graniec.coralreef.network.DisconnectReason;
import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.ConnectionListener;
import pl.graniec.coralreef.network.server.RemoteClient;
import pl.graniec.coralreef.network.server.Server;
import pl.graniec.coralreef.network.services.packets.ServiceDataPacket;
import pl.graniec.coralreef.network.services.packets.ServiceJoinPacket;
import pl.graniec.coralreef.network.services.packets.ServiceJoinResponsePacket;
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
	
	private static class ClientHandler {
		/** Instance to RemoteClient object */
		RemoteClient remoteClient;
		/** Services that this client has joined */
		private Map/*<Integer, Service>*/ services = new HashMap();
		
		public ClientHandler(RemoteClient remoteClient) {
			super();
			this.remoteClient = remoteClient;
		}
		
	}
	
	private static Logger logger = Logger.getLogger(ServiceServer.class.getName());

	/** Server implementation */
	private final Server serverImpl;
	/** Map of available services */
	private Map/*<Integer, Service>*/ services = new HashMap();
	/** Map of client handlers */
	private final Map/*<RemoteClient, ClientHandler>*/ clients = new HashMap();
	
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
	
	public void close() {
		serverImpl.close();
	}
	
	public int getPort() {
		return serverImpl.getPort();
	}
	
	private synchronized void handleClientConnected(final RemoteClient client) {
		clients.put(client, new ClientHandler(client));
		
		client.addPacketListener(new PacketListener() {
			public void packetReceived(Object data) {
				handlePacketReceived(client, data);
			}
		});
	}
	
	/** When client disconnects */
	private synchronized void handleClientDisconnected(RemoteClient client, int reason, String reasonString) {
		
		// let the all services know about disconnection
		final ClientHandler handler = (ClientHandler) clients.get(client);
		
		if (handler == null) {
			logger.severe("got message from client " + client + " but no handler found; this is possible a bug!");
			return;
		}
		
		Service service;
		for (final Iterator itor = handler.services.values().iterator(); itor.hasNext();) {
			service = (Service) itor.next();
			service.notifyClientDisconnected(client, reason, reasonString);
		}
		
		clients.remove(client);
	}
	
	private void handlePacketReceived(RemoteClient sender, Object data) {
		if (!(data instanceof ServicePacket)) {
			return;
		}
		
		if (data instanceof ServiceDataPacket) {
			handleServiceDataPacet(sender, (ServiceDataPacket) data);
		}
		else if (data instanceof ServiceListingRequestPacket) {
			handleServiceListingRequestPacket(sender, (ServiceListingRequestPacket) data);
		}
		else if (data instanceof ServiceJoinPacket) {
			handleServiceJoinPacket(sender, (ServiceJoinPacket) data);
		}
	}

	/**
	 * @param sender
	 * @param packet
	 */
	private synchronized void handleServiceDataPacet(RemoteClient sender, ServiceDataPacket packet) {
		// sender must belong to this service
		final Integer serviceId = Integer.valueOf(packet.getServiceId());
		final ClientHandler clientHandler = (ClientHandler) clients.get(sender);
		
		if (!clientHandler.services.containsKey(serviceId)) {
			logger.warning("got packet from " + sender + " to service " + serviceId + ", but he doesn't belong to that service");
			return;
		}
		
		// just put the packet throu
		final Service targetService = (Service) clientHandler.services.get(serviceId);
		targetService.notifyPacketReceived(sender, packet.getData());
	}

	/**
	 * @param sender
	 * @param packet
	 */
	private synchronized void handleServiceJoinPacket(RemoteClient sender, ServiceJoinPacket packet) {
		
		try {
			final ClientHandler handler = (ClientHandler) clients.get(sender);
			
			if (handler == null) {
				logger.severe("got message from client " + sender + " but no handler found; this is possible a bug!");
				return;
			}
			
			// join the service
			
			final int[] services = packet.getServices();
			final List/*<Integer>*/ servicesJoined = new LinkedList();
			
			Integer serviceId;
			Service service;
			
			for (int i = 0; i < services.length; ++i) {
				
				serviceId = Integer.valueOf(services[i]);
				
				if (!this.services.containsKey(serviceId)) {
					continue;
				}
				
				if (!handler.services.containsKey(serviceId)) {
					service = (Service) this.services.get(serviceId);
					service.notifyClientConnected(sender);
					
					handler.services.put(serviceId, service);
				}
				
				servicesJoined.add(serviceId);
			}
			
			// send a feedback
			final int[] servicesJoinedInt = new int[servicesJoined.size()];
			
			int counter = 0;
			for (final Iterator itor = servicesJoined.iterator(); itor.hasNext();) {
				servicesJoinedInt[counter++] = ((Integer)itor.next()).intValue();
			}
		
			sender.send(new ServiceJoinResponsePacket(servicesJoinedInt));
		} catch (NotSerializableException e) {
			// impossible
		} catch (NetworkException e) {
			// do nothing
		}
	}
	
	/**
	 * Tells if <code>client</code> is currently connected to service
	 * <code>serviceId</code> or not.
	 * <p>
	 * When client is not found in clients lists then it's treated
	 * like not connected.
	 * 
	 * @param client ServiceServer RemoteClient instance.
	 * @param serviceId Id of service.
	 * 
	 * @return <code>true</code> if client is connected to specified service.
	 */
	synchronized boolean isClientConnected(RemoteClient client, int serviceId) {
		final ClientHandler handler = (ClientHandler) clients.get(client);
		
		if (handler == null) {
			return false;
		}
		
		return handler.services.containsKey(Integer.valueOf(serviceId));
	}
	
	synchronized void disconnectClientFromService(RemoteClient client, int serviceId) {
		final ClientHandler handler = (ClientHandler) clients.get(client);
		
		if (handler == null) {
			return;
		}
		
		final Service service = (Service) handler.services.get(Integer.valueOf(serviceId));
		
		if (service == null) {
			return;
		}
		
		handler.services.remove(Integer.valueOf(serviceId));
		service.notifyClientDisconnected(client, DisconnectReason.UserAction, "user action");
		
		// FIXME: send disconnection information to ServiceClient
	}
	
	/**
	 * Sends back the service listing information.
	 * 
	 * @param data
	 */
	private synchronized void handleServiceListingRequestPacket(RemoteClient sender, ServiceListingRequestPacket data) {
		
		try {
			
			int[] ids;
			
			ids = new int[services.size()];
			
			Service service;
			int counter = 0;
			for (Iterator itor = services.values().iterator(); itor.hasNext(); ) {
				service = (Service) itor.next();
				ids[counter++] = service.getId();
			}
			
			ServicePacket packet = new ServiceListingPacket(ids);
			
			sender.send(packet);
			
		} catch (NotSerializableException e) {
			// not possible
		} catch (NetworkException e) {
			// ignore
		}
	}

	/**
	 * Creates a new service that is bound to this server.
	 * <p>
	 * A service should be used as regular {@link Server} implementation for
	 * Coral Reef network applications.
	 * 
	 * @return New service (a Server implementation)
	 */
	public synchronized Service newService(int id) {
		final Service service = new Service(this, id);
		
		services.put(Integer.valueOf(id), service);
		
		return service;
	}
	
	public void open(int port) throws NetworkException {
		serverImpl.open(port);
	}
	
	
	
}
