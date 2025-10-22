package au.com.telstra.simcardactivator.repository;

import au.com.telstra.simcardactivator.entity.SimCardRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimCardRepository extends JpaRepository<SimCardRecord, Long> {
}
