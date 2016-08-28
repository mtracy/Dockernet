FROM openjdk:7
COPY . /myapp
WORKDIR /myapp/src
RUN javac /myapp/src/Dockernode.java
CMD ["java", "Dockernode", "http://127.0.0.1:8199"]
