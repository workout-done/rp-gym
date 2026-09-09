-- 랭킹을 Redis ZSET 으로 옮기면서 level 기준 DB 정렬이 사라졌다.
-- 이 인덱스를 타는 쿼리가 없는데 onXpChanged() 의 upsert 마다 갱신 비용만 발생한다.
DROP INDEX IF EXISTS idx_characters_level;
