package cl.andesstay.audit.web;

import cl.andesstay.audit.kafka.admin.KafkaAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador REST de administración de Kafka.
 * Endpoints bajo /api/kafka (enrutados desde el BFF en el puerto 8080 con validación JWT).
 */
@RestController
@RequestMapping("/api/kafka")
@PreAuthorize("hasRole('Admin')")
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaAdminController {

    private final KafkaAdminService adminService;

    public KafkaAdminController(KafkaAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/topics")
    public ResponseEntity listTopics() throws Exception {
        return ResponseEntity.ok(adminService.listTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<Void> createTopic(@RequestBody Map<String, Object> body) throws Exception {
        String name = String.valueOf(body.get("name"));
        Integer partitions = toInteger(body.get("partitions"));
        Integer replicationFactor = toInteger(body.get("replicationFactor"));
        Map<String, String> config = toStringMap(body.get("config"));
        adminService.createTopic(name, partitions, replicationFactor, config);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/topics/{topicName}/config")
    public ResponseEntity<Void> alterTopic(@PathVariable String topicName,
                                           @RequestBody Map<String, Object> body) throws Exception {
        Map<String, String> config = toStringMap(body.get("config"));
        Integer partitions = toInteger(body.get("partitions"));
        adminService.alterTopic(topicName, config, partitions);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/topics/{topicName}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String topicName) throws Exception {
        adminService.deleteTopic(topicName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity listConsumerGroups() throws Exception {
        return ResponseEntity.ok(adminService.listConsumerGroups());
    }

    @DeleteMapping("/consumer-groups/{groupId}")
    public ResponseEntity<Void> deleteConsumerGroup(@PathVariable String groupId) throws Exception {
        adminService.deleteConsumerGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    private Integer toInteger(Object value) {
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    private Map<String, String> toStringMap(Object raw) {
        if (!(raw instanceof Map rawMap)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        rawMap.forEach((k, v) -> {
            if (k != null && v != null) {
                result.put(k.toString(), v.toString());
            }
        });
        return result;
    }
}