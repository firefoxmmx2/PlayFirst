#-- !Ups
create sequence author_id_seq;
create sequence book_id_seq;

create table authors(
  id number not null default nextval('author_id_seq'),
  firstName varchar(255) not null,
  lastName varchar(255) not null,
  email varchar(255)
);

create table book(
  id number not null default nextval('book_id_seq'),
  title varchar(255),
  author_id number not null,
  coAuthorId number
);
#-- !Downs
drop sequence author_id_seq;
drop sequence book_id_seq;
drop table authors;
drop table book;
