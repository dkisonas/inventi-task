# bank-statements-api for Inventi

###  

### How to run application:

    mvn clean install
    docker-compose up -d

### How to redeploy app after code changes:

    mvn clean install
    docker-compose build --no-cache app app
	docker-compose up --build --force-recreate --no-deps -d app