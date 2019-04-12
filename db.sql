create database db;
use db;
create table host
(
	id INT PRIMARY KEY auto_increment,
    name varchar(30) not null,
    ip varchar(30) not null,
    mac varchar(30) not null,
    revision varchar(200) null,
    serial varchar(30) null,
    model varchar(30) null,
    build varchar(30) not null,
    floor varchar(30) null,
    upswitch varchar(30) null,
    postop varchar(20) null,
    posleft varchar(20) null
);
create table buildings
(
	id int primary key auto_increment,
	name varchar(30),
    addr varchar(30)
);
create table floors
(
	id int primary key auto_increment,
	build varchar(30),
    floor varchar(30)
);
