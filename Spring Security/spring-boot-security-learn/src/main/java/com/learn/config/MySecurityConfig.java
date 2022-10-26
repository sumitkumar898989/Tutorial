package com.learn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled=true) this annotation is used when we use @PreAuthorize in controller
public class MySecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		http
		//authorizeRequests().antMatchers("/home").permitAll()
		.csrf().disable()                            //csrf is to disable csrf attack
		.authorizeRequests()
		.antMatchers("/home/**").hasRole("NORMAL")
		.antMatchers("/users/**").hasRole("ADMIN")
		.anyRequest().authenticated().and().httpBasic();
	}

	//this is used when we don't want any password encoder
	/*
	 * @Override 
	 * protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	 *   auth.inMemoryAuthentication().withUser("sumit").password("amit").roles("NORMAL");
	 *   auth.inMemoryAuthentication().withUser("alam").password("saheb").roles("ADMIN"); }
	 */
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("sumit").password(this.passwordEncoder().encode("amit")).roles("NORMAL");
		auth.inMemoryAuthentication().withUser("alam").password(this.passwordEncoder().encode("saheb")).roles("ADMIN");
	}

	//this is used when we don't want any password encoder
	/*
	 * @Bean 
	 * public PasswordEncoder passwordEncoder() 
	 * { return NoOpPasswordEncoder.getInstance(); 
	 * }
	 */
	
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(10);
	}
	
}
