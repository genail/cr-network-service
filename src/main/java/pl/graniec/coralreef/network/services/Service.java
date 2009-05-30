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

import java.util.HashSet;
import java.util.Set;

import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.ConnectionListener;
import pl.graniec.coralreef.network.server.Server;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class Service implements Server {

	/** Parent server */
	private final ServiceServer parent;
	
	/** Service identification number */
	private final int id;
	/** Connection listeners */
	private final Set connectionListeners = new HashSet();
	
	Service(ServiceServer parent, int id) {
		this.parent = parent;
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#addConnectionListener(pl.graniec.coralreef.network.server.ConnectionListener)
	 */
	public boolean addConnectionListener(ConnectionListener l) {
		return connectionListeners.add(l);
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#close()
	 */
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#getPort()
	 */
	public int getPort() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#isOpen()
	 */
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#open(int)
	 */
	public void open(int port) throws NetworkException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pl.graniec.coralreef.network.server.Server#removeConnectionListener(pl.graniec.coralreef.network.server.ConnectionListener)
	 */
	public boolean removeConnectionListener(ConnectionListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public int getId() {
		return id;
	}

}
