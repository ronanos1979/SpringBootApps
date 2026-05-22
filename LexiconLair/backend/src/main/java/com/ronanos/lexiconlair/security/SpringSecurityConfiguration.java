package com.ronanos.lexiconlair.security;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/assets/**", "/favicon.*", "/error").permitAll()
						.requestMatchers("/api/auth/login").permitAll()
						.requestMatchers("/api/auth/me").authenticated()
						.requestMatchers("/api/game/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/books/search").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/books").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/books/*/save").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/authors").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/authors/search").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/authors").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/users/**").hasRole("ADMIN")
						.requestMatchers("/api/definitions/**").hasRole("ADMIN")
						.requestMatchers("/api/words/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/books").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/books/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/authors/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/authors/**").hasRole("ADMIN")
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				.formLogin(form -> form
						.loginProcessingUrl("/api/auth/login")
						.successHandler((req, res, authentication) ->
								res.setStatus(HttpServletResponse.SC_OK))
						.failureHandler((req, res, ex) ->
								res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad credentials"))
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler((req, res, authentication) ->
								res.setStatus(HttpServletResponse.SC_OK))
						.deleteCookies("JSESSIONID")
						.permitAll())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, authEx) ->
								res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
				.headers(headers -> headers.frameOptions(withDefaults()))
				.build();
	}
}
