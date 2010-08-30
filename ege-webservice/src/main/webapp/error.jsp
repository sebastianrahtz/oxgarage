<?xml version="1.0" encoding="UTF-8"?>
<%@ page isErrorPage="true" language="java" 
    import="java.io.*"  %><?xml version="1.0" encoding="UTF-8"?>
<!-- *********************************************************************************************************** -->
<!-- *********************************************************************************************************** -->
<%
	if(exception != null){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			out.println("<error msg=\""+exception.getMessage()+"\" exclass=\""+exception.getClass()+"\" >"+sw+"</error>");
			sw.close();
			pw.close();
	}
	
%>
<!-- *********************************************************************************************************** -->
<!-- *********************************************************************************************************** -->