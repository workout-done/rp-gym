-- 같은 서비스의 테이블이 public 과 game_service 로 갈려 있었다.
-- quests 엔티티가 이미 game_service 를 쓰고 있으므로 characters 도 그쪽으로 통일한다.
--
-- ALTER TABLE ... SET SCHEMA 는 테이블에 딸린 인덱스와 제약조건도 함께 옮긴다.
-- (uk_characters_user_id 유니크 인덱스 포함 — upsertLevel 의 ON CONFLICT 전제다)
--
-- IF EXISTS 인 이유:
-- spring.flyway.schemas: game_service 가 켜지면 Flyway 가 search_path 를 game_service 로
-- 옮기므로, V1 의 수식어 없는 CREATE TABLE characters 가 처음부터 game_service 에 생긴다.
-- 그런 신규 DB 에는 public.characters 가 존재한 적이 없어 IF EXISTS 없이는 여기서 터진다.
-- 이미 public 에 만들어진 개발 DB 에서는 그대로 옮겨간다. (PR #38 리뷰 피드백)
CREATE SCHEMA IF NOT EXISTS game_service;

ALTER TABLE IF EXISTS public.characters SET SCHEMA game_service;
