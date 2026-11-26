package cl.andesstay.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
@PreAuthorize("hasRole('Admin')")
public class KafkaProxyController {

    private final RestClient restClient;
    private final String auditUrl;

    public KafkaProxyController(
            @Value("${andesstay.services.audit-url:http://audit:8084}") String auditUrl) {
        this.restClient = RestClient.create();
        this.auditUrl = auditUrl;
    }

    @GetMapping("/topics")
    public ResponseEntity<Object> listTopics(HttpServletRequest request) {
        return forward(HttpMethod.GET, auditUrl + "/api/kafka/topics", null, request);
    }

    @PostMapping("/topics")
    public ResponseEntity<Object> createTopic(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return forward(HttpMethod.POST, auditUrl + "/api/kafka/topics", body, request);
    }

    @PutMapping("/topics/{topicName}/config")
    public ResponseEntity<Object> alterTopic(
            @PathVariable String topicName,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.PUT, auditUrl + "/api/kafka/topics/" + topicName + "/config", body, request);
    }

    @DeleteMapping("/topics/{topicName}")
    public ResponseEntity<Object> deleteTopic(@PathVariable String topicName, HttpServletRequest request) {
        return forward(HttpMethod.DELETE, auditUrl + "/api/kafka/topics/" + topicName, null, request);
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<Object> listConsumerGroups(HttpServletRequest request) {
        return forward(HttpMethod.GET, auditUrl + "/api/kafka/consumer-groups", null, request);
    }

    @DeleteMapping("/consumer-groups/{groupId}")
    public ResponseEntity<Object> deleteConsumerGroup(@PathVariable String groupId, HttpServletRequest request) {
        return forward(HttpMethod.DELETE, auditUrl + "/api/kafka/consumer-groups/" + groupId, null, request);
    }

    private ResponseEntity<Object> forward(HttpMethod method, String uri, Object body, HttpServletRequest request) {
        try {
            RestClient.RequestBodySpec spec = restClient.method(method).uri(uri);
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && !auth.isBlank()) {
                spec.header(HttpHeaders.AUTHORIZATION, auth);
            }
            if (body != null) {
                spec.body(body);
            }
            return spec.retrieve().toEntity(Object.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }
}