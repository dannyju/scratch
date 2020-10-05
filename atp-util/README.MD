```
# Running with ojdbc8-12.2 driver:
export CLASSPATH=ojdbc8-12.2.0.1.jar:osdt_cert-2.0.0-141105.1046.jar:ucp-12.2.0.1.0.jar:oraclepki-12.2.0.1.jar:osdt_core-2.0.0-141105.1046.jar:util-1.0-SNAPSHOT.jar

# Running with ojdbc8-19.3.0 driver
export CLASSPATH=ojdbc8-19.3.0.0.jar:oraclepki-19.3.0.0.jar:osdt_cert-19.3.0.0.jar:osdt_core-19.3.0.0.jar:ucp-19.3.0.0.jar:util-1.0-SNAPSHOT.jar:

export DB_USER=ADMIN
export DB_PASSWORD=xxxxx
DB_URL="jdbc:oracle:thin:@dbname_high?TNS_ADMIN=/Users/test/wallet_dbname"
java atp.DbUtil
```

### Reference:
[Connect to ATP database with JDBC](https://docs.oracle.com/en/cloud/paas/atp-cloud/atpug/connect-jdbc-thin-wallet.html#GUID-5ED3C08C-1A84-4E5A-B07A-A5114951AA9E)
