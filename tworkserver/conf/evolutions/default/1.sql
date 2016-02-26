# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table all_computation (
  computation_id            varchar(40) not null,
  function_name             varchar(255),
  computation_name          varchar(255),
  failed                    boolean,
  running                   boolean,
  completed                 boolean,
  customer_computation_id   varchar(40),
  jobs_left                 integer,
  input                     varchar(1024),
  logo_image_id             bigint,
  constraint pk_all_computation primary key (computation_id))
;

create table all_completed_computation (
  customer_computation_id   varchar(40) not null,
  function_name             varchar(255),
  computation_name          varchar(255),
  computation_description   varchar(1024),
  status                    integer,
  computation_id            varchar(40),
  customer_name             varchar(255),
  time_stamp                bigint,
  total_jobs                integer,
  input                     varchar(1024),
  output                    varchar(1024),
  constraint pk_all_completed_computation primary key (customer_computation_id))
;

create table all_data (
  data_id                   varchar(40) not null,
  is_file                   boolean,
  data                      bytea,
  constraint pk_all_data primary key (data_id))
;

create table all_device (
  device_id                 varchar(255) not null,
  jobs_done                 integer,
  jobs_failed               integer,
  constraint pk_all_device primary key (device_id))
;

create table all_jobs (
  job_id                    varchar(40) not null,
  job_description           varchar(255),
  parent_computation_computation_id varchar(40),
  computation_id            varchar(40),
  input_data_id             varchar(40),
  function_code_name        varchar(255),
  output_data_id            varchar(40),
  failed                    boolean,
  constraint pk_all_jobs primary key (job_id))
;

alter table all_jobs add constraint fk_all_jobs_parentComputation_1 foreign key (parent_computation_computation_id) references all_computation (computation_id);
create index ix_all_jobs_parentComputation_1 on all_jobs (parent_computation_computation_id);



# --- !Downs

drop table if exists all_computation cascade;

drop table if exists all_completed_computation cascade;

drop table if exists all_data cascade;

drop table if exists all_device cascade;

drop table if exists all_jobs cascade;

