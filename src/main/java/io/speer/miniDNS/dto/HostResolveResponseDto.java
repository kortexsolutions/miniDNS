package io.speer.miniDNS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HostResolveResponseDto {
    private String hostName;
    private String pointTo;
    private String recordType;
    private List<String> resolveIps;
}

