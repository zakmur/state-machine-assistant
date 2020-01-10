#!/bin/bash

# Environment
. ./00-environment.sh

cat << EOF | oc -n ${PROJECT_NAME} apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: hl7-to-events 
  namespace: "${PROJECT_NAME}"  
data:
  application.properties: |-
    my.message=value-1
    kafka.bootstrap-servers=${CLUSTER_NAME}-kafka-brokers:9092
    kafka.topic=${HL7_EVENTS_TOPIC_NAME}
    kafka.clientId=kafkaClientHl7ToEvents
    kafka.groupId=kafkaHl7EventsConsumerGroup
    telegram-bot.host=telegram-bot
    telegram-bot.port="8080" 
    logging.level.org.apache.camel=INFO
EOF

#./kamel run --configmap=events-to-bot ./integrations/SimpleJava.java --dev
./kamel run --configmap=hl7-to-events \
  -d camel-gson -d mvn:ca.uhn.hapi:hapi-base:2.3 -d mvn:ca.uhn.hapi:hapi-structures-v24:2.3 \
  ./integrations/HisToBot.java --dev


#./kamel run -d camel-http4 -d camel-kafka -d camel-gson \
#  -d mvn:ca.uhn.hapi:hapi-base:2.3 -d mvn:ca.uhn.hapi:hapi-structures-v24:2.3 -d camel-hl7 \
#  --configmap=events-to-bot \
#  ./integrations/HisToBot.java --dev
