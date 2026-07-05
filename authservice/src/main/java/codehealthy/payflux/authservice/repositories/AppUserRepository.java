package codehealthy.payflux.authservice.repositories;

import codehealthy.payflux.authservice.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByEmail(String email);

	Optional<AppUser> findByEmail(String email);
}
