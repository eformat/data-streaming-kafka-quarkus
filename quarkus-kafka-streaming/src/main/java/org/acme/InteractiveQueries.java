package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.errors.InvalidStateStoreException;

@ApplicationScoped
public class InteractiveQueries {

    @Inject
    KafkaStreams streams;

    private final String STORE_NAME = "missing_data_store";

    public String getRecords() {
        ReadOnlyKeyValueStore<String, String> store = getStore();
        String mesg = "";
        for (KeyValueIterator<String, String> it = store.all(); it.hasNext(); ) {
            KeyValue<String, String> kv = it.next();
            mesg = mesg.concat("\n").concat("Not Processed Records -- key: " + kv.key + " and value: " + kv.value);
        }
        return mesg.concat("\n");
    }

    private ReadOnlyKeyValueStore<String, String> getStore() {
        while (true) {
            try {
                return streams.store(STORE_NAME, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
