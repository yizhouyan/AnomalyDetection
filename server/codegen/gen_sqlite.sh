#!/usr/bin/env bash
rm -rf jars/
rm sqlite/*jar
mkdir -p jars
wget https://repo.maven.apache.org/maven2/org/jooq/jooq/3.8.4/jooq-3.8.4.jar -O jars/jooq-3.8.4.jar
wget https://repo.maven.apache.org/maven2/org/jooq/jooq-meta/3.8.4/jooq-meta-3.8.4.jar -O jars/jooq-meta-3.8.4.jar
wget https://repo.maven.apache.org/maven2/org/jooq/jooq-codegen/3.8.4/jooq-codegen-3.8.4.jar -O jars/jooq-codegen-3.8.4.jar
wget https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.15.1/sqlite-jdbc-3.15.1.jar -O sqlite/sqlite-jdbc-3.15.1.jar

cat ./sqlite/createDb.sql | sqlite3 anomalydetection.db  &&
cat ./sqlite/createDb.sql | sqlite3 anomalydetection_test.db  &&
java -classpath jars/jooq-3.8.4.jar:jars/jooq-meta-3.8.4.jar:jars/jooq-codegen-3.8.4.jar:sqlite/sqlite-jdbc-3.15.1.jar:. org.jooq.util.GenerationTool sqlite/library.xml &&
mv anomalydetection.db ../
mv anomalydetection_test.db ../
chmod a+wrx ../anomalydetection.db
chmod a+wrx ../anomalydetection_test.db

# clear old modeldb mongodb data and start mongodb server
#mongo anomalydetection_metadata --eval "db.dropDatabase()"
#mkdir -p mongodb
#mongod --dbpath mongodb &
