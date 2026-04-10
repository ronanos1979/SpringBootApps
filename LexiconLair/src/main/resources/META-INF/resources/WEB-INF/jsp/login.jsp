<!DOCTYPE html>
<html lang="en">
<head>
<link href="webjars/bootstrap/5.3.3/css/bootstrap.css" rel="stylesheet">
<title>Welcome to the Login Page</title>
</head>
<body>Welcome to the Login Page!
<div class="container">
	
	<div>${errorMessage}</div>
	<form method="post">
	Name: <input type="text" name="name"/>
	Password: <input type="password" name="password"/>
	
	<input type="submit"/>
	
	</form>
	<script src="webjars/bootstrap/5.3.3/js/bootstrap.js"></script>
	<script src="webjars/jquery/3.7.1/jquery.js"></script>
</div>
</body>
</html>