<html>
<head>
<title>Import</title>
</head>
<body>
<h1>Import</h1>
<form method="POST" action="${pageContext.request.contextPath}/Data/Import.xml">
	TaskId : <input type="text" name="taskId"/> <BR />
	BandId : <input type="text" name="bandId"/> <BR />
	<input type="hidden" name="srcnodata" value="-1" />
	<input type="submit"/>
</form>
</body>
</html>