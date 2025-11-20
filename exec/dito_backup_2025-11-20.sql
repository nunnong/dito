--
-- PostgreSQL database dump
--

\restrict 2qJZgej9hQ6eMKPkIqBIihEK1NlEkBcri8qIFwwkIz5kgAaprGDuSQe75a6ACE6

-- Dumped from database version 16.10
-- Dumped by pg_dump version 16.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: dito
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO dito;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: dito
--

COMMENT ON SCHEMA public IS '';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_usage_log; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.app_usage_log (
    log_id bigint NOT NULL,
    user_id bigint NOT NULL,
    app_name character varying(100) NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL,
    duration_seconds integer NOT NULL,
    session_count integer,
    time_slot character varying(20) NOT NULL,
    usage_date date NOT NULL
);


ALTER TABLE public.app_usage_log OWNER TO dito;

--
-- Name: app_usage_log_log_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.app_usage_log_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.app_usage_log_log_id_seq OWNER TO dito;

--
-- Name: app_usage_log_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.app_usage_log_log_id_seq OWNED BY public.app_usage_log.log_id;


--
-- Name: content_cache; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.content_cache (
    cache_id bigint NOT NULL,
    content_id character varying(255) NOT NULL,
    title character varying(500),
    channel_name character varying(255),
    content_type character varying(20) NOT NULL,
    education_score numeric(3,2),
    addiction_score numeric(3,2),
    cached_at timestamp without time zone,
    expires_at timestamp without time zone NOT NULL
);


ALTER TABLE public.content_cache OWNER TO dito;

--
-- Name: content_cache_cache_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.content_cache_cache_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.content_cache_cache_id_seq OWNER TO dito;

--
-- Name: content_cache_cache_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.content_cache_cache_id_seq OWNED BY public.content_cache.cache_id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO dito;

