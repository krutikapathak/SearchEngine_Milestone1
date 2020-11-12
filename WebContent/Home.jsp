<%@page import="org.apache.tomcat.util.security.PrivilegedSetTccl"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ page import="cecs429.documents.*"%>
<%@ page import="java.nio.file.Paths"%>
<%@ page import="java.nio.file.Path"%>
<%@ page import="java.nio.file.Files"%>
<%@ page import="cecs429.index.*"%>
<%@ page import="edu.csulb.*"%>
<%@ page import="java.io.*"%>

<html>
<meta charset="UTF-8">
<head>
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
		var result_style = document.getElementById('in_progress').style;
		result_style.display = 'table-row';
	}
}

function showMe(type) {
	if( type === 'boolean') {
		var query_box = document.getElementById('boolean_query_type').style;
		query_box.display = 'table-row';
		var type_box = document.getElementById('enter_query').style;
		type_box.display = 'table-row';
		var formula_box = document.getElementById('formula_type').style;
		formula_box.display = 'none';
	} else if(type === 'ranked') {
		var formula_box = document.getElementById('formula_type').style;
		formula_box.display = 'table-row';
		var query_box = document.getElementById('boolean_query_type').style;
		query_box.display = 'none';
		var type_box = document.getElementById('enter_query').style;
		type_box.display = 'table-row';
	} else if(type === 'formula') {
		var query_box = document.getElementById('boolean_query_type').style;
		query_box.display = 'none';
	}
}

function loadMe() {
	var urlParams = new URLSearchParams(window.location.search);
	var change = urlParams.get('change');
	if(change != null) {
		showMe('ranked');
		var query_box = document.getElementById('ranked');
		query_box.checked = true;
	}
}

</script>
</head>
	<body onload="loadMe()">
		<% 
		response.setContentType("text/html");
		String navigate = "<a href='Web.jsp'>Home</a>";
		PrintWriter pwriter = response.getWriter();
		String htmlCode = "";
		String directory = request.getParameter("dir");
		String indexAction = request.getParameter("index");
		if(directory == null) {
			directory = (String) session.getAttribute("Directory");
			indexAction = (String) session.getAttribute("IndexAction");
		}
		if (!directory.endsWith("/")){
			directory += "/";
		}
		Path path = Paths.get(directory + "index/postings.bin");
		
		String soundexDir = "/Users/krutikapathak/eclipse-workspace/Milestone1/mlb-articles-4000/";
		
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), getExtension(directory));
		DocumentCorpus soundexCorpus = DirectoryCorpus.loadTextDirectory(Paths.get(soundexDir).toAbsolutePath(), getExtension(soundexDir));
		
		long Start = System.currentTimeMillis();
		 //Index corpus for Boolean Queries 
		Index index = FinalTermDocumentIndexer.indexCorpus(corpus, "advance", directory, indexAction);
		 //Index corpus for Soundex 
		Index soundexIndex = FinalTermDocumentIndexer.indexCorpus(soundexCorpus, "soundex", soundexDir, indexAction);
		long Stop = System.currentTimeMillis();
		long timeTaken = (Stop - Start);
		
		session.setAttribute("Index", index);
		session.setAttribute("soundexDir", soundexDir);
		session.setAttribute("soundexIndex", soundexIndex);
		session.setAttribute("DocumentCorpus", corpus);
		session.setAttribute("DocumentCorpus1", soundexCorpus);
		session.setAttribute("Directory", directory);
		session.setAttribute("IndexAction", indexAction);
		
		if (indexAction.equalsIgnoreCase("query")) {
			if(!Files.exists(path)) {
				response.sendRedirect("Web.jsp?index=no");
			}
		} else {
			htmlCode = "Your directory <b>\"" + directory + "\"</b>";
			htmlCode += " has been indexed in <b>" + timeTaken + "</b> ms!";
		}
		%>
		<form name="homeForm" action="Query.jsp" onsubmit="return validate()">
			<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
			  <TR>
			    <TD style="text-align: center; font-size: 30px;" colspan="4">Search Engine</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-size: 17px; padding: 20px;" colspan="4">
			    	<%= navigate %>
			    </TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-size: 17px;" colspan="4">
			    	<%= htmlCode %>
			    </TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-weight: bold; padding: 10px" colspan="4">What type of Query would you like to do?</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-weight: bold; padding: 20px" colspan="4">
					<input type="radio" id="boolean" name="queryType" value="boolean" onclick="showMe('boolean');">
					<label for="boolean">Boolean Query</label>
					<input type="radio" id="ranked" name="queryType" value="ranked" onclick="showMe('ranked');">
					<label for="ranked">Ranked Query</label>
				</TD>
			  </TR>
			  <TR id="boolean_query_type" style="display: none;">
			    <TD style="text-align: left; padding: 25px;" colspan="4">
			    	Enter your search query or try one of the below special query:
			    	<ul>
					  <li><b>:stem</b> <i>token</i> - take the token string and stem it, then print the stemmed term.</li>
					  <li><b>:index</b> <i>directoryname</i> - index the folder specified by directory name and then begin querying it, effectively restarting the program.</li>
					  <li><b>:vocab</b> - print the first 1000 terms in the vocabulary of the corpus, sorted alphabetically, one term per line. Then print the count of the total number of vocabulary terms.</li>
					  <li><b>:author __________</b> - search the author soundex index by specifying the author name.</li>
					  <li><b>:q</b> - exit the program.</li>
					</ul>
			    </TD>
			  </TR>
			  <TR id="formula_type" style="display: none;">
			    <TD style="text-align: center; font-weight: bold; padding: 20px;">
					<input type="radio" id="1" name="formulaType" value="1" onclick="showMe('formula');">
					<label for="1">Default</label>
				</TD>
				<TD style="text-align: center; font-weight: bold; padding: 20px;">
					<input type="radio" id="2" name="formulaType" value="2" onclick="showMe('formula');">
					<label for="2">tf-idf</label>
				</TD>
				<TD style="text-align: center; font-weight: bold; padding: 20px;">
					<input type="radio" id="3" name="formulaType" value="3" onclick="showMe('formula');">
					<label for="3">Okapi BM25</label>
				</TD>
				<TD style="text-align: center; font-weight: bold; padding: 20px;">
					<input type="radio" id="4" name="formulaType" value="4" onclick="showMe('formula');">
					<label for="4">Wacky</label>
				</TD>
			  </TR>
			  <TR id="enter_query" style="display: none;">
			    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 20px;" colspan="4">
			      <input type="text" name="query" placeholder="Enter your query" style="border-radius: 25px; height: 25px; width: 400px;" />
			      <p><input type="submit" value="Search" style="text-align: center; background-color: white; border-radius: 25px; height: 30px; width: 150px;" /></p>
			    </TD>
			  </TR>
			  <TR id="in_progress" style="display: none;">
			    <TD style="text-align: center;" colspan="4"><b>Please wait while your directory is being indexed...</b></TD>
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