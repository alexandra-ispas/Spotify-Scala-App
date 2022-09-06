use spotify_db;

drop table songs_list;
drop table history;
drop table song;
drop table artist;
drop table playlist;
drop table user;
drop table parser;

create table artist (
    id int AUTO_INCREMENT,
    name varchar(100) unique,
    no_songs int,
    constraint pk_artist_id primary key (id)
);

create table song (
    id int not null AUTO_INCREMENT,
    acousticness double,
    danceability double,
    duration int,
    energy double,
    instrumentalness double,
    song_key int,
    liveness double,
    loudness double,
    song_mode int,
    speechiness double,
    tempo double,
    time_signature double,
    valence double,
    target int,
    name varchar(152) unique,
    artist_id int,
    times_played int,
    constraint pk_song_id primary key (id),
    constraint fk_song_artist foreign key (artist_id) references artist(id)
);

create table user (
      id int AUTO_INCREMENT,
      username varchar(32) unique,
      constraint pk_user_id primary key (id)
);

create table history (
    user_id int,
    song_id int,
    constraint fk_history_user foreign key (user_id) references user(id),
    constraint fk_history_song foreign key (song_id) references song(id)
);

create table playlist (
    id int AUTO_INCREMENT,
    user_id int,
    name varchar(32),
    constraint pk_playlist_id primary key (id),
    constraint fk_playlist_user foreign key (user_id) references user(id),
    constraint uq_entry unique (user_id, name)
);

create table songs_list (
    playlist_id int AUTO_INCREMENT,
    song_id int not null,
    constraint fk_songs_playlist foreign key (playlist_id) references playlist(id),
    constraint fb_songs_song foreign key (song_id) references song(id),
    constraint uq_songs unique (playlist_id, song_id)
);

create table parser
(
    acousticness double,
    danceability double,
    duration int,
    energy double,
    instrumentalness double,
    key_ int,
    liveness double,
    loudness double,
    song_mode int,
    speechiness double,
    tempo double,
    time_signature double,
    valence double,
    target int,
    song_title varchar(152),
    artist varchar(232));

load data infile '/mariadb-volume/data.csv'
    into table parser
    fields terminated by ';'
    lines terminated by '\r\n'
    (acousticness,
     danceability,
     duration,
     energy,
    instrumentalness,
     key_,
     liveness,
     loudness,
     song_mode,
     speechiness,
     tempo,
    time_signature,
     valence,
     target,
     song_title,
     artist);

insert into artist (name, no_songs)
    (select distinct p.artist, 0
     from parser p
     group by p.artist);

insert into song (acousticness,
                  danceability,
                  duration,
                  energy,
                  instrumentalness,
                  song_key,
                  liveness,
                  loudness,
                  song_mode,
                  speechiness,
                  tempo,
                  time_signature,
                  valence,
                  target,
                  name,
                  artist_id,
                  times_played)
select distinct p.acousticness,p.danceability,
       p.duration,p.energy,p.instrumentalness,
       p.key_,p.liveness,p.loudness,p.song_mode,
       p.speechiness,p.tempo,p.time_signature,
       p.valence,p.target,p.song_title,
       (select id from artist where name= p.artist), 0
from parser p
group by p.song_title;

update
    artist t
set
    no_songs = (select count(*)
                from song s
                where s.artist_id = (select a.id
                                   from artist a
                                   where a.name=t.name))
where no_songs = 0;

insert into user values (1, "root");