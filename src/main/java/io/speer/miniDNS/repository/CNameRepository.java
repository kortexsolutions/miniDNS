package io.speer.miniDNS.repository;

import io.speer.miniDNS.entity.CName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CNameRepository extends JpaRepository<CName, Long>{
    Optional<CName> findById(Long id);
}
