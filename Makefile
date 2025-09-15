# ==== Config ====
COMPOSE ?= docker compose
SERVICE ?= pg
DB      ?= bench
DB_USER ?= bench
APP_URL ?= http://localhost:8080
ROWS    ?= 200000   # reseed 時の投入件数（上書き可）

# .env があれば取り込む（ROWS など上書きしたい時に便利）
-include .env

# ==== Helper ====
cid = $$($(COMPOSE) ps -q $(SERVICE))

# ==== Help ====
.PHONY: help
help: ## このヘルプを表示
	@echo "Usage: make <target>\n"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ==== Docker / DB ====
.PHONY: db-up
db-up: ## DBコンテナ起動（バックグラウンド）
	$(COMPOSE) up -d $(SERVICE)

.PHONY: db-down
db-down: ## DBコンテナ停止（ボリュームは残す）
	$(COMPOSE) down

.PHONY: db-reset
db-reset: ## DB初期化やり直し（ボリューム削除→再起動→ログ確認）
	$(COMPOSE) down -v
	$(COMPOSE) up -d $(SERVICE)
	$(COMPOSE) logs -n 100 $(SERVICE)

.PHONY: db-logs
db-logs: ## DBログを追従
	$(COMPOSE) logs -f $(SERVICE)

.PHONY: db-ps
db-ps: ## docker compose ps
	$(COMPOSE) ps

.PHONY: db-wait
db-wait: ## DBがaccepting connectionsになるまで待機
	until docker exec -T $(cid) pg_isready -U $(DB_USER) -d $(DB) -h 127.0.0.1 >/dev/null; do \
		echo "waiting for postgres..."; sleep 1; \
	done; \
	echo "postgres is ready."

.PHONY: db-psql
db-psql: ## コンテナ内 psql を開く（終了は \q）
	docker exec -it $(cid) psql -U $(DB_USER) -d $(DB)

.PHONY: db-count
db-count: ## orders 件数確認
	docker exec -T $(cid) psql -U $(DB_USER) -d $(DB) -c "SELECT COUNT(*) FROM orders;"

.PHONY: db-truncate
db-truncate: ## orders を空にする（注意）
	docker exec -T $(cid) psql -U $(DB_USER) -d $(DB) -c "TRUNCATE TABLE orders;"

.PHONY: db-seed
db-seed: ## 02_seed.sql を明示実行（空なら投入。ROWS 上書き可）
	docker exec -T $(cid) psql -U $(DB_USER) -d $(DB) -v ROWS=$(ROWS) -f /docker-entrypoint-initdb.d/02_seed.sql

.PHONY: db-reseed
db-reseed: db-truncate ## TRUNCATE → 02_seed.sql で再投入（ROWS 上書き可）
	docker exec -T $(cid) psql -U $(DB_USER) -d $(DB) -v ROWS=$(ROWS) -f /docker-entrypoint-initdb.d/02_seed.sql
	make db-count

# ==== App ====
.PHONY: app-run-jdbc
app-run-jdbc:
	./gradlew bootRun --args='--spring.profiles.active=jdbc'

.PHONY: app-run-r2dbc
app-run-r2dbc:
	./gradlew bootRun --args='--spring.profiles.active=r2dbc'

.PHONY: app-clean
app-clean: ## gradle clean
	./gradlew clean

# ==== Curl（疎通確認） ====
# 例:
# make curl-orders CUSTOMER_ID=123 FROM=2025-06-01T00:00:00Z TO=2025-09-14T00:00:00Z PAGE=0 SIZE=50
CUSTOMER_ID ?= 123
FROM        ?= 2025-06-01T00:00:00Z
TO          ?= 2025-09-14T00:00:00Z
PAGE        ?= 0
SIZE        ?= 50

.PHONY: curl-orders
curl-orders: ## /orders を叩く（パラメータは変数で上書き可）
	@echo "GET $(APP_URL)/orders?customerId=$(CUSTOMER_ID)&from=$(FROM)&to=$(TO)&page=$(PAGE)&size=$(SIZE)"
	curl -s "$(APP_URL)/orders?customerId=$(CUSTOMER_ID)&from=$(FROM)&to=$(TO)&page=$(PAGE)&size=$(SIZE)" | jq '.'

