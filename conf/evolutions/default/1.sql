# --- !Ups

create table user (userID VARCHAR(255) NOT NULL, firstName VARCHAR(40), lastName VARCHAR(40), fullName VARCHAR(80), email VARCHAR(100), avatarURL VARCHAR(255), PRIMARY KEY (userID));
create table logininfo (id BIGINT  NOT NULL AUTO_INCREMENT, providerID VARCHAR(255) NOT NULL, providerKey VARCHAR(255) NOT NULL, PRIMARY KEY (id));
create table userlogininfo (userID VARCHAR(255) NOT NULL, loginInfoId BIGINT NOT NULL);
create table passwordinfo (hasher VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, salt VARCHAR(255), loginInfoId BIGINT NOT NULL);
create table oauth2info (id BIGINT NOT NULL AUTO_INCREMENT, accesstoken VARCHAR(255) NOT NULL, tokentype VARCHAR(255), expiresin INTEGER, refreshtoken VARCHAR(255), logininfoid BIGINT NOT NULL, PRIMARY KEY (id));

# --- !Downs

drop table oauth2info;
drop table passwordinfo;
drop table userlogininfo;
drop table logininfo;
drop table user;