ALTER TABLE companies ADD COLUMN IF NOT EXISTS is_partner boolean DEFAULT false;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS partner_priority integer DEFAULT 0;

INSERT INTO companies
(active, address_line, business_registration_number, capability_note, created_at, email, kakao_place_id, latitude, longitude, max_estimated_quote_krw, min_estimated_quote_krw, name, phone, postal_code, representative_name, service_region_label, source, status, updated_at, username, is_partner, partner_priority)
VALUES
(true, '서울특별시 강남구 테헤란로 100', '100-01-00001', '균열, 누수, 곰팡이 진단 및 보수 가능', now(), 'seoulfix01@test.com', 'PARTNER_SEOUL_01', 37.4979, 127.0276, 150000, 30000, '서울 하자케어 1호점', '010-1000-0001', '06234', '김서울', '서울', 'ADMIN', 'APPROVED', now(), 'partner_seoul_01', true, 100),
(true, '서울특별시 마포구 양화로 50', '100-01-00002', '주거 하자 수리 및 방문 견적 가능', now(), 'seoulfix02@test.com', 'PARTNER_SEOUL_02', 37.5563, 126.9236, 180000, 40000, '서울 홈픽스 2호점', '010-1000-0002', '04038', '이서울', '서울', 'ADMIN', 'APPROVED', now(), 'partner_seoul_02', true, 99),
(true, '서울특별시 송파구 올림픽로 200', '100-01-00003', '벽면 손상, 곰팡이, 누수 보수 전문', now(), 'seoulfix03@test.com', 'PARTNER_SEOUL_03', 37.5145, 127.1059, 200000, 50000, '서울 수리닥터 3호점', '010-1000-0003', '05500', '박서울', '서울', 'ADMIN', 'APPROVED', now(), 'partner_seoul_03', true, 98),

(true, '경기도 수원시 팔달구 정조로 100', '100-02-00001', '수도권 주거 하자 방문 수리', now(), 'gyeonggi01@test.com', 'PARTNER_GG_01', 37.2636, 127.0286, 160000, 30000, '경기 하자케어 1호점', '010-2000-0001', '16490', '김경기', '경기', 'ADMIN', 'APPROVED', now(), 'partner_gyeonggi_01', true, 100),
(true, '경기도 성남시 분당구 판교역로 120', '100-02-00002', '균열, 누수, 벽면 보수 가능', now(), 'gyeonggi02@test.com', 'PARTNER_GG_02', 37.3947, 127.1112, 180000, 40000, '경기 홈픽스 2호점', '010-2000-0002', '13529', '이경기', '경기', 'ADMIN', 'APPROVED', now(), 'partner_gyeonggi_02', true, 99),
(true, '경기도 고양시 일산동구 중앙로 1300', '100-02-00003', '주거 하자 점검 및 수리 전문', now(), 'gyeonggi03@test.com', 'PARTNER_GG_03', 37.6584, 126.8320, 200000, 50000, '경기 수리닥터 3호점', '010-2000-0003', '10401', '박경기', '경기', 'ADMIN', 'APPROVED', now(), 'partner_gyeonggi_03', true, 98),

(true, '인천광역시 남동구 인주대로 600', '100-03-00001', '누수, 곰팡이, 벽면 손상 보수 가능', now(), 'incheon01@test.com', 'PARTNER_IC_01', 37.4563, 126.7052, 150000, 30000, '인천 하자케어 1호점', '010-3000-0001', '21556', '김인천', '인천', 'ADMIN', 'APPROVED', now(), 'partner_incheon_01', true, 100),
(true, '인천광역시 부평구 부평대로 80', '100-03-00002', '소형 주거 하자 방문 수리', now(), 'incheon02@test.com', 'PARTNER_IC_02', 37.5070, 126.7219, 170000, 40000, '인천 홈픽스 2호점', '010-3000-0002', '21389', '이인천', '인천', 'ADMIN', 'APPROVED', now(), 'partner_incheon_02', true, 99),
(true, '인천광역시 연수구 컨벤시아대로 160', '100-03-00003', '주거 하자 진단 및 보수 전문', now(), 'incheon03@test.com', 'PARTNER_IC_03', 37.3896, 126.6454, 190000, 50000, '인천 수리닥터 3호점', '010-3000-0003', '22004', '박인천', '인천', 'ADMIN', 'APPROVED', now(), 'partner_incheon_03', true, 98),

(true, '부산광역시 해운대구 센텀중앙로 90', '100-04-00001', '누수, 곰팡이, 균열 보수 가능', now(), 'busan01@test.com', 'PARTNER_BS_01', 35.1695, 129.1306, 160000, 30000, '부산 하자케어 1호점', '010-4000-0001', '48059', '김부산', '부산', 'ADMIN', 'APPROVED', now(), 'partner_busan_01', true, 100),
(true, '부산광역시 부산진구 중앙대로 700', '100-04-00002', '원룸, 오피스텔 하자 수리 전문', now(), 'busan02@test.com', 'PARTNER_BS_02', 35.1577, 129.0592, 180000, 40000, '부산 홈픽스 2호점', '010-4000-0002', '47291', '이부산', '부산', 'ADMIN', 'APPROVED', now(), 'partner_busan_02', true, 99),
(true, '부산광역시 남구 수영로 300', '100-04-00003', '벽면 손상 및 방수 보수 가능', now(), 'busan03@test.com', 'PARTNER_BS_03', 35.1379, 129.1000, 200000, 50000, '부산 수리닥터 3호점', '010-4000-0003', '48434', '박부산', '부산', 'ADMIN', 'APPROVED', now(), 'partner_busan_03', true, 98),

