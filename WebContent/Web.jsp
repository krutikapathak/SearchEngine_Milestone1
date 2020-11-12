<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<% String status = ""; %>
	<head>
		<meta charset="UTF-8">
		<title>Search engine</title>
		<script type="text/javascript">
		function validate() {
			var directory = document.forms["homeForm"]["dir"].value;
			if(directory == "") {
				alert("You did not enter a directory path.");
				return false;
			} else {
				<% status = "Please wait while your directory is being indexed..."; %>
			}
		}
		
		function showMe() {
			var text_box = document.getElementById('enter_here').style;
			text_box.display = 'table-row';
			var status = document.getElementById('in_progress').style;
			status.display = 'none';
		}
		
		function getRadioVal() {
		    var val;
		    // get list of radio buttons with specified name
		    var radios = document.forms["homeForm"]["index"];
		    
		    // loop through list of radio buttons
		    for (var i=0, len=radios.length; i<len; i++) {
		        if ( radios[i].checked ) { // radio checked?
		            val = radios[i].value; // if so, hold its value in val
		            break; // and break out of for loop
		        }
		    }
		    return val; // return value of checked radio or undefined if none checked
		}
		</script>
	</head>
	<body>
		<%
			if(request.getParameter("index") != null) {
				status = "Your directory hasn't been indexed! Please use 'Build Index' option to create index.";
			} else {
				status = "";
			}
		%>
		<form name="homeForm" action="Home.jsp" onsubmit="return validate()">
			<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
			  <TR>
			    <TD style="text-align: center; font-size: 30px;" colspan="2">Search Engine</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-style: italic;" colspan="2">Milestone 2.0 - developed by Krutika and Aanchal</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: center; font-weight: bold; padding: 20px;" colspan="2">What would you like to do?</TD>
			  </TR>
			  <TR>
			    <TD style="text-align: right; font-weight: bold; padding: 10px">
					<input type="radio" id="build" name="index" value="build" onclick="showMe()">
					<label for="build">Build Index</label>
				</TD>
				<TD style="text-align: left; font-weight: bold; padding: 10px">
					<input type="radio" id="query" name="index" value="query" onclick="showMe()">
					<label for="query">Query Index</label>
				</TD>
			  </TR>
			  <TR id="enter_here" style="display: none;">
			    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 20px;" colspan="2">
			      <input type="text" name="dir" placeholder="Enter a directory" style="border-radius: 25px; height: 25px; width: 400px;" />
			      <p><input type="submit" value="Index" style="text-align: center; background-color: white; border-radius: 25px; height: 30px; width: 150px;" /></p>
			    </TD>
			  </TR>
			  <TR id="in_progress">
			    <TD style="text-align: center;" colspan="2"><b><%= status %></b></TD>
			  </TR>
			</TABLE>
		</form>
	</body>
</html>