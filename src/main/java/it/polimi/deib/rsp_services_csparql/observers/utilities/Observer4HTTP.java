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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Observer_Interface;

import eu.larkc.csparql.common.RDFTable;

public class Observer4HTTP implements Continuous_Query_Observer_Interface {

	private final ExecutorService executor;

	protected final String clientAddress;
	protected final String format;

	private HttpClient client;
	private URI uri;
	private boolean sendEmptyResults;

	private Logger logger = LoggerFactory.getLogger(Observer4HTTP.class
			.getName());
	private OutputDataMarshaller outputDataMarshaller;

	public Observer4HTTP(String callbackUrl, String outputFormat) {
		super();
		this.clientAddress = callbackUrl;
		this.format = outputFormat;
		try {
			this.sendEmptyResults = Config.getInstance()
					.getSendEmptyResultsProperty();
		} catch (Exception e) {
			logger.error(
					"Error while loading sendEmptyResults property from configuration",
					e);
			this.sendEmptyResults = true;
		}

		try {

			client = new DefaultHttpClient();
			uri = new URI(this.clientAddress);

		} catch (URISyntaxException e) {
			logger.error("error while creating URI", e);
		}

		executor = Executors.newCachedThreadPool();
	}

	// public void update(final GenericObservable<RDFTable> observed, final
	// RDFTable q) {
	//
	// try {
	//
	// if(sendEmptyResults){
	// if(!q.getJsonSerialization().isEmpty()){
	// method.setEntity(new StringEntity(q.getJsonSerialization()));
	//
	// httpResponse = client.execute(method);
	// httpEntity = httpResponse.getEntity();
	//
	// httpParams = client.getParams();
	// HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
	//
	// EntityUtils.consume(httpEntity);
	// }
	// } else {
	// method.setEntity(new StringEntity(q.getJsonSerialization()));
	//
	// httpResponse = client.execute(method);
	// httpEntity = httpResponse.getEntity();
	//
	// httpParams = client.getParams();
	// HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
	//
	// EntityUtils.consume(httpEntity);
	// }
	//
	// } catch(org.apache.http.conn.HttpHostConnectException e){
	// logger.error("Connection to {} refused", clientAddress);
	// } catch (UnsupportedEncodingException e) {
	// logger.error("error while encoding", e);
	// } catch (ClientProtocolException e) {
	// logger.error("error while calling rest service", e);
	// } catch (IOException e) {
	// logger.error("error during IO operation", e);
	// } finally {
	// method.releaseConnection();
	// }
	// }

	@Override
	public void update(Observable o, Object arg) {

		RDFTable q = (RDFTable) arg;

		HttpPost method = null;
		try {

			if (sendEmptyResults || !q.isEmpty()) {
				String serialization = getOutputDataMarshaller().marshal(q,
						format);
				if (serialization != null) {
					logger.debug("Sending {} bytes to {}",
							serialization.length(), clientAddress);
					method = new HttpPost(uri);
					method.setHeader("Cache-Control", "no-cache");
					method.setEntity(new StringEntity(serialization));

					executor.execute(new AsyncRequest(client, method));
				} else {
					logger.error("Serialization returned null, data won't be sent");
				}
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("error while encoding", e);
		} finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private OutputDataMarshaller getOutputDataMarshaller() {
		if (outputDataMarshaller == null) {
			try {
				outputDataMarshaller = OutputDataMarshaller.DEFAULT_OUTPUT_DATA_MARSHALLER_IMPL
						.newInstance();
			} catch (Exception e) { // this should not happen
				throw new RuntimeException(e);
			}
			String className = System
					.getProperty(OutputDataMarshaller.OUTPUT_DATA_MARSHALLER_IMPL_PROPERTY_NAME);
			if (className != null) {
				try {
					outputDataMarshaller = (OutputDataMarshaller) getClass()
							.getClassLoader().loadClass(className)
							.newInstance();
				} catch (Exception e) {
					logger.error(
							"Provided OutputDataMarshaller implementation {} raised an exception "
									+ "while trying to load the class, the default one will be used",
							className, e);
				}
			}
			logger.debug("Using {} as OutputDataMarshaller implementation",
					outputDataMarshaller.getClass().getName());
		}
		return outputDataMarshaller;
	}

	public class AsyncRequest implements Runnable {

		private HttpClient client;
		private HttpPost method;

		public AsyncRequest(HttpClient client, HttpPost method) {
			this.client = client;
			this.method = method;
		}

		@Override
		public void run() {
			try {
				HttpResponse httpResponse = client.execute(method);
				HttpEntity httpEntity = httpResponse.getEntity();

				EntityUtils.consume(httpEntity);

			} catch (HttpHostConnectException e) {
				logger.error("Connection to {} refused", clientAddress);
			} catch (UnsupportedEncodingException e) {
				logger.error("error while encoding", e);
			} catch (ClientProtocolException e) {
				logger.error("error while calling rest service", e);
			} catch (IOException e) {
				logger.error("error during IO operation", e);
			} finally {
				method.releaseConnection();
			}
		}

	}

}
