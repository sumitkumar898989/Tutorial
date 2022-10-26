package com.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.user.enity.User;

@Service
public class UserServiceImpl implements UserService{

	
	//fake user list
	
	List<User> list = List.of(
			
			new User(1311L,"Durgesh Tiwari","23525625"),
			new User(1312L,"Ankit Tiwari","99999"),
			new User(1314L,"Ravi Tiwari","888")
			
     );
	
	@Override
	public User getUser(Long id) {
		// TODO Auto-generated method stub
		return list.stream().filter(user -> user.getUserId().equals(id)).findAny().orElse(null);
	}

}
