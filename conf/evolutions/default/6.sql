#-- !Ups
create sequence druid_id_seq;

create table t_druid(
	id integer not null default nextval('druid_id_seq'),
	name varchar(255) not null
);

#-- !Downs
drop sequence druid_id_seq;
drop table t_druid
