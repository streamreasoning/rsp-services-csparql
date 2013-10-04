package polimi.deib.rsp_service4csparql_server.stream.utilities;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

public class CsparqlStream{

	private RdfStream stream;
	private CsparqlComponentStatus status;
	
	public CsparqlStream() {
		super();
	}
	public CsparqlStream(RdfStream stream, CsparqlComponentStatus status) {
		super();
		this.stream = stream;
		this.status = status;
	}
	
	public RdfStream getStream() {
		return stream;
	}
	
	public void setStream(RdfStream stream) {
		this.stream = stream;
	}
	
	public CsparqlComponentStatus getStatus() {
		return status;
	}
	
	public void setStatus(CsparqlComponentStatus status) {
		this.status = status;
	}
	
	public void feedStream(RdfQuadruple quadruple){
		stream.put(quadruple);
	}
}
