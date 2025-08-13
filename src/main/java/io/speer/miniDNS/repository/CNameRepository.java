package io.speer.miniDNS.repository;

import io.speer.miniDNS.entity.CName;
import io.speer.miniDNS.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CNameRepository extends JpaRepository<CName, Long>{
    Optional<CName> findById(Long id);
    CName findByHost(Host host);
    CName findByAliasIgnoreCase(String alias);
}
