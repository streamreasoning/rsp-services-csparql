package polimi.deib.rsp_service4csparql_server.stream.utilities;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;

public class CsparqlStreamDescriptionForGet {

	private String streamIRI;
	private CsparqlComponentStatus status;
	
	public CsparqlStreamDescriptionForGet() {
		super();
	}
	public CsparqlStreamDescriptionForGet(String streamIRI, CsparqlComponentStatus status) {
		super();
		this.streamIRI = streamIRI;
		this.status = status;
	}
	
	public String getStream() {
		return streamIRI;
	}
	public void setStream(String streamIRI) {
		this.streamIRI = streamIRI;
	}
	public CsparqlComponentStatus getStatus() {
		return status;
	}
	public void setStatus(CsparqlComponentStatus status) {
		this.status = status;
	}
}