(true, '대구광역시 중구 중앙대로 390', '100-05-00001', '곰팡이, 누수, 균열 보수 가능', now(), 'daegu01@test.com', 'PARTNER_DG_01', 35.8714, 128.6014, 150000, 30000, '대구 하자케어 1호점', '010-5000-0001', '41911', '김대구', '대구', 'ADMIN', 'APPROVED', now(), 'partner_daegu_01', true, 100),
(true, '대구광역시 수성구 달구벌대로 2400', '100-05-00002', '주거 하자 방문 견적 및 수리', now(), 'daegu02@test.com', 'PARTNER_DG_02', 35.8584, 128.6305, 170000, 40000, '대구 홈픽스 2호점', '010-5000-0002', '42085', '이대구', '대구', 'ADMIN', 'APPROVED', now(), 'partner_daegu_02', true, 99),
(true, '대구광역시 달서구 월배로 250', '100-05-00003', '벽지 손상, 곰팡이, 누수 보수 전문', now(), 'daegu03@test.com', 'PARTNER_DG_03', 35.8167, 128.5281, 190000, 50000, '대구 수리닥터 3호점', '010-5000-0003', '42753', '박대구', '대구', 'ADMIN', 'APPROVED', now(), 'partner_daegu_03', true, 98),

(true, '광주광역시 서구 상무중앙로 70', '100-06-00001', '주거 하자 점검 및 보수 가능', now(), 'gwangju01@test.com', 'PARTNER_GJ_01', 35.1520, 126.8514, 150000, 30000, '광주 하자케어 1호점', '010-6000-0001', '61949', '김광주', '광주', 'ADMIN', 'APPROVED', now(), 'partner_gwangju_01', true, 100),
(true, '광주광역시 북구 서암대로 100', '100-06-00002', '균열, 누수, 곰팡이 보수 전문', now(), 'gwangju02@test.com', 'PARTNER_GJ_02', 35.1740, 126.9110, 170000, 40000, '광주 홈픽스 2호점', '010-6000-0002', '61217', '이광주', '광주', 'ADMIN', 'APPROVED', now(), 'partner_gwangju_02', true, 99),
(true, '광주광역시 광산구 임방울대로 330', '100-06-00003', '수리 전후 리포트 작성 지원 가능', now(), 'gwangju03@test.com', 'PARTNER_GJ_03', 35.1900, 126.8200, 190000, 50000, '광주 수리닥터 3호점', '010-6000-0003', '62277', '박광주', '광주', 'ADMIN', 'APPROVED', now(), 'partner_gwangju_03', true, 98),

(true, '대전광역시 서구 둔산로 100', '100-07-00001', '주거 하자 방문 진단 및 수리 가능', now(), 'daejeon01@test.com', 'PARTNER_DJ_01', 36.3504, 127.3845, 150000, 30000, '대전 하자케어 1호점', '010-7000-0001', '35229', '김대전', '대전', 'ADMIN', 'APPROVED', now(), 'partner_daejeon_01', true, 100),
(true, '대전광역시 유성구 대학로 90', '100-07-00002', '누수, 곰팡이, 벽면 손상 보수', now(), 'daejeon02@test.com', 'PARTNER_DJ_02', 36.3622, 127.3560, 170000, 40000, '대전 홈픽스 2호점', '010-7000-0002', '34134', '이대전', '대전', 'ADMIN', 'APPROVED', now(), 'partner_daejeon_02', true, 99),
(true, '대전광역시 중구 중앙로 120', '100-07-00003', '원룸 및 빌라 하자 수리 전문', now(), 'daejeon03@test.com', 'PARTNER_DJ_03', 36.3250, 127.4210, 190000, 50000, '대전 수리닥터 3호점', '010-7000-0003', '34921', '박대전', '대전', 'ADMIN', 'APPROVED', now(), 'partner_daejeon_03', true, 98),

(true, '울산광역시 남구 삼산로 200', '100-08-00001', '누수, 균열, 곰팡이 보수 가능', now(), 'ulsan01@test.com', 'PARTNER_US_01', 35.5384, 129.3114, 150000, 30000, '울산 하자케어 1호점', '010-8000-0001', '44705', '김울산', '울산', 'ADMIN', 'APPROVED', now(), 'partner_ulsan_01', true, 100),
(true, '울산광역시 중구 번영로 450', '100-08-00002', '주거 하자 방문 점검 가능', now(), 'ulsan02@test.com', 'PARTNER_US_02', 35.5683, 129.3326, 170000, 40000, '울산 홈픽스 2호점', '010-8000-0002', '44530', '이울산', '울산', 'ADMIN', 'APPROVED', now(), 'partner_ulsan_02', true, 99),
(true, '울산광역시 북구 산업로 1000', '100-08-00003', '수리 전후 사진 기록 및 리포트 지원', now(), 'ulsan03@test.com', 'PARTNER_US_03', 35.5820, 129.3610, 190000, 50000, '울산 수리닥터 3호점', '010-8000-0003', '44248', '박울산', '울산', 'ADMIN', 'APPROVED', now(), 'partner_ulsan_03', true, 98)
ON CONFLICT (kakao_place_id) DO NOTHING;
