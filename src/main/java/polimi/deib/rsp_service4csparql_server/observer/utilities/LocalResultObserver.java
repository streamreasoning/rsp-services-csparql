package polimi.deib.rsp_service4csparql_server.observer.utilities;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.streams.format.GenericObservable;
import eu.larkc.csparql.common.streams.format.GenericObserver;

public class LocalResultObserver implements GenericObserver<RDFTable>{

	public void update(final GenericObservable<RDFTable> observed, final RDFTable q) {

		System.out.println("UPDATEEEEEE");
		System.out.println(q.getJsonSerialization());

	}
}
