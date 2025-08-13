package io.speer.miniDNS.controller;

import io.speer.miniDNS.dto.HostRequestDto;
import io.speer.miniDNS.dto.HostResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dns")
public class HostController {

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<HostResponseDto> addDNSRecord(@RequestBody HostRequestDto host) throws Exception {


        return null;
    }


    @RequestMapping(value = "/{hostname}", method = RequestMethod.GET)
    public ResponseEntity<?> resolveHostname(@PathVariable("hostname") String hostname) throws Exception {


        return null;
    }



    @RequestMapping(value = "/{hostname}/records", method = RequestMethod.GET)
    public ResponseEntity<?> resolveDNSRecords(@PathVariable("hostname") String hostname) throws Exception {

        return null;
    }

    @RequestMapping(value = "/{hostname}/records", method = RequestMethod.DELETE)
    public void deleteDNSRecord(@PathVariable("hostname") String hostname) throws Exception {

    }


}
