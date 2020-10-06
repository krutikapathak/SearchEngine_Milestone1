<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ page import="java.io.*"%>
<%@ page import="cecs429.documents.*"%>
<%@ page import="java.util.Scanner"%>
<html>
<head>
<meta charset="UTF-8">
<title>Search engine</title>
</head>
<body>
	<%
	DocumentCorpus corpus;
	String content = "";
	String param;
	if(request.getParameter("param").startsWith("a")){
		param = request.getParameter("param").substring(1, request.getParameter("param").length());
		corpus = (DocumentCorpus) session.getAttribute("DocumentCorpus1");
	} else { 
		param = request.getParameter("param");
		corpus = (DocumentCorpus) session.getAttribute("DocumentCorpus");
	}
	int docId = Integer.parseInt(param);
	PrintWriter pwriter = response.getWriter();
	
	Document doc = corpus.getDocument(docId);
	String docName = doc.getTitle();
	Reader reader = doc.getContent();
	try (Scanner scanner = new Scanner(reader).useDelimiter("\\Z")) {
	    content = scanner.next();
	}
	%>
	
	<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
	  <TR>
	    <TD style="text-align: center; font-size: 30px;">Search Engine</TD>
	  </TR>
	   <TR>
	    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 10px ">
	      <a href="javascript:history.back()">Back to list of documents</a>
	    </TD>
	  </TR>
	  <TR>
	    <TD style="text-align: center; font-size: 17px;">
	    	<b><%= docName %></b>
	    </TD>
	  </TR>
	  <TR>
	    <TD style="text-align: left; font-size: 17px;">
	    	<div style="overflow:scroll; height:550px;">
	    		<%= content %>
	    	</div>
	    </TD>
	  </TR>
	</TABLE>
</body>
</html>