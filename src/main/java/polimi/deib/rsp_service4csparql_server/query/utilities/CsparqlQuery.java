package polimi.deib.rsp_service4csparql_server.query.utilities;

import java.util.Collection;
import java.util.HashMap;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;

public class CsparqlQuery {

	private String id;
	private String name;
	private String type;
	private Collection<String> streams;
	private String body;
	private CsparqlQueryResultProxy resultProxy;
	private HashMap<String, CsparqlObserver> observers;
	private CsparqlComponentStatus status;

	public CsparqlQuery() {
		super();
	}

	public CsparqlQuery(String id, String name, String type,
			Collection<String> streams, String body,
			CsparqlQueryResultProxy resultProxy,
			HashMap<String, CsparqlObserver> observers, CsparqlComponentStatus status) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.streams = streams;
		this.body = body;
		this.resultProxy = resultProxy;
		this.observers = observers;
		this.status = status;
	}

	public CsparqlQueryResultProxy getResultProxy() {
		return resultProxy;
	}

	public void setResultProxy(CsparqlQueryResultProxy resultProxy) {
		this.resultProxy = resultProxy;
	}

	public HashMap<String, CsparqlObserver> getObservers() {
		return observers;
	}

	public void setObservers(HashMap<String, CsparqlObserver> observers) {
		this.observers = observers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Collection<String> getStreams() {
		return streams;
	}

	public void setStreams(Collection<String> streams) {
		this.streams = streams;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public CsparqlComponentStatus getStatus() {
		return status;
	}

	public void setStatus(CsparqlComponentStatus status) {
		this.status = status;
	}

	public void addObserver(CsparqlObserver cso){
		resultProxy.addObserver(cso.getObserver());
		observers.put(cso.getId(), cso);
	}

	public void removeObserver(String obsID){
		CsparqlObserver cso = observers.get(obsID);
		resultProxy.removeObserver(cso.getObserver());
		observers.remove(obsID);
	}
}
