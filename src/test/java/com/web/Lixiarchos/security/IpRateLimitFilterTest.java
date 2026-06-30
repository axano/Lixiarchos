package com.web.Lixiarchos.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpRateLimitFilterTest {

    private IpRateLimitFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new IpRateLimitFilter();
        setField("maxRequests", 3);
        setField("windowSeconds", 60L);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = IpRateLimitFilter.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(filter, value);
    }

    private MockHttpServletRequest requestFrom(String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(ip);
        return req;
    }

    @Test
    void firstRequest_passesThrough() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = requestFrom("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNotEquals(429, res.getStatus());
    }

    @Test
    void requestsUpToLimit_allPassThrough() throws Exception {
        String ip = "10.0.0.2";
        for (int i = 0; i < 3; i++) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(requestFrom(ip), res, mock(FilterChain.class));
            assertNotEquals(429, res.getStatus(), "Request " + (i + 1) + " should not be rate-limited");
        }
    }

    @Test
    void requestOverLimit_returns429AndBlocksChain() throws Exception {
        String ip = "10.0.0.3";
        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(requestFrom(ip), new MockHttpServletResponse(), mock(FilterChain.class));
        }

        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(requestFrom(ip), res, chain);

        assertEquals(429, res.getStatus());
        assertEquals("Slow down buddy!", res.getContentAsString());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void differentIps_haveIndependentCounters() throws Exception {
        String ip1 = "10.0.0.4";
        String ip2 = "10.0.0.5";

        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(requestFrom(ip1), new MockHttpServletResponse(), mock(FilterChain.class));
        }

        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilterInternal(requestFrom(ip2), res, chain);

        verify(chain).doFilter(any(), any());
        assertNotEquals(429, res.getStatus());
    }

    @Test
    void windowReset_countersResetAfterWindowExpires() throws Exception {
        // windowSeconds=0 means every request's window has already expired,
        // so the counter resets to 0 before each increment → count is always 1
        setField("windowSeconds", 0L);
        String ip = "10.0.0.6";

        for (int i = 0; i < 10; i++) {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(requestFrom(ip), res, chain);

            assertNotEquals(429, res.getStatus(), "Request " + (i + 1) + " should pass after window reset");
            verify(chain).doFilter(any(), any());
        }
    }

    @Test
    void maxRequestsOfOne_secondRequestIsBlocked() throws Exception {
        setField("maxRequests", 1);
        String ip = "10.0.0.7";

        filter.doFilterInternal(requestFrom(ip), new MockHttpServletResponse(), mock(FilterChain.class));

        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilterInternal(requestFrom(ip), res, mock(FilterChain.class));

        assertEquals(429, res.getStatus());
    }
}
