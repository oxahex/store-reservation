docker network create redis-network

docker run --name store-reservation-redis \
            -p 6379:6379 \
            --network redis-network \
            -d redis:latest