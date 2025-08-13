package io.speer.miniDNS.controller;

import io.speer.miniDNS.common.enums.TypeEnum;
import io.speer.miniDNS.dto.HostRequestDto;
import io.speer.miniDNS.dto.HostResponseDto;

import io.speer.miniDNS.service.HostService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/dns")
public class HostController {
    @Autowired
    private HostService _hService;

    private List<String> whiteList = Arrays.asList("a", "cname");

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<HostResponseDto> addDNSRecord(@RequestBody HostRequestDto host) throws Exception {
        if (!whiteList.contains(host.getType().toLowerCase()))
            throw new BadRequestException("This Host type is not supported!");

        /* save dns record base on host type */
        HostResponseDto response = _hService.save(host);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = "/{hostname}", method = RequestMethod.GET)
    public ResponseEntity<?> resolveHostname(@PathVariable("hostname") String hostname) throws Exception {


        return null;
    }



    @RequestMapping(value = "/{hostname}/records", method = RequestMethod.GET)
    public ResponseEntity<?> resolveDNSRecords(@PathVariable("hostname") String hostname) throws Exception {

        return null;
    }

    @RequestMapping(value = "/{hostname}/?", method = RequestMethod.DELETE)
    public void deleteDNSRecord(@PathVariable("hostname") String hostname,
                                @RequestParam String type,
                                @RequestParam String value) throws Exception {

    }


}
