<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ page import="cecs429.documents.*"%>
<%@ page import="java.nio.file.Paths"%>
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
</script>
</head>
	<body>
		<% 
		response.setContentType("text/html");
		PrintWriter pwriter = response.getWriter();
		
		String directory = request.getParameter("dir");
		String htmlCode = "Your directory <b>\"" + directory + "\"</b>";
		
		String soundexDir = "/Users/krutikapathak/eclipse-workspace/GUI/mlb-articles-4000/";
		
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), getExtension(directory));
		DocumentCorpus corpus1 = DirectoryCorpus.loadTextDirectory(Paths.get(soundexDir).toAbsolutePath(), getExtension(soundexDir));
		
		long Start = System.currentTimeMillis();
		Index index = FinalTermDocumentIndexer.indexCorpus(corpus);
		Index soundexIndex = FinalTermDocumentIndexer.indexAuthorCorpus(corpus1);
		long Stop = System.currentTimeMillis();
		long timeTaken = (Stop - Start);
		
		session.setAttribute("Index", index);
		session.setAttribute("soundexDir", soundexDir);
		session.setAttribute("soundexIndex", soundexIndex);
		session.setAttribute("DocumentCorpus", corpus);
		session.setAttribute("DocumentCorpus1", corpus1);
		session.setAttribute("Directory", directory);
		
		htmlCode += " has been indexed in <b>" + timeTaken + "</b> ms!";
		%>
		<form name="homeForm" action="Query.jsp" onsubmit="return validate()">
			<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
			  <TR>
			    <TD style="text-align: center; font-size: 30px;">Search Engine</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-size: 17px;">
			    	<%= htmlCode %>
			    </TD>
			  </TR>
			  <TR>
			    <TD style="text-align: left; padding: 25px;">
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
			  <TR>
			    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 20px ">
			      <input type="text" name="query" placeholder="Enter your query or special query" style="border-radius: 25px; height: 25px; width: 400px;" />
			      <p><input type="submit" value="Search" style="text-align: center; background-color: white; border-radius: 25px; height: 30px; width: 150px;" /></p>
			    </TD>
			  </TR>
			  <TR id="in_progress" style="display: none;">
			    <TD style="text-align: center;"><b>Please wait while your directory is being indexed...</b></TD>
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