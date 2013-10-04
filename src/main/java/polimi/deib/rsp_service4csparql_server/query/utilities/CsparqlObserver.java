package polimi.deib.rsp_service4csparql_server.query.utilities;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.streams.format.GenericObserver;

public class CsparqlObserver {
	
	private String id;
	private GenericObserver<RDFTable> observer;
	
	public CsparqlObserver(String id, GenericObserver<RDFTable> observer) {
		super();
		this.id = id;
		this.observer = observer;
	}
	
	public CsparqlObserver() {
		super();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public GenericObserver<RDFTable> getObserver() {
		return observer;
	}
	
	public void setObserver(GenericObserver<RDFTable> observer) {
		this.observer = observer;
	}
	
	

}
