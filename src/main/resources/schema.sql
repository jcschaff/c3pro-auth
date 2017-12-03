create table if not exists users (
  clientid VARCHAR(255),
  clientsecret VARCHAR(255),
  PRIMARY KEY (clientid)
);
create table if not exists userroles (
  clientid VARCHAR(255),
  roles VARCHAR(255)
);
create table if not exists usertokens (
  clientid VARCHAR(255),
  token VARCHAR(255),
  expirationdate TIMESTAMP,
  PRIMARY KEY(token)
);
create table if not exists antispamtoken (
  token VARCHAR(255)
);

