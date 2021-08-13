# Data Streaming with Kafka and Quarkus

- https://developers.redhat.com/blog/2020/09/28/build-a-data-streaming-pipeline-using-kafka-streams-and-quarkus/

### Basic Usage

Run kafka cluster

```bash
podman-compose up -d
```

Create kafka topics

```bash
add_path /opt/kafka_2.13-2.8.0/bin
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic left-stream-topic
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic right-stream-topic
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic stream-stream-outerjoin
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic processed-topic
```

Check
```bash
kafkacat -b localhost:9092 -L
```

Watch processed topic
```bash
kafkacat -b localhost:9092 -t processed-topic -o beginning -C -f '\nKey (%K bytes): %k
Value (%S bytes): %s
Timestamp: %T
Partition: %p
Offset: %o
Headers: %h'
```

Run Streaming application
````bash
cd quarkus-kafka-streaming
mvn quarkus:dev
````

Run Producer application
````bash
cd quarkus-kafka-producer
mvn quarkus:dev
````

### Use cases

**Use case 1** - Send few records to left-stream-topic, right-stream-topic topics

Couple of records will be missing in left-stream-topic. You will notice that punctuator will process those records which have been put into state store.

```bash
curl localhost:8082/sendfewrecords
```
