🧠 Tổng hợp cuối bảng
Tình huống	Cần làm	Lệnh

🔧 Sửa commons	Build lib + rebuild 2 service	mvn clean install -DskipTests; docker-compose build; docker-compose up -d --force-recreate


🔧 Sửa account-service	Rebuild + restart account	docker-compose up -d --build --force-recreate account-service-app

🔧 Sửa product-service	Rebuild + restart product	docker-compose up -d --build --force-recreate product-service-app
🔁 Làm sạch toàn hệ thống	Stop + rebuild lại từ đầu	docker-compose down -v && mvn clean install -DskipTests && docker-compose build && docker-compose up -d