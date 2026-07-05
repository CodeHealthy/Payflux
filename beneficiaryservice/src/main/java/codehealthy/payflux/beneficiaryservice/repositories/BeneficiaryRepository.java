package codehealthy.payflux.beneficiaryservice.repositories;

import codehealthy.payflux.beneficiaryservice.models.Beneficiary;
import codehealthy.payflux.beneficiaryservice.models.BeneficiaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

	boolean existsByOwnerUserIdAndBeneficiaryAccountNumber(Long ownerUserId, String beneficiaryAccountNumber);

	List<Beneficiary> findByOwnerUserIdAndStatusOrderByCreatedAtDesc(Long ownerUserId, BeneficiaryStatus status);

	Optional<Beneficiary> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
