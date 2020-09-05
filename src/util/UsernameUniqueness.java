package util;

import javax.servlet.ServletContext;

import dao.AdminDAO;
import dao.HostDAO;
import dao.UserDAO;
import model.Admin;
import model.Host;
import model.User;

public class UsernameUniqueness {
	
	public UsernameUniqueness() {
		
	}
	
	public static boolean isUsernameUnique(String username, ServletContext servletContext) {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		AdminDAO adminDAO = (AdminDAO) servletContext.getAttribute("adminDAO");
		HostDAO hostDAO = (HostDAO) servletContext.getAttribute("hostDAO");
		
		User user = userDAO.findUserByUsername(username);
		Admin admin = adminDAO.findAdminByUsername(username);
		Host host = hostDAO.findHostByUsername(username);
		
		return user == null && admin == null && host == null;
	}
}
