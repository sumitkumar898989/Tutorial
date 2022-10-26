package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userNmae=principal.getName();
		System.out.println("USERNAME"+userNmae);
		User user=userRepository.getUserbyUserName(userNmae);
		System.out.println("USER"+user);
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		try {
			String name=principal.getName();
			User user= this.userRepository.getUserbyUserName(name);
			//processing and uploading file
			
			if(file.isEmpty()) {
				System.out.println("File is Empty");
				contact.setImage("contact.png");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image Uploaded");
				
				
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println("CONTACT"+contact);
			System.out.println("Added to database");
			
			//message success
			session.setAttribute("message", new Message("Your Contact is added !! Add more contacts..", "success"));
			
		}
		catch(Exception e)
		{
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//message error
			session.setAttribute("message", new Message("Something went wrong !! Try Again..", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page 5 contacts
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page,  Model m,Principal principal) {
		m.addAttribute("title", "View Contacts");
		/*
		 * String userNmae=principal.getName(); 
		 * User user=this.userRepository.getUserbyUserName(userNmae); 
		 * user.getContacts();
		 */
		String userNmae=principal.getName(); 
		User user=this.userRepository.getUserbyUserName(userNmae);
		
		Pageable pageable=  PageRequest.of(page, 5);
		
		Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show-contacts";
		
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		// solving security bug
		String userName=principal.getName();
		User user= this.userRepository.getUserbyUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		//model.addAttribute("contact", contact);
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model,HttpSession session,Principal principal) {
		
		Contact contact =this.contactRepository.findById(cid).get();
		
		User user= this.userRepository.getUserbyUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Contact Deleted Successfully..", "success"));
		
		
		
		return "redirect:/user/show-contacts/0";
		
	}
	
	//update contact handler
	@PostMapping("/update-contact/{cid}")
	public String updateFomr(@PathVariable("cid") Integer cid,Model m)
	{
		m.addAttribute("title", "Update Contact");
		Contact contact =this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file , Model m, HttpSession session,Principal principal)
	{
		
		try {
			
				//old contact details
			    Contact oldContactDetails = this.contactRepository.findById(contact.getCid()).get();
			
			
			//image
			
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				
				//delete old photo and 
				File deleteFile = new ClassPathResource("/static/img").getFile();
				File file1= new File(deleteFile,oldContactDetails.getImage());
				file1.delete();
				
				
				//update new photo
				File saveFile = new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
			}
			else {
					contact.setImage(oldContactDetails.getImage());
			}
			
			User user= this.userRepository.getUserbyUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact has been updated..", "success"));
			
			
		}
		catch (Exception e) {
			
		}
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID "+contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}

	
	//chnage password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		System.out.println("OLD PASSWORD"+oldPassword);
		System.out.println("NEW PASSWORD"+newPassword);
		String userName=principal.getName();
		User currentUser= this.userRepository.getUserbyUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password has been updated..", "success"));
		}
		else {
			//error
			session.setAttribute("message", new Message("Please Enter Correct Old Password..", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
}
