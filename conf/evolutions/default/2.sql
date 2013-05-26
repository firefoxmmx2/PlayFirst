# Bar database scheme

# --- !Ups

CREATE SEQUENCE bar_id_seq;
create table bar (
	id integer NOT NULL DEFAULT nextval('bar_id_seq'),
	name	varchar(255) not null,
	summary clob,
	image blob
);
# --- !Downs

DROP TABLE bar;
DROP SEQUENCE bar_id_seq;