CREATE SCHEMA jobcrawler
    AUTHORIZATION postgres;

CREATE TABLE jobcrawler.aanvraag
(
    id uuid NOT NULL,
    aanvraag_url text NOT NULL,
    title text,
    broker text,
    aanvraag_nummer text,
    hours text,
    location text,
    posting_date text,
    about text,
    skillset text[],
    PRIMARY KEY (id)
);

ALTER TABLE jobcrawler.aanvraag
    OWNER to postgres;
