# Video-app

An TikTok-like app


# Middleware and db

all tools below installed by docker
please make sure you have installed docker in your machine

| tool     | version | docker download                                              |
| -------- | ------- | ------------------------------------------------------------ |
| rabbitmq | 3.8.5   | docker pull rabbitmq:3.8.5                                   |
| redis    | 3.2     | docker pull redis:3.2                                        |
| mongodb  | latest  | docker pull mongo                                            |
| minio    | latest  | docker pull minio/minio                                      |
| nacos    | latest  | docker pull nacos/nacos-server                               |
| mysql    | 5.7     | docker pull mysql:5.7                                        |
| JDK      | 1.8     | https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html |

# Structure

```
video-app
  ├── api -- controller
  ├── common -- common tools package and configuration, interceptor
  ├── mapper -- dao, mapper of service
  ├── model -- pojos, VO, BO, MO(mongo document)
  ├── mybatis-generator
  └── service -- the services of the project
```



# How to start

1. Install all tools needed in this project

   ```shell
   # enter all this in your machine
   
   # docker install mysql
   docker pull mysql:5.7
   docker run -p 3306:3306 --name mysql -v /mydata/mysql/log:/var/log/mysql -v /mydata/mysql/data:/var/lib/mysql -v /mydata/mysql/conf:/etc/mysql -v /mydata/mysql/conf/my.cnf:/etc/mysql/mysql.conf.d/mysqld.cnf -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7
   
   # docker install redis
   docker pull redis:3.2
   docker run -p 6379:6379 --name redis -v /mydata/redis/data:/data -d redis:3.2 redis-server --appendonly yes
   
   # docker install rabbitmq
   docker pull rabbitmq:3.8.5
   docker run -d --name rabbitmq --publish 5671:5671 --publish 5672:5672 --publish 4369:4369 --publish 25672:25672 --publish 15671:15671 --publish 15672:15672 rabbitmq:3.8.5
   
   # docker install mongo
   docker pull mongo
   docker run -p 27017:27017 --name mongo -v /mydata/mongo/db:/data/db -d mongo
   
   # docker install mongo
   docker pull minio/minio
   docker run -p 9000:9000 -p 9001:9001 --name minio  -e "MINIO_ROOT_USER=root"  -e "MINIO_ROOT_PASSWORD=root123456" -v /mydata/minio/data:/data   -v /mydata/minio/config:/root/.minio   -d minio/minio server /data --address :9000 --console-address :9001
   
   # docker install nacos
   # when install nacos, please make sure your machine have enough memory. If memory not enough, you can adjust the 'JVM_XMS' and 'JVM_XMX' to 256m
   docker pull nacos/nacos-server
   docker run -e JVM_XMS=512m -e JVM_XMX=512m --env MODE=standalone --name nacos -d -p 8848:8848 nacos/nacos-server
   ```

2. Download the git project and open it in Intellj Idea, use maven to install all dependencies.

3. Change all configs in 'api/src/main/resources/application-dev.yml' and 'api/src/main/resources/application-prod.yaml' , the ip address of **MINIO** should be the public address if you deploy the project on Cloud Server.

4. Also change the configs in 'api/src/main/resources/bootstrap.yml'

5. First 'clean' and then 'install' the project in 'shortVideo(root)' :

   ![](http://8.209.98.139:9000/video-app/asdasasd.png)

6. After installing, you can find out the ".jar" file in "api/target/video-app.jar", upload it to your machine, and enter command below to your console:

   ```shell
   nohup java -jar video-app.jar >my.log 2>&1 &
   ```



# Mysql configuration

Please make sure your mysql set the encoding correctly. 

The encoding should be 'utf8mb4', if not mysql can not save emoji

You can do as below if you use docker

```shell
# first install mysql
docker pull mysql:5.7
docker run -p 3306:3306 --name mysql -v /mydata/mysql/log:/var/log/mysql -v /mydata/mysql/data:/var/lib/mysql -v /mydata/mysql/conf:/etc/mysql -v /mydata/mysql/conf/my.cnf:/etc/mysql/mysql.conf.d/mysqld.cnf -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7

# stop rm the container
docker stop mysql
docker rm mysql

# go into the mysql dir
cd /mydata/mysql
rm -rf conf
mkdir conf
cd conf
vi my.cnf

# paster the configs below and save it in the 'my.cnf'
[client]
default-character-set = utf8mb4
[mysql]
default-character-set = utf8mb4
[mysqld]
character-set-client-handshake = FALSE
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
#!includedir /etc/mysql/conf.d/
#!includedir /etc/mysql/mysql.conf.d/

# rerun the mysql in docker
docker run -p 3306:3306 --name mysql -v /mydata/mysql/log:/var/log/mysql -v /mydata/mysql/data:/var/lib/mysql -v /mydata/mysql/conf:/etc/mysql -v /mydata/mysql/conf/my.cnf:/etc/mysql/mysql.conf.d/mysqld.cnf -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7
```



# Front End

Soon....



# Demo

![](http://8.209.98.139:9000/video-app/sedvt-4qxno.gif)

<img src="http://8.209.98.139:9000/video-app/sedvt-4qxno.gif" alt="show" />
