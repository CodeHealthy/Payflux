package codehealthy.payflux.authservice.controllers;

import codehealthy.payflux.authservice.dto.UserResponse;
import codehealthy.payflux.authservice.services.UserAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
public class AuthAdminController {

	private final UserAdminService userAdminService;

	public AuthAdminController(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	@GetMapping("/users")
	public List<UserResponse> findUsers() {
		return userAdminService.findUsers();
	}
}
