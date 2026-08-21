-- 실시간 회의를 WebRTC로 직접 구현하면서 LiveKit Egress 녹음을 제거했습니다.
-- egress id를 채우는 주체가 사라져 컬럼을 내립니다. 오디오 다시듣기용 audio_key는 유지합니다.
alter table meeting drop column audio_egress_id;
