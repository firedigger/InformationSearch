Build
==================
Build project using
```
mvn clean package
```

Run
==================
To create index use argument: -index.
To search use argument: -search

Example:
```
java -jar InformSearch1-1.0-SNAPSHOT.one-jar.jar -index
```

Create Index
==================
Insert path to directory with documents. Index will be saved into index.dat

Search
==================
Insert path to index file. After it is loaded, you can type requests.
