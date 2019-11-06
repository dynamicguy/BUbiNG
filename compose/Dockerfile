FROM openjdk:slim

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp

RUN apt-get update && apt-get install -y --no-install-recommends curl bash

RUN /usr/src/myapp/ant_ivy_bootstrap.sh

CMD ["/usr/src/myapp/run.sh"]
