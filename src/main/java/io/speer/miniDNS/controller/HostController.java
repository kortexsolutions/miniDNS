package io.speer.miniDNS.controller;

import io.speer.miniDNS.common.Message;
import io.speer.miniDNS.dto.HostListResponseDto;
import io.speer.miniDNS.dto.HostRequestDto;
import io.speer.miniDNS.dto.HostResolveResponseDto;
import io.speer.miniDNS.dto.HostResponseDto;
import io.speer.miniDNS.service.HostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/dns")
public class HostController {
    @Autowired
    private HostService _hService;
    private List<String> whiteList = Arrays.asList("a", "cname");

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<HostResponseDto> addDNSRecord(@RequestBody HostRequestDto host) {
        if (!whiteList.contains(host.getType().toLowerCase()))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, Message.NOT_ALLOWED);

        try {
            /* save dns record base on host type */
            HostResponseDto response = _hService.save(host);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{hostname}", method = RequestMethod.GET)
    public ResponseEntity<HostResolveResponseDto> resolveHostname(@PathVariable("hostname") String hostname) {
        if (hostname == null || hostname.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, Message.NULL_VALUE);

        try {
            return new ResponseEntity<>(_hService.resolve(hostname), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{hostname}/records", method = RequestMethod.GET)
    public ResponseEntity<HostListResponseDto> listDNSRecords(@PathVariable("hostname") String hostname) {
        if (hostname == null || hostname.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, Message.NULL_VALUE);

        try {
            return new ResponseEntity<>(_hService.records(hostname), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{hostname}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeDSRecord(@PathVariable("hostname") String hostname,
                                            @RequestParam String type,
                                            @RequestParam String value) {
        try {
            if (hostname == null || type == null || value == null)
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, Message.NULL_VALUE);

            return new ResponseEntity<>(_hService.remove(hostname, type, value), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }
}
