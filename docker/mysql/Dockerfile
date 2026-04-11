FROM docker.m.daocloud.io/library/mysql:8.0.33

# Force utf8mb4 as the default charset for any client tool inside the container.
# Stops latin1-default clients from double-encoding Chinese strings when importing
# SQL files that don't carry their own SET NAMES.
COPY client-utf8.cnf /etc/mysql/conf.d/client-utf8.cnf
RUN chmod 644 /etc/mysql/conf.d/client-utf8.cnf