--
-- Name: group; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public."group" (
    id bigint NOT NULL,
    group_name character varying(100) NOT NULL,
    invite_code character varying(4) NOT NULL,
    period integer NOT NULL,
    start_date date,
    end_date date,
    goal_description text,
    penalty_description text,
    status character varying(20) DEFAULT 'pending'::character varying NOT NULL,
    total_bet_coins integer NOT NULL,
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public."group" OWNER TO dito;

--
-- Name: group_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.group_id_seq OWNER TO dito;

--
-- Name: group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.group_id_seq OWNED BY public."group".id;


--
-- Name: group_participant; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.group_participant (
    user_id bigint NOT NULL,
    group_id bigint NOT NULL,
    role character varying(10) NOT NULL,
    bet_coins integer NOT NULL,
    rank integer NOT NULL,
    avg_screen_time numeric(5,2) NOT NULL,
    joined_at timestamp without time zone NOT NULL
);


ALTER TABLE public.group_participant OWNER TO dito;

--
-- Name: item; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.item (
    id bigint NOT NULL,
    type character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    price integer NOT NULL,
    img_url character varying(255),
    on_sale boolean DEFAULT true NOT NULL
);


ALTER TABLE public.item OWNER TO dito;

--
-- Name: TABLE item; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON TABLE public.item IS '아이템';


--
-- Name: COLUMN item.type; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.item.type IS '아이템 타입';


--
-- Name: COLUMN item.name; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.item.name IS '아이템 이름';


--
-- Name: COLUMN item.price; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.item.price IS '아이템 가격';


--
-- Name: COLUMN item.img_url; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.item.img_url IS '아이템 이미지 URL';


--
-- Name: COLUMN item.on_sale; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.item.on_sale IS '판매 여부';


--
-- Name: item_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.item_id_seq OWNER TO dito;

--
-- Name: item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.item_id_seq OWNED BY public.item.id;


--
-- Name: mission; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.mission (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    mission_type character varying(20) NOT NULL,
    mission_text text NOT NULL,
    coin_reward integer NOT NULL,
    trigger_time timestamp without time zone,
    duration_seconds integer NOT NULL,
    target_app character varying(100) NOT NULL,
    stat_change_self_care integer NOT NULL,
    stat_change_focus integer NOT NULL,
    stat_change_sleep integer NOT NULL,
    status character varying(20) NOT NULL,
    prompt text NOT NULL
);


ALTER TABLE public.mission OWNER TO dito;

--
-- Name: COLUMN mission.trigger_time; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.mission.trigger_time IS '미션 시작 시간';


--
-- Name: mission_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.mission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mission_id_seq OWNER TO dito;

--
-- Name: mission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.mission_id_seq OWNED BY public.mission.id;


--
-- Name: mission_result; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.mission_result (
    id bigint NOT NULL,
    mission_id bigint NOT NULL,
    result character varying(20) NOT NULL,
    completed_at timestamp without time zone,
    feedback text DEFAULT ''::text
);


ALTER TABLE public.mission_result OWNER TO dito;

--
-- Name: mission_result_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.mission_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mission_result_id_seq OWNER TO dito;

--
-- Name: mission_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.mission_result_id_seq OWNED BY public.mission_result.id;


--
-- Name: report; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.report (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    report_overview text,
    advice text,
    mission_success_rate integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    insights jsonb,
    status character varying(20),
    report_date date DEFAULT CURRENT_DATE
);


ALTER TABLE public.report OWNER TO dito;

--
-- Name: TABLE report; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON TABLE public.report IS 'AI 리포트 데이터 테이블';


--
-- Name: COLUMN report.user_id; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.user_id IS 'user 테이블 FK';


--
-- Name: COLUMN report.report_overview; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.report_overview IS '리포트 요약';


--
-- Name: COLUMN report.advice; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.advice IS 'AI 조언(Advice)';


--
-- Name: COLUMN report.mission_success_rate; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.mission_success_rate IS '미션 성공률(%)';


--
-- Name: COLUMN report.insights; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.insights IS '인사이트 목록 (JSONB)';


--
-- Name: COLUMN report.report_date; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.report.report_date IS '리포트 대상 날짜';


--
-- Name: report_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.report_id_seq OWNER TO dito;

--
-- Name: report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.report_id_seq OWNED BY public.report.id;


--
-- Name: status; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.status (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    self_care_stat integer DEFAULT 0,
    focus_stat integer DEFAULT 0,
    sleep_stat integer DEFAULT 0,
    total_stat integer DEFAULT 0
);


ALTER TABLE public.status OWNER TO dito;

--
-- Name: status_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.status_id_seq OWNER TO dito;

--
-- Name: status_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.status_id_seq OWNED BY public.status.id;


--
-- Name: user; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public."user" (
    id bigint NOT NULL,
    personal_id character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    nickname character varying(50) NOT NULL,
    birth date NOT NULL,
    gender character varying(10) NOT NULL,
    job character varying(255) DEFAULT 'ETC'::character varying NOT NULL,
    frequency character varying(50) DEFAULT 'NORMAL'::character varying NOT NULL,
    coin_balance integer DEFAULT 0 NOT NULL,
    last_login_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fcm_token character varying(255)
);


ALTER TABLE public."user" OWNER TO dito;

--
-- Name: COLUMN "user".personal_id; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".personal_id IS '로그인용 개인 아이디';


--
-- Name: COLUMN "user".password; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".password IS '암호화된 비밀번호 (Bcrypt 등)';


--
-- Name: COLUMN "user".nickname; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".nickname IS '사용자 닉네임';


--
-- Name: COLUMN "user".birth; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".birth IS '생년월일';


--
-- Name: COLUMN "user".gender; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".gender IS '성별 (Enum: MALE, FEMALE, ETC)';


--
-- Name: COLUMN "user".job; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".job IS '직업 (Enum: STUDENT, WORKER, ETC)';


--
-- Name: COLUMN "user".frequency; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".frequency IS '앱 사용 빈도 (Enum: LOW, NORMAL, HIGH)';


--
-- Name: COLUMN "user".coin_balance; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".coin_balance IS '코인 잔액';


--
-- Name: COLUMN "user".last_login_at; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".last_login_at IS '마지막 로그인 시각';


--
-- Name: COLUMN "user".created_at; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".created_at IS '회원 가입일';


--
-- Name: COLUMN "user".fcm_token; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public."user".fcm_token IS '로그인 시 갱신되는 FCM 토큰 (푸시 알림용)';


--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_id_seq OWNER TO dito;

--
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.user_id_seq OWNED BY public."user".id;


--
-- Name: user_item; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.user_item (
    item_id bigint NOT NULL,
    user_id bigint NOT NULL,
    purchased_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_equipped boolean DEFAULT false NOT NULL
);


ALTER TABLE public.user_item OWNER TO dito;

--
-- Name: TABLE user_item; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON TABLE public.user_item IS '유저 - 아이템 매핑';


--
-- Name: COLUMN user_item.item_id; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.user_item.item_id IS '아이템 ID';


--
-- Name: COLUMN user_item.user_id; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.user_item.user_id IS '유저 ID';


--
-- Name: COLUMN user_item.purchased_at; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.user_item.purchased_at IS '구매 일시';


--
-- Name: COLUMN user_item.is_equipped; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON COLUMN public.user_item.is_equipped IS '착용 여부';


--
-- Name: weekly_goal; Type: TABLE; Schema: public; Owner: dito
--

CREATE TABLE public.weekly_goal (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    goal text DEFAULT '목표를 설정해주세요.'::text NOT NULL,
    start_at timestamp without time zone NOT NULL,
    is_active boolean DEFAULT false
);


ALTER TABLE public.weekly_goal OWNER TO dito;

--
-- Name: weekly_goal_id_seq; Type: SEQUENCE; Schema: public; Owner: dito
--

CREATE SEQUENCE public.weekly_goal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.weekly_goal_id_seq OWNER TO dito;

--
-- Name: weekly_goal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dito
--

ALTER SEQUENCE public.weekly_goal_id_seq OWNED BY public.weekly_goal.id;


--
-- Name: app_usage_log log_id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.app_usage_log ALTER COLUMN log_id SET DEFAULT nextval('public.app_usage_log_log_id_seq'::regclass);


--
-- Name: content_cache cache_id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.content_cache ALTER COLUMN cache_id SET DEFAULT nextval('public.content_cache_cache_id_seq'::regclass);


--
-- Name: group id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."group" ALTER COLUMN id SET DEFAULT nextval('public.group_id_seq'::regclass);


--
-- Name: item id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.item ALTER COLUMN id SET DEFAULT nextval('public.item_id_seq'::regclass);


--
-- Name: mission id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.mission ALTER COLUMN id SET DEFAULT nextval('public.mission_id_seq'::regclass);


--
-- Name: mission_result id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.mission_result ALTER COLUMN id SET DEFAULT nextval('public.mission_result_id_seq'::regclass);


--
-- Name: report id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.report ALTER COLUMN id SET DEFAULT nextval('public.report_id_seq'::regclass);


--
-- Name: status id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.status ALTER COLUMN id SET DEFAULT nextval('public.status_id_seq'::regclass);


--
-- Name: user id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public.user_id_seq'::regclass);


--
-- Name: weekly_goal id; Type: DEFAULT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.weekly_goal ALTER COLUMN id SET DEFAULT nextval('public.weekly_goal_id_seq'::regclass);


--
-- Data for Name: app_usage_log; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.app_usage_log (log_id, user_id, app_name, start_time, end_time, duration_seconds, session_count, time_slot, usage_date) FROM stdin;
\.


--
-- Data for Name: content_cache; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.content_cache (cache_id, content_id, title, channel_name, content_type, education_score, addiction_score, cached_at, expires_at) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	initial schema	SQL	V1__initial_schema.sql	1208504908	dito	2025-11-10 06:25:31.654549	163	t
2	2	update user table structure	SQL	V2__update_user_table_structure.sql	629342371	dito	2025-11-10 06:25:31.863497	27	t
3	3	update mission trigger time nullable	SQL	V3__update_mission_trigger_time_nullable.sql	-106052002	dito	2025-11-10 06:25:31.913064	5	t
4	4	create app and media event tables	SQL	V4__create_app_and_media_event_tables.sql	114397748	dito	2025-11-10 06:25:31.930661	67	t
5	5	add unique constraint to personal id	SQL	V5__add_unique_constraint_to_personal_id.sql	-660448019	dito	2025-11-10 06:25:32.015931	9	t
6	6	sync item and user item with jpa	SQL	V6__sync_item_and_user_item_with_jpa.sql	2006335374	dito	2025-11-10 06:25:32.041939	47	t
7	7	drop postgresql event tables	SQL	V7__drop_postgresql_event_tables.sql	-1518415809	dito	2025-11-10 06:25:32.110064	14	t
8	8	insert default items	SQL	V8__insert_default_items.sql	526325136	dito	2025-11-10 07:08:58.540106	15	t
9	9	add insights jsonb column	SQL	V9__add_insights_jsonb_column.sql	-257529033	dito	2025-11-16 09:17:19.168159	26	t
10	10	add status column to report	SQL	V10__add_status_column_to_report.sql	-2144211341	dito	2025-11-18 01:52:19.91363	11	t
11	11	add report date column	SQL	V11__add_report_date_column.sql	375239294	dito	2025-11-18 03:19:33.861175	17	t
\.


--
-- Data for Name: group; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public."group" (id, group_name, invite_code, period, start_date, end_date, goal_description, penalty_description, status, total_bet_coins, created_at) FROM stdin;
85	A708	W2U9	1	2025-11-19	2025-11-20	유튜브 하루 1시간 이하	바나 프레소 쏘기!!	in_progress	130	2025-11-19 13:18:20.517663
87	Dito	J5R4	1	\N	\N	유튜브 하루 1시간 이하	Coffee	pending	10	2025-11-20 01:24:51.937579
86	dito	4LB2	1	2025-11-20	2025-11-21	유튜브 하루 1시간 이하	coffee	in_progress	10	2025-11-20 01:24:09.545888
\.


--
-- Data for Name: group_participant; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.group_participant (user_id, group_id, role, bet_coins, rank, avg_screen_time, joined_at) FROM stdin;
62	85	host	10	0	0.00	2025-11-19 13:18:20.518812
63	85	guest	10	0	0.00	2025-11-19 13:18:38.826559
38	85	guest	10	0	0.00	2025-11-19 13:18:40.389681
61	85	guest	100	0	0.00	2025-11-19 13:18:49.421661
24	86	host	10	0	0.00	2025-11-20 01:24:09.547265
56	87	host	10	0	0.00	2025-11-20 01:24:51.939139
\.


--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.item (id, type, name, price, img_url, on_sale) FROM stdin;
1	COSTUME	레몬 의상	0	https://k13a708.p.ssafy.io/media/lemon.png	f
2	COSTUME	포도 의상	120	https://k13a708.p.ssafy.io/media/grape.png	t
3	COSTUME	키위 의상	100	https://k13a708.p.ssafy.io/media/kiwi.png	t
4	COSTUME	메론 의상	100	https://k13a708.p.ssafy.io/media/melon.png	t
5	COSTUME	오렌지 의상	100	https://k13a708.p.ssafy.io/media/orange.png	t
6	COSTUME	토마토 의상	100	https://k13a708.p.ssafy.io/media/tomato.png	t
7	BACKGROUND	기본 배경	100	https://k13a708.p.ssafy.io/media/bg/default.png	t
8	BACKGROUND	야구 배경	100	https://k13a708.p.ssafy.io/media/bg/baseball.png	t
9	BACKGROUND	해변 배경	100	https://k13a708.p.ssafy.io/media/bg/beach.png	t
10	BACKGROUND	과일가게 배경1	100	https://k13a708.p.ssafy.io/media/bg/fruits-store.png	t
11	BACKGROUND	제주도 배경	100	https://k13a708.p.ssafy.io/media/bg/jeju.png	t
12	BACKGROUND	우주 배경	100	https://k13a708.p.ssafy.io/media/bg/space.png	t
13	BACKGROUND	새 배경	100	https://k13a708.p.ssafy.io/media/bg/bird.png	t
14	BACKGROUND	부산 배경	100	https://k13a708.p.ssafy.io/media/bg/busan.png	t
15	BACKGROUND	크리스마스 배경	100	https://k13a708.p.ssafy.io/media/bg/christmas.png	t
16	BACKGROUND	화이트 크리스마스 배경	100	https://k13a708.p.ssafy.io/media/bg/christmas-snow.png	t
17	BACKGROUND	콘서트 배경	100	https://k13a708.p.ssafy.io/media/bg/concert.png	t
18	BACKGROUND	사막 배경	100	https://k13a708.p.ssafy.io/media/bg/desert.png	t
20	BACKGROUND	과일가게 배경2	100	https://k13a708.p.ssafy.io/media/bg/fruits-store2.png	t
21	BACKGROUND	한강 배경	100	https://k13a708.p.ssafy.io/media/bg/hanriver.png	t
22	BACKGROUND	집 배경	100	https://k13a708.p.ssafy.io/media/bg/home.png	t
23	BACKGROUND	달 배경	100	https://k13a708.p.ssafy.io/media/bg/moon.png	t
24	BACKGROUND	밤 배경	100	https://k13a708.p.ssafy.io/media/bg/night.png	t
25	BACKGROUND	해변 배경	100	https://k13a708.p.ssafy.io/media/bg/ocean.png	t
26	BACKGROUND	소풍 배경	100	https://k13a708.p.ssafy.io/media/bg/picnic.png	t
27	BACKGROUND	분홍 하늘 배경	100	https://k13a708.p.ssafy.io/media/bg/pinksky.png	t
28	BACKGROUND	놀이터 배경1	100	https://k13a708.p.ssafy.io/media/bg/playground.png	t
30	BACKGROUND	트랙 배경	100	https://k13a708.p.ssafy.io/media/bg/road.png	t
31	BACKGROUND	학교 배경	100	https://k13a708.p.ssafy.io/media/bg/school.png	t
32	BACKGROUND	양때 배경	100	https://k13a708.p.ssafy.io/media/bg/sheep.png	t
33	BACKGROUND	쇼핑몰 배경	100	https://k13a708.p.ssafy.io/media/bg/shopping.png	t
34	BACKGROUND	토마토 배경	100	https://k13a708.p.ssafy.io/media/bg/tomato.png	t
35	BACKGROUND	나무 배경	100	https://k13a708.p.ssafy.io/media/bg/tree.png	t
36	BACKGROUND	은하계 배경	100	https://k13a708.p.ssafy.io/media/bg/universe.png	t
37	BACKGROUND	화산 배경	100	https://k13a708.p.ssafy.io/media/bg/volcano.png	t
19	BACKGROUND	과일 배경	100	https://k13a708.p.ssafy.io/media/bg/fruits.png	t
29	BACKGROUND	놀이터 배경2	100	https://k13a708.p.ssafy.io/media/bg/playground2.png	t
38	BACKGROUND	포도 농장 배경	120	https://k13a708.p.ssafy.io/media/bg/grape.png	t
39	BACKGROUND	오믈렛 배경	120	https://k13a708.p.ssafy.io/media/bg/omlet.png	t
\.


--
-- Data for Name: mission; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.mission (id, user_id, mission_type, mission_text, coin_reward, trigger_time, duration_seconds, target_app, stat_change_self_care, stat_change_focus, stat_change_sleep, status, prompt) FROM stdin;
294	61	REST	잠시 인스타를 그만하고 휴식하시는 건 어때요?	40	2025-11-20 00:35:12.516727	15	com.instagram.android	20	10	0	COMPLETED	AI Intervention
295	61	MEDITATION	유튜브를 너무 많이 보셨어요. 명상을 시작해보세요!	40	2025-11-20 00:38:02.111125	20	YouTube Shorts	30	30	30	COMPLETED	AI Intervention
\.


--
-- Data for Name: mission_result; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.mission_result (id, mission_id, result, completed_at, feedback) FROM stdin;
134	221	SUCCESS	2025-11-19 04:32:29.096502	Instagram Reels을(를) 사용하지 않아 디지털 디톡스에 성공했습니다! 훌륭해요! 🎉
135	223	SUCCESS	2025-11-19 04:42:25.952575	와, 인스타 릴스 유혹을 이겨냈네! 👏 쉽지 않은 도전인데 정말 잘했어. 이렇게 디지털 휴식을 취하면 집중력도 높아지고 마음도 한결 가벼워질 거야. 다음엔 조금 더 긴 시간 도전해볼까?
136	225	SUCCESS	2025-11-19 04:44:18.018147	와! 유튜브 쇼츠 유혹을 이겨내고 명상에 집중했구나 👏 마음의 여유를 찾는 첫걸음을 멋지게 성공했어! 내일은 명상 시간을 5분 더 늘려보는 건 어때?
137	226	SUCCESS	2025-11-19 04:47:59.04902	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 시간을 보냈다니 정말 대단해! 🎉 마음이 한결 가벼워지고 집중력도 좋아졌을 거야. 이 조용한 시간이 주는 평온함을 기억하면서, 내일은 명상 시간을 5분만 더 늘려보는 건 어때?
138	227	SUCCESS	2025-11-19 04:48:52.491019	와! 유튜브 쇼츠 유혹을 완벽하게 이겨냈네! 🎉 대신 명상으로 마음을 차분하게 가라앉힌 너의 선택이 정말 멋져. 이제 하루 10분 명상을 일주일 동안 꾸준히 해보는 건 어때?
139	228	SUCCESS	2025-11-19 04:48:58.429112	와, 인스타 릴스 유혹을 이겨냈네! 👏 10초 휴식으로 눈도 쉬고 집중력도 충전했을 거야. 다음엔 20초 도전해볼까? 지금처럼만 하면 충분히 할 수 있어!
140	229	SUCCESS	2025-11-19 04:49:34.475872	와, 인스타 릴스 유혹을 이겨냈네! 👏 짧은 영상의 무한 스크롤에서 벗어나 진짜 휴식을 취한 거 정말 멋져. 이제 좀 더 긴 시간 동안 디지털 디톡스에 도전해보는 건 어때?
141	230	FAILURE	2025-11-19 04:56:10.982201	Instagram Reels 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
142	231	FAILURE	2025-11-19 04:57:02.752459	Instagram Reels 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
143	232	FAILURE	2025-11-19 04:57:59.602111	Instagram Reels 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
144	233	FAILURE	2025-11-19 04:58:49.581158	Instagram Reels 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
145	234	FAILURE	2025-11-19 05:00:21.033683	Instagram Reels 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
146	235	FAILURE	2025-11-19 05:00:56.352547	YouTube Shorts 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
147	236	FAILURE	2025-11-19 05:01:43.042134	YouTube Shorts 사용 기록이 없습니다. 다음 미션에서는 꼭 실천해보세요!
148	237	SUCCESS	2025-11-19 05:08:42.089222	와! 인스타 릴스 유혹을 이겨내고 15초 휴식 완료했네! 👏 짧은 시간이지만 스스로를 위한 의미 있는 선택이었어. 이런 작은 성공들이 모여서 디지털 습관을 바꾸는 거야. 다음엔 30초도 도전해볼까? 😊
149	238	SUCCESS	2025-11-19 05:09:36.842546	와! 인스타 릴스 유혹을 뿌리치고 15초 휴식 성공했네! 👏 짧은 시간이지만 디지털에서 벗어나 눈과 마음을 쉬게 해준 거 정말 잘했어. 이번엔 30초 도전해볼까?
150	239	SUCCESS	2025-11-19 05:12:10.141841	YouTube Shorts을(를) 잘 참았어! 훌륭해! 💪
151	240	SUCCESS	2025-11-19 05:12:49.035673	YouTube Shorts을(를) 잘 참았어! 훌륭해! 💪
152	241	SUCCESS	2025-11-19 05:13:33.807189	YouTube Shorts을(를) 잘 참았어! 훌륭해! 💪
153	242	SUCCESS	2025-11-19 05:15:59.864984	YouTube Shorts을(를) 잘 참았어! 훌륭해! 💪
154	243	SUCCESS	2025-11-19 05:16:38.163339	YouTube Shorts을(를) 잘 참았어! 훌륭해! 💪
155	244	SUCCESS	2025-11-19 05:17:51.2484	와, 인스타 릴스 유혹을 이겨냈네! 👏 15초 동안 눈도 쉬고 머리도 맑아졌을 거야. 다음엔 30초 도전해볼까? 이 조자으로 계속 가보자! 💪
156	245	SUCCESS	2025-11-19 05:23:51.029653	와! 인스타 릴스의 유혹을 이겨내고 15초 휴식 미션 성공! 👏 \n\n짧은 시간이지만 디지털에서 눈을 떼는 게 생각보다 쉽지 않은데 정말 잘했어. 이제 30초, 1분으로 조금씩 늘려가면서 디지털 프리 타임을 더 즐겨보는 건 어때?
157	246	SUCCESS	2025-11-19 05:27:13.464883	와, 15초 동안 인스타 릴스 유혹을 이겨냈네! 👏 짧은 시간이지만 스스로 통제할 수 있다는 걸 증명했어. 다음엔 30초 도전해볼까?
158	247	SUCCESS	2025-11-19 05:29:56.085723	와! 인스타 릴스의 유혹을 뿌리치고 15초 휴식에 성공했네! 👏 짧은 시간이지만 디지털에서 눈을 떼는 게 생각보다 쉽지 않은데 정말 잘했어. 이제 30초, 1분으로 조금씩 늘려가면서 너만의 휴식 리듬을 만들어보는 건 어때?
159	248	SUCCESS	2025-11-19 05:33:42.811275	와! 인스타 릴스 유혹을 이겨내고 15초 휴식 성공했네! 👏 짧은 시간이지만 스스로 통제할 수 있다는 걸 증명했어. 다음엔 30초 도전해볼까?
160	249	SUCCESS	2025-11-19 05:44:32.448291	와! 인스타 릴스의 유혹을 이겨내고 15초 휴식에 성공했네! 🎉 짧은 시간이지만 스마트폰에서 눈을 떼는 연습이 쌓이면 더 긴 시간도 가능해질 거야. 다음엔 30초 휴식에 도전해볼래?
161	250	SUCCESS	2025-11-19 05:52:42.201737	와! 인스타 릴스 유혹을 이겨내고 15초 휴식 미션 성공이야! 👏 짧은 시간이지만 스스로 통제할 수 있다는 걸 증명했어. 다음엔 30초 휴식에도 도전해볼까?
162	251	SUCCESS	2025-11-19 06:19:22.180818	와! 인스타 릴스의 유혹을 이겨내고 15초 휴식 미션을 성공했네! 👏 짧은 시간이지만 스스로 통제할 수 있다는 걸 증명한 거야. 다음엔 30초에 도전해볼래?
163	255	FAILURE	2025-11-19 08:08:59.148288	아쉽게도 유튜브를 완전히 놓지 못했네! 하지만 도전한 것만으로도 대단해 👏 명상은 처음엔 어렵지만, 다음엔 휴대폰을 다른 방에 두고 시도해보면 어떨까? 너는 할 수 있어!
164	256	FAILURE	2025-11-19 08:18:32.573918	아쉽지만 괜찮아! 2분 쉬기가 생각보다 어렵지? 😊 다음엔 알람 맞춰두고 자리에서 일어나서 스트레칭하거나 물 한 잔 마시면서 눈을 쉬어보는 건 어때? 넌 충분히 할 수 있어, 다음 기회에 도전해보자! 💪
165	257	SUCCESS	2025-11-19 08:20:36.292159	Instagram Reels을(를) 잘 참았어! 훌륭해! 💪
166	258	SUCCESS	2025-11-19 08:21:02.335541	Instagram Reels을(를) 잘 참았어! 훌륭해! 💪
167	259	SUCCESS	2025-11-19 08:21:41.386094	와! 인스타 릴스 유혹을 이겨냈네! 👏 쉽지 않았을 텐데 정말 잘했어. 이렇게 디지털 휴식을 취하면 머리도 맑아지고 집중력도 올라가거든. 다음엔 조금 더 긴 시간 동안 도전해볼까?
168	260	SUCCESS	2025-11-19 08:22:31.737965	와! 인스타 릴스 유혹을 이겨내다니 정말 대단해! 🎉 휴식 시간을 온전히 자신에게 집중할 수 있었겠다. 이제 조금 더 긴 시간도 도전해볼까? 분명 할 수 있어!
169	261	SUCCESS	2025-11-19 08:23:50.156763	와! 인스타 릴스 유혹을 이겨내고 제대로 쉬었네! 👏 스마트폰 없이도 충분히 힐링할 수 있다는 걸 스스로 증명했어. 이제 조금 더 긴 시간 도전해볼까? 넌 충분히 할 수 있어! 💪
170	262	SUCCESS	2025-11-19 08:25:34.770804	와! 인스타 릴스 유혹을 이겨냈네! 🎉 쉽지 않은 일인데 정말 잘했어. 이렇게 디지털 휴식을 취하면 집중력도 올라가고 마음도 한결 가벼워질 거야. 다음엔 조금 더 긴 시간 도전해볼까?
171	263	FAILURE	2025-11-19 08:27:43.384294	괜찮아, 인스타그램을 잠깐 열어본 것 같은데 바로 나온 거 보니 의지는 있었던 것 같아! 다음엔 휴대폰을 좀 더 멀리 두거나 알림을 꺼두면 도움이 될 거야. 작은 시도가 모여서 큰 변화를 만드니까 다시 한번 도전해보자! 💪
172	264	FAILURE	2025-11-19 08:38:10.834824	아쉽지만 괜찮아! 1분이라도 인스타 사용을 인식했다는 게 첫걸음이야. 다음번엔 인스타 열고 싶을 때 폰을 다른 곳에 두거나, 5분만 산책해보는 건 어때? 넌 충분히 할 수 있어! 💪
173	265	FAILURE	2025-11-19 08:39:27.854319	아쉽지만 괜찮아! 인스타를 습관적으로 열게 되는 건 누구나 겪는 일이야. 다음번엔 휴대폰을 손 닿지 않는 곳에 두거나, 알림을 꺼두면 도움이 될 거야. 조금씩 시도하다 보면 분명 성공할 수 있어! 💪
174	266	SUCCESS	2025-11-19 08:40:59.658898	오늘 유튜브 쇼츠 유혹을 이겨내고 명상으로 마음을 챙긴 너 정말 멋져! 🎉 디지털 디톡스의 첫걸음을 성공적으로 내디뎠어. 내일은 명상 시간을 5분 더 늘려보는 건 어때?
175	267	FAILURE	2025-11-19 09:25:42.712744	아쉽게도 인스타그램을 15초 사용했네! 하지만 괜찮아, 디지털 디톡스는 한 번에 완벽하게 할 필요 없어. 다음엔 휴대폰을 손 닿지 않는 곳에 두거나, 알림을 꺼두면 더 쉬울 거야. 다음 미션에서 다시 도전해보자! 💪
176	268	SUCCESS	2025-11-19 09:28:42.22043	와! 유튜브 쇼츠 유혹을 이겨내고 명상에 집중했구나! 디지털 디톡스의 첫 걸음을 성공적으로 내디뎠어 👏 이제 명상을 조금씩 일상 루틴으로 만들어보는 건 어때? 매일 5분씩만 투자해도 마음이 훨씬 편안해질 거야!
177	269	SUCCESS	2025-11-19 09:29:40.567418	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 대체했다니 정말 멋져! 🧘‍♀️ 디지털 없이 나만의 시간을 가진 경험이 어땠어? 이런 평온한 순간들이 쌓이면 더 여유로운 일상을 만들 수 있을 거야. 내일은 5분 더 길게 도전해볼까? 💪✨
178	270	SUCCESS	2025-11-19 10:18:19.56929	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 대체했다니 정말 대단해! 🎉 마음이 한결 가벼워지고 집중력도 올라갔을 거야. 내일은 명상 시간을 5분만 더 늘려보는 건 어때?
179	271	SUCCESS	2025-11-19 10:18:54.116905	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 마음을 가라앉히다니 정말 멋져! 디지털 디톡스의 첫걸음을 완벽하게 해냈네 👏 이제 명상을 일상의 루틴으로 만들어보는 건 어때? 매일 5분씩만 투자해도 마음이 훨씬 평온해질 거야!
180	272	SUCCESS	2025-11-19 10:20:14.859063	와! 유튜브 쇼츠 유혹을 뿌리치고 명상에 집중했구나 👏 마음의 여유를 찾는 첫걸음을 성공적으로 완료했어! 다음엔 명상 시간을 조금 더 늘려보는 건 어때? 너의 집중력이 점점 강해지고 있어 💪
181	273	SUCCESS	2025-11-19 10:20:51.962379	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 마음을 다스렸네! 👏 숏폼 영상 대신 자신을 돌보는 시간을 가진 너 정말 멋져. 이번 성공을 발판 삼아 내일은 명상 시간을 5분 더 늘려볼까?
182	274	SUCCESS	2025-11-19 10:41:01.462827	와! 유튜브 쇼츠 유혹을 이겨내고 명상에 성공했네! 👏 짧은 영상의 중독성을 끊어낸 게 정말 대단해. 이제 조금 더 긴 시간 명상에 도전하거나, 하루에 두 번 명상하는 루틴을 만들어보는 건 어때?
183	275	FAILURE	2025-11-19 12:10:14.330898	괜찮아, 처음부터 완벽할 순 없지! 15초만 사용한 거면 이미 인스타 유혹을 꽤 잘 버틴 거야 👏 다음엔 핸드폰을 손 닿지 않는 곳에 두거나, 알림을 끄고 도전해보는 건 어때? 분명 할 수 있어!
184	276	FAILURE	2025-11-19 13:20:55.261321	인스타그램을 잠깐 열어봤지만, 15초 만에 끈 건 정말 잘한 거야! 완전히 끊기는 어렵지만, 짧게라도 자제한 건 큰 발전이야. 다음엔 휴식 시간 동안 스마트폰을 손 닿지 않는 곳에 두고, 좋아하는 음악을 듣거나 스트레칭을 해보는 건 어때? 넌 충분히 할 수 있어! 💪
185	277	SUCCESS	2025-11-19 13:25:38.119709	와! 유튜브 숏츠 유혹을 뿌리치고 명상에 집중했네! 🧘‍♀️ 디지털 디톡스 첫 발을 멋지게 내디뎠어. 이 여유로운 마음가짐으로 내일은 조금 더 긴 시간 명상에 도전해보는 건 어때?
186	278	SUCCESS	2025-11-19 13:26:31.109955	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 마음의 여유를 찾았네? 정말 멋져! 🧘‍♀️ 디지털 디톡스의 첫 걸음을 훌륭하게 완수했어. 이제 하루 10분씩 명상 시간을 늘려보는 건 어때?
187	279	SUCCESS	2025-11-19 13:41:46.846905	와! 유튜브 쇼츠를 참고 명상 미션을 성공했네! 👏 짧은 영상의 유혹을 이겨낸 너의 의지력이 정말 대단해. 이 좋은 습관을 유지하면서, 다음엔 명상 시간을 5분 더 늘려보는 건 어때?
188	280	SUCCESS	2025-11-19 14:25:05.772502	와! 인스타그램 릴스 유혹을 이겨내고 명상에 집중했네! 👏 사랑의 의미를 디지털 세상이 아닌 내면에서 찾은 너 정말 멋져. 다음엔 더 긴 시간 명상하면서 깊은 통찰을 얻어보는 건 어때?
189	281	SUCCESS	2025-11-19 14:28:57.26289	와! 인스타그램 릴스의 유혹을 이겨내고 명상에 집중했구나! 🧘‍♀️ 사랑의 답을 찾는 여정에서 디지털 소음 없이 내면의 소리에 귀 기울인 너 정말 멋져! 다음엔 명상 시간을 조금 더 늘려서 더 깊은 통찰을 얻어보는 건 어때? ✨
190	282	SUCCESS	2025-11-19 14:34:30.212503	와! 인스타그램 릴스 유혹을 이겨내고 명상에 집중했구나 👏 디지털 소음 없이 내면의 소리에 귀 기울인 너 정말 멋져! 다음엔 명상 시간을 조금 더 늘려보는 건 어때? 분명 더 깊은 통찰을 얻을 수 있을 거야 🧘‍♀️✨
191	283	SUCCESS	2025-11-19 14:35:33.613096	와! 인스타그램 릴스를 참고 명상에 집중했네! 👏 짧은 영상의 유혹을 뿌리치고 내면의 평화를 찾은 너, 정말 멋져! 이제 조금 더 긴 시간 동안 디지털 없이 지내는 것도 도전해볼까? 😊
192	284	SUCCESS	2025-11-19 14:58:37.240995	Instagram Reels을(를) 잘 참았어! 훌륭해! 💪
193	285	SUCCESS	2025-11-19 15:30:09.453369	와, 정말 대단해! 🎉 인스타그램 릴스의 유혹을 이겨내고 명상에 집중했다니 멋진데? 디지털 세상 속 사랑보다 내 마음속 사랑을 먼저 찾은 너, 정말 멋있어! 다음엔 명상 시간을 5분 더 늘려보는 건 어때? 💙
194	286	SUCCESS	2025-11-19 21:54:06.023416	와! 인스타그램 릴스의 유혹을 이겨내고 명상에 집중했네! 👏 디지털 소음 없이 내면의 소리에 귀 기울인 너 자신이 정말 자랑스러울 거야. 이제 더 긴 시간 동안 명상하거나, 다른 SNS도 끊고 도전해볼까?
195	287	SUCCESS	2025-11-19 21:54:48.821555	와! 인스타 릴스 유혹을 이겨내고 명상에 집중했구나! 👏 디지털 소음 대신 내면의 소리에 귀 기울인 너 정말 멋져! 이제 조금씩 명상 시간을 늘려가면서, 사랑이 어디서 오는지 더 깊이 탐험해보자 💙
196	288	SUCCESS	2025-11-19 21:55:29.487688	와! 인스타그램 릴스 유혹을 이겨내고 명상에 집중했구나! 🧘‍♀️ 짧은 영상 대신 내면의 목소리에 귀 기울인 너의 선택이 정말 멋져. 다음엔 명상 시간을 5분 더 늘려보는 건 어때? 더 깊은 평온함을 느낄 수 있을 거야! ✨
197	289	SUCCESS	2025-11-19 21:57:50.718607	와! 인스타 릴스 유혹을 이겨내고 명상에 집중했구나, 정말 대단해! 🧘‍♀️ 디지털 소음 없이 나만의 시간을 가지면서 마음속 사랑을 발견했을 것 같아. 이제 명상 시간을 조금씩 늘려가면서 더 깊은 내면의 평화를 찾아보는 건 어때?
198	290	SUCCESS	2025-11-19 23:11:39.352207	와! 유튜브 쇼츠 유혹을 이겨냈네! 👏 명상으로 마음을 차분히 가라앉히는 시간을 가진 게 정말 멋져. 이제 조금씩 디지털 습관을 바꿔가고 있으니, 다음엔 명상 시간을 5분 더 늘려보는 건 어때?
199	291	FAILURE	2025-11-19 23:26:10.052237	괜찮아, 인스타 습관을 바꾸는 게 쉽지 않지! 15초만 사용한 건 이미 의식적으로 노력한 거야. 다음엔 알림을 꺼두거나 휴대폰을 다른 방에 두고 5분만 버텨보자. 작은 성공부터 시작하면 분명 할 수 있어! 💪
200	292	SUCCESS	2025-11-19 23:28:36.085424	와! 유튜브 쇼츠 유혹을 이겨내고 명상에 집중했구나 👏 마음을 차분히 가라앉히는 시간을 가진 너 자신이 자랑스러울 거야! 이제 디지털 휴식의 달인이 되어가고 있어 - 내일은 더 긴 시간 도전해볼까? 🧘‍♀️✨
201	293	SUCCESS	2025-11-19 23:31:40.481313	와! 유튜브 쇼츠 유혹을 이겨내고 명상을 선택했네! 👏 마음의 평화를 찾는 시간이 얼마나 소중한지 직접 느꼈을 거야. 이제 하루 10분 명상을 습관으로 만들어보는 건 어때?
202	294	FAILURE	2025-11-20 00:35:34.357016	괜찮아, 인스타 습관을 바꾸는 게 쉽지 않지! 15초만 사용한 것도 의식적으로 노력한 흔적이 보여. 다음번엔 인스타 앱을 눌렀을 때 잠깐 멈추고 "지금 꼭 봐야 하나?"라고 스스로에게 물어보는 건 어떨까? 작은 성공부터 쌓아가자! 💪
203	295	SUCCESS	2025-11-20 00:38:28.383813	와! 유튜브 쇼츠 유혹을 이겨내고 명상으로 마음을 다스렸구나 👏 디지털 단식 첫 걸음을 멋지게 성공했어! 내일은 명상 시간을 5분 더 늘려보는 건 어때?
\.


--
-- Data for Name: report; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.report (id, user_id, report_overview, advice, mission_success_rate, created_at, insights, status, report_date) FROM stdin;
6	23	\N	\N	\N	2025-11-18 03:30:51.138845	\N	\N	2025-11-18
7	23	\N	\N	\N	2025-11-18 03:41:00.75878	\N	\N	2025-11-18
9	23	\N	\N	\N	2025-11-18 04:30:21.350587	\N	\N	2025-11-18
10	23	오늘은 디지털 기기를 거의 사용하지 않은 완벽한 디톡스 데이였어요! 앱 사용 시간과 미디어 시청 시간이 모두 0분으로 기록되었습니다. 디지털 기기에서 완전히 벗어나 오프라인 활동에 집중한 하루를 보내셨네요.	오늘처럼 디지털 기기 없이 보낸 시간이 정말 소중해! 이 패턴을 유지하면서 내일도 의미 있는 오프라인 활동을 계획해보는 건 어때?	0	2025-11-18 04:36:45.445235	[{"type": "POSITIVE", "description": "스마트폰 앱 사용 시간이 0분으로, 디지털 기기 없이 하루를 보냈어요. 오프라인 활동에 완전히 집중한 멋진 하루예요!"}, {"type": "POSITIVE", "description": "미디어 시청 시간도 0분으로 기록되어, 숏폼이나 영상 콘텐츠에 빠지지 않고 건강한 하루를 유지했어요."}, {"type": "POSITIVE", "description": "특정 앱에 과도하게 의존하지 않고, 디지털 웰빙을 실천한 이상적인 패턴을 보여줬어요."}]	COMPLETED	2025-11-17
11	23	\N	\N	\N	2025-11-18 04:39:50.45592	\N	\N	2025-11-18
12	23	오늘은 디지털 기기를 거의 사용하지 않은 특별한 하루였어! 앱 사용 시간과 미디어 시청 시간이 모두 0분으로, 완벽한 디지털 디톡스를 실천한 날이야. 스마트폰 없이 오프라인 활동에 집중한 멋진 하루를 보냈네!	오늘은 정말 대단한 하루였어! 이 경험을 바탕으로 일주일에 한 번씩 '디지털 프리 데이'를 만들어보는 건 어때? 오늘 뭘 하면서 시간을 보냈는지 기록해두면 다음에도 참고하기 좋을 거야.	100	2025-11-18 05:09:34.293515	[{"type": "POSITIVE", "description": "오늘 총 디지털 사용 시간이 0분으로, 완벽한 디지털 프리 데이를 달성했어! 스마트폰 없이도 충분히 의미 있는 하루를 보낼 수 있다는 걸 증명했네."}, {"type": "POSITIVE", "description": "미디어 콘텐츠 시청 시간도 0분으로, 숏폼이나 영상 콘텐츠의 유혹을 완전히 차단했어. 수동적 소비 대신 능동적인 활동에 시간을 투자한 것 같아!"}, {"type": "POSITIVE", "description": "앱 사용 기록이 전혀 없어서 스마트폰 의존도를 크게 낮춘 날이야. 이런 날들이 쌓이면 디지털 습관이 건강하게 변화할 거야."}]	COMPLETED	2025-11-17
13	23	오늘 총 95분 동안 앱을 사용했어요. Instagram을 가장 많이 사용했고(12회 접속), YouTube에서는 주로 음악과 라이브 공연 영상을 시청했네요. 특히 고양이 숏폼 콘텐츠를 75분이나 시청한 점이 눈에 띄어요.	Instagram 확인 횟수를 줄이기 위해 알림을 끄거나 정해진 시간에만 접속해보는 건 어때? 숏폼 콘텐츠는 타이머를 설정해서 15분 이내로 제한해보자!	0	2025-11-18 05:10:37.0802	[{"type": "POSITIVE", "description": "일일 앱 사용 시간이 95분으로 적정 수준을 유지하고 있어요. 디지털 웰빙을 잘 관리하고 있는 모습이에요!"}, {"type": "NEGATIVE", "description": "Instagram을 하루에 12번이나 접속했어요. 잦은 확인 습관이 집중력을 방해할 수 있어요."}, {"type": "NEGATIVE", "description": "고양이 숏폼 영상을 75분(4500초)이나 시청했어요. 짧은 콘텐츠에 시간이 많이 흘러갔네요."}, {"type": "POSITIVE", "description": "업무용 배경음악과 라이브 공연 등 생산적인 미디어 콘텐츠도 시청했어요. 균형 잡힌 사용 패턴이에요!"}]	COMPLETED	2025-11-17
5	23	새벽 2시 이후에 잠드는 날이 많고, 침대에 누워 쇼츠를 40~60분 정도 연속 시청하는 패턴이 관찰됐어요.\n수면 전 스크린 타임이 길어, 다음날 피로 누적 위험이 높은 상태입니다.\n야간 시간대에 모바일 사용이 집중되어 있어, 수면 패턴이 불규칙할 가능성이 높습니다.	저녁 쇼츠는 멈추고 그때 개발학습에 집중해요. 오늘은 미션탭에서 오늘의 미션을 확인하고 바로 시작해봅시다.	50	2025-11-16 09:18:59.395198	[{"type": "POSITIVE", "description": "야간에 사용하는 시간이 전일 대비 -24% 감소했어요"}, {"type": "POSITIVE", "description": "미션을 잘 실천하고 있어요"}, {"type": "NEGATIVE", "description": "핸드폰 사용 시간이 2시간 증가했어요"}]	COMPLETED	2025-11-18
4	29	새벽 2시 이후에 잠드는 날이 많고, 침대에 누워 쇼츠를 40~60분 정도 연속 시청하는 패턴이 관찰됐어요.\n수면 전 스크린 타임이 길어, 다음날 피로 누적 위험이 높은 상태입니다.\n야간 시간대에 모바일 사용이 집중되어 있어, 수면 패턴이 불규칙할 가능성이 높습니다.	저녁 쇼츠는 멈추고 그때 개발학습에 집중해요. 오늘은 미션탭에서 오늘의 미션을 확인하고 바로 시작해봅시다.	85	2025-11-16 09:18:20.776697	[{"type": "POSITIVE", "description": "야간에 사용하는 시간이 전일 대비 -24% 감소했어요"}, {"type": "POSITIVE", "description": "미션을 잘 실천하고 있어요"}, {"type": "NEGATIVE", "description": "핸드폰 사용 시간이 2시간 증가했어요"}]	COMPLETED	2025-11-18
8	23	string	string	100	2025-11-18 04:29:42.419202	[{"type": "POSITIVE", "description": "string"}]	COMPLETED	2025-11-18
14	29	오늘 총 95분간 디지털 기기를 사용했어. Instagram에서 36분, YouTube에서 21분을 보냈고, 특히 Instagram을 12번이나 자주 열어봤네. 전체적으로 짧은 시간 동안 자주 접속하는 패턴이 보여.	Instagram은 알림을 끄고 확인 시간을 정해두면 습관적 접속을 줄일 수 있어. 내일은 작은 미션 하나부터 도전해볼까?	0	2025-11-18 06:16:44.790902	[{"type": "POSITIVE", "description": "하루 총 사용 시간 95분은 꽤 절제된 편이야! 2시간 이내로 잘 관리하고 있어."}, {"type": "NEGATIVE", "description": "Instagram을 12번 접속해서 평균 3분씩 사용했어. 습관적으로 자주 확인하는 패턴이 보여서 주의가 필요해."}, {"type": "NEGATIVE", "description": "YouTube에서 고양이 숏폼 영상을 75분이나 시청했어. 짧은 영상들이 연속 재생되면서 시간이 많이 흘러갔을 거야."}, {"type": "NEGATIVE", "description": "오늘 설정한 미션을 하나도 달성하지 못했어. 목표를 다시 점검하고 실천 가능한 작은 목표부터 시작해보자."}]	COMPLETED	2025-11-17
22	54	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 03:15:12.809333	[{"type": "POSITIVE", "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "description": "[주의] 잦은 세션 전환 (Switching Cost)"}]	COMPLETED	2025-11-18
15	29	오늘 하루 동안 총 95분의 디지털 사용 시간이 기록되었어요. Instagram에서 36분을 가장 많이 사용했고, YouTube에서 25.5분의 영상을 시청하며 주로 음악과 숏폼 콘텐츠를 즐겼네요. 아직 미션을 시작하지 않아 성공률은 0%이지만, 적당한 사용 시간을 유지하고 있어요.	오늘부터 작은 미션 하나를 시작해보는 건 어때? 예를 들어 '자기 전 30분은 휴대폰 보지 않기' 같은 쉬운 목표로 시작하면 성취감도 느끼고 디지털 웰빙도 챙길 수 있을 거야!	0	2025-11-18 06:24:26.469399	[{"type": "POSITIVE", "description": "디지털 사용 0분과 야간 미션 성공으로 최적의 수면 환경을 조성했습니다"}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 집중력이 최상으로 유지되었습니다"}, {"type": "NEGATIVE", "description": "아직 미션을 시작하지 않았어요. 작은 목표부터 도전해보세요!"}]	COMPLETED	2025-11-17
16	29	오늘 하루 디지털 기기를 총 95분 사용했어요. Instagram에서 36분간 12번의 세션으로 가장 많이 사용했고, YouTube에서는 음악과 숏폼 콘텐츠를 중심으로 25.5분을 시청했네요. 미션은 아직 시도하지 않았지만, 전반적으로 적당한 수준의 디지털 사용 패턴을 보였습니다.	Instagram 확인 횟수가 12번으로 많은 편이니, 알림을 줄이고 의식적으로 확인 횟수를 줄여보면 어떨까? 그리고 오늘부터 작은 미션 하나만 도전해보자!	0	2025-11-18 06:24:59.076696	[{"type": "POSITIVE", "description": "디지털 기기 사용 없이 밤 시간대 미션을 성공하여 수면에 이상적인 환경을 조성했습니다."}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 최고의 집중력을 유지했어요!"}, {"type": "NEGATIVE", "description": "아직 시도한 미션이 없어요, 작은 목표부터 시작해보세요!"}]	COMPLETED	2025-11-17
17	29	오늘은 총 95분간 디지털 기기를 사용했어요. Instagram을 36분간 12번 접속하며 가장 많이 사용했고, YouTube에서는 음악과 고양이 영상 등 25.5분의 콘텐츠를 시청했습니다. 아직 디톡스 미션을 시작하지 않았지만, 적당한 수준의 디지털 사용 패턴을 보이고 있어요.	하루 평균 95분 사용은 나쁘지 않은 편이야! 이제 첫 미션을 설정해서 Instagram 확인 횟수를 줄여보는 건 어떨까? 작은 목표부터 시작하면 충분히 달성할 수 있을 거야.	0	2025-11-18 06:28:43.60199	[{"type": "POSITIVE", "description": "디지털 기기를 사용하지 않고 야간 미션을 성공하여 양질의 수면 환경을 조성했습니다."}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 완벽한 디지털 디톡스를 달성했습니다"}, {"type": "NEGATIVE", "description": "아직 미션을 시작하지 않았어요, 첫 목표를 설정해보세요!"}]	COMPLETED	2025-11-17
18	29	오늘은 총 95분 동안 디지털 기기를 사용했어요. Instagram을 가장 많이 사용했고(36분, 12회 접속), YouTube에서는 음악과 귀여운 고양이 영상을 포함해 25.5분간 콘텐츠를 시청했네요. 아직 미션을 시작하지 않아 성공률은 0%이지만, 전반적으로 적당한 수준의 디지털 사용 패턴을 보이고 있습니다.	Instagram 접속 횟수가 12번으로 많은 편이니, 내일은 특정 시간대에만 확인하는 걸 목표로 해볼까? 작은 미션부터 시작해서 성공 경험을 쌓아보자!	0	2025-11-18 06:32:38.214331	[{"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않고 야간 미션을 성공하여 수면에 최적화된 하루입니다."}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 집중력이 매우 우수합니다."}, {"type": "NEGATIVE", "description": "아직 미션을 시작하지 않았으니 오늘 작은 목표부터 도전해보세요."}]	COMPLETED	2025-11-17
19	29	오늘은 총 95분 동안 디지털 기기를 사용했어요. Instagram을 가장 많이 사용했고(36분, 12회), YouTube에서는 발라드 음악과 라이브 영상, 고양이 숏폼 등 다양한 콘텐츠를 약 25.5분간 시청했네요. 미션에 도전하지 않아 성공률은 0%지만, 전체적으로 적당한 수준의 디지털 사용 패턴을 보였어요.	Instagram 확인 횟수가 12회로 꽤 많은 편이야. 오늘부터 가벼운 미션 하나에 도전해서 집중 시간을 만들어보는 건 어때? 작은 성공이 쌓이면 디지털 습관이 훨씬 건강해질 거야!	0	2025-11-18 06:36:25.320257	[{"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않고 야간 미션을 성공하여 양질의 수면을 취했습니다."}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 완벽한 집중 환경을 유지했습니다"}, {"type": "NEGATIVE", "description": "아직 미션에 도전하지 않았으니 첫 걸음을 시작해보세요!"}]	COMPLETED	2025-11-17
20	29	오늘은 총 95분 동안 디지털 기기를 사용했어요. Instagram을 가장 많이 사용했고(36분, 12회 접속), YouTube에서는 음악과 고양이 영상 등 25.5분의 콘텐츠를 시청했네요. 아직 디톡스 미션에 도전하지 않았지만, 적당한 수준의 디지털 사용을 보여주고 있습니다.	Instagram 접속 횟수가 12회로 꽤 잦은 편이야. 오늘부터 미션에 도전해서 무의식적인 앱 체크 습관을 줄여보는 건 어떨까? 작은 목표부터 시작하면 충분히 할 수 있어!	0	2025-11-18 06:40:31.930231	[{"type": "POSITIVE", "description": "디지털 기기를 사용하지 않고 밤 시간 미션도 성공하여 최적의 수면 환경을 유지했습니다."}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 최상의 집중력을 유지했습니다"}, {"type": "NEGATIVE", "description": "아직 미션을 시작하지 않았으니 오늘 바로 도전해보세요!"}]	COMPLETED	2025-11-17
21	23	오늘 총 95분 동안 디지털 기기를 사용했어요. Instagram에서 12번의 세션으로 36분, YouTube에서 25.5분의 영상을 시청하며 주로 음악과 숏폼 콘텐츠를 즐겼네요. 야간에는 디지털 기기를 전혀 사용하지 않아 좋은 수면 환경을 만들었어요.	야간 디지털 디톡스는 완벽했어! 이제 낮 시간에도 작은 미션부터 도전해보자. Instagram 세션 횟수를 줄이거나, 숏폼 시청 전에 타이머를 설정하는 것부터 시작해볼까?	0	2025-11-18 07:16:22.021894	[{"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않고 야간 미션을 완벽히 달성했어요!"}, {"type": "POSITIVE", "description": "디지털 기기를 전혀 사용하지 않아 최상의 집중 환경을 유지했어요"}, {"type": "NEGATIVE", "description": "아직 미션을 시도하지 않았으니 오늘부터 작은 목표로 시작해보세요."}]	COMPLETED	2025-11-17
23	54	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 03:16:12.397524	[{"type": "POSITIVE", "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "description": "[주의] 잦은 세션 전환 (Switching Cost)"}]	COMPLETED	2025-11-18
24	29	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 05:17:48.946501	[{"type": "POSITIVE", "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "description": "[주의] 잦은 세션 전환 (Switching Cost)"}]	COMPLETED	2025-11-18
25	29	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 05:42:45.222598	[{"type": "POSITIVE", "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
26	29	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 05:45:25.516049	[{"type": "POSITIVE", "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
27	29	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 05:51:26.607171	[{"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
28	29	완벽한 디지털 웰니스 하루였습니다! 적절한 시점의 중재(Intervention)를 통해 불필요한 스크롤링을 멈추고, 집중과 휴식의 이상적인 밸런스를 찾아냈습니다.	완벽합니다! 내일은 이 리듬을 유지하며 아침 10분 독서 미션에 도전해보는 건 어떨까요?	100	2025-11-19 05:56:48.800846	[{"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[최적] 수면 보호 모드 작동"}, {"type": "POSITIVE", "score": {"after": 40, "before": 0}, "description": "[우수] 능동적 조절력 입증"}, {"type": "POSITIVE", "score": {"after": 30, "before": 10}, "description": "[향상] 고몰입 환경 조성"}]	COMPLETED	2025-11-18
29	29	완벽한 디지털 웰니스 하루였습니다! 적절한 시점의 중재(Intervention)를 통해 불필요한 스크롤링을 멈추고, 집중과 휴식의 이상적인 밸런스를 찾아냈습니다.	완벽합니다! 내일은 이 리듬을 유지하며 아침 10분 독서 미션에 도전해보는 건 어떨까요?	100	2025-11-19 05:57:36.109999	[{"type": "POSITIVE", "score": {"after": 50, "before": 30}, "description": "[최적] 수면 보호 모드 작동"}, {"type": "POSITIVE", "score": {"after": 50, "before": 40}, "description": "[우수] 능동적 조절력 입증"}, {"type": "POSITIVE", "score": {"after": 40, "before": 30}, "description": "[향상] 고몰입 환경 조성"}]	COMPLETED	2025-11-18
30	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 06:22:43.603871	[{"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
31	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 09:05:41.816338	[{"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
32	29	절반의 성공을 하셨네요. 적절한 시점의 중재(Intervention)를 통해 불필요한 스크롤링을 멈추고, 집중과 휴식의 이상적인 밸런스를 찾아냈습니다.	완벽합니다! 내일은 이 리듬을 유지하며 아침 10분 독서 미션에 도전해보는 건 어떨까요?	50	2025-11-19 09:32:29.503363	[{"type": "POSITIVE", "score": {"after": 50, "before": 30}, "description": "[최적] 수면 보호 모드 작동"}, {"type": "POSITIVE", "score": {"after": 50, "before": 40}, "description": "[우수] 능동적 조절력 입증"}, {"type": "POSITIVE", "score": {"after": 40, "before": 30}, "description": "[향상] 고몰입 환경 조성"}]	COMPLETED	2025-11-18
33	29	절반의 성공을 하셨네요. 적절한 시점의 중재(Intervention)를 통해 불필요한 스크롤링을 멈추고, 집중과 휴식의 이상적인 밸런스를 찾아냈습니다.	완벽합니다! 내일은 이 리듬을 유지하며 아침 10분 독서 미션에 도전해보는 건 어떨까요?	61	2025-11-19 09:32:52.525539	[{"type": "POSITIVE", "score": {"after": 50, "before": 30}, "description": "[최적] 수면 보호 모드 작동"}, {"type": "POSITIVE", "score": {"after": 50, "before": 40}, "description": "[우수] 능동적 조절력 입증"}, {"type": "POSITIVE", "score": {"after": 40, "before": 30}, "description": "[향상] 고몰입 환경 조성"}]	COMPLETED	2025-11-18
35	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 09:36:40.349914	[{"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-18
34	61	절반의 성공을 하셨네요. 적절한 시점의 중재(Intervention)를 통해 불필요한 스크롤링을 멈추고, 집중과 휴식의 이상적인 밸런스를 찾아냈습니다.	완벽합니다! 내일은 이 리듬을 유지하며 아침 10분 독서 미션에 도전해보는 건 어떨까요?	50	2025-11-19 09:33:10.638401	[{"type": "POSITIVE", "score": {"after": 50, "before": 30}, "description": "[최적] 수면 보호 모드 작동"}, {"type": "POSITIVE", "score": {"after": 50, "before": 40}, "description": "[우수] 능동적 조절력 입증"}, {"type": "POSITIVE", "score": {"after": 40, "before": 30}, "description": "[향상] 고몰입 환경 조성"}]	COMPLETED	2025-11-18
36	62	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 10:49:19.214232	[{"type": "POSITIVE", "score": {"after": 20, "before": 0}, "description": "[분석] 야간 각성 상태 감지"}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[분석] 자발적 휴식 부재"}, {"type": "POSITIVE", "score": {"after": 30, "before": 0}, "description": "[주의] 잦은 세션 전환"}]	COMPLETED	2025-11-19
37	62	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 11:13:37.277656	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "[수면 패턴] 늦은 시간까지 이어지는 디지털 자극이 뇌를 과각성시켜, 자연스러운 수면 진입을 방해하고 있습니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "[자기 조절] 뇌가 쉴 틈 없이 정보를 주입받으며, 피로해도 스스로 멈추지 못하는 '휴식 결핍' 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "[디지털 몰입] 목적 없는 잦은 앱 전환과 짧은 시청 지속 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
38	62	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 11:15:08.596368	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "늦은 디지털 자극이 수면 진입을 방해하고 뇌를 과각성시킵니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "지속적인 정보 주입으로 뇌가 쉬지 못하는 휴식 결핍 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "잦은 앱 전환과 짧은 시청 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
39	62	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 11:16:07.324164	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "늦은 디지털 자극이 수면을 방해하고 뇌를 과각성시킵니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "지속적인 정보 주입으로 뇌가 쉬지 못하는 휴식 결핍 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "잦은 앱 전환과 짧은 시청 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
40	61	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 11:23:33.228994	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
41	62	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 11:25:26.474643	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
42	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 12:04:37.091037	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "늦은 디지털 자극이 수면을 방해하고 뇌를 과각성시킵니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 0}, "description": "지속적인 정보 주입으로 뇌가 쉬지 못하는 휴식 결핍 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "잦은 앱 전환과 짧은 시청 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
43	62	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 12:12:29.351264	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
44	62	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 13:25:40.31046	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
45	62	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 13:26:41.28523	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
46	61	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 13:27:02.274041	[{"type": "POSITIVE", "score": {"after": 85, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 50, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 70, "before": 10}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
47	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 23:08:52.750447	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "늦은 디지털 자극이 수면을 방해하고 뇌를 과각성시킵니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "지속적인 정보 주입으로 뇌가 쉬지 못하는 휴식 결핍 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "잦은 앱 전환과 짧은 시청 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
48	61	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-19 23:28:28.150189	[{"type": "POSITIVE", "score": {"after": 60, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 60, "before": 30}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
49	61	총 95분의 사용 로그 중 Instagram과 Shorts 위주의 단발성 소비 패턴이 65%를 차지합니다. 목적 없는 앱 실행과 짧은 시청 지속 시간은 디지털 피로도를 높이는 주원인으로 분석됩니다.	지금의 패턴이라면, 내일은 의도적으로 앱 실행 횟수를 5회 미만으로 줄이는 것부터 시작해야 합니다.	0	2025-11-19 23:42:44.801274	[{"type": "NEGATIVE", "score": {"after": 20, "before": 0}, "description": "늦은 디지털 자극이 수면을 방해하고 뇌를 과각성시킵니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "지속적인 정보 주입으로 뇌가 쉬지 못하는 휴식 결핍 상태입니다."}, {"type": "NEGATIVE", "score": {"after": 30, "before": 0}, "description": "잦은 앱 전환과 짧은 시청 시간이 주의력을 분산시키고 디지털 피로도를 높입니다."}]	COMPLETED	2025-11-19
50	61	인스타그램 차단은 실패했지만, 잠들기 전 명상을 선택한 것은 탁월했습니다. 이 행동 하나가 과도한 스마트폰 사용을 막고 양질의 수면을 확보하는 핵심 열쇠가 되었습니다.	어제 성공한 '수면 전 명상'을 오늘도 이어가세요. 이 루틴만 지켜도 디지털 디톡스의 절반은 성공입니다.	50	2025-11-20 00:38:42.557208	[{"type": "POSITIVE", "score": {"after": 60, "before": 20}, "description": "[수면 패턴] 취침 전 명상이 뇌의 야간 각성을 진정시키고, 깊은 휴식으로 이끄는 이상적인 입면 루틴이 되었습니다."}, {"type": "NEGATIVE", "score": {"after": 40, "before": 30}, "description": "[자기 조절] 명상을 통한 내면 돌봄은 성공했으나, 즉각적 자극(인스타)에 대한 충동 조절은 여전히 과제로 남았습니다."}, {"type": "POSITIVE", "score": {"after": 60, "before": 30}, "description": "[디지털 몰입] 명상 시도가 무의식적 스크롤링의 흐름을 끊어, 결과적으로 전체 디지털 사용 시간을 유의미하게 줄였습니다."}]	COMPLETED	2025-11-20
\.


--
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.status (id, user_id, self_care_stat, focus_stat, sleep_stat, total_stat) FROM stdin;
56	56	50	50	50	65
54	54	100	100	30	76
61	61	30	50	60	46
66	66	50	50	50	65
25	25	50	50	50	65
27	27	48	48	48	48
28	28	50	50	50	65
30	30	50	50	50	65
58	58	50	50	50	65
59	59	50	50	50	65
31	31	50	50	50	65
32	32	50	50	50	65
33	33	50	50	50	65
34	34	50	50	50	65
35	35	50	50	50	65
36	36	50	50	50	65
37	37	50	50	50	65
39	39	50	50	50	65
40	40	50	50	50	65
42	42	50	50	50	65
43	43	50	50	50	65
45	45	60	70	80	70
46	46	60	70	80	70
49	49	60	70	80	70
50	50	60	70	80	70
48	48	60	70	80	70
51	51	50	50	50	65
47	47	70	90	100	86
44	44	30	10	0	13
52	52	50	50	50	65
38	38	100	100	0	66
53	53	50	50	50	65
24	24	40	30	20	30
41	41	40	30	20	30
55	55	10	0	0	3
26	26	10	0	0	3
57	57	40	30	20	30
29	29	50	90	0	46
62	62	50	50	50	65
63	63	50	50	50	65
60	60	10	30	20	70
23	23	64	62	62	62
64	64	10	30	70	65
65	65	20	50	80	65
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public."user" (id, personal_id, password, nickname, birth, gender, job, frequency, coin_balance, last_login_at, created_at, fcm_token) FROM stdin;
32	svp2511	$2a$10$Di3kd.tCMrAY3ibpaFOEKOZ50zW0Ek3sFk0MmuF/mJ9Xd/yU698IO	svp	1993-05-15	MALE	EMPLOYEE	NORMAL	0	\N	2025-11-11 03:23:09.503291	dTRJdXqwSFmSZLDijnSRYC:APA91bHkBE7oRKvInYru0AV1zSFcvLKcfCzKmay_ZyLZoMu3jJb5OG_KZWRIGFvKyTxOH6V1SZuNJEuQAqFP9Rx0EkT0bY3zkRSGYAuYF-dRMT8fHUIzO2Q
47	kkkjjj	$2a$10$ArInev2ZoprU7ylfNKUBSOb2uvDecbRetjCOzQ0f9.0XFHfrQq9Qu	삼성테스트하자	1990-01-01	MALE	EMPLOYEE	LOW	70	\N	2025-11-11 04:25:31.449584	fF29ALYcTUm3iVoE1mJLPW:APA91bEOuK_DrA9vV0uLFVKSD17LJ-K49f5u0fdO3vbgPQpKe_UZ3f0U7cJvRFQvGXBUUFJzKKNDHC9UAdabpCDSxmfGf5Pua9QuAO8l74zsm1XGMGqBZbk
27	u000	$2a$10$9u8vik3ngDaZoFW9MzHqRO4LhaIfJROQZJg/WD4PClFqwUj3xHQa.	uu	1990-01-01	FEMALE	FREELANCER	NORMAL	900	\N	2025-11-10 07:23:35.240937	efTrOjNRQDmE7ZWgXbYlOT:APA91bEjWxNZDpCnU3oHNaNpTDtNPD-vPuekM0O0v2dDpvj9vtZlxiFmaYOu1RP-XjYSOeIllJzTP7UOeIZ8V6LwohAQyrzm30xY1KdwnEGWmgQX9QmsYUw
25	test2	$2a$10$Vjb1rDsLHCMbXr8BgQ1u9Oyigss7LXCgbtECulsYNsf7LHJTOXoGu	테스트계정투	2025-11-10	MALE	STUDENT	LOW	100	\N	2025-11-10 07:17:40.877226	asdgasdh2342352
44	zxcv1	$2a$10$/RX9OVn1rd16fJ9FFuNyOucOFdkEV7vu0duOPRgDyTebwe3vzRtBq	가가가가	1990-01-01	MALE	STUDENT	NORMAL	90	\N	2025-11-11 04:11:07.297721	cVlWZ7RiS3C2_nGyxnmO3W:APA91bED8_ATNOs6vmmwGZHvUW8_RtikfUW4Y1QEbG-km1z4cLneVNSc0qwFY-YgmkrPEGyqS9AJgI34ueWpaYNyESot9tXXQCyNl9fV_IqZS2ssRlAH9ew
35	svp2512	$2a$10$gzexsDCoalv7NHXw5.NdIuCDO24fBTrKxdBU9L/LEYssFsdFgK.Fe	svp	1993-05-15	MALE	EMPLOYEE	HIGH	0	\N	2025-11-11 03:45:08.592622	dTRJdXqwSFmSZLDijnSRYC:APA91bFj3j7ZsNeoVcAVr3hoiBKQ5l-szaDlWyBdoy8GyNaSMcAFTkq8N9ECaA7Scse3Zpw0RfI_fjyY-ZUFpsUmCJx5rLEVEpIZLhcm-m7xzl1U7r3OhGE
41	juyy99	$2a$10$.RctpOZPGe7oVUtz7XxUl.eO5NNn70STQ6UGoKFeAs6WR7g3V8FaO	nun	1990-01-01	FEMALE	FREELANCER	NORMAL	60	\N	2025-11-11 04:09:03.208298	dleTpbs2SSW-r2Tihd2dfY:APA91bEjQMxFL4jnQRkoOecWU2_FLFkcH7TsUfxrXejOq-DGNg3hWF8yd4HFni-lXCwUEKELPol7ElfqNDbpBZYQy9D_Hnn8IisSuJGwizCgNasdHV1d3LM
28	nunnong2	$2a$10$wJxblgNlhfY9Xg5EiTs0VOgmUSjFuDoL67i5387kP/r0hGUfIFJaC	눈농	1999-07-26	FEMALE	STUDENT	NORMAL	0	\N	2025-11-10 14:07:11.39344	dleTpbs2SSW-r2Tihd2dfY:APA91bFUFmcCObcm_djBD6JZMxcOolmD6dzNQcLKHDrET1Ggm2jWzACSX0_az7uutKuXdddRDdVYZC6R2R-_ULN_QvILctM7QHTPMtlV8ewnqCElMzZFSm0
36	samsung22	$2a$10$lkrUmCC6xFhvlOyGfE5kxugxDtbkdv.O4TGvQDVnYwTYJ3Hkp43Ke	jayu	1990-01-01	MALE	EMPLOYEE	LOW	90	\N	2025-11-11 03:53:52.53075	dqxYUQl9Rr-HzOQ-M6sWa9:APA91bF3rhhwRzaeqaE31VMYAs3JevRudtcLzRhn1xZ_18VsVLNqSM8EASceC0qRCT0BLS1ILAG0H4wHFNMM4PivCLJoZZNjBTA6bAxO7yGgMw7E5c0-bfc
31	samsung2	$2a$10$4J9gR2YV3Rbmb5kOHnVQ5.hC6dAh.HGqAmNwzKd0O/791EsEwRrru	rirak	1990-01-01	MALE	EMPLOYEE	HIGH	100	\N	2025-11-11 03:21:43.81305	fowGWGyfQx2AmwimzQoume:APA91bHho4lW5uTED2sBj1W84gxxwOocYsDbdFmFFVysI04wrPI9aaE22nIxK09QqwTJ0Z-3C5I39HyLr-aXgI-LJ1Ib7I12noR_oCShDlZVGvAauoHZTfs
42	dnlwlgns	$2a$10$hm6mP8F/szfrozV9aBpNdeEnklhZTOTTHQT78/2q75H1szdZW1HCK	super	1990-01-01	MALE	STUDENT	NORMAL	0	\N	2025-11-11 04:09:09.693033	cVlWZ7RiS3C2_nGyxnmO3W:APA91bGdZqGLxypoHx_cKRXLqPI_bmVKo2hKCMPjUnT28X6iOf965sRb7erRf7kVRieY0b4Pum9XSUP8m6nAi5SfYeX2Ddh0qvuZEKtzkkyBwKSY2j9VakQ
33	samsungtest	$2a$10$4DqPEUQemU6ZQ1YhU9ckHeeUwSfwU/IFUNsikmkORFkTmrawbiAJ2	jayu	1990-01-01	MALE	EMPLOYEE	NORMAL	89	\N	2025-11-11 03:36:52.223099	cq-VyPHwQ_CAkNgS3_oN0y:APA91bExiqz3RdTXGbTh8hKq-Cs7F4Bh4UsZp1w9xeTczJ_7UNax4UFl30sdRZpFbO0yZNEuLqzXtHZmoXK4331t-fRQxKjKjgElB-6yEnBTtpypRPIG3LA
37	jamesnam	$2a$10$7G0cVM5ZCavU9CH5aQGNzujNhEeSqBsg3/eoiV2MN4EH6OLfTWHcm	삼성테스트	1990-01-01	MALE	ETC	HIGH	0	\N	2025-11-11 03:55:20.977564	dDh9tSeiTby3jMj93z8Rr9:APA91bH1p4naHokI2FcDKNxORNFwmVOBqJsAX_QJ1PhK5IhXrAKSJj-3jVsvmuQKC-EPRfRK_rOL1H5GQh6qg5_223TLJKocb_V85_0i0B_iUf3KT0wyteg
38	ilovesamsung	$2a$10$IMMElavNeNZ/Ak3BmpcrUuXePOfNvpba4clRY8Y1TMZXMgFKT.xby	ssafy	1990-01-01	MALE	EMPLOYEE	NORMAL	8971	\N	2025-11-11 03:57:37.527208	fW3oSf9bRzqbDS-qEB0ABc:APA91bHGVW4z6MikYPXrVflSMz1m7xkBz5LbJ9uAFptE9SpPIyWODrYGZj5v8XkgzMqNwWIgaF-0IeyCJNNajd9mT5rLGgV8CPLGhevWm-6asbYQyfDx63Q
24	nunnong	$2a$10$Vn8zUIRMcYnCU1AK8F30tO7jw/Fgq5BEL8QU7xgSGmhxc9VP2eGGy	디토	1999-07-26	FEMALE	ETC	NORMAL	8795	\N	2025-11-10 07:14:13.432048	dleTpbs2SSW-r2Tihd2dfY:APA91bHRX5OeDdezmYBddLpeb-jbHJ3ulkSHK_8tmsF3dNn8iTpqMsrBlxGBEwfl6qSFH0lePNQmucgtCf2WNZ-P43rub1w28eSqQaXGRwFCIFyjxRdSiwQ
26	dito2025	$2a$10$9vI915IlxzoXTI6yrJwM7OaywoW2PKtkAggrltdCMcbgzWoM1fDjS	장디토	1999-11-17	MALE	FREELANCER	NORMAL	6180	\N	2025-11-10 07:18:51.865818	\N
43	dito1	$2a$10$VpM1R5iv8qB6LkSuH3Imd.xf/xbL0Bw/OWxgnHjKokey9k77t9f7a	JIHOON	1990-01-01	FEMALE	STUDENT	HIGH	20	\N	2025-11-11 04:10:07.027878	\N
39	qwert	$2a$10$Tp0fh/fJdLfLIIvlCo2P1OfQ.oKjKQBCVO4jEqXWLzXq3G0dbAPqO	qwert	1990-01-01	MALE	EMPLOYEE	NORMAL	1	\N	2025-11-11 03:58:11.546169	cBfGrBERQF6wWX7dkUaBHs:APA91bHM1OIz0WCAxWb5M2wANSnbI-XF2tr6LVmT0QCycE5CvMFPVSpDjsYwlLEajAUyJSYZruDTWDjnRPomIVA1du7286954ir55qJ00gwVARLd761m9NA
45	samsungkkk	$2a$10$fLI22Hc1a7PxROR8Xn3fcOenXyh4UwgmnG3ZfUskNrH4lP/UYAAjq	삼성테스트기	1990-01-01	MALE	EMPLOYEE	NORMAL	50	\N	2025-11-11 04:14:00.089941	fF29ALYcTUm3iVoE1mJLPW:APA91bHAsEO6xDGePdkibTyfOjtBUwl0WIR_8QY3XKE_dYRqkxheoqsiZdfR46--uKcw2OrO4KtzQyM6RukTIIIlZSLtwuPNBpx0SakdK1hOuaIRnTsqhf8
29	dito	$2a$10$1aXtYBSEprt93y2raPqkS.eTv2cefeLyvHuq1SByan4R35gu4wgWm	dididi	1990-01-01	FEMALE	EMPLOYEE	LOW	10774	\N	2025-11-11 00:12:52.546519	eF-I5EjbSui0-fRi0IGI4F:APA91bEp_N1UJVONBTzgtx6P_nJjJQBNLeQO1blet3twcn3vMb8p7RNRUbOhlnvkIbiVbIgbQkh62Pcu82-aExzxSWFf3nX-vIBXtk_VXCRj0Sq7l4cU_8c
40	sjs1114	$2a$10$rr43nhax6J6Neg8uXkZhw.U0dQJMH9wpGbRa9Lia6L5WM6lWxGRja	리락	1994-11-14	FEMALE	EMPLOYEE	LOW	0	\N	2025-11-11 03:59:53.234386	cuJDOT8BS7iirLzQ_pOYx2:APA91bERGHAgph5JQ8Hz3c0qtNAmJyJKFnLNZrNYSJyZmGJBkS140cvUcWUCmjXQpJiDgLorD2sN8dsXCOOf5sJwAewuHagtGafmpQuTxz5FxHtzhq_PazE
34	nampassion	$2a$10$Bv7.XRZvxE1Cg9XdSuRAzu3qC6LMoW2Ac8P4wDPYWy757zJNXB1aW	janes	1990-01-01	MALE	EMPLOYEE	LOW	90	\N	2025-11-11 03:42:52.352183	fF29ALYcTUm3iVoE1mJLPW:APA91bEkORcvAbDWeLsaVUr0joUwK4xDdvzs3dYE_n3dXlv-LA_wToQA2JGh5JJio_kiE-q1tXZy2fiWJm22N9S7RCUFP2mKfmANPluSpA4K7ZXblAdo2ck
23	test	$2a$10$UdHRGxf9GpaBnVqMhln/hOK.y1u0vrvvCtisx5WGK3VqUKybEaIg6	디토짱	2025-11-10	FEMALE	STUDENT	LOW	8594	\N	2025-11-10 07:12:21.050637	fW3oSf9bRzqbDS-qEB0ABc:APA91bHtHMhGVavHqCmibzeIqmQKIM0cd31tdqwXFH8es9K2YjY-kFkzyHc-T4HpYfsRchpiBpixAdf_eAGSgEGaiIHWGyqTB_zTC1rxxnaotybVyeZcd-8
30	nunnong3	$2a$10$pNZzjt3211bH4ZG5Iowh0.cpxDcjQhfj48cUCoZsEyryi7v.Pf4HK	nunnong	1990-01-01	FEMALE	HOMEMAKER	NORMAL	75	\N	2025-11-11 00:20:44.179592	dleTpbs2SSW-r2Tihd2dfY:APA91bE2WeTMqJKZmejPZNvdFVxWcb0pys2EdydoiwJTI7Y3sBmlEqURG4B8hCFdG_Qmtc1hFceBuXOa3LvYjOM7G1L-ZGWKFt8r7w-mBnWjQJvCgq-H8Oc
59	user3	$2a$10$Qfp5HoWXUI3gvvnaNFDZW..F8kKgSOEfFrCoCPMkyC/beLIEQjrz.	carrie	2020-01-01	FEMALE	EMPLOYEE	NORMAL	9979	\N	2025-11-15 15:43:00.614281	dleTpbs2SSW-r2Tihd2dfY:APA91bH_pDenPv-xTIpFNeGYFBtHiNyZDOaFbm_mNhfe39p6jHaAyh0ssMjViO-CswwZKkWA-qypl6k-vE2CJk5WMXfx7PI55POzhHp8yoW0khd-2H8p36k
46	samsung222	$2a$10$UxRFyVxbiZ5jvSdr/7B8r.YQNVJHGtTj824KCEDerUJnPnNZkDagW	jayu	1990-01-01	MALE	EMPLOYEE	NORMAL	80	\N	2025-11-11 04:23:36.08605	dqxYUQl9Rr-HzOQ-M6sWa9:APA91bF4-4-AqeTpd1Z6BDbYwutL8QiEcAN0pYl4nydOmEEHDYcFSJH9ydar0sN6lgZpGnEi2uj4L1BxiPCa4iCkY7lSH4-11KZGcsPCGoVVoosAXvpB0rM
55	test1	$2a$10$Ag9G8/n.XJ1l8FmKCY3Pj.hOJX1bz7koU8sZEyqtmDyDd2eaMrtAm	감자고구마얌	1990-01-01	MALE	STUDENT	NORMAL	70	\N	2025-11-13 04:37:27.553221	string
60	user4	$2a$10$5QlfVMqfp4jldQQsLW0IFevOVEn5by.oTkEuA/MMqEV81HHbN5TVq	hello	1990-01-01	MALE	STUDENT	NORMAL	9509	\N	2025-11-15 15:43:58.326505	dleTpbs2SSW-r2Tihd2dfY:APA91bEJRCSFveJpvGP94wNMeuWdfGKjS3IrkmvgRgLLHmaBK_rfEatlSvTDz1BgYn9BUawV8R7IlVvv0sxXXdlwOdud98_Vh1muCPKC3_wTZNQlrN4OH34
51	dito3	$2a$10$IwlOBTtt8bSxD0djFRRa7.eUhhoAuXRpEf.EX/zQvgv8RoBqh0zXy	유디토	1990-01-01	MALE	EMPLOYEE	NORMAL	80	\N	2025-11-11 04:52:44.569897	cBFCvCKkRiK8aj5GcXFcCp:APA91bFpcaZZYqK-cewHKyCoEfd8KZs1Eza0UxJ5a0NNntij8zVaPL_Xuq23cEIKIipsezQs3kuJ6-4EZqN_mbgr3HH8HqWV-c3Mm6cPPIQ1EhiANvUs-HM
49	sjs1994	$2a$10$l/5D9PD0gGcO22c9JZL5yOxEGDFEAjQb7HSTQ2BGShOMe7JyjA6cu	리락	1990-01-01	MALE	EMPLOYEE	NORMAL	10	\N	2025-11-11 04:27:06.518742	cuJDOT8BS7iirLzQ_pOYx2:APA91bFwOom7IKSht6-8q5Eui_AHG2_EABP_2jnxk5i4dWPRKBjPPaYNfW61nxeT-4U1Eh_PzA0b5hPCqQSl3YJH7-xfqmNntz-8sMRqyAS8kz_3hEwZXEg
54	cookie	$2a$10$ymckHjpKyDlFq551fD029OfFEAlikCbuX8KLuC2mEtOto7TbwS6G6	cookie	1990-01-01	FEMALE	STUDENT	HIGH	10019	\N	2025-11-13 01:36:33.329297	fEjLnqn1RYCj_qqFOXxBz_:APA91bHE_V-V4vJKk9xaoZXK7hCZteTHQdvYE15VOOG0vX8FCi8tTER6569bHcFDPZT0KJ-hL--_GYD8ajWcL_wJY_SUj-VhTLR-Y2jnFkFo16JReP-RwQ8
48	asdfg	$2a$10$xG3dQfkErHZyYmuKMH88AuMM6OEH6h7/M8bpZJgIaiZufBTgb..pq	asdfg	1990-01-01	MALE	EMPLOYEE	NORMAL	78	\N	2025-11-11 04:27:02.332355	cBfGrBERQF6wWX7dkUaBHs:APA91bGtJMx0XBH0VCIlvimzFAlAi16fFmjYqPt6IKI15JJlshBl4jSvpkE-0TuxoKoGP2jrL2ifcA_NPqS3NDYC7GWtc-hX05aowN-3aDSkzLB6cujRusM
50	sam2	$2a$10$YJMM.v1eYyDE29LUlkmwwOqCFGbqvqub7OrwtSZzua3hITn3HfioS	jayu	1990-01-01	MALE	EMPLOYEE	HIGH	80	\N	2025-11-11 04:27:22.158165	dqxYUQl9Rr-HzOQ-M6sWa9:APA91bHwSTgG0vZDJ-eZAf4QqhuOpZmvW5KlNyC6JczkdVWKbOpLWOLn8RFDhIWxyN5IrsT42PKZtf9NWyrh6FOhkoag9YjeWIx7zjLJerhmqlV-JmWuhXU
57	user1	$2a$10$ElYrMqSOY8nRD2fZmyYCeexDH1d.VPx1QEprm9vOQ9HMDRUzAfANK	dog1	1990-01-01	FEMALE	HOMEMAKER	NORMAL	9014	\N	2025-11-15 15:40:25.673	fbBFKjQKQ9yOa98mRNlzvg:APA91bGdFKoxYQsqANS6bgLzjuGssx9eFXmD9w6XH9HG4geChzfFDo-fMs5pk_nuqdObPkcUfHcW5ohRweI6jwBYjr-MPY7xWcTwYuYL8Nti1ME9pke47zI
53	dito4	$2a$10$ymDxxK/nQemQY58PGw9kKe1V0GJOkFZomlTzSh6stSJRtNEthX0pi	ditome	1990-01-01	FEMALE	STUDENT	NORMAL	80	\N	2025-11-11 08:01:46.509068	fj1r-V0UT16s-YhEKLdppR:APA91bHO46Z-U2Nx67518jkABlWftsXyGwfem3jyBJSLg_Pd3asH8yEb6ZXXVnpLykKucwnKd1wIZDTgWB2VkyRhR12E3SRbX7_PQC9cMRvMbftEQuAd9Ts
64	real2	$2a$10$874d/GR0FelHZwjVS0BtBuVuZBmtOU/gxV57UOdKcObUo2EedyF/q	dito	1990-01-01	FEMALE	FREELANCER	NORMAL	1860	\N	2025-11-19 09:18:49.570633	dbxQq8HnRt6btVLbZYqAKU:APA91bFKvTTX0rGfMY9jNyGdoEN56Cx5x9EL1GWef0-qUiYSBmzI8v9-hdBuUzfxVJPYsETttZdo7FDa6Bp3O0nCbUW1s8oXwvxtb788VldaAuSD_5yd9Ag
52	nunnong4	$2a$10$ggwQIAOrryH.UEV3WKRlPeO8IqdtGeLMdpeGaVTz105BfggcaqwAS	nunnong	1999-07-26	FEMALE	STUDENT	NORMAL	9349	\N	2025-11-11 07:17:31.887679	dleTpbs2SSW-r2Tihd2dfY:APA91bFK2AS9_5odqS1eYtQ7Dxt1If0hjiCuPCOTyrcsFABtvtKf4rDkyQHeViIn8Ap7xF0VNMBbT6-QGbTdf5f9pCMn3yJ8agEscfxBpbm9eUXQgIGVnhE
65	widito1	$2a$10$SKMd8nP3m2UJiYrYja33POf9knpJfW3nGIrrQo8snK42EUTPv7ble	위디토	1990-01-01	MALE	STUDENT	NORMAL	0	\N	2025-11-19 13:11:39.143342	evoSzM5WRmmhgYAzOFS6Fi:APA91bEYsOqzrJ8Cx9N18jwnWTQO8o3GkZf0PWMvQQkVtkncbOrBt7ql1XVL5qIxWM65BmTR-yhx75MqLSh9dJlu5KbxK2tteiWvENHQIfl7yPGhhsBhlSM
62	onlyvip	$2a$10$z.Wuyrg0n30LfpUXysMdguTPYcFLuKChXJjaBQ9ExlWJA0tiPlhDW	디토짱	2000-10-09	FEMALE	ETC	NORMAL	740	\N	2025-11-19 09:03:35.048836	evoSzM5WRmmhgYAzOFS6Fi:APA91bFbAb0-gLhOI_SQhnMQ6-RCOyYH4QaBnoQsEpK5Rks4DaIw5S2zmzrI8w78g31kelH8-uzzDzOvEivgp2YrdoDBbPhNN5tOmniqaqjAj8MJlc8XVc8
58	user2	$2a$10$zt6R6hESLmualtgC6QMlwemGfIWwpJuR2z3VOJdoEhhjB0s7q7H/G	ddori	1990-01-01	MALE	FREELANCER	NORMAL	9339	\N	2025-11-15 15:41:35.112806	dleTpbs2SSW-r2Tihd2dfY:APA91bHEIL_9foClUa0B7s0mZ0aIb7WhR1TxzZ91mC4eNEgiBCkWCdpuAdysgzb8pe7YMMcvwx3AjNmiW04lU6jWOT8y1ObYLtfOU-tMqeWcq5k2zvUxA2U
63	real1	$2a$10$C8a7rAPIRooIqRjqXcyRTeB5VoExZpUnMIzivgw7K7RewgTtZ0eIm	눈농	1990-01-01	FEMALE	EMPLOYEE	NORMAL	975	\N	2025-11-19 09:06:30.206725	\N
56	dito2	$2a$10$9.h.ww2XA0m8Y.uZMtms7ObH7Mf4TJU5A4g.MjS4fnMelb/4qPgPa	허디토	1990-01-01	MALE	FREELANCER	NORMAL	8219	\N	2025-11-13 05:47:51.639449	fW3oSf9bRzqbDS-qEB0ABc:APA91bGgfHtdRvuJ4EcIj9rA_1Ar9jA4_3ib3WVD3y5GL6K5FQYeSIc4BapdxWje47dMGEKqozWH9-_IxzwvH0eNqfz5P945XtvayvCywKsdmpGgVw7W93c
61	widito	$2a$10$OqYowUEvTFA39WYfGJw1ieB8HpqNKA8eKRz5N.69p1UjTd.criYBm	위디토	1999-11-17	MALE	STUDENT	NORMAL	90	\N	2025-11-19 06:16:20.173291	\N
66	test4	$2a$10$wRByzzu7X1U/1MzQBW0/Tu/Csp.KTg.wajDZuguf21yX1IawXaMY.	dito	1990-01-01	FEMALE	STUDENT	NORMAL	100	\N	2025-11-20 01:49:04.016853	dleTpbs2SSW-r2Tihd2dfY:APA91bEaizzZYOj8u1iu50MchwYmnJnT2fwlhPvn-58M1XMCbvBcsCdwYFpagCjOFpNKYwBfirRK4heeSwm9Ae9f7Jf3O0OyQ0dSAw2g0mVwUY0-fBNlSuY
\.


--
-- Data for Name: user_item; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.user_item (item_id, user_id, purchased_at, is_equipped) FROM stdin;
1	25	2025-11-10 07:17:40.884798	t
7	25	2025-11-10 07:17:40.88578	t
1	31	2025-11-11 03:21:43.829098	t
7	31	2025-11-11 03:21:43.835206	t
3	26	2025-11-10 08:18:16.819861	f
7	26	2025-11-10 07:18:51.875817	f
7	27	2025-11-10 07:23:35.250166	t
7	32	2025-11-11 03:23:09.512527	t
37	24	2025-11-12 10:53:11.099476	t
1	52	2025-11-11 07:17:31.90436	f
7	24	2025-11-10 07:14:13.444521	f
1	45	2025-11-11 04:14:00.094343	t
7	45	2025-11-11 04:14:00.094967	t
1	46	2025-11-11 04:23:36.090852	t
7	46	2025-11-11 04:23:36.09203	t
1	47	2025-11-11 04:25:31.453977	t
7	47	2025-11-11 04:25:31.454534	t
1	48	2025-11-11 04:27:02.338382	t
7	48	2025-11-11 04:27:02.339041	t
3	32	2025-11-11 03:35:20.73464	t
1	32	2025-11-11 03:23:09.511585	f
1	33	2025-11-11 03:36:52.230858	t
7	33	2025-11-11 03:36:52.231668	t
1	49	2025-11-11 04:27:06.5239	t
8	26	2025-11-10 08:35:34.521704	f
7	49	2025-11-11 04:27:06.524831	t
1	50	2025-11-11 04:27:22.163004	t
7	50	2025-11-11 04:27:22.163531	t
1	34	2025-11-11 03:42:52.358526	t
7	34	2025-11-11 03:42:52.36043	t
17	26	2025-11-10 08:35:39.609198	f
1	35	2025-11-11 03:45:08.598717	t
7	35	2025-11-11 03:45:08.600318	t
1	36	2025-11-11 03:53:52.535619	t
7	36	2025-11-11 03:53:52.536256	t
1	37	2025-11-11 03:55:20.983232	t
20	26	2025-11-12 23:32:25.101076	f
1	39	2025-11-11 03:58:11.55149	t
7	39	2025-11-11 03:58:11.552252	t
6	26	2025-11-10 07:59:51.682946	t
1	40	2025-11-11 03:59:53.239904	t
24	26	2025-11-12 10:46:14.361495	f
4	23	2025-11-11 00:48:39.525258	f
16	26	2025-11-10 08:17:37.338046	f
1	51	2025-11-11 04:52:44.574331	t
1	56	2025-11-13 05:47:51.651047	t
7	23	2025-11-10 07:12:21.169728	f
4	24	2025-11-10 07:23:52.280529	f
1	38	2025-11-11 03:57:37.533218	t
7	28	2025-11-10 14:07:11.408027	t
37	43	2025-11-12 23:42:32.493472	f
5	28	2025-11-10 14:27:34.890017	t
1	28	2025-11-10 14:07:11.406269	f
7	51	2025-11-11 04:52:44.574982	t
36	26	2025-11-10 07:59:44.893552	f
1	30	2025-11-11 00:20:44.185359	t
7	30	2025-11-11 00:20:44.186125	t
5	27	2025-11-11 00:44:55.102934	t
1	27	2025-11-10 07:23:35.249112	f
13	23	2025-11-17 07:02:20.56494	f
3	38	2025-11-11 07:47:28.16841	f
29	26	2025-11-12 05:12:18.585371	f
38	26	2025-11-10 08:16:18.70784	f
5	23	2025-11-11 04:05:04.263836	t
1	53	2025-11-11 08:01:46.518118	t
13	26	2025-11-12 23:32:44.64634	f
7	53	2025-11-11 08:01:46.519564	t
4	58	2025-11-16 11:31:46.258733	t
10	26	2025-11-12 10:48:39.150143	f
18	26	2025-11-10 08:35:31.716494	f
5	26	2025-11-12 10:44:48.618801	f
1	23	2025-11-10 07:12:21.154933	f
7	38	2025-11-11 03:57:37.533916	f
22	26	2025-11-10 08:22:26.607441	f
37	40	2025-11-11 04:06:12.430722	t
7	40	2025-11-11 03:59:53.240561	f
33	37	2025-11-11 04:06:15.16671	t
7	37	2025-11-11 03:55:20.98418	f
34	26	2025-11-10 07:59:47.301024	f
4	26	2025-11-10 07:23:17.632014	f
26	26	2025-11-10 08:25:16.249774	f
1	41	2025-11-11 04:09:03.214708	t
7	41	2025-11-11 04:09:03.21544	t
1	42	2025-11-11 04:09:09.697191	t
7	42	2025-11-11 04:09:09.697832	t
9	26	2025-11-10 08:35:45.485125	f
1	44	2025-11-11 04:11:07.304931	t
7	44	2025-11-11 04:11:07.305623	t
31	26	2025-11-12 10:40:25.573927	f
28	26	2025-11-12 06:44:41.345468	f
39	26	2025-11-10 08:50:59.553876	f
30	26	2025-11-12 10:40:40.044044	f
3	24	2025-11-12 10:51:38.999158	f
19	26	2025-11-12 10:43:18.316382	f
23	26	2025-11-12 10:43:23.09325	f
12	26	2025-11-10 08:35:42.628922	f
26	58	2025-11-17 23:19:22.210011	t
27	26	2025-11-10 07:23:02.845329	f
21	26	2025-11-12 10:43:36.303688	f
15	26	2025-11-12 10:43:54.469884	f
34	24	2025-11-12 10:53:03.366851	f
35	24	2025-11-12 10:53:19.278981	f
12	24	2025-11-10 07:24:52.262485	f
39	38	2025-11-11 08:19:15.110228	f
1	26	2025-11-10 07:18:51.872869	f
4	52	2025-11-12 11:47:24.447037	t
38	24	2025-11-12 10:52:47.560924	f
21	52	2025-11-12 11:47:54.268542	t
7	52	2025-11-11 07:17:31.910243	f
6	24	2025-11-12 10:51:35.37478	f
6	52	2025-11-12 11:47:22.44283	f
39	24	2025-11-12 10:50:19.791548	f
30	24	2025-11-12 10:52:41.631422	f
33	26	2025-11-10 08:17:55.943338	f
37	26	2025-11-12 10:36:55.078749	f
35	26	2025-11-12 10:37:42.87284	f
10	23	2025-11-11 04:05:42.615923	f
39	23	2025-11-11 04:05:20.444099	f
7	29	2025-11-11 00:12:52.558166	f
38	23	2025-11-11 04:05:16.643672	f
24	43	2025-11-14 03:26:55.756157	f
1	29	2025-11-11 00:12:52.556505	f
34	29	2025-11-13 07:20:41.918346	f
32	26	2025-11-12 10:39:43.806743	f
14	26	2025-11-12 10:48:13.480895	f
11	26	2025-11-12 23:35:21.454208	f
22	43	2025-11-18 02:31:12.594505	t
9	43	2025-11-18 02:33:13.337381	f
8	23	2025-11-11 04:04:49.934028	f
1	57	2025-11-15 15:40:25.713508	t
32	29	2025-11-13 07:20:47.376822	t
5	43	2025-11-12 23:46:54.407763	f
36	43	2025-11-12 23:44:09.364926	f
23	43	2025-11-14 03:48:23.983022	f
33	43	2025-11-12 23:49:21.244932	f
5	52	2025-11-15 10:02:15.320302	f
5	29	2025-11-19 06:17:36.163333	t
3	52	2025-11-15 10:02:17.609424	f
7	63	2025-11-19 09:06:30.214246	t
3	23	2025-11-11 04:05:06.233789	f
7	57	2025-11-15 15:40:25.720767	f
9	57	2025-11-16 08:02:13.786423	t
1	60	2025-11-15 15:43:58.334806	f
2	23	2025-11-11 04:05:07.227115	f
7	60	2025-11-15 15:43:58.33577	f
15	54	2025-11-16 07:12:37.80345	f
29	60	2025-11-16 08:46:14.625688	f
1	63	2025-11-19 09:06:30.213031	f
11	23	2025-11-17 07:04:32.702951	f
6	43	2025-11-12 23:45:41.689788	f
39	43	2025-11-12 23:42:09.113433	f
25	26	2025-11-10 08:35:25.779801	t
4	43	2025-11-12 23:46:45.977365	t
6	23	2025-11-11 04:05:05.434542	f
34	43	2025-11-12 23:49:18.213279	f
14	43	2025-11-18 02:48:31.937044	f
3	62	2025-11-19 09:06:53.413626	f
35	56	2025-11-18 03:24:55.683743	f
4	60	2025-11-16 10:41:48.466193	f
7	64	2025-11-19 09:18:49.576769	t
1	64	2025-11-19 09:18:49.575624	f
29	56	2025-11-18 04:14:02.407437	f
8	61	2025-11-19 06:17:32.621291	t
31	56	2025-11-18 04:14:39.866501	f
25	56	2025-11-18 03:29:12.61173	f
14	23	2025-11-17 07:02:48.929241	t
4	56	2025-11-18 03:24:33.688526	f
2	56	2025-11-18 05:06:34.022885	f
27	56	2025-11-18 05:06:44.875409	f
4	54	2025-11-18 12:01:43.012768	t
3	54	2025-11-16 07:12:12.086351	f
1	65	2025-11-19 13:11:39.147545	t
7	65	2025-11-19 13:11:39.148953	f
8	62	2025-11-19 16:08:59.237458	t
6	62	2025-11-19 09:08:50.198234	t
7	61	2025-11-19 23:45:25.587017	f
1	61	2025-11-19 06:16:20.19603	f
1	24	2025-11-10 07:14:13.443391	t
37	38	2025-11-20 01:45:18.897349	f
36	38	2025-11-20 01:45:40.387235	f
1	66	2025-11-20 01:49:04.021073	t
7	66	2025-11-20 01:49:04.021542	t
31	43	2025-11-13 01:17:23.920894	f
6	58	2025-11-16 06:47:51.417614	f
4	57	2025-11-16 06:42:07.864903	f
36	23	2025-11-17 05:44:36.269593	f
32	43	2025-11-13 01:08:25.279733	f
30	43	2025-11-13 01:17:26.990501	f
2	29	2025-11-16 06:47:50.829166	f
7	58	2025-11-15 15:41:35.124591	f
28	43	2025-11-13 01:19:55.751813	f
12	43	2025-11-14 03:10:17.173511	f
7	43	2025-11-11 04:10:07.033105	f
4	63	2025-11-19 09:06:43.554066	t
34	38	2025-11-14 08:03:17.625187	f
20	38	2025-11-14 08:02:44.913936	f
2	64	2025-11-19 09:19:57.029812	t
27	43	2025-11-13 01:23:19.833064	f
29	43	2025-11-13 01:17:30.384564	f
5	24	2025-11-12 10:51:48.626736	f
26	43	2025-11-13 01:20:21.479419	f
11	43	2025-11-13 01:49:12.569335	f
38	43	2025-11-12 23:42:21.141009	f
25	65	2025-11-19 13:12:49.539255	t
2	52	2025-11-15 10:02:19.803505	f
2	43	2025-11-13 01:20:33.010366	f
7	62	2025-11-19 09:03:35.058677	f
1	59	2025-11-15 15:43:00.621324	t
7	59	2025-11-15 15:43:00.622354	t
35	43	2025-11-12 23:49:15.396238	f
1	55	2025-11-13 04:37:27.566042	t
7	55	2025-11-13 04:37:27.573058	t
1	62	2025-11-19 09:03:35.05788	f
1	58	2025-11-15 15:41:35.12305	f
4	29	2025-11-13 07:20:26.892331	f
1	54	2025-11-13 01:36:33.3371	f
7	54	2025-11-13 01:36:33.337795	f
2	61	2025-11-20 00:39:53.530497	t
3	56	2025-11-18 04:10:45.791186	f
2	38	2025-11-14 08:00:42.825626	f
1	43	2025-11-11 04:10:07.032498	f
25	38	2025-11-14 08:02:51.168948	t
38	38	2025-11-14 08:02:07.108596	f
8	43	2025-11-13 01:48:28.981109	f
2	26	2025-11-10 07:25:58.572903	f
15	57	2025-11-16 07:59:58.217251	f
3	43	2025-11-12 23:47:04.001242	f
3	57	2025-11-16 07:59:17.708488	f
16	43	2025-11-18 02:17:30.559586	f
25	43	2025-11-13 01:20:16.326982	f
39	56	2025-11-18 02:56:08.571261	f
38	56	2025-11-18 03:06:03.941124	f
16	57	2025-11-16 07:59:51.61052	f
2	57	2025-11-16 08:00:45.363058	f
7	56	2025-11-13 05:47:51.65352	f
38	57	2025-11-16 08:01:03.592965	f
13	57	2025-11-16 07:59:02.981128	f
5	58	2025-11-16 08:42:55.911782	f
16	54	2025-11-16 08:46:17.59523	t
21	60	2025-11-16 08:46:38.605712	t
26	38	2025-11-14 08:03:14.947601	f
6	56	2025-11-18 03:11:28.85998	f
37	56	2025-11-18 03:10:47.683616	f
6	60	2025-11-16 08:45:45.390814	t
21	56	2025-11-18 04:08:26.508493	f
34	56	2025-11-18 04:14:27.2711	f
33	56	2025-11-18 05:05:07.461949	f
26	56	2025-11-18 09:37:11.579607	t
8	56	2025-11-18 03:27:26.948406	f
\.


--
-- Data for Name: weekly_goal; Type: TABLE DATA; Schema: public; Owner: dito
--

COPY public.weekly_goal (id, user_id, goal, start_at, is_active) FROM stdin;
\.


--
-- Name: app_usage_log_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.app_usage_log_log_id_seq', 1, false);


--
-- Name: content_cache_cache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.content_cache_cache_id_seq', 1, false);


--
-- Name: group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.group_id_seq', 87, true);


--
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.item_id_seq', 39, true);


--
-- Name: mission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.mission_id_seq', 295, true);


--
-- Name: mission_result_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.mission_result_id_seq', 203, true);


--
-- Name: report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.report_id_seq', 50, true);


--
-- Name: status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.status_id_seq', 66, true);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.user_id_seq', 66, true);


--
-- Name: weekly_goal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dito
--

SELECT pg_catalog.setval('public.weekly_goal_id_seq', 12, true);


--
-- Name: app_usage_log app_usage_log_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.app_usage_log
    ADD CONSTRAINT app_usage_log_pkey PRIMARY KEY (log_id);


--
-- Name: content_cache content_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.content_cache
    ADD CONSTRAINT content_cache_pkey PRIMARY KEY (cache_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: group group_invite_code_key; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT group_invite_code_key UNIQUE (invite_code);


--
-- Name: group_participant group_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.group_participant
    ADD CONSTRAINT group_participant_pkey PRIMARY KEY (user_id, group_id);


--
-- Name: group group_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT group_pkey PRIMARY KEY (id);


--
-- Name: item item_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);


--
-- Name: mission mission_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.mission
    ADD CONSTRAINT mission_pkey PRIMARY KEY (id);


--
-- Name: mission_result mission_result_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.mission_result
    ADD CONSTRAINT mission_result_pkey PRIMARY KEY (id);


--
-- Name: report report_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_pkey PRIMARY KEY (id);


--
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id);


--
-- Name: user uq_user_personal_id; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT uq_user_personal_id UNIQUE (personal_id);


--
-- Name: CONSTRAINT uq_user_personal_id ON "user"; Type: COMMENT; Schema: public; Owner: dito
--

COMMENT ON CONSTRAINT uq_user_personal_id ON public."user" IS 'personal_id 중복 방지 제약 조건';


--
-- Name: user_item user_item_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.user_item
    ADD CONSTRAINT user_item_pkey PRIMARY KEY (user_id, item_id);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: weekly_goal weekly_goal_pkey; Type: CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.weekly_goal
    ADD CONSTRAINT weekly_goal_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_app_usage_log_usage_date; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_app_usage_log_usage_date ON public.app_usage_log USING btree (usage_date);


--
-- Name: idx_app_usage_log_user_id; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_app_usage_log_user_id ON public.app_usage_log USING btree (user_id);


--
-- Name: idx_group_created_at; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_group_created_at ON public."group" USING btree (created_at DESC);


--
-- Name: idx_group_invite_code; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_group_invite_code ON public."group" USING btree (invite_code);


--
-- Name: idx_group_status; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_group_status ON public."group" USING btree (status);


--
-- Name: idx_mission_status; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_mission_status ON public.mission USING btree (status);


--
-- Name: idx_mission_user_id; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_mission_user_id ON public.mission USING btree (user_id);


--
-- Name: idx_user_item_item_id; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_user_item_item_id ON public.user_item USING btree (item_id);


--
-- Name: idx_user_item_user_id; Type: INDEX; Schema: public; Owner: dito
--

CREATE INDEX idx_user_item_user_id ON public.user_item USING btree (user_id);


--
-- Name: group_participant fk_group_to_group_participant_1; Type: FK CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.group_participant
    ADD CONSTRAINT fk_group_to_group_participant_1 FOREIGN KEY (group_id) REFERENCES public."group"(id);


--
-- Name: user_item fk_item_to_user_item_1; Type: FK CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.user_item
    ADD CONSTRAINT fk_item_to_user_item_1 FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: report fk_report_user; Type: FK CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: group_participant fk_user_to_group_participant_1; Type: FK CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.group_participant
    ADD CONSTRAINT fk_user_to_group_participant_1 FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: user_item fk_user_to_user_item_1; Type: FK CONSTRAINT; Schema: public; Owner: dito
--

ALTER TABLE ONLY public.user_item
    ADD CONSTRAINT fk_user_to_user_item_1 FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: dito
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

\unrestrict 2qJZgej9hQ6eMKPkIqBIihEK1NlEkBcri8qIFwwkIz5kgAaprGDuSQe75a6ACE6

