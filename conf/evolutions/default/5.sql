#-- !Ups
create sequence computer_id_seq;

create table t_computer(
	id number not null default nextval('computer_id_seq'),
	name varchar(255) not null
);

#-- !Downs
drop sequence computer_id_seq;
drop table t_computer
