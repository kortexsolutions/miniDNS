package io.speer.miniDNS.service.impl;

import io.speer.miniDNS.service.UtilityService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UtilityServiceImpl implements UtilityService {
    /* RFC 1123-compliant regex (allows subdomains, max length per label is 63 chars) */
    private static final String HOSTNAME_REGEX = "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*$";
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(HOSTNAME_REGEX);

    private InetAddressValidator validator = InetAddressValidator.getInstance();

    @Override
    public boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return validator.isValidInet4Address(ip) || validator.isValidInet6Address(ip);
    }

    @Override
    public boolean isValidHostname(String hostname) {
        return hostname != null && HOSTNAME_PATTERN.matcher(hostname).matches();
    }
}
