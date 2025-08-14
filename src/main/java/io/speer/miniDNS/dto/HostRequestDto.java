package io.speer.miniDNS.dto;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HostRequestDto {
    private String hostname;
    private String type;
    private String value;
}
