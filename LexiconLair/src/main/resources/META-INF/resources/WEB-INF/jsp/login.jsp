<!DOCTYPE html>
<html lang="en">
<head>
<link href="webjars/bootstrap/5.3.8/css/bootstrap.css" rel="stylesheet">
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
	<script src="webjars/bootstrap/5.3.8/js/bootstrap.js"></script>
	<script src="webjars/jquery/4.0.0/jquery.js"></script>
</div>
</body>
</html>