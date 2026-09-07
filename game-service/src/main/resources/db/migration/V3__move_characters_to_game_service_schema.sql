-- 같은 서비스의 테이블이 public 과 game_service 로 갈려 있었다.
-- quests 엔티티가 이미 game_service 를 쓰고 있으므로 characters 도 그쪽으로 통일한다.
--
-- ALTER TABLE ... SET SCHEMA 는 테이블에 딸린 인덱스와 제약조건도 함께 옮긴다.
-- (uk_characters_user_id 유니크 인덱스 포함 — upsertLevel 의 ON CONFLICT 전제다)
CREATE SCHEMA IF NOT EXISTS game_service;

ALTER TABLE public.characters SET SCHEMA game_service;
