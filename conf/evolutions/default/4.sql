#-- !Ups
create sequence user_id_seq;
create sequence address_id_seq;

create table user(
	id number not null default nextval('user_id_seq'),
	name varchar(255) not null,
	username varchar(255) not null,
	password varchar(255) not null,
	email varchar(255),
	addressid number not null
);
create table address(
	id number not null default nextval('address_id_seq'),
	province varchar(255) not null,
	city varchar(255) not null,
	country varchar(255) not null,
	street varchar(255),
	road varchar(255),
	No varchar(255)
);
#-- !Downs
drop sequence user_id_seq;
drop sequence address_id_seq;
drop table user;
drop table address;
