package cl.andesstay.audit.kafka.admin;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaAdminService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private AdminClient adminClient() {
        return AdminClient.create(Map.of("bootstrap.servers", bootstrapServers));
    }

    // ----------------------------------------------------- TÓPICOS -----------------------------------------------------

    public List listTopics() throws Exception {
        try (AdminClient admin = adminClient()) {
            Set<String> names = admin.listTopics().names().get();
            if (names.isEmpty()) {
                return List.of();
            }
            DescribeTopicsResult describe = admin.describeTopics(names);
            Map<String, TopicDescription> descriptions = describe.allTopicNames().get();

            List result = new ArrayList();
            for (String name : names) {
                TopicDescription td = descriptions.get(name);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", name);
                int partitions = (td != null && td.partitions() != null) ? td.partitions().size() : 0;
                item.put("partitions", partitions);
                item.put("replicationFactor", replicationFactor(td));
                item.put("configs", topicConfigs(admin, name));
                result.add(item);
            }
            return result;
        }
    }

    private int replicationFactor(TopicDescription td) {
        if (td == null || td.partitions() == null || td.partitions().isEmpty()) {
            return 0;
        }
        Set<Integer> factors = new HashSet<>();
        for (TopicPartitionInfo p : td.partitions()) {
            factors.add(p.replicas().size());
        }
        return factors.stream().max(Integer::compare).orElse(0);
    }

    private Map<String, String> topicConfigs(AdminClient admin, String topicName) throws Exception {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        DescribeConfigsResult describeConfigs = admin.describeConfigs(List.of(resource));
        Map<ConfigResource, Config> all = describeConfigs.all().get();
        Map<String, String> configs = new LinkedHashMap<>();
        Config cfg = all.get(resource);
        if (cfg != null) {
            for (ConfigEntry entry : cfg.entries()) {
                configs.put(entry.name(), entry.value());
            }
        }
        return configs;
    }

    public void alterTopic(String topicName, Map<String, String> config, Integer newPartitions) throws Exception {
        try (AdminClient admin = adminClient()) {
            if (config != null && !config.isEmpty()) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                List<AlterConfigOp> ops = config.entrySet().stream()
                        .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                        .collect(Collectors.toList());
                admin.incrementalAlterConfigs(Map.of(resource, ops)).all().get();
            }
            if (newPartitions != null && newPartitions > 0) {
                admin.createPartitions(Map.of(topicName, NewPartitions.increaseTo(newPartitions))).all().get();
            }
        }
    }

    public void createTopic(String topicName, Integer partitions, Integer replicationFactor,
                            Map<String, String> config) throws Exception {
        try (AdminClient admin = adminClient()) {
            NewTopic newTopic = new NewTopic(topicName,
                    partitions != null ? partitions : 1,
                    replicationFactor != null ? replicationFactor.shortValue() : 1);
            if (config != null && !config.isEmpty()) {
                newTopic.configs(config);
            }
            admin.createTopics(List.of(newTopic)).all().get();
        }
    }

    public void deleteTopic(String topicName) throws Exception {
        try (AdminClient admin = adminClient()) {
            admin.deleteTopics(List.of(topicName)).all().get();
        }
    }

    // ----------------------------------------------------- CONSUMER GROUPS -----------------------------------------------------

    public List listConsumerGroups() throws Exception {
        try (AdminClient admin = adminClient()) {
            Collection<ConsumerGroupListing> groups = admin.listConsumerGroups().all().get();
            if (groups.isEmpty()) {
                return List.of();
            }
            List<String> groupIds = groups.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
            Map<String, ConsumerGroupDescription> descriptions = admin.describeConsumerGroups(groupIds).all().get();

            List result = new ArrayList();
            for (String groupId : groupIds) {
                ConsumerGroupDescription desc = descriptions.get(groupId);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("groupId", groupId);
                item.put("state", desc != null && desc.state() != null ? desc.state().toString() : "UNKNOWN");

                List members = new ArrayList();
                if (desc != null && desc.members() != null) {
                    for (MemberDescription m : desc.members()) {
                        Map<String, Object> member = new LinkedHashMap<>();
                        member.put("clientId", m.clientId());
                        member.put("host", m.host());
                        member.put("assignment",
                                m.assignment() != null ? m.assignment().topicPartitions().toString() : "");
                        members.add(member);
                    }
                }
                item.put("memberCount", members.size());
                item.put("members", members);

                Map<String, Object> offsets = groupOffsetsAndLag(admin, groupId);
                item.put("offsets", offsets.get("offsets"));
                item.put("totalLag", offsets.get("totalLag"));
                result.add(item);
            }
            return result;
        }
    }

    private Map<String, Object> groupOffsetsAndLag(AdminClient admin, String groupId) throws Exception {
        ListConsumerGroupOffsetsResult offsetsResult = admin.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, OffsetAndMetadata> current =
                offsetsResult.partitionsToOffsetAndMetadata().get();

        if (current.isEmpty()) {
            return Map.of("offsets", List.of(), "totalLag", 0L);
        }

        Map<TopicPartition, OffsetSpec> latestSpecs = new HashMap<>();
        for (TopicPartition tp : current.keySet()) {
            latestSpecs.put(tp, OffsetSpec.latest());
        }
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest = admin.listOffsets(latestSpecs).all().get();

        List detail = new ArrayList();
        long totalLag = 0;
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : current.entrySet()) {
            TopicPartition tp = entry.getKey();
            long currentOffset = entry.getValue() != null ? entry.getValue().offset() : 0L;
            long latestOffset = latest.containsKey(tp) ? latest.get(tp).offset() : currentOffset;
            long lag = Math.max(0L, latestOffset - currentOffset);
            totalLag += lag;

            Map<String, Object> d = new LinkedHashMap<>();
            d.put("topic", tp.topic());
            d.put("partition", tp.partition());
            d.put("currentOffset", currentOffset);
            d.put("latestOffset", latestOffset);
            d.put("lag", lag);
            detail.add(d);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offsets", detail);
        result.put("totalLag", totalLag);
        return result;
    }

    public void deleteConsumerGroup(String groupId) throws Exception {
        try (AdminClient admin = adminClient()) {
            admin.deleteConsumerGroups(List.of(groupId)).all().get();
        }
    }
}