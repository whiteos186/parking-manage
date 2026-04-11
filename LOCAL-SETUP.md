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
- `sql/parking/parking_bootstrap.sql`
- `sql/parking/parking_menu.sql`

> ⚠️ **重要：** `/docker-entrypoint-initdb.d` 里的脚本**只在 MySQL 数据卷为空时（即容器首次初始化）**自动执行一次。容器后续启动 / 重启**不会**重新跑这些脚本。
>
> 当你修改了 `parking_menu.sql`（或其他 init 脚本）需要让现有数据库吸收变更时，有两种做法：
>
> **方式 A · 推荐：手工再跑一次（保留现有业务数据）**
>
> 这个脚本是用 `insert ... where not exists` + `update ... where ...` 写法做的幂等 upsert，重复执行安全：
>
> ```bash
> docker exec -i parking-mvp-mysql mysql -uroot -p123456 -D ry_vue \
>   < sql/parking/parking_menu.sql
> ```
>
> 跑完之后，**前端要退出重新登录**才能在侧边栏看到新菜单（RuoYi 把菜单树缓存到 Redis，登录时刷新）。`admin` 用户因为有 `*:*:*` 通配符权限，自动有所有新菜单的访问权；非 admin 角色需要去`系统管理 → 角色管理`里把新菜单勾上。
>
> **方式 B · 销毁数据卷重建（会丢失所有业务数据）**
>
> 见下文"重建数据库"小节。

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
