package io.speer.miniDNS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name="host")
@NamedQuery(name = "Host.findAll", query = "SELECT h FROM Host h")
public class Host {
    @Id
    @Column(name = "host_name", nullable = false, unique = true)
    private String hostName;

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ARecord> records;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
