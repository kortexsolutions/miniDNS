package io.speer.miniDNS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HostResponseDto {
    private String hostName;
    private String type;
    private String value;
    private LocalDateTime createdAt;
}
