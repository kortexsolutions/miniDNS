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
public class HostListResponseDto {
    private String hostName;
    private List<RecordResponseDto> records;
}

