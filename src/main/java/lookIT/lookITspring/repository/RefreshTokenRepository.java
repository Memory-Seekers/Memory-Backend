package lookIT.lookITspring.repository;

import java.util.Optional;
import lookIT.lookITspring.entity.RefreshToken;
import lookIT.lookITspring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);
	Optional<RefreshToken> findByUser(User user);

	@Modifying
	int deleteByUser(User user);

}
