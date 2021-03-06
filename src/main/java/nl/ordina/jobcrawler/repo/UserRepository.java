package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    long count();
    Optional<User> findByUsername(String username);
    Optional<User> findByIdAndUsername(long id, String username);
    Boolean existsByUsername(String username);
}
