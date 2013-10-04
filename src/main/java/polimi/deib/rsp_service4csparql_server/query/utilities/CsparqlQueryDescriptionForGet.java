package polimi.deib.rsp_service4csparql_server.query.utilities;

import java.util.Collection;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;

public class CsparqlQueryDescriptionForGet {
	
	private String id;
	private String name;
	private String type;
	private Collection<String> streams;
	private String body;
	private CsparqlComponentStatus status;
	
	public CsparqlQueryDescriptionForGet() {
		super();
	}

	public CsparqlQueryDescriptionForGet(String id, String name, String type,
			Collection<String> streams, String body,CsparqlComponentStatus status) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.streams = streams;
		this.body = body;
		this.status = status;
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
}
