package org.acme;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Path("/")
@ApplicationScoped
public class KafkaStreaming {

    private final String LEFT_STREAM_TOPIC = "left-stream-topic";
    private final String RIGHT_STREAM_TOPIC = "right-stream-topic";
    private final String OUTER_JOIN_STREAM_OUT_TOPIC = "stream-stream-outerjoin";
    private final String PROCESSED_STREAM_OUT_TOPIC = "processed-topic";
    static final String STORE_NAME = "missing_data_store";

    @Inject
    InteractiveQueries interactiveQueries;

    @Produces
    public Topology startStreamStreamOuterJoin() {
        // build the state store that will eventually store all unprocessed items
        Map<String, String> changelogConfig = new HashMap<>();
        // override min.insync.replicas
        //changelogConfig.put("min.insyc.replicas", "1");

        StoreBuilder<KeyValueStore<String, String>> stateStore = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STORE_NAME),
                Serdes.String(),
                Serdes.String())
                //.withLoggingEnabled(changelogConfig);
                .withLoggingDisabled();

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> rightSource = builder.stream(RIGHT_STREAM_TOPIC);

        // do the outer join
        // change the value to be a mix of both streams value
        // have a moving window of 5 seconds
        // output the last value received for a specific key during the window
        // push the data to OUTER_JOIN_STREAM_OUT_TOPIC topic
        builder.stream(LEFT_STREAM_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .outerJoin(
                        rightSource,
                        new ValueJoiner<String, String, String>() {
                            @Override
                            public String apply(String leftValue, String rightValue) {
                                return "left=" + leftValue + ", right=" + rightValue;
                            }
                        },
                        JoinWindows.of(Duration.ofSeconds(5))
                )
                .groupByKey(
                )
                .reduce(((key, lastValue) -> lastValue))
                .toStream()
                .to(OUTER_JOIN_STREAM_OUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // build the streams topology
        return builder.build()
                .addSource("Source", OUTER_JOIN_STREAM_OUT_TOPIC)
                .addProcessor("StateProcessor",
                        () -> new DataProcessor(),
                        "Source"
                )
                .addSink("Sink", PROCESSED_STREAM_OUT_TOPIC, "StateProcessor")
                .addStateStore(stateStore, "StateProcessor");
    }

    @GET
    @Path("/storedata")
    public String getStoreData() {
        return interactiveQueries.getRecords();
    }
}
