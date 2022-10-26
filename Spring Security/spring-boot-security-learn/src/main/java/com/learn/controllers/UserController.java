package com.learn.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.services.UserService;
import com.learn.models.User;

@RestController
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	//all users
	@GetMapping("/")
	public List<User> getAllUsers()
	{
		return this.userService.getAllUsers();
	}
	
	//return single user
	//@PreAuthorize("hasRole('ADMIN')")  we can use this directly instead of antmatcher url thing
	@GetMapping("/{username}")
	public User getUser(@PathVariable("username") String username)
	{
		return this.userService.getUser(username);
	}
	
	//add new user
	@PostMapping("/")
	public User add(@RequestBody User user)
	{
		return this.userService.addUser(user);
	}

}
