package polimi.deib.rsp_service4csparql_server.observer.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.streams.format.GenericObservable;
import eu.larkc.csparql.common.streams.format.GenericObserver;

public class Observer4HTTP implements GenericObserver<RDFTable>{

	private String clientAddress;

	private DefaultHttpClient client = null;
	private HttpPost method = null;
	private URI uri;
	private HttpResponse httpResponse;
	private HttpEntity httpEntity;
	private HttpParams httpParams;
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

	public void update(final GenericObservable<RDFTable> observed, final RDFTable q) {

		try {

			if(sendEmptyResults){
				if(!q.getJsonSerialization().isEmpty()){
					method.setEntity(new StringEntity(q.getJsonSerialization()));

					httpResponse = client.execute(method);
					httpEntity = httpResponse.getEntity();

					httpParams = client.getParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

					EntityUtils.consume(httpEntity);
				}
			} else {
				method.setEntity(new StringEntity(q.getJsonSerialization()));
				
				httpResponse = client.execute(method);
				httpEntity = httpResponse.getEntity();

				httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

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
