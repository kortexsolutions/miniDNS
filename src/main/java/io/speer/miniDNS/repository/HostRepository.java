package io.speer.miniDNS.repository;

import io.speer.miniDNS.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HostRepository extends JpaRepository<Host, String>{
    Optional<Host> findById(String hostName);
    Optional<Host> findByHostName(String hostName);
}
