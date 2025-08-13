package io.speer.miniDNS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name="record")
@NamedQuery(name = "ARecord.findAll", query = "SELECT r FROM ARecord r")
public class ARecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private Host host;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
