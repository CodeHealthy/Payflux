package codehealthy.payflux.authservice.services;

import codehealthy.payflux.authservice.dto.UserResponse;
import codehealthy.payflux.authservice.repositories.AppUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

	private final AppUserRepository appUserRepository;

	public UserAdminService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findUsers() {
		return appUserRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
				.stream()
				.map(UserResponse::from)
				.toList();
	}
}
