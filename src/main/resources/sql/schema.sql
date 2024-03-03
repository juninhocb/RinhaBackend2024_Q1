SET time_zone = '-03:00';
drop table if exists transacoes;
create table transacoes(

    id int auto_increment,
    cliente_id int not null,
    valor int not null,
    tipo char(1) not null,
    descricao varchar(10) not null,
    realizada_em timestamp not null default current_timestamp,
    primary key (id)

) ENGINE=MEMORY;

drop table if exists saldos;
create table saldos(

    id int auto_increment,
    cliente_id int not null,
    limite int not null,
    valor int not null,
    primary key (id)

) ENGINE=MEMORY;