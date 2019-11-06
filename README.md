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
}'

curl -X PUT "https://localhost:9200/bubing/_doc/5" -u admin:admin --insecure -H 'Content-Type: application/json' -d'
{
  "my_vector": [6.5, 7.5],
  "price": 18,
  "name": "Nurul Ferdous"
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

Start Crawl
-----------

1. Go the project root directory.

2. Then run the following ant command:

    ant clean crawl
    
Create a keystore for ES client
-------------------------------
    
    keytool -genkeypair -dname "cn=Nurul Ferdous, ou=com.dynamicguy, o=dynamicguy, c=BD" -alias NurulF -keypass zaq123 -keystore bubing.jks -storepass zaq123 -validity 180
    
Download the root-ca.pem file from bubing_elasticsearch docker container and run:
    
    keytool -importcert -file root-ca.pem -keystore bubing.jks -alias "test"
    
Key store password is: zaq123