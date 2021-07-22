-- Criar extensão no banco de dados local para uso do uuid
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE keymanager.chave_pix (

    id_chave UUID NOT NULL,

	cliente_id UUID NOT NULL,
	tipo_chave varchar(15) NOT NULL,
	chave varchar(255) NOT NULL,
	tipo_conta varchar(15) NOT NULL,

	id_conta_associada int8 NOT NULL
);

ALTER TABLE keymanager.chave_pix ADD CONSTRAINT chave_pix_pkey PRIMARY KEY (id_chave);
ALTER TABLE keymanager.chave_pix ADD CONSTRAINT uk_chave_pix UNIQUE (chave);

CREATE TABLE keymanager.conta_associada (

    id_conta_associada int8 NOT NULL,

	instituicao varchar(255) NOT NULL,
	nome_titular varchar(255) NOT NULL,
	cpf_titular varchar(255) NOT NULL,
	agencia varchar(255) NOT NULL,
	numero_conta varchar(255) NOT NULL
);

ALTER TABLE keymanager.conta_associada ADD CONSTRAINT conta_associada_pkey PRIMARY KEY (id_conta_associada);
CREATE SEQUENCE keymanager.sq_conta_associada NO MINVALUE NO MAXVALUE;



-- Chave estrangeira da conta associada na chave pix
ALTER TABLE keymanager.chave_pix ADD CONSTRAINT fk_chave_pix_01 FOREIGN KEY (id_conta_associada) REFERENCES keymanager.conta_associada(id_conta_associada);