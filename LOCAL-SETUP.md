# Local Setup

## Docker dependencies

Start the local MySQL and Redis services from the repository root:

```bash
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
```

Default local endpoints:

- MySQL: `127.0.0.1:13306`
- Database: `ry_vue`
- User: `root`
- Password: `123456`
- Redis: `127.0.0.1:16379`

The MySQL container initializes from these SQL files on first startup:

- `sql/ry_20260320.sql`
- `sql/quartz.sql`
- `sql/parking/parking_init.sql`

> ⚠️ Important: scripts under `/docker-entrypoint-initdb.d` run automatically only when the MySQL data volume is empty, which usually means the first initialization. Restarting containers later will not rerun them.
>
> The parking module baseline is now consolidated into `sql/parking/parking_init.sql`. If you change that script and want the database to fully reflect the latest schema and seed data, rebuild the MySQL volume and initialize from scratch.

Stop the local services:

```bash
docker compose -f docker-compose.local.yml down
```

If you need to rebuild the local database from scratch, stop the services and remove
the named MySQL volume before starting again:

```bash
docker compose -f docker-compose.local.yml down
docker volume rm parking-mvp_ry_vue_mysql_data
docker compose -f docker-compose.local.yml up -d
```
