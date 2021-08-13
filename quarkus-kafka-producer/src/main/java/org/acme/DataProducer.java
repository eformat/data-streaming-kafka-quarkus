package org.acme;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@ApplicationScoped
public class DataProducer {

    @Inject
    @Channel("left-emit-out")
    Emitter<String> leftEmitter;

    @Inject
    @Channel("right-emit-out")
    Emitter<String> rightEmitter;

    private static final Map<Integer, ATDData> ATD;
    private static final Map<Integer, RNData> RN;

    static {
        ATD = new HashMap<>();
        ATD.put(1, new ATDData("A", "ATD Txn - A"));
        ATD.put(2, new ATDData("B", "ATD Txn - B"));
        ATD.put(3, new ATDData("C", "ATD Txn - C"));
        ATD.put(4, new ATDData("D", "ATD Txn - D"));
        ATD.put(5, new ATDData("E", "ATD Txn - E"));
        ATD.put(6, new ATDData("F", "ATD Txn - F"));
        ATD.put(7, new ATDData("G", "ATD Txn - G"));
        ATD.put(8, new ATDData("H", "ATD Txn - H"));
    }

    static {
        RN = new HashMap<Integer, RNData>();
        RN.put(1, new RNData("A", "RN Txn - A"));
        RN.put(2, new RNData("B", "RN Txn - B"));
        RN.put(3, new RNData("C", "RN Txn - C"));
        RN.put(4, new RNData("D", "RN Txn - D"));
        RN.put(5, new RNData("E", "RN Txn - E"));
        RN.put(6, new RNData("F", "RN Txn - F"));
        RN.put(7, new RNData("G", "RN Txn - G"));
        RN.put(8, new RNData("H", "RN Txn - H"));
        RN.put(9, new RNData("I", "RN Txn - I"));
        RN.put(10, new RNData("J", "RN Txn - J"));
    }

    @GET
    @Path("/sendmanyrecords")
    public void sendManyRecords() {
        for (int i = 0; i < 10000; i++) {
            leftEmitter.send(KafkaRecord.of("A".concat(String.valueOf(i)), "ATD - txn ".concat(String.valueOf(i))));
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
            leftEmitter.send(KafkaRecord.of("A".concat(String.valueOf(i)), "RN - txn ".concat(String.valueOf(i))));
        }
    }

    @GET
    @Path("/sendfewrecords")
    public void sendFewRecords() {
        final int max = 3;
        final int min = 1;

        for (int i = 0; i < 10; i++) {
            if (ATD.containsKey(i + 1)) {
                ATDData data = ATD.get(i + 1);
                leftEmitter.send(KafkaRecord.of(data.getKey(), data.getValue()));
            }
            if (RN.containsKey(i + 1)) {
                int sleep_time = (int) (Math.random() * ((max - min) + 1)) + min;
                try {
                    Thread.sleep(sleep_time * 1000);
                } catch (InterruptedException e) {
                }
                RNData rnData = RN.get(i + 1);
                leftEmitter.send(KafkaRecord.of(rnData.getKey(), rnData.getValue()));
                rightEmitter.send(KafkaRecord.of(rnData.getKey(), rnData.getValue()));
            }
        }
    }

    @GET
    @Path("/sendoneleftrecord")
    public void sendOneLeftRecord() {
        for (int i = 0; i < 1; i++) {
            if (ATD.containsKey(i + 1)) {
                ATDData data = ATD.get(i + 1);
                leftEmitter.send(KafkaRecord.of(data.getKey(), data.getValue()));
            }
        }
    }
}
