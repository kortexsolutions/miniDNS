package io.speer.miniDNS.repository;

import io.speer.miniDNS.entity.ARecord;
import io.speer.miniDNS.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<ARecord, Long>{
    Optional<ARecord> findById(Long id);
    ARecord findByIpAddress(String ip);
    List<ARecord> findByHost(Host host);
}
