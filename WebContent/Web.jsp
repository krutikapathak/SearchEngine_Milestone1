<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
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
		var result_style = document.getElementById('in_progress').style;
		result_style.display = 'table-row';
	}
}
</script>
</head>
	<body>
		<form name="homeForm" action="Home.jsp" onsubmit="return validate()">
			<TABLE style="width: 50%; margin-left: auto; margin-right: auto;">
			  <TR>
			    <TD style="text-align: center; font-size: 30px;">Search Engine</TD>
			  </TR>
			   <TR>
			    <TD style="text-align: center; font-style: italic;">Milestone1 - developed by Krutika and Aanchal</TD>
			  </TR>
			  <TR>
			    <TD style="margin-left: auto; margin-right: auto; text-align: center; padding: 20px ">
			      <input type="text" name="dir" placeholder="Enter a directory to index" style="border-radius: 25px; height: 25px; width: 400px;" />
			      <p><input type="submit" value="Index" style="text-align: center; background-color: white; border-radius: 25px; height: 30px; width: 150px;" /></p>
			    </TD>
			  </TR>
			  <TR id="in_progress" style="display: none;">
			    <TD style="text-align: center;"><b>Please wait while your directory is being indexed...</b></TD>
			  </TR>
			</TABLE>
		</form>
	</body>
</html>