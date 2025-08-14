package io.speer.miniDNS.service;

import io.speer.miniDNS.dto.HostListResponseDto;
import io.speer.miniDNS.dto.HostRequestDto;
import io.speer.miniDNS.dto.HostResolveResponseDto;
import io.speer.miniDNS.dto.HostResponseDto;
import org.apache.coyote.BadRequestException;

public interface HostService {

    HostResponseDto save(HostRequestDto host) throws BadRequestException;
    HostResolveResponseDto resolve(String hostName);
    HostListResponseDto records(String hostName);
}
