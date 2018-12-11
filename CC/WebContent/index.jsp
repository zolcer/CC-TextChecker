<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*, java.net.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
String jspPath = "D:\\Martin\\Documents\\UNI\\WS 18\\CC\\PaaS\\petovProjekt\\CC-TextChecker\\CC\\WebContent\\WEB-INF\\README.txt";
BufferedReader reader = new BufferedReader(new FileReader(jspPath));
//BufferedReader br = new InputStreamReader(new FileInputStream(txtFilePath));
StringBuilder sb = new StringBuilder();
String line = reader.readLine();
String wholeDocument = "";

while(line != null)
{
	wholeDocument = wholeDocument + line;
	line = reader.readLine();
}

reader.close();
%>
<form action="IndexServlet" method="post"
                        enctype="multipart/form-data">
<input type="text" name="description" />
<br>
<input type="file" name="file" />
<br>
<input type="submit" />
</form>

<% String inputData=(String)request.getAttribute("inputData"); %>

Value is: <%=inputData%>


</body>
</html>