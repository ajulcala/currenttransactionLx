FROM openjdk:11
VOLUME /tmp
EXPOSE 8017
ADD ./target/currenttransaction-0.0.1-SNAPSHOT.jar currenttransaction.jar
ENTRYPOINT ["java","-jar","/currenttransaction.jar"]