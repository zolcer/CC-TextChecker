<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*, java.net.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Text Checker v.0.1</title>
</head>
<body>
<h1>Text Checker</h1>
<h2>Please choose file or copy your text to the textfield</h2>
<h3>Select a file to check for similarities</h3>
<form action="IndexServlet" method="post" enctype="multipart/form-data">
<input type="file" name="file" />
<br>
<input type="submit" />
</form>
<h3>Copy text which you can check for similarities</h3>
<form action="IndexServlet" method="post">
<textArea name="textFieldText" placeholder="Copy your text in here" cols="40" rows="5"></textArea>
<br>
<input type="submit" />
</form>

<% String inputData=(String)request.getAttribute("inputData");
	if (inputData == null) 
		inputData = "";
	%>

<%=inputData%>
<br>
<p>
*either select a textFile or add a text to the text field<br>
*if both are fulfilled only the selected file will be proceeded
</p>


</body>
</html>