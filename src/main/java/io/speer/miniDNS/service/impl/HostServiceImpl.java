package io.speer.miniDNS.service.impl;

import io.speer.miniDNS.common.Message;
import io.speer.miniDNS.common.enums.TypeEnum;
import io.speer.miniDNS.dto.*;
import io.speer.miniDNS.entity.ARecord;
import io.speer.miniDNS.entity.CName;
import io.speer.miniDNS.entity.Host;
import io.speer.miniDNS.repository.CNameRepository;
import io.speer.miniDNS.repository.HostRepository;
import io.speer.miniDNS.repository.RecordRepository;
import io.speer.miniDNS.service.HostService;
import io.speer.miniDNS.service.UtilityService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public HostResponseDto save(HostRequestDto requestHost) {
        LocalDateTime now = LocalDateTime.now();

        TypeEnum type = requestHost.getType().equalsIgnoreCase(TypeEnum.A.getTypeName()) ? TypeEnum.A : TypeEnum.CNAME;
        String hostName = requestHost.getHostname();

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

    @Override
    public HostResolveResponseDto resolve(String hostname) {
        Optional<Host> foundHost = _hRepo.findByHostName(hostname);
        HostResolveResponseDto response;

        if (foundHost.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND, null);

        try {
            Host host = foundHost.get();
            response = new HostResolveResponseDto();
            response.setHostName(host.getHostName());
            response.setRecordType(host.getType().getTypeName().toUpperCase());

            String pointTo = null;
            List<String> resolvedIps = new ArrayList<>();

            if (host.getType().equals(TypeEnum.CNAME)) {
                pointTo = host.getCName().getAlias();
                Optional<Host> parent = _hRepo.findByHostName(pointTo);

                if (parent.isPresent()) {
                    resolvedIps.addAll(
                       parent.get().getRecords().stream()
                          .map(ARecord::getIpAddress)
                          .collect(Collectors.toList())
                    );
                }
            } else {
                resolvedIps.addAll(
                   host.getRecords().stream()
                      .map(ARecord::getIpAddress)
                      .collect(Collectors.toList())
                );
            }

            response.setResolveIps(resolvedIps);
            response.setPointTo(pointTo);

        } catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }

        return response;
    }

    @Override
    public HostListResponseDto records(String hostname) {
        Optional<Host> foundHost = _hRepo.findByHostName(hostname);
        HostListResponseDto response;

        if (foundHost.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND, null);

        try {
            Host host = foundHost.get();
            response = new HostListResponseDto();
            response.setHostName(host.getHostName());
            List<RecordResponseDto> records = new ArrayList<>();

            if (host.getType().equals(TypeEnum.CNAME)) {
                String alias = host.getCName().getAlias();
                Optional<Host> parent = _hRepo.findByHostName(alias);

                if (parent.isPresent()) {
                    for (ARecord r : parent.get().getRecords()) {
                        RecordResponseDto record = new RecordResponseDto();
                        record.setType(r.getHost().getType().getTypeName());
                        record.setValue(r.getIpAddress());
                        records.add(record);
                    }
                }
            } else {
                for (ARecord r : host.getRecords()) {
                    RecordResponseDto record = new RecordResponseDto();
                    record.setType(r.getHost().getType().getTypeName());
                    record.setValue(r.getIpAddress());
                    records.add(record);
                }
            }

            response.setRecords(records);
        } catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }

        return response;
    }


    /*******************************************************************************************************************
     *   @addRecord: This function is used validate and add CName alias for a specific hostname
     *   @params host: Accepts host object retrieve from db
     *   @params newHost: Accepts new host from api request body
     *   @return response: Returns a customise response object
     ******************************************************************************************************************/
    private HostResponseDto addRecord(Host host, HostRequestDto newHost) {
        LocalDateTime now = LocalDateTime.now();
        CName foundCName = _cRepo.findByHost(host);

        if (foundCName != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, Message.EXIST_CNAME, null);

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


    /*******************************************************************************************************************
     *   @addRecord: This function is used validate and add record for a specific hostname
     *   @params host: Accepts host object retrieve from db
     *   @params newHost: Accepts new host from api request body
     *   @return response: Returns a customise response object
     ******************************************************************************************************************/
    private HostResponseDto addCName(Host host, HostRequestDto newHost) {
        LocalDateTime now = LocalDateTime.now();
        List<ARecord> foundRecords = _rRepo.findByHost(host);

        if (!foundRecords.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, Message.EXIST_RECORD, null);

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
