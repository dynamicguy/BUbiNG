BUbiNG
======

[![Build Status](https://travis-ci.org/LAW-Unimi/BUbiNG.svg?branch=master)](https://travis-ci.org/LAW-Unimi/BUbiNG)

This is the public repository of [BUbiNG](http://law.di.unimi.it/software.php#bubing), the next generation web crawler from the [Laboratory of Web Algorithmics](http://law.di.unimi.it); on the lab website you can find the  [API](http://law.di.unimi.it/software/bubing-docs/) and [configuration](http://law.di.unimi.it/software/bubing-docs/overview-summary.html#overview.description) documentation, and instructions on [how to stop](http://law.di.unimi.it/BUbiNG.html) from accessing your site.

Docker
------
We put a docker-compose.yml file for you and which equipped with a custom Dockerfile
that build and install `opendistro-knn` elasticsearch plugin. We also linked it with
a kibana container so that you can play with it.

Please issue the following command to start our two containers:

```shell script
docker-compose up --build
```
This will start the kibana in port 5601 and elasticsearch on 9200 port. You may access 
kibana at http://localhost:5601 

Default username and password is:

  - Username: admin
  - Password: admin

**Get a list of installed plugins in elasticsearch:**
```shell script
curl -XGET https://localhost:9200/_cat/plugins?v -u admin:admin --insecure
```

**Create an index:**
Please visit http://localhost:5601/app/kibana#/dev_tools/console and paste the following
code in console:
```
PUT /bubing
{
  "settings": {
    "index": {
      "codec": "KNNCodec"
    }
  },
  "mappings": {
    "properties": {
      "my_vector": {
        "type": "knn_vector"
      },
      "price": {
        "type": "integer"
      },
      "name": {
        "type": "text"
      }
    }
  }
}
```
Click on play button and your index is created.

**Push some data:**
```shell script
curl -X PUT "https://localhost:9200/bubing/_doc/1" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{
  "my_vector": [1.5, 2.5],
  "price": 10,
  "name": "Nurul"
}
'

curl -X PUT "https://localhost:9200/bubing/_doc/2" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{
  "my_vector": [2.5, 3.5],
  "price": 12,
  "name": "Ferdous"
}
'

curl -X PUT "https://localhost:9200/bubing/_doc/3" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{
  "my_vector": [3.5, 4.5],
  "price": 15,
  "name": "Dynamic"
}
'

curl -X PUT "https://localhost:9200/bubing/_doc/4" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{
  "my_vector": [5.5, 6.5],
  "price": 17,
  "name": "Guy"
}
'
```

**Search for a KNN query:**
```shell script
curl -X POST "https://localhost:9200/bubing/_search" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{"size" : 10,
 "query": {
  "knn": {
   "my_vector": {
     "vector": [3, 4],
     "k": 2
   }
  }
 }
}
'
```

**If everything goes well you would see a response like this:**
```json
{
  "took": 158,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 2,
      "relation": "eq"
    },
    "max_score": 1.4142135,
    "hits": [
      {
        "_index": "bubing",
        "_type": "_doc",
        "_id": "2",
        "_score": 1.4142135,
        "_source": {
          "my_vector": [
            2.5,
            3.5
          ],
          "price": 12,
          "name": "Ferdous"
        }
      },
      {
        "_index": "bubing",
        "_type": "_doc",
        "_id": "3",
        "_score": 1.4142135,
        "_source": {
          "my_vector": [
            3.5,
            4.5
          ],
          "price": 15,
          "name": "Dynamic"
        }
      }
    ]
  }
}
```

Enjoy!

-server -Xss256K -Xms2G -XX:+UseNUMA -Djavax.net.ssl.sessionCacheSize=8192 -XX:+UseTLAB -XX:+ResizeTLAB -XX:NewRatio=4 -XX:MaxTenuringThreshold=15 -XX:+CMSParallelRemarkEnabled -verbose:gc -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -Djava.rmi.server.hostname=0.0.0.0 -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=0.0.0.0 -Dlogback.configurationFile=./bubing-logback.xml -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dfile.encoding=UTF-8 
-classpath ./build:./jarscompile/activation.jar:./jarscompile/animal-sniffer-annotations.jar:./jarscompile/checker-qual.jar:./jarscompile/classworlds-boot.jar:./jarscompile/classworlds.jar:./jarscompile/commons-beanutils.jar:./jarscompile/commons-cli.jar:./jarscompile/commons-codec.jar:./jarscompile/commons-collections.jar:./jarscompile/commons-configuration.jar:./jarscompile/commons-io.jar:./jarscompile/commons-lang.jar:./jarscompile/commons-lang3.jar:./jarscompile/commons-logging.jar:./jarscompile/commons-math3-javadoc.jar:./jarscompile/commons-math3.jar:./jarscompile/dnsjava-javadoc.jar:./jarscompile/dnsjava.jar:./jarscompile/dsiutils-javadoc.jar:./jarscompile/dsiutils.jar:./jarscompile/error_prone_annotations.jar:./jarscompile/failureaccess.jar:./jarscompile/fastutil-javadoc.jar:./jarscompile/fastutil.jar:./jarscompile/guava-javadoc.jar:./jarscompile/guava.jar:./jarscompile/httpclient-javadoc.jar:./jarscompile/httpclient.jar:./jarscompile/httpcore-javadoc.jar:./jarscompile/httpcore.jar:./jarscompile/j2objc-annotations.jar:./jarscompile/jai4j-javadoc.jar:./jarscompile/jai4j.jar:./jarscompile/javacc.jar:./jarscompile/javax.mail.jar:./jarscompile/jcl-over-slf4j-javadoc.jar:./jarscompile/jcl-over-slf4j.jar:./jarscompile/jcli.jar:./jarscompile/je-javadoc.jar:./jarscompile/je.jar:./jarscompile/jericho-html-dev.jar:./jarscompile/jgroups.jar:./jarscompile/jline.jar:./jarscompile/jmxterm.jar:./jarscompile/jsap.jar:./jarscompile/jsr305.jar:./jarscompile/jung-algorithms.jar:./jarscompile/jung-api.jar:./jarscompile/jung-io.jar:./jarscompile/junit.jar:./jarscompile/listenablefuture.jar:./jarscompile/log4j-over-slf4j-javadoc.jar:./jarscompile/log4j-over-slf4j.jar:./jarscompile/logback-classic-javadoc.jar:./jarscompile/logback-classic.jar:./jarscompile/logback-core.jar:./jarscompile/mx4j-tools.jar:./jarscompile/mx4j.jar:./jarscompile/pojo-mbean-javadoc.jar:./jarscompile/pojo-mbean.jar:./jarscompile/slf4j-api.jar:./jarscompile/sux4j-javadoc.jar:./jarscompile/sux4j.jar:./jarscompile/umlgraph-javadoc.jar:./jarscompile/umlgraph.jar:./jarscompile/webgraph-big-javadoc.jar:./jarscompile/webgraph-big.jar:./jarscompile/webgraph-javadoc.jar:./jars/compile/webgraph.jar:/Library/Java/JavaVirtualMachines/jdk-11.0.3.jdk/Contents/Home/lib/jrt-fs.jar it.unimi.di.law.bubing.Agent 
-h 0.0.0.0 -P ./folklore.properties -g folklore agent -n