package io.speer.miniDNS.service.impl;

import io.speer.miniDNS.common.enums.TypeEnum;
import io.speer.miniDNS.dto.HostRequestDto;
import io.speer.miniDNS.dto.HostResponseDto;
import io.speer.miniDNS.entity.ARecord;
import io.speer.miniDNS.entity.CName;
import io.speer.miniDNS.entity.Host;
import io.speer.miniDNS.repository.CNameRepository;
import io.speer.miniDNS.repository.HostRepository;
import io.speer.miniDNS.repository.RecordRepository;
import io.speer.miniDNS.service.HostService;
import io.speer.miniDNS.service.UtilityService;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HostServiceImpl implements HostService {
    @Autowired
    HostRepository _hRepo;

    @Autowired
    RecordRepository _rRepo;

    @Autowired
    CNameRepository _cRepo;

    @Autowired
    UtilityService _uService;

    @Autowired
    ModelMapper _mMapper;

    @Override
    public HostResponseDto save(HostRequestDto requestHost) throws BadRequestException {
        LocalDateTime now = LocalDateTime.now();

        TypeEnum type = requestHost.getType().equalsIgnoreCase(TypeEnum.A.getTypeName()) ? TypeEnum.A : TypeEnum.CNAME;
        String hostName = requestHost.getHostName();

        String value = requestHost.getValue();
        boolean isValid = type.equals(TypeEnum.A) ? _uService.isValidIp(value) : _uService.isValidHostname(hostName);

        if (!_uService.isValidHostname(hostName) || !isValid)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Invalid form field value. Please check format and try again.");

        if (type.equals(TypeEnum.A)) {
            ARecord existRecord = _rRepo.findByIpAddress(requestHost.getValue());

            if (existRecord != null)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This record already exist.");
        }

        Optional<Host> foundHost = _hRepo.findById(hostName);
        Host host = !foundHost.isPresent() ? null : foundHost.get();

        if (host == null) {
            Host newHost = new Host();
            newHost.setCreatedAt(now);
            newHost.setHostName(hostName);

            newHost.setType(type);
            host = _hRepo.save(newHost);
        }

        HostResponseDto response = type.equals(TypeEnum.A) ? addRecord(host, requestHost) : addCName(host, requestHost);
        return response;
    }

    private HostResponseDto addRecord(Host host, HostRequestDto newHost) {
        LocalDateTime now = LocalDateTime.now();
        CName foundCName = _cRepo.findByHost(host);

        if (foundCName != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to add a record with existing CName entry.", null);

        ARecord record = new ARecord();
        record.setIpAddress(newHost.getValue());
        record.setHost(host);
        record.setCreatedAt(now);
        record = _rRepo.save(record);

        /* construct response dto */
        HostResponseDto response = _mMapper.map(host, HostResponseDto.class);
        response.setValue(record.getIpAddress());
        return response;
    }


    private HostResponseDto addCName(Host host, HostRequestDto newHost) {
        LocalDateTime now = LocalDateTime.now();
        List<ARecord> foundRecords = _rRepo.findByHost(host);

        if (!foundRecords.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to add CName record with existing record entries.", null);

        CName foundCName = _cRepo.findByHost(host);
        if (foundCName == null) {
            CName cName = new CName();
            cName.setHost(host);
            cName.setCreatedAt(now);
            Host link = null;

            if (!this.circularChainValidation(newHost.getValue())) {
                link = _hRepo.findByHostName(newHost.getValue()).get();
            }

            cName.setAlias(link != null ? link.getHostName() : null);
            foundCName = _cRepo.save(cName);
        }

        /* construct response dto */
        HostResponseDto response = _mMapper.map(host, HostResponseDto.class);
        response.setValue(newHost.getValue());
        return response;

    }









    /*******************************************************************************************************************
     *   @circularChainValidation: This function is used to determine if circular reference exist
     *   @params h: Accepts exiting Host entity object
     *   @return exist: Returns a boolean value
     ******************************************************************************************************************/
    private boolean circularChainValidation(String value) {
        boolean exist = false;
        Set<String> visited = new HashSet<>();
        String current = value.toLowerCase();

        while (current != null) {
            if (visited.contains(current)) {
                exist = true;
                break;
            }

            visited.add(current.toLowerCase());
            Host host = _hRepo.findByHostName(current).get();
            current = (host.getCName() != null) ? host.getCName().getAlias() : null;
        }

        return exist;
    }
}
