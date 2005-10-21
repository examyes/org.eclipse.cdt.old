connect 'jdbc:derby:db;create=true';

-- Strings table
create table Strings
	(id int generated always as identity primary key,
	 str varchar(256) not null);
create index StringIx on Strings(str);

-- Files table
create table Files
	(id int generated always as identity primary key,
	 name varchar(256) not null);
create index FilesIx on Files(name);

-- Names table (IASTName)
create table Names
	(nameId int not null,
	 fileId int not null,
	 offset int not null,
	 length int not null,
	 role int not null,
	 bindingId int not null);
create index NamesFileIdIx on Names(fileId);

-- Bindings table
create table Bindings
	(id int generated always as identity primary key,
	 scopeId int not null,
	 nameId int not null,
	 type int not null);
create index BindingsIx on Bindings(scopeId, nameId, type);

-- Inheritance table
create table Inheritance
	(subId int,
	 superId int);
create index InheritanceIx on Inheritance(subId);

disconnect;
exit;
