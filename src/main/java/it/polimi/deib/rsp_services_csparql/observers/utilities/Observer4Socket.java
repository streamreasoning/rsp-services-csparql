/*******************************************************************************
 * Copyright 2014 DEIB - Politecnico di Milano
 *  
 * Marco Balduini (marco.balduini@polimi.it)
 * Emanuele Della Valle (emanuele.dellavalle@polimi.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work was partially supported by the European project LarKC (FP7-215535) and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package it.polimi.deib.rsp_services_csparql.observers.utilities;

import it.polimi.deib.rsp_services_csparql.configuration.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Observer_Interface;

import eu.larkc.csparql.common.RDFTable;

public class Observer4Socket implements Continuous_Query_Observer_Interface{
	
	private final ExecutorService executor;
	
	protected final String observerHost;
	protected final int observerPort;
	private final String protocol;
	protected final String format;
	
	private boolean sendEmptyResults;

	private Logger logger = LoggerFactory.getLogger(Observer4Socket.class.getName());
	private OutputDataMarshaller outputDataMarshaller;

	public Observer4Socket(String observerHost, int observerPort, String protocol, String format) {
		super();
		this.observerHost = observerHost;
		this.observerPort = observerPort;
		this.protocol = protocol;
		this.format = format;
		try {
			this.sendEmptyResults = Config.getInstance().getSendEmptyResultsProperty();
		} catch (Exception e) {
			logger.error("Error while loading sendEmptyResults property from configuration", e);
			this.sendEmptyResults = true;
		}
		executor = Executors.newCachedThreadPool();
	}

	protected Socket createSocket() throws UnknownHostException, IOException {
		return new Socket(observerHost, observerPort);
	}

	@Override
	public void update(Observable o, Object arg) {

		RDFTable q = (RDFTable) arg;	

		if (sendEmptyResults || !q.isEmpty()) {
			String serialization = getOutputDataMarshaller().marshal(q, format);
			executor.execute(new AsyncRequest(serialization));
		}

	}
	
	private OutputDataMarshaller getOutputDataMarshaller() {
		if (outputDataMarshaller  == null) {
			try {
				outputDataMarshaller = OutputDataMarshaller.DEFAULT_OUTPUT_DATA_MARSHALLER_IMPL.newInstance();
			} catch (Exception e) { // this should not happen
				throw new RuntimeException(e);
			}
			String className = System.getProperty(OutputDataMarshaller.OUTPUT_DATA_MARSHALLER_IMPL_PROPERTY_NAME);
			if (className != null){
				try {
					outputDataMarshaller = (OutputDataMarshaller) getClass()
							.getClassLoader().loadClass(className).newInstance();
				}
				catch (Exception e) {
					logger.error("Provided OutputDataMarshaller implementation {} raised an exception "
							+ "while trying to load the class, the default one will be used", className, e);
				}
			}
			logger.debug("Using {} as OutputDataMarshaller implementation", outputDataMarshaller.getClass().getName());
		}
		return outputDataMarshaller;
	}
	
	public class AsyncRequest implements Runnable {


		private String data;

		public AsyncRequest(String data) {
			this.data = data;
		}

		@Override
		public void run() {

			switch (protocol.toUpperCase()) {
			case "TCP":
				sendTCP();
				break;
			case "UDP":
				sendUDP();
				break;

			default:
				logger.warn("Protocol {} unkwown, using TCP", protocol);
				sendTCP();
				break;
			}
		}

		private void sendUDP() {
			DatagramSocket clientSocket = null;
			try {
				logger.debug("Sending {} bytes to {} via UDP", data.length(), observerHost);
				clientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(observerHost);
				byte[] sendData = new byte[1024];
				sendData = data.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, observerPort);
				clientSocket.send(sendPacket);
			} catch (Exception e) {
				logger.error("Error while sending data to observer: {}",
						e.getMessage());
			} finally {
				if (clientSocket != null)
					clientSocket.close();
			}
		}

		private void sendTCP() {
			Socket clientSocket = null;
			try {
				logger.debug("Sending {} bytes to {} via TCP", data.length(), observerHost);
				InetAddress IPAddress = InetAddress.getByName(observerHost);
				clientSocket = new Socket(IPAddress,observerPort);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes(data);
			} catch (Exception e) {
				logger.error("Error while sending data to observer: {}",
						e.getMessage());
			} finally {
				if (clientSocket != null){
					try {
						clientSocket.close();
					} catch (IOException e) {
						logger.error("Error while trying to close the connection", e);
					}
				}
			}
		}

	}
	
}
