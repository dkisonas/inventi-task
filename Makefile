.PHONY: redeploy

redeploy:
	docker-compose build --no-cache app app
	docker-compose up --build --force-recreate --no-deps -d app
