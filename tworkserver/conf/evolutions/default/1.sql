# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table all_computation (
  computation_id            bigserial not null,
  computation_name          varchar(255),
  computation_description   varchar(255),
  jobs_left                 integer,
  total_jobs                integer,
  data                      varchar(255),
  logo_image_id             bigint,
  constraint pk_all_computation primary key (computation_id))
;

create table all_data (
  data_id                   bigserial not null,
  type                      varchar(255),
  data                      varchar(255),
  constraint pk_all_data primary key (data_id))
;

create table all_jobs (
  job_id                    bigserial not null,
  job_name                  varchar(255),
  computation_id            bigint,
  input_data_id             bigint,
  function_id               varchar(255),
  output_data_id            bigint,
  retries                   integer,
  constraint pk_all_jobs primary key (job_id))
;




# --- !Downs

drop table if exists all_computation cascade;

drop table if exists all_data cascade;

drop table if exists all_jobs cascade;

