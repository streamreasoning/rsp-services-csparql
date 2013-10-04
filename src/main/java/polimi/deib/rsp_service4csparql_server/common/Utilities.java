package polimi.deib.rsp_service4csparql_server.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Utilities {
	
	public static String getStackTrace(Exception e) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		return result.toString();
	}

}
