# Litebans-Bridge
A Velocity plugin that blocks banned players from connecting, based on LiteBans database

## Config
```toml
[database]
host = "localhost"
port = 3306
name = "litebans"
user = "litebans"
pass = "yourpassword"

[database.pool]
minimum-idle = 5
maximum-pool-size = 10
connection-timeout = 30000

[randomid]
# Copy the full value produced by "litebans reveal web" (format: 1234:abc...)
secret = ""
```

You can also put your litebans messages.yml into plugin config folder

## License
Under AGPL-3.0 