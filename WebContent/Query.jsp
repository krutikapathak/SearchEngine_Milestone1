<%@page import="org.apache.tomcat.util.security.PrivilegedSetTccl"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ page import="cecs429.documents.*"%>
<%@ page import="java.nio.file.Paths"%>
<%@ page import="cecs429.index.*"%>
<%@ page import="cecs429.text.*"%>
<%@ page import="java.io.*"%>
<%@ page import="edu.csulb.*"%>
<%@ page import="cecs429.query.*"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.gson.JsonObject" %>
<%@ page import="com.google.gson.Gson" %>
<html>
<head>
<meta charset="UTF-8">
<title>Search engine</title>
<script type="text/javascript">
function validate() {
	var query = document.forms["homeForm"]["query"].value;
	if(query == "") {
		alert("You did not enter any query!\nEnter your query or special query");
		return false;
	}
	if(query == ":q" || query == "quit") {
	  alert("Thank you for using the Search Engine!\nYou will now be redirected to Homepage.\nGoodbye!");
	}
	if(query.startsWith(":") && !query.startsWith(":q") && !query.startsWith(":vocab") && !query.startsWith(":index") && !query.startsWith(":stem") && !query.startsWith(":author")) {
		alert("Your special query does not match existing options!");
		return false;
	}
	if(query.startsWith(":index")) {
		var inprogress_style = document.getElementById('in_progress').style;
		inprogress_style.display = 'table-row';
		var result_title_style = document.getElementById('result_title').style;
		result_title_style.display = 'none';
		var result_list_style = document.getElementById('result_list').style;
		result_list_style.display = 'none';
	}
}
</script>
</head>
<body>
<%
	response.setContentType("text/html");
	PrintWriter pwriter = response.getWriter();
	
	Index index = (Index) session.getAttribute("Index");
	DocumentCorpus corpus = (DocumentCorpus) session.getAttribute("DocumentCorpus");
	Index soundexIndex = (Index) session.getAttribute("soundexIndex");
	DocumentCorpus soundexCorpus = (DocumentCorpus) session.getAttribute("DocumentCorpus1");
	String soundexDir = (String) session.getAttribute("soundexDir");
	
	String[] splittedString;
	String resultList = "", resultTitle = "";
	String query = request.getParameter("query");
	BooleanQueryParser bqp = new BooleanQueryParser();
	QueryComponent qc = bqp.parseQuery(query);
	  //Special Queries
	if (query.startsWith(":")) {
		splittedString = query.split(" ");
		switch (splittedString[0]) {
		case ":q":
			session.invalidate();
			response.sendRedirect("Web.jsp");
			break;

		case ":stem":
			resultTitle = "Stemmed word of \"" + splittedString[1] + "\" is: <b>" + AdvanceTokenProcessor.stemWord(splittedString[1]) + "</b>";
			break;

		case ":index": {
			session.removeAttribute("DocumentCorpus");
			corpus = DirectoryCorpus.loadTextDirectory(Paths.get(splittedString[1]).toAbsolutePath(), getExtension(splittedString[1]));
			String directory = splittedString[1].toString();
			session.removeAttribute("Directory");
			session.setAttribute("Directory", directory);
			session.setAttribute("DocumentCorpus", corpus);
			long Start = System.currentTimeMillis();
			index = FinalTermDocumentIndexer.indexCorpus(corpus, "advance");
			long Stop = System.currentTimeMillis();
			long timeTaken = Stop - Start;
			resultTitle = "Your directory <b>\"" + directory + "\"</b>";
			resultTitle += " has been indexed in <b>" + timeTaken + "</b> ms!";
			session.removeAttribute("Index");
			session.setAttribute("Index", index);
			break;
		}

		case ":vocab": {
			List<String> vocab = index.getVocabulary();
			int limit = 0;
			if (vocab.size() < 1000) {
				limit = vocab.size();
			} else
				limit = 1000;
			for (int i = 0; i < limit; i++) {
				resultList += vocab.get(i) + "</br>";
			}
			for(int i=0; i<vocab.size(); i++)
			resultTitle = "Your vocabulary has <b>" + vocab.size() + "</b> words! <br> Showing <b>" + limit + " </b> words";
			break;
		}
		
		case ":author": {
			List<Posting> result = soundexIndex.getPostings(splittedString[1]);
			resultTitle = "Your query <b>" + splittedString[1] + "</b> returned <b>" + result.size() + "</b> documents!!";
			if(result.size() > 0) {
				resultTitle += "<br>Click on the document you wish to open<br>";
			} else {
				resultTitle = "<b>Your query returned no results or your session is closed!! </b></br>";
			}
			resultList += "<table border='1' style='border-collapse: collapse;'>";
			resultList += "<tr><td style='text-align: center;'><b>Author</b></td>";
			resultList += "<td style='text-align: center;'><b>Document Name</b></td></tr>";
			for (Posting p : result) {
				String docName = soundexCorpus.getDocument(p.getDocumentId()).getTitle();
				Reader reader;
					reader = soundexCorpus.getDocument(p.getDocumentId()).getContent();
					Gson gson = new Gson();
					JsonObject doc = gson.fromJson(reader, JsonObject.class);
					resultList += "<tr><td><b></b>" + doc.get("author").getAsString() + "</td>";
					resultList += "<td><a href = 'Document.jsp?param=a" + p.getDocumentId() + "'>" + docName + "</a></td></tr>";
			}
			resultList += "</table>";
		}
		}
	} else if(query.equalsIgnoreCase("quit")) {
		session.invalidate();
		response.sendRedirect("Web.jsp");
	}
	else {
		// Boolean query retrieval
		if(index !=null) {
			List<Posting> result = qc.getPostings(index);
			for (Posting p : result) {
				resultList += "<a href = 'Document.jsp?param=" + p.getDocumentId() + "'>" + corpus.getDocument(p.getDocumentId()).getTitle() + "</a> </br>";
			}
			resultTitle = "Your query <b>" + query + "</b> returned <b>" + result.size() + "</b> documents!!";
			if(result.size() > 0) {
				resultTitle += "<br>Click on the document you wish to open<br>";
			}
		} else {
			resultTitle = "<b>Your query returned no results or your session is closed!! </b></br>";
		}
	}
%>

<form name="homeForm" action="Query.jsp" onsubmit="return validate()">
			<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
			  <TR>
			    <TD style="text-align: center; font-size: 30px;">Search Engine</TD>
			  </TR>
			   <TR>
			    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 20px ">
			      <input type="text" name="query" placeholder="Enter your query or special query" style="border-radius: 25px; height: 25px; width: 400px;" />
			      <p><input type="submit" value="Search" style="text-align: center; background-color: white; border-radius: 25px; height: 30px; width: 150px;" /></p>
			    </TD>
			  </TR>
			  <TR id="in_progress" style="display: none;">
			    <TD style="text-align: center;"><b>Please wait while your directory is being indexed...</b></TD>
			  </TR>
			  <TR id="result_title">
			    <TD style="text-align: center; font-size: 17px;">
			    	<%= resultTitle %>
			    </TD>
			  </TR>
			  <TR id="result_list">
			    <TD style="text-align: center; font-size: 17px;">
				    <div style="overflow:scroll; height:450px;" align="center">
				    	<%= resultList %>
			    	</div>
			    </TD>
			    
			  </TR>
			</TABLE>
		</form>
	<%!
		public String getExtension(String directory) {
			String fileName = new File(directory).listFiles()[0].getName();
			return fileName.substring(fileName.lastIndexOf('.'));
		}
	%>
</body>
</html>