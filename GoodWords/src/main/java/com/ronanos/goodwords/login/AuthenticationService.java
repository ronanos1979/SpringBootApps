package com.ronanos.goodwords.login;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {
	
	public boolean authentication(String username, String password) {
		
		boolean isValidUserName = username.equalsIgnoreCase("ronan");
		boolean isValidPassword = password.equalsIgnoreCase("password");
		
		return isValidPassword && isValidUserName;
	}
}
