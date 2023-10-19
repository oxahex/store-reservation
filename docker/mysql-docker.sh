docker run -d \
--name store-reservation-mysql \
-e MYSQL_ROOT_PASSWORD="storereservation" \
-e MYSQL_USER="storereservation" \
-e MYSQL_PASSWORD="storereservation" \
-e MYSQL_DATABASE="store-reservation" \
-p 3306:3306 \
mysql:latest