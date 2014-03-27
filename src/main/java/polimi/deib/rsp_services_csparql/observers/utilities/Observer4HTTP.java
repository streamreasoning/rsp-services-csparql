/*******************************************************************************
 * Copyright 2013 Marco Balduini, Emanuele Della Valle
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
 * Acknowledgements:
 *   
 * This work was partially supported by the European project LarKC (FP7-215535) 
 * and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package polimi.deib.rsp_services_csparql.observers.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Observer_Interface;

import eu.larkc.csparql.common.RDFTable;

public class Observer4HTTP implements Continuous_Query_Observer_Interface{

	private String clientAddress;

	private HttpClient client = null;
	private HttpPost method = null;
	private URI uri;
	private HttpResponse httpResponse;
	private HttpEntity httpEntity;
	private boolean sendEmptyResults;

	private Logger logger = LoggerFactory.getLogger(Observer4HTTP.class.getName());

	public Observer4HTTP(String clientAddress, boolean sendEmptyResults) {
		super();
		this.clientAddress = clientAddress;
		this.sendEmptyResults = sendEmptyResults;

		try {

			client = new DefaultHttpClient();
			uri = new URI(this.clientAddress);
			method = new HttpPost(uri);
			method.setHeader("Cache-Control","no-cache");

		} catch (URISyntaxException e) {
			logger.error("error while creating URI", e);
		}

	}

//	public void update(final GenericObservable<RDFTable> observed, final RDFTable q) {
//
//		try {
//
//			if(sendEmptyResults){
//				if(!q.getJsonSerialization().isEmpty()){
//					method.setEntity(new StringEntity(q.getJsonSerialization()));
//
//					httpResponse = client.execute(method);
//					httpEntity = httpResponse.getEntity();
//
//					httpParams = client.getParams();
//					HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
//
//					EntityUtils.consume(httpEntity);
//				}
//			} else {
//				method.setEntity(new StringEntity(q.getJsonSerialization()));
//
//				httpResponse = client.execute(method);
//				httpEntity = httpResponse.getEntity();
//
//				httpParams = client.getParams();
//				HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
//
//				EntityUtils.consume(httpEntity);
//			}
//
//		} catch(org.apache.http.conn.HttpHostConnectException e){
//			logger.error("Connection to {} refused", clientAddress);
//		} catch (UnsupportedEncodingException e) {
//			logger.error("error while encoding", e);
//		} catch (ClientProtocolException e) {
//			logger.error("error while calling rest service", e);
//		} catch (IOException e) {
//			logger.error("error during IO operation", e);
//		} finally {
//			method.releaseConnection();
//		}
//	}

	@Override
	public void update(Observable o, Object arg) {

		RDFTable q = (RDFTable) arg;	

		try {

			if(sendEmptyResults){
								
				if(!q.getJsonSerialization().isEmpty()){
					
					method.setEntity(new StringEntity(q.getJsonSerialization()));

					httpResponse = client.execute(method);
					httpEntity = httpResponse.getEntity();

					EntityUtils.consume(httpEntity);
				}
			} else {
								
				method.setEntity(new StringEntity(q.getJsonSerialization()));

				httpResponse = client.execute(method);
				httpEntity = httpResponse.getEntity();

				EntityUtils.consume(httpEntity);
			}

		} catch(org.apache.http.conn.HttpHostConnectException e){
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
