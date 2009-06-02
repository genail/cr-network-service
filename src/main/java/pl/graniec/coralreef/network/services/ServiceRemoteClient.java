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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.RemoteClient;
import pl.graniec.coralreef.network.services.packets.ServiceDataPacket;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class ServiceRemoteClient implements RemoteClient {

	/** ServiceServer that this client is running on */
	private final ServiceServer serviceServer;
	/** Service id that this client belongs to */
	private final int serviceId;
	/** Orginal RemoteClient */
	private final RemoteClient remoteClient;
	
	public ServiceRemoteClient(ServiceServer serviceServer, int serviceId, RemoteClient remoteClient) {
		super();
		this.serviceServer = serviceServer;
		this.serviceId = serviceId;
		this.remoteClient = remoteClient;
	}

	/** Packet listeners for this service */
	private final Set/*<PacketListener>*/ packetListeners = new HashSet();
	
	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.RemoteClient#addPacketListener(pl.graniec.coralreef.network.PacketListener)
	 */
	public boolean addPacketListener(PacketListener l) {
		synchronized (packetListeners) {
			return packetListeners.add(l);
		}
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.RemoteClient#disconnect()
	 */
	public void disconnect() {
		
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.RemoteClient#isConnected()
	 */
	public boolean isConnected() {
		return serviceServer.isClientConnected(remoteClient, serviceId);
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.RemoteClient#removePacketListener(pl.graniec.coralreef.network.PacketListener)
	 */
	public boolean removePacketListener(PacketListener l) {
		synchronized (packetListeners) {
			return packetListeners.remove(l);
		}
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.RemoteClient#send(java.lang.Object)
	 */
	public void send(Object data) throws NotSerializableException, NetworkException {
		remoteClient.send(new ServiceDataPacket(serviceId, data));
	}
	
	void notifyPacketReceived(Object data) {
		synchronized (packetListeners) {
			for (final Iterator itor = packetListeners.iterator(); itor.hasNext();) {
				((PacketListener)itor.next()).packetReceived(data);
			}
		}
	}

}
