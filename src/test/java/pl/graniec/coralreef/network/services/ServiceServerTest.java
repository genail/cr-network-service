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

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.services.packets.ServiceJoinPacket;
import pl.graniec.coralreef.network.services.packets.ServiceJoinResponsePacket;
import pl.graniec.coralreef.network.services.packets.ServiceListingPacket;
import pl.graniec.coralreef.network.services.packets.ServiceListingRequestPacket;
import pl.graniec.coralreef.network.stream.client.StreamClient;
import pl.graniec.coralreef.network.stream.server.StreamServer;
import junit.framework.TestCase;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class ServiceServerTest extends TestCase {

	ServiceServer serviceServer;
	boolean gotPacket;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		serviceServer = new ServiceServer(new StreamServer());
		serviceServer.open(0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/** No services */
	public void testServiceListing1() throws NetworkException, NotSerializableException, InterruptedException {
		StreamClient client = new StreamClient();
		
		client.addPacketListener(new PacketListener(){
			public void packetReceived(Object data) {
				assertTrue(data instanceof ServiceListingPacket);
				assertEquals(0, ((ServiceListingPacket)data).getServices().length);
				gotPacket = true;
			}
		});
		
		client.connect("127.0.0.1", serviceServer.getPort());
		
		client.send(new ServiceListingRequestPacket());
		
		Thread.sleep(50);
		
		assertTrue(gotPacket);
	}
	
	/** One service */
	public void testServiceListing2() throws NetworkException, NotSerializableException, InterruptedException {
		
		serviceServer.newService(10);
		
		StreamClient client = new StreamClient();
		
		client.addPacketListener(new PacketListener(){
			public void packetReceived(Object data) {
				assertTrue(data instanceof ServiceListingPacket);
				assertEquals(1, ((ServiceListingPacket)data).getServices().length);
				assertEquals(10, ((ServiceListingPacket)data).getServices()[0]);
				gotPacket = true;
			}
		});
		
		client.connect("127.0.0.1", serviceServer.getPort());
		
		client.send(new ServiceListingRequestPacket());
		
		Thread.sleep(50);
		
		assertTrue(gotPacket);
	}
	
	/** One service, proper join */
	public void testServiceJoining1() throws NetworkException, NotSerializableException, InterruptedException {
		
		serviceServer.newService(10);
		
		StreamClient client = new StreamClient();
		
		client.addPacketListener(new PacketListener(){
			public void packetReceived(Object data) {
				assertTrue(data instanceof ServiceJoinResponsePacket);
				assertEquals(1, ((ServiceJoinResponsePacket)data).getServicesJoined().length);
				assertEquals(10, ((ServiceJoinResponsePacket)data).getServicesJoined()[0]);
				gotPacket = true;
			}
		});
		
		client.connect("127.0.0.1", serviceServer.getPort());
		
		client.send(new ServiceJoinPacket(new int[] {10}));
		client.send(new ServiceJoinPacket(new int[] {10}));
		
		Thread.sleep(50);
		
		assertTrue(gotPacket);
	}

}
