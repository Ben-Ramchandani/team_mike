# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table all_data (
  data_id                   bigint not null,
  type                      varchar(255),
  data                      varchar(255),
  constraint pk_all_data primary key (data_id))
;

create sequence all_data_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists all_data;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists all_data_seq;

