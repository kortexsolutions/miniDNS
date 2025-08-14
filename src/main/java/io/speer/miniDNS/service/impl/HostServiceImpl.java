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

import jakarta.transaction.Transactional;
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

        TypeEnum type = TypeEnum.A.getTypeName().equalsIgnoreCase(requestHost.getType())
                ? TypeEnum.A
                : TypeEnum.CNAME;

        String hostName = requestHost.getHostname();
        String value = requestHost.getValue();

        Optional<Host> foundHost = _hRepo.findById(hostName);
        boolean validHostName = _uService.isValidHostname(hostName);

        if (type == TypeEnum.CNAME) {
            if (!validHostName || !_uService.isValidHostname(value)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        Message.ERR_FORM);
            }
            if (foundHost.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        Message.ERR_EXIST);
            }
        } else if (type == TypeEnum.A) {
            if (!validHostName || !_uService.isValidIp(value)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        Message.ERR_FORM);
            }
            if (_rRepo.findByIpAddress(value) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        Message.ERR_EXIST);
            }
        }

        Host host = foundHost.orElseGet(() -> {
            Host newHost = new Host();
            newHost.setCreatedAt(now);
            newHost.setHostName(hostName);
            newHost.setType(type);
            return _hRepo.save(newHost);
        });

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

    @Override
    @Transactional
    public boolean remove(String hostname, String type, String value) {
        Optional<Host> foundHost = _hRepo.findByHostName(hostname);
        if (foundHost.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND, null);

        try {
            Host host = foundHost.get();
            if (type.equalsIgnoreCase(TypeEnum.A.getTypeName())) {
                ARecord target = null;
                List<ARecord> records = host.getRecords();

                if (!records.isEmpty()) {
                    for (ARecord r : records) {
                        if (r.getIpAddress().equalsIgnoreCase(value)) {
                            try {
                                target = r;
                                _rRepo.delete(r);
                            } catch (Exception ex) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
                            }
                        }
                    }

                    records.remove(target);
                }

                if (records.isEmpty() || records.size() < 1) _hRepo.delete(host);
            } else {
                CName foundCName = host.getCName();
                if (foundCName != null) _cRepo.delete(foundCName);
                _hRepo.delete(host);
            }
            return true;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }
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

        if (!foundRecords.isEmpty()) {
            _hRepo.delete(host);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Message.EXIST_RECORD, null);
        }

        CName foundCName = _cRepo.findByHost(host);
        if (foundCName == null) {
            CName cName = new CName();
            cName.setHost(host);
            cName.setCreatedAt(now);

            if (this.circularChainValidation(newHost.getValue())) {
                _hRepo.delete(host);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, Message.INVL_LNK, null);
            }

            Optional<Host> link = _hRepo.findByHostName(newHost.getValue());
            cName.setAlias(link.isPresent() ? link.get().getHostName() : newHost.getValue());
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
        Set<String> visited = new HashSet<>();
        String current = value.toLowerCase();

        while (current != null) {
            if (!visited.add(current)) {
                return true;
            }

            Optional<Host> host = _hRepo.findByHostName(current);
            if (host.isEmpty() || host.get().getCName() == null) {
                return false;
            }

            current = host.get().getCName().getAlias().toLowerCase();
        }

        return false;
    }
}
